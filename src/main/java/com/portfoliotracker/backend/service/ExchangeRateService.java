package com.portfoliotracker.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestClient restClient = RestClient.create();
    private final Map<String, BigDecimal> rateCache = new ConcurrentHashMap<>();
    private volatile LocalDateTime lastUpdated = null;
    private final Object refreshLock = new Object();

    private static final Map<String, String> CURRENCY_SYMBOLS = Map.of(
        "USD", "$",
        "EUR", "€",
        "GBP", "£",
        "JPY", "¥",
        "CHF", "CHF",
        "CAD", "C$",
        "AUD", "A$",
        "MXN", "MX$"
    );

    public BigDecimal convertFromUsd(BigDecimal amountUsd, String targetCurrency) {
        if (targetCurrency == null || targetCurrency.equals("USD")) return amountUsd;
        BigDecimal rate = getRate(targetCurrency);
        return amountUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertToUsd(BigDecimal amount, String fromCurrency) {
        if (fromCurrency == null || fromCurrency.equals("USD")) return amount;
        BigDecimal rate = getRate(fromCurrency);
        if (rate.compareTo(BigDecimal.ZERO) == 0) return amount;
        return amount.divide(rate, 8, RoundingMode.HALF_UP);
    }

    public String getSymbol(String currency) {
        return CURRENCY_SYMBOLS.getOrDefault(currency, currency);
    }

    public Map<String, String> getSupportedCurrencies() {
        return CURRENCY_SYMBOLS;
    }

    public Map<String, BigDecimal> getRates() {
        ensureFresh();
        return Collections.unmodifiableMap(new HashMap<>(rateCache));
    }

    private BigDecimal getRate(String currency) {
        ensureFresh();
        return rateCache.getOrDefault(currency, BigDecimal.ONE);
    }

    private void ensureFresh() {
        if (lastUpdated == null || lastUpdated.isBefore(LocalDateTime.now().minusHours(1))) {
            synchronized (refreshLock) {
                if (lastUpdated == null || lastUpdated.isBefore(LocalDateTime.now().minusHours(1))) {
                    refreshRates();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshRates() {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://api.frankfurter.app/latest?from=USD")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.containsKey("rates")) {
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
                rates.forEach((currency, rate) -> {
                    if (rate instanceof Double d) rateCache.put(currency, BigDecimal.valueOf(d));
                    else if (rate instanceof Integer i) rateCache.put(currency, BigDecimal.valueOf(i));
                });
                rateCache.put("USD", BigDecimal.ONE);
                lastUpdated = LocalDateTime.now();
                log.info("Tipos de cambio actualizados: {} monedas", rateCache.size());
            }
        } catch (Exception e) {
            log.error("Error actualizando tipos de cambio: {}", e.getMessage());
        }
    }
}