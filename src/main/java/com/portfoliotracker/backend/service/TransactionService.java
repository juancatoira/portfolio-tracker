package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.dto.request.ManualPositionRequest;
import com.portfoliotracker.backend.dto.request.TransactionRequest;
import com.portfoliotracker.backend.dto.response.PositionResponse;
import com.portfoliotracker.backend.dto.response.TransactionResponse;
import com.portfoliotracker.backend.entity.Coin;
import com.portfoliotracker.backend.entity.Transaction;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.repository.CoinPriceRepository;
import com.portfoliotracker.backend.repository.CoinRepository;
import com.portfoliotracker.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CoinRepository coinRepository;
    private final PriceService priceService;
    private final CoinPriceRepository coinPriceRepository;

    final static Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionResponse create(TransactionRequest request, User user) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .coinId(request.getCoinId())
                .coinName(request.getCoinName())
                .coinSymbol(request.getCoinSymbol())
                .type(request.getType())
                .quantity(request.getQuantity())
                .priceUsd(request.getPriceUsd())
                .date(request.getDate())
                .notes(request.getNotes())
                .build();

        transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    public List<TransactionResponse> getHistory(User user) {
        return transactionRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(UUID transactionId, User user) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta transacción");
        }

        String coinId = transaction.getCoinId();
        transactionRepository.delete(transaction);

        // Si ya no hay transacciones para esta moneda en ningún portfolio
        // eliminamos el precio cacheado
        boolean hasMoreTransactions = transactionRepository
                .existsByCoinId(coinId);

        if (!hasMoreTransactions) {
            coinPriceRepository.deleteById(coinId);

            log.info("Precio cacheado eliminado para {}", coinId);
        }
    }

    public List<PositionResponse> getPositions(User user) {
        List<Transaction> allTransactions = transactionRepository
                .findByUserIdOrderByDateDesc(user.getId());

        Map<String, List<Transaction>> byCoin = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCoinId));

        List<String> coinIds = new ArrayList<>(byCoin.keySet());

        // Usamos precios cacheados
        Map<String, BigDecimal> currentPrices = priceService.getCachedPrices(coinIds);

        // Si no hay precios cacheados para alguna moneda, actualizamos
        List<String> missingPrices = coinIds.stream()
                .filter(id -> !currentPrices.containsKey(id))
                .collect(Collectors.toList());

        if (!missingPrices.isEmpty()) {
            priceService.updatePrices(missingPrices);
            currentPrices.putAll(priceService.getCachedPrices(missingPrices));
        }

        List<PositionResponse> positions = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byCoin.entrySet()) {
            PositionResponse position = calculatePosition(
                    entry.getKey(), entry.getValue(), currentPrices
            );
            if (position != null && position.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                positions.add(position);
            }
        }

        return positions;
    }

    public TransactionResponse addManualPosition(ManualPositionRequest request, User user) {
        // Si ya existe una posición manual para esta moneda, la reemplazamos
        transactionRepository.findByUserIdAndCoinIdAndType(
                user.getId(), request.getCoinId(), Transaction.TransactionType.MANUAL
        ).ifPresent(transactionRepository::delete);

        Transaction transaction = Transaction.builder()
                .user(user)
                .coinId(request.getCoinId())
                .coinName(request.getCoinName())
                .coinSymbol(request.getCoinSymbol())
                .type(Transaction.TransactionType.MANUAL)
                .quantity(request.getQuantity())
                .priceUsd(request.getAveragePriceUsd())
                .date(java.time.LocalDateTime.now())
                .notes("Posición manual")
                .build();

        transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    private PositionResponse calculatePosition(
            String coinId,
            List<Transaction> transactions,
            Map<String, BigDecimal> currentPrices
    ) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        String coinName = "";
        String coinSymbol = "";
        String imageUrl = "";

        // Ordenar por fecha ascendente para calcular PMP correctamente
        transactions.sort(Comparator.comparing(Transaction::getDate));

        for (Transaction t : transactions) {
            coinName = t.getCoinName();
            coinSymbol = t.getCoinSymbol();
            imageUrl = coinRepository.findById(coinId)
                    .map(Coin::getImageUrl)
                    .orElse("");

            if (t.getType() == Transaction.TransactionType.BUY
                    || t.getType() == Transaction.TransactionType.MANUAL) {
                totalInvested = totalInvested.add(
                        t.getQuantity().multiply(t.getPriceUsd())
                );
                totalQuantity = totalQuantity.add(t.getQuantity());
            } else {
                // SELL: reducimos cantidad e inversión proporcionalmente
                if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ratio = t.getQuantity().divide(totalQuantity, 8, RoundingMode.HALF_UP);
                    totalInvested = totalInvested.subtract(
                            totalInvested.multiply(ratio)
                    );
                    totalQuantity = totalQuantity.subtract(t.getQuantity());
                }
            }
        }

        if (totalQuantity.compareTo(BigDecimal.ZERO) <= 0) return null;

        // Precio medio ponderado
        BigDecimal averagePrice = totalInvested.divide(totalQuantity, 8, RoundingMode.HALF_UP);

        // Precio actual de CoinGecko
        BigDecimal currentPrice = currentPrices.getOrDefault(coinId, BigDecimal.ZERO);

        // Valor actual
        BigDecimal currentValue = totalQuantity.multiply(currentPrice);

        // P&L
        BigDecimal pnlUsd = currentValue.subtract(totalInvested);
        BigDecimal pnlPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? pnlUsd.divide(totalInvested, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return PositionResponse.builder()
                .coinId(coinId)
                .coinName(coinName)
                .coinSymbol(coinSymbol)
                .quantity(totalQuantity.setScale(8, RoundingMode.HALF_UP))
                .averagePriceUsd(averagePrice.setScale(2, RoundingMode.HALF_UP))
                .currentPriceUsd(currentPrice.setScale(2, RoundingMode.HALF_UP))
                .totalInvestedUsd(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .currentValueUsd(currentValue.setScale(2, RoundingMode.HALF_UP))
                .pnlUsd(pnlUsd.setScale(2, RoundingMode.HALF_UP))
                .pnlPercent(pnlPercent.setScale(2, RoundingMode.HALF_UP))
                .imageUrl(imageUrl)
                .build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .coinId(t.getCoinId())
                .coinName(t.getCoinName())
                .coinSymbol(t.getCoinSymbol())
                .type(t.getType())
                .quantity(t.getQuantity())
                .priceUsd(t.getPriceUsd())
                .date(t.getDate())
                .notes(t.getNotes())
                .build();
    }
}