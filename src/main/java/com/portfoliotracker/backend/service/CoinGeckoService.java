package com.portfoliotracker.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoinGeckoService {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private static final String DEMO_APIKEY_HEADER = "x-cg-demo-api-key";

    // URLs
    public static final String PRICES_USD = "/simple/price?ids={ids}&vs_currencies=usd";
    public static final String SEARCH_COINS = "/search?query={query}";
    public static final String GET_MARKET_DATA = "/coins/markets?vs_currency=usd&ids={ids}&price_change_percentage=7d";
    public static final String GET_COIN_LIST = "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=250&page=1";
    public static final String GET_COIN_LIST_PAGE = "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=250&page={page}";

    private final String apiKey;
    private final RestClient restClient;

    public CoinGeckoService(@Value("${coingecko.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(DEMO_APIKEY_HEADER, apiKey)
                .build();
    }


    @SuppressWarnings("unchecked")
    public Map<String, BigDecimal> getPricesUsd(List<String> coinIds) {
        String ids = String.join(",", coinIds);

        Map<String, Map<String, Object>> response = restClient.get()
                .uri(BASE_URL + PRICES_USD, ids)

                .retrieve()
                .body(Map.class);

        if (response == null) return Map.of();

        Map<String, BigDecimal> prices = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : response.entrySet()) {
            Object price = entry.getValue().get("usd");
            if (price instanceof Integer i) prices.put(entry.getKey(), BigDecimal.valueOf(i));
            else if (price instanceof Double d) prices.put(entry.getKey(), BigDecimal.valueOf(d));
        }

        return  prices;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchCoins(String query) {
        Map<String, Map<String, Object>> response = restClient.get()
                .uri(BASE_URL + SEARCH_COINS, query)
                .retrieve()
                .body(Map.class);


        if (response == null) return List.of();

        return (List<Map<String, Object>>) response.get("coins");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMarketData(List<String> coinIds) {
        String ids = String.join(",", coinIds);

        List<Map<String, Object>> response = restClient.get()
                .uri(BASE_URL + GET_MARKET_DATA, ids)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        return response != null ? response : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCoinList() {
        List<Map<String, Object>> response = restClient.get()
                .uri(BASE_URL + GET_COIN_LIST)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});
        return response != null ? response : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCoinListPage(int page) {
        List<Map<String, Object>> response = restClient.get()
                .uri(BASE_URL + GET_COIN_LIST_PAGE, page)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});
        return response != null ? response : List.of();
    }
}