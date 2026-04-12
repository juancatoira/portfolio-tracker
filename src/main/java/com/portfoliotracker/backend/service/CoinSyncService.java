package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.entity.Coin;
import com.portfoliotracker.backend.repository.CoinRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinSyncService {

    private final CoinRepository coinRepository;
    private final CoinGeckoService coinGeckoService;

    @Value("${coins.sync.on-startup:true}")
    private boolean syncOnStartup;

    @PostConstruct
    public void syncOnStartup() {
        if (!syncOnStartup) {
            log.info("Sincronización al arrancar desactivada.");
            return;
        }
        log.info("Sincronizando monedas al arrancar...");
        sync();
    }

    @Scheduled(cron = "0 0 0 * * *") // Cada día a medianoche
    public void syncScheduled() {
        log.info("Sincronización programada de monedas...");
        sync();
    }

    public void sync() {
        try {
            List<Coin> allCoins = new ArrayList<>();

            for (int page = 1; page <= 5; page++) {
                log.info("Descargando página {} de monedas...", page);

                try {
                    List<Map<String, Object>> pageData = coinGeckoService.getCoinListPage(page);

                    if (pageData.isEmpty()) break;

                    for (Map<String, Object> data : pageData) {
                        Coin coin = Coin.builder()
                                .id((String) data.get("id"))
                                .symbol(((String) data.get("symbol")).toUpperCase())
                                .name((String) data.get("name"))
                                .imageUrl((String) data.get("image"))
                                .marketCapRank(data.get("market_cap_rank") instanceof Integer i ? i : null)
                                .build();
                        allCoins.add(coin);
                    }

                    // Guardamos tras cada página por si falla la siguiente
                    coinRepository.saveAll(allCoins);
                    log.info("Página {} guardada. Total acumulado: {} monedas", page, allCoins.size());
                    allCoins.clear();

                } catch (Exception e) {
                    log.warn("Error en página {}, continuando con lo descargado: {}", page, e.getMessage());
                    break;
                }

                if (page < 5) Thread.sleep(25000);
            }

            log.info("Sincronización completada");

        } catch (Exception e) {
            log.error("Error durante la sincronización de monedas: {}", e.getMessage());
        }
    }
}