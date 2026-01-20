package com.noisevisionsoftware.vitema.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheMonitoringServiceTest {

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CacheMonitoringService cacheMonitoringService;

    @Test
    void logCacheStatistics_ShouldLogStatisticsForAllCaches() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("usersCache", "recipesCache"));

        CaffeineCache userCache = mock(CaffeineCache.class);
        CaffeineCache recipesCache = mock(CaffeineCache.class);

        when(cacheManager.getCache("usersCache")).thenReturn(userCache);
        when(cacheManager.getCache("recipesCache")).thenReturn(recipesCache);

        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeUserCache = mock(Cache.class);
        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeRecipesCache = mock(Cache.class);

        when(userCache.getNativeCache()).thenReturn(nativeUserCache);
        when(recipesCache.getNativeCache()).thenReturn(nativeRecipesCache);

        CacheStats userCacheStats = mock(CacheStats.class);
        CacheStats recipesCacheStats = mock(CacheStats.class);

        when(nativeUserCache.stats()).thenReturn(userCacheStats);
        when(nativeRecipesCache.stats()).thenReturn(recipesCacheStats);

        // Act
        cacheMonitoringService.logCacheStatistics();

        // Assert
        verify(cacheManager, times(1)).getCacheNames();
        verify(cacheManager, times(1)).getCache("usersCache");
        verify(cacheManager, times(1)).getCache("recipesCache");
        verify(userCache, times(1)).getNativeCache();
        verify(recipesCache, times(1)).getNativeCache();
        verify(nativeUserCache, times(1)).stats();
        verify(nativeRecipesCache, times(1)).stats();
    }

    @Test
    void cleanupCaches_ShouldCleanupAllCaches() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("usersCache", "recipesCache"));

        CaffeineCache userCache = mock(CaffeineCache.class);
        CaffeineCache recipesCache = mock(CaffeineCache.class);

        when(cacheManager.getCache("usersCache")).thenReturn(userCache);
        when(cacheManager.getCache("recipesCache")).thenReturn(recipesCache);

        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeUserCache = mock(Cache.class);
        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeRecipesCache = mock(Cache.class);

        when(userCache.getNativeCache()).thenReturn(nativeUserCache);
        when(recipesCache.getNativeCache()).thenReturn(nativeRecipesCache);

        // Act
        cacheMonitoringService.cleanupCaches();

        // Assert
        verify(cacheManager, times(1)).getCacheNames();
        verify(cacheManager, times(1)).getCache("usersCache");
        verify(cacheManager, times(1)).getCache("recipesCache");
        verify(userCache, times(1)).getNativeCache();
        verify(recipesCache, times(1)).getNativeCache();
        verify(nativeUserCache, times(1)).cleanUp();
        verify(nativeRecipesCache, times(1)).cleanUp();
    }

    @Test
    void getCacheStatistics_ShouldReturnStatisticsForAllCaches() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("usersCache", "recipesCache"));

        CaffeineCache userCache = mock(CaffeineCache.class);
        CaffeineCache recipesCache = mock(CaffeineCache.class);

        when(cacheManager.getCache("usersCache")).thenReturn(userCache);
        when(cacheManager.getCache("recipesCache")).thenReturn(recipesCache);

        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeUserCache = mock(Cache.class);
        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeRecipesCache = mock(Cache.class);

        when(userCache.getNativeCache()).thenReturn(nativeUserCache);
        when(recipesCache.getNativeCache()).thenReturn(nativeRecipesCache);

        CacheStats userCacheStats = mock(CacheStats.class);
        CacheStats recipesCacheStats = mock(CacheStats.class);

        when(nativeUserCache.stats()).thenReturn(userCacheStats);
        when(nativeRecipesCache.stats()).thenReturn(recipesCacheStats);

        // Tylko dla tego testu potrzebujemy wartości statystyk
        when(userCacheStats.hitRate()).thenReturn(0.75);
        when(userCacheStats.hitCount()).thenReturn(75L);
        when(userCacheStats.missCount()).thenReturn(25L);
        when(userCacheStats.evictionCount()).thenReturn(10L);
        when(userCacheStats.averageLoadPenalty()).thenReturn(2_000_000.0);
        when(nativeUserCache.estimatedSize()).thenReturn(100L);

        when(recipesCacheStats.hitRate()).thenReturn(0.6);
        when(recipesCacheStats.hitCount()).thenReturn(60L);
        when(recipesCacheStats.missCount()).thenReturn(40L);
        when(recipesCacheStats.evictionCount()).thenReturn(5L);
        when(recipesCacheStats.averageLoadPenalty()).thenReturn(3_000_000.0);
        when(nativeRecipesCache.estimatedSize()).thenReturn(200L);

        // Act
        Map<String, Map<String, Object>> result = cacheMonitoringService.getCacheStatistics();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("usersCache"));
        assertTrue(result.containsKey("recipesCache"));

        Map<String, Object> userStats = result.get("usersCache");
        assertEquals(0.75, userStats.get("hitRate"));
        assertEquals(75L, userStats.get("hitCount"));
        assertEquals(25L, userStats.get("missCount"));
        assertEquals(10L, userStats.get("evictionCount"));
        assertEquals(2.0, userStats.get("averageLoadPenalty"));
        assertEquals(100L, userStats.get("estimatedSize"));

        Map<String, Object> recipesStats = result.get("recipesCache");
        assertEquals(0.6, recipesStats.get("hitRate"));
        assertEquals(60L, recipesStats.get("hitCount"));
        assertEquals(40L, recipesStats.get("missCount"));
        assertEquals(5L, recipesStats.get("evictionCount"));
        assertEquals(3.0, recipesStats.get("averageLoadPenalty"));
        assertEquals(200L, recipesStats.get("estimatedSize"));
    }

    @Test
    void invalidateCache_ShouldClearSpecifiedCache() {
        // Arrange
        CaffeineCache userCache = mock(CaffeineCache.class);
        when(cacheManager.getCache("usersCache")).thenReturn(userCache);

        // Act
        cacheMonitoringService.invalidateCache("usersCache");

        // Assert
        verify(cacheManager, times(1)).getCache("usersCache");
        verify(userCache, times(1)).clear();
    }

    @Test
    void invalidateCache_ShouldLogWarning_WhenCacheNotFound() {
        // Arrange
        String nonExistentCacheName = "nonExistentCache";
        when(cacheManager.getCache(nonExistentCacheName)).thenReturn(null);

        // Act
        cacheMonitoringService.invalidateCache(nonExistentCacheName);

        // Assert
        verify(cacheManager, times(1)).getCache(nonExistentCacheName);
    }
}