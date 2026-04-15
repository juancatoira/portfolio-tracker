package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.dto.request.WatchlistRequest;
import com.portfoliotracker.backend.dto.response.WatchlistResponse;
import com.portfoliotracker.backend.entity.Coin;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.entity.Watchlist;
import com.portfoliotracker.backend.repository.CoinRepository;
import com.portfoliotracker.backend.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final CoinGeckoService coinGeckoService;
    private final CoinRepository coinRepository;

    public WatchlistResponse add(WatchlistRequest request, User user) {
        if (watchlistRepository.existsByUserIdAndCoinId(user.getId(), request.getCoinId())) {
            throw new RuntimeException("La moneda ya está en tu watchlist");
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .coinId(request.getCoinId())
                .coinName(request.getCoinName())
                .coinSymbol(request.getCoinSymbol())
                .build();

        watchlistRepository.save(watchlist);

        return WatchlistResponse.builder()
                .id(watchlist.getId())
                .coinId(watchlist.getCoinId())
                .coinName(watchlist.getCoinName())
                .coinSymbol(watchlist.getCoinSymbol())
                .currentPriceUsd(BigDecimal.ZERO)
                .priceChangePercent24h(BigDecimal.ZERO)
                .priceChangePercent7d(BigDecimal.ZERO)
                .build();
    }

    public List<WatchlistResponse> getWatchlist(User user) {
        List<Watchlist> items = watchlistRepository.findByUserId(user.getId());

        if (items.isEmpty()) return List.of();

        List<Map<String, Object>> marketData = coinGeckoService.getMarketDataWithSparkline(
                items.stream().map(Watchlist::getCoinId).collect(Collectors.toList())
        );

        Map<String, Map<String, Object>> priceById = marketData.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("id"),
                        m -> m
                ));

        return items.stream().map(item -> {
            Map<String, Object> data = priceById.get(item.getCoinId());
            BigDecimal currentPrice = BigDecimal.ZERO;
            BigDecimal change24h = BigDecimal.ZERO;
            BigDecimal change7d = BigDecimal.ZERO;
            BigDecimal marketCap = BigDecimal.ZERO;
            BigDecimal volume24h = BigDecimal.ZERO;
            BigDecimal high24h = BigDecimal.ZERO;
            BigDecimal low24h = BigDecimal.ZERO;
            List<Double> sparkline = List.of();

            if (data != null) {
                currentPrice = toBigDecimal(data.get("current_price"));
                change24h = toBigDecimal(data.get("price_change_percentage_24h"));
                change7d = toBigDecimal(data.get("price_change_percentage_7d_in_currency"));
                marketCap = toBigDecimal(data.get("market_cap"));
                volume24h = toBigDecimal(data.get("total_volume"));
                high24h = toBigDecimal(data.get("high_24h"));
                low24h = toBigDecimal(data.get("low_24h"));

                Map<String, Object> sparklineData = (Map<String, Object>) data.get("sparkline_in_7d");
                if (sparklineData != null) {
                    List<Double> prices = (List<Double>) sparklineData.get("price");
                    if (prices != null) {
                        // Reducimos a 24 puntos para no sobrecargar
                        int step = Math.max(1, prices.size() / 24);
                        sparkline = new java.util.ArrayList<>();
                        for (int i = 0; i < prices.size(); i += step) {
                            sparkline.add(prices.get(i));
                        }
                    }
                }
            }

            String imageUrl = coinRepository.findById(item.getCoinId())
                    .map(Coin::getImageUrl)
                    .orElse("");

            return WatchlistResponse.builder()
                    .id(item.getId())
                    .coinId(item.getCoinId())
                    .coinName(item.getCoinName())
                    .coinSymbol(item.getCoinSymbol())
                    .imageUrl(imageUrl)
                    .currentPriceUsd(currentPrice)
                    .priceChangePercent24h(change24h)
                    .priceChangePercent7d(change7d)
                    .marketCap(marketCap)
                    .volume24h(volume24h)
                    .high24h(high24h)
                    .low24h(low24h)
                    .sparkline7d(sparkline)
                    .build();
        }).collect(Collectors.toList());
    }

    public void remove(String coinId, User user) {
        Watchlist item = watchlistRepository
                .findByUserIdAndCoinId(user.getId(), coinId)
                .orElseThrow(() -> new RuntimeException("Moneda no encontrada en tu watchlist"));
        watchlistRepository.delete(item);
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case Double d -> BigDecimal.valueOf(d);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case null, default -> BigDecimal.ZERO;
        };
    }

}