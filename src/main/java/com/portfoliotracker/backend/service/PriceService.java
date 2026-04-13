package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.entity.CoinPrice;
import com.portfoliotracker.backend.repository.CoinPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private final CoinPriceRepository coinPriceRepository;
    private final CoinGeckoService coinGeckoService;

    // Cada 4 horas
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000)
    public void scheduledUpdate() {
        log.info("Actualización automática de precios...");
        updatePrices();
    }

    public LocalDateTime getLastUpdated() {
        return coinPriceRepository.findAll().stream()
                .map(CoinPrice::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public Map<String, BigDecimal> getCachedPrices(List<String> coinIds) {
        return coinPriceRepository.findAllById(coinIds).stream()
                .collect(Collectors.toMap(
                        CoinPrice::getCoinId,
                        CoinPrice::getPriceUsd
                ));
    }

    public void updatePrices(List<String> coinIds) {
        if (coinIds.isEmpty()) return;

        try {
            Map<String, BigDecimal> prices = coinGeckoService.getPricesUsd(coinIds);
            List<CoinPrice> toSave = prices.entrySet().stream()
                    .map(e -> CoinPrice.builder()
                            .coinId(e.getKey())
                            .priceUsd(e.getValue())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            coinPriceRepository.saveAll(toSave);
            log.info("Precios actualizados: {} monedas", toSave.size());
        } catch (Exception e) {
            log.error("Error actualizando precios: {}", e.getMessage());
        }
    }

    public void updatePrices() {
        List<String> allCoinIds = coinPriceRepository.findAll().stream()
                .map(CoinPrice::getCoinId)
                .collect(Collectors.toList());
        if (!allCoinIds.isEmpty()) updatePrices(allCoinIds);
    }
}