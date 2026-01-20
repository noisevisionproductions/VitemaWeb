package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.service.CacheMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final CacheMonitoringService cacheMonitoringService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Map<String, Object>>> getCacheStatistics() {
        return ResponseEntity.ok(cacheMonitoringService.getCacheStatistics());
    }

    @PostMapping("/{cacheName}/invalidate")
    public ResponseEntity<String> invalidateCache(@PathVariable String cacheName) {
        cacheMonitoringService.invalidateCache(cacheName);
        return ResponseEntity.ok("Cache '" + cacheName + "' invalidated successfully");
    }

    @PostMapping("/invalidate-all")
    public ResponseEntity<String> invalidateAllCaches() {
        cacheMonitoringService.invalidateCache("recipesCache");
        cacheMonitoringService.invalidateCache("recipesBatchCache");
        cacheMonitoringService.invalidateCache("recipesPageCache");
        cacheMonitoringService.invalidateCache("recipesSearchCache");
        return ResponseEntity.ok("All caches invalidated successfully");
    }
}