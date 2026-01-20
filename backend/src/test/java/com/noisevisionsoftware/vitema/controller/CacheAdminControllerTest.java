package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.service.CacheMonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheAdminControllerTest {

    @Mock
    private CacheMonitoringService cacheMonitoringService;

    @InjectMocks
    private CacheAdminController cacheAdminController;

    @Test
    void getCacheStatistics_ShouldReturnCacheStatistics() {
        // Arrange
        Map<String, Map<String, Object>> mockStats = new HashMap<>();
        Map<String, Object> userCacheStats = new HashMap<>();
        userCacheStats.put("hitRate", 0.75);
        userCacheStats.put("hitCount", 75L);
        mockStats.put("usersCache", userCacheStats);

        when(cacheMonitoringService.getCacheStatistics()).thenReturn(mockStats);

        // Act
        ResponseEntity<Map<String, Map<String, Object>>> response = cacheAdminController.getCacheStatistics();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockStats, response.getBody());
        verify(cacheMonitoringService, times(1)).getCacheStatistics();
    }

    @Test
    void invalidateCache_ShouldInvalidateSpecifiedCache() {
        // Arrange
        String cacheName = "usersCache";
        doNothing().when(cacheMonitoringService).invalidateCache(cacheName);

        // Act
        ResponseEntity<String> response = cacheAdminController.invalidateCache(cacheName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cache 'usersCache' invalidated successfully", response.getBody());
        verify(cacheMonitoringService, times(1)).invalidateCache(cacheName);
    }

    @Test
    void invalidateAllCaches_ShouldInvalidateAllCaches() {
        // Arrange
        doNothing().when(cacheMonitoringService).invalidateCache(anyString());

        // Act
        ResponseEntity<String> response = cacheAdminController.invalidateAllCaches();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("All caches invalidated successfully", response.getBody());

        // Verify that invalidateCache was called for each recipe cache
        verify(cacheMonitoringService, times(1)).invalidateCache("recipesCache");
        verify(cacheMonitoringService, times(1)).invalidateCache("recipesBatchCache");
        verify(cacheMonitoringService, times(1)).invalidateCache("recipesPageCache");
        verify(cacheMonitoringService, times(1)).invalidateCache("recipesSearchCache");

        // Verify that invalidateCache was called exactly 4 times
        verify(cacheMonitoringService, times(4)).invalidateCache(anyString());
    }
}