package com.noisevisionsoftware.nutrilog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMonitoringService {

    private final CacheManager cacheManager;

    /*
     * Loguje statystyki wszystkich cache co 3 godziny
     * */
    @Scheduled(fixedRate = 3 * 60 * 60 * 1000)
    public void logCacheStatistics() {
        Map<String, CacheStats> statsMap = new HashMap<>();

        // Statystyki ze wszystkich cache
        cacheManager.getCacheNames().forEach(name -> {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(name);
            if (caffeineCache != null) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                statsMap.put(name, nativeCache.stats());
            }
        });

        log.info("Cache Statistics:");
        statsMap.forEach((name, stats) -> {
            log.info("Cache '{}': hit_rate={}%, hit_count={}, miss_count={}, eviction_count={}, avg_load_time={}ms",
                    name,
                    String.format("%.2f", stats.hitRate() * 100),
                    stats.hitCount(),
                    stats.missCount(),
                    stats.evictionCount(),
                    stats.averageLoadPenalty() / 1_000_000.0);
        });
    }

    /*
     * Wymusza czyszczenie nieużywanych wpisów w cache co 2 godziny
     * */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    public void cleanupCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(name);
            if (caffeineCache != null) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                nativeCache.cleanUp();
            }
        });
        log.info("Cache cleanup completed");
    }

    /*
     * Zwraca aktualne statystyki cache
     * */
    public Map<String, Map<String, Object>> getCacheStatistics() {
        Map<String, Map<String, Object>> result = new HashMap<>();

        cacheManager.getCacheNames().forEach(name -> {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(name);
            if (caffeineCache != null) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();

                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("hitRate", stats.hitRate());
                cacheStats.put("hitCount", stats.hitCount());
                cacheStats.put("missCount", stats.missCount());
                cacheStats.put("evictionCount", stats.evictionCount());
                cacheStats.put("averageLoadPenalty", stats.averageLoadPenalty() / 1_000_000.0);
                cacheStats.put("estimatedSize", nativeCache.estimatedSize());

                result.put(name, cacheStats);
            }
        });

        return result;
    }

    /**
     * Ręcznie opróżnia wskazany cache
     */
    public void invalidateCache(String cacheName) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            caffeineCache.clear();
            log.info("Cache '{}' has been manually invalidated", cacheName);
        } else {
            log.warn("Cache '{}' not found", cacheName);
        }
    }
}
