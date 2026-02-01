package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.dto.request.diet.DietTemplateExcelRequest;
import com.noisevisionsoftware.vitema.dto.response.ValidationResponse;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationCacheServiceTest {

    private ValidationCacheService cacheService;
    private DietTemplateExcelRequest mockRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        cacheService = new ValidationCacheService();

        mockFile = new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test data".getBytes()
        );

        mockRequest = mock(DietTemplateExcelRequest.class);
        when(mockRequest.getFile()).thenReturn(mockFile);
        when(mockRequest.getMealsPerDay()).thenReturn(3);
        when(mockRequest.getDuration()).thenReturn(7);
        when(mockRequest.getStartDate()).thenReturn("2023-10-15");
    }

    @Test
    @DisplayName("Powinien generować unikalny klucz cache dla danego żądania")
    void generateCacheKey_shouldGenerateUniqueKeyForRequest() {
        // given
        DietTemplateExcelRequest request1 = mock(DietTemplateExcelRequest.class);
        when(request1.getFile()).thenReturn(mockFile);
        when(request1.getMealsPerDay()).thenReturn(3);
        when(request1.getDuration()).thenReturn(7);
        when(request1.getStartDate()).thenReturn("2023-10-15");

        DietTemplateExcelRequest request2 = mock(DietTemplateExcelRequest.class);
        when(request2.getFile()).thenReturn(mockFile);
        when(request2.getMealsPerDay()).thenReturn(4);
        when(request2.getDuration()).thenReturn(7);
        when(request2.getStartDate()).thenReturn("2023-10-15");

        // when
        String key1 = cacheService.generateCacheKey(request1, "");
        String key2 = cacheService.generateCacheKey(request2, "");

        // then
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Powinien zwracać empty Optional gdy nie ma wpisu w cache")
    void getFromCache_shouldReturnEmptyWhenNotInCache() {
        // given
        String nonExistingKey = "non-existing-key";

        // when
        Optional<ValidationResponse> result = cacheService.getFromCache(nonExistingKey);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Powinien zapisywać i pobierać odpowiedź z cache")
    void putInCache_shouldSaveResponseAndGetFromCache() {
        // given
        String cacheKey = "test-key";
        ValidationResponse validResponse = new ValidationResponse();
        validResponse.setValid(true);
        validResponse.setValidationResults(Collections.singletonList(
                new ValidationResult(true, "Test validation", ValidationSeverity.SUCCESS)
        ));

        // when
        cacheService.putInCache(cacheKey, validResponse);
        Optional<ValidationResponse> retrievedResponse = cacheService.getFromCache(cacheKey);

        // then
        assertTrue(retrievedResponse.isPresent());
        assertEquals(validResponse, retrievedResponse.get());
    }

    @Test
    @DisplayName("Nie powinien zapisywać nieprawidłowych odpowiedzi do cache")
    void putInCache_shouldNotSaveInvalidResponses() {
        // given
        String cacheKey = "test-key";
        ValidationResponse invalidResponse = new ValidationResponse();
        invalidResponse.setValid(false);
        invalidResponse.setValidationResults(Collections.singletonList(
                new ValidationResult(false, "Error validation", ValidationSeverity.ERROR)
        ));

        // when
        cacheService.putInCache(cacheKey, invalidResponse);
        Optional<ValidationResponse> retrievedResponse = cacheService.getFromCache(cacheKey);

        // then
        assertFalse(retrievedResponse.isPresent());
    }

    @Test
    @DisplayName("Powinien usuwać przeterminowane wpisy z cache")
    @SuppressWarnings("unchecked")
    void cleanExpiredCacheEntries_shouldRemoveExpiredEntries() throws Exception {
        // given
        String key1 = "key1";
        String key2 = "key2";

        ValidationResponse response1 = new ValidationResponse();
        response1.setValid(true);

        ValidationResponse response2 = new ValidationResponse();
        response2.setValid(true);

        Field validationCacheField = ValidationCacheService.class.getDeclaredField("validationCache");
        validationCacheField.setAccessible(true);
        Map<String, ValidationResponse> validationCache = (Map<String, ValidationResponse>) validationCacheField.get(cacheService);

        Field cacheTimestampsField = ValidationCacheService.class.getDeclaredField("cacheTimestamps");
        cacheTimestampsField.setAccessible(true);
        Map<String, Long> cacheTimestamps = (Map<String, Long>) cacheTimestampsField.get(cacheService);

        validationCache.put(key1, response1);
        validationCache.put(key2, response2);

        long now = System.currentTimeMillis();
        long ttl = 10 * 60 * 1000;

        cacheTimestamps.put(key1, now - ttl - 1000);
        cacheTimestamps.put(key2, now);

        // when
        cacheService.getFromCache("any-key");

        // then
        assertFalse(validationCache.containsKey(key1));
        assertTrue(validationCache.containsKey(key2));
    }

    @Test
    @DisplayName("Powinien usuwać najstarsze wpisy gdy cache jest przepełniony")
    void cleanExpiredCacheEntries_shouldRemoveOldestEntriesWhenCacheIsFull() throws Exception {
        // given
        Field validationCacheField = ValidationCacheService.class.getDeclaredField("validationCache");
        validationCacheField.setAccessible(true);

        Field cacheTimestampsField = ValidationCacheService.class.getDeclaredField("cacheTimestamps");
        cacheTimestampsField.setAccessible(true);

        ValidationCacheService testCacheService = new ValidationCacheService();
        Map<String, ValidationResponse> validationCache = new ConcurrentHashMap<>();
        Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

        validationCacheField.set(testCacheService, validationCache);
        cacheTimestampsField.set(testCacheService, cacheTimestamps);

        long now = System.currentTimeMillis();
        ValidationResponse validResponse = new ValidationResponse();
        validResponse.setValid(true);

        for (int i = 0; i < 110; i++) {
            String key = "key-" + i;
            validationCache.put(key, validResponse);
            cacheTimestamps.put(key, now - (i * 1000));
        }

        // when
        testCacheService.getFromCache("any-key");

        // then
        assertTrue(validationCache.size() <= 88);

        for (int i = 100; i < 110; i++) {
            String oldKey = "key-" + i;
            assertFalse(validationCache.containsKey(oldKey), "Klucz " + oldKey + " powinien być usunięty");
        }
    }

    @Test
    @DisplayName("Metoda fallback generateCacheKey powinna działać gdy MD5 jest niedostępne")
    void generateCacheKey_shouldFallbackWhenMD5IsUnavailable() {
        // given
        ValidationCacheService spyCacheService = spy(cacheService);

        try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
            mockedMessageDigest.when(() -> MessageDigest.getInstance("MD5"))
                    .thenThrow(new NoSuchAlgorithmException("MD5 unavailable"));

            String result = spyCacheService.generateCacheKey(mockRequest, "");

            // then
            assertNotNull(result);
            assertTrue(result.contains("test.xlsx"));
            assertTrue(result.contains(String.valueOf(mockFile.getSize())));
            assertTrue(result.contains("3"));
            assertTrue(result.contains("7"));
            assertTrue(result.contains("2023-10-15"));
        }
    }
}