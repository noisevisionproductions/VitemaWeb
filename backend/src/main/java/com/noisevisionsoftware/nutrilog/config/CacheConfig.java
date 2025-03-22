package com.noisevisionsoftware.nutrilog.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<>();

        // Podstawowe cache z domyślną konfiguracją
        Caffeine<Object, Object> defaultCaffeine = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .recordStats();

        // Dodajemy domyślne cache
        caches.add(new CaffeineCache("usersCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("userEmailCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("userRoles", defaultCaffeine.build()));
        caches.add(new CaffeineCache("dietsCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("dietsListCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("measurementsCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("changelogCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("categories", defaultCaffeine.build()));
        caches.add(new CaffeineCache("shoppingListCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("categorizationCache", defaultCaffeine.build()));
        caches.add(new CaffeineCache("newsletterSubscribers", defaultCaffeine.build()));
        caches.add(new CaffeineCache("newsletterStats", defaultCaffeine.build()));

        // Specjalne cache dla przepisów
        caches.add(new CaffeineCache("recipesCache", singleRecipeCaffeine().build()));
        caches.add(new CaffeineCache("recipesBatchCache", batchRecipeCaffeine().build()));
        caches.add(new CaffeineCache("recipesPageCache", pageResultsCaffeine().build()));
        caches.add(new CaffeineCache("recipesSearchCache", searchResultsCaffeine().build()));

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    // Metody pomocnicze dostarczające specyficzne konfiguracje
    private Caffeine<Object, Object> singleRecipeCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(5000)                    // Więcej miejsc dla pojedynczych przepisów
                .expireAfterAccess(8, TimeUnit.HOURS) // Długi czas życia, odświeżany przy dostępie
                .recordStats();
    }

    private Caffeine<Object, Object> batchRecipeCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)                     // Mniej wpisów dla grup przepisów
                .expireAfterWrite(30, TimeUnit.MINUTES) // Krótszy czas życia
                .recordStats();
    }

    private Caffeine<Object, Object> pageResultsCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(100)                     // Ograniczona liczba stron w cache
                .expireAfterWrite(15, TimeUnit.MINUTES) // Krótki czas życia
                .recordStats();
    }

    private Caffeine<Object, Object> searchResultsCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(50)                      // Bardzo ograniczone, bo wyniki wyszukiwania są specyficzne
                .expireAfterWrite(5, TimeUnit.MINUTES) // Bardzo krótki czas życia
                .recordStats();
    }

    @Bean
    public Cache<String, UserRole> roleCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, String> userEmailCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }
}