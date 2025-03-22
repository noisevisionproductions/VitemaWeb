package com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation;

import com.noisevisionsoftware.nutrilog.dto.request.DietTemplateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.ValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ValidationCacheService {
    private final Map<String, ValidationResponse> validationCache = new ConcurrentHashMap<>(100);
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>(100);
    private static final long CACHE_TTL_MS = 10 * 60 * 1000;

    public Optional<ValidationResponse> getFromCache(String cacheKey) {
        cleanExpiredCacheEntries();

        if (validationCache.containsKey(cacheKey)) {
            log.debug("Cache hit for validation request, returning cached response");
            return Optional.of(validationCache.get(cacheKey));
        }

        return Optional.empty();
    }

    public void putInCache(String cacheKey, ValidationResponse response) {
        if (response.isValid()) {
            validationCache.put(cacheKey, response);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }
    }

    public String generateCacheKey(DietTemplateRequest request, String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Dodajemy nazwę pliku i jego rozmiar
            String fileName = request.getFile().getOriginalFilename();
            long fileSize = request.getFile().getSize();
            md.update((fileName + fileSize).getBytes());

            // Dodajemy inne parametry
            md.update(String.valueOf(request.getMealsPerDay()).getBytes());
            md.update(String.valueOf(request.getDuration()).getBytes());
            md.update(request.getStartDate().getBytes());

            if (userId != null) {
                md.update(userId.getBytes());
            }

            // Konwertujemy hash do string
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // Fallback, jeśli nie można użyć MD5
            return request.getFile().getOriginalFilename() + request.getFile().getSize() +
                    request.getMealsPerDay() + request.getDuration() + request.getStartDate();
        }
    }

    private void cleanExpiredCacheEntries() {
        long now = System.currentTimeMillis();

        // Usuwamy wpisy starsze niż CACHE_TTL_MS
        cacheTimestamps.entrySet().removeIf(entry -> {
            boolean isExpired = (now - entry.getValue()) > CACHE_TTL_MS;
            if (isExpired) {
                validationCache.remove(entry.getKey());
            }
            return isExpired;
        });

        // Jeśli cache jest za duży, usuń najstarsze wpisy
        if (validationCache.size() > 100) {
            List<Map.Entry<String, Long>> entries = new ArrayList<>(cacheTimestamps.entrySet());
            entries.sort(Map.Entry.comparingByValue());

            // Usuń 20% najstarszych wpisów
            int toRemove = Math.max(1, validationCache.size() / 5);
            for (int i = 0; i < toRemove && i < entries.size(); i++) {
                String key = entries.get(i).getKey();
                validationCache.remove(key);
                cacheTimestamps.remove(key);
            }
        }
    }
}