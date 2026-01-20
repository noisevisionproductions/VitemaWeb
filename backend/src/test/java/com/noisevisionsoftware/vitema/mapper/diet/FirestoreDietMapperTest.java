package com.noisevisionsoftware.vitema.mapper.diet;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.diet.*;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FirestoreDietMapperTest {

    @InjectMocks
    private FirestoreDietMapper firestoreDietMapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private Diet testDiet;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_RECIPE_ID = "recipe123";
    private static final String TEST_FILE_NAME = "test.pdf";
    private static final String TEST_FILE_URL = "http://example.com/test.pdf";
    private static final String TEST_TIME = "12:00";

    @BeforeEach
    void setUp() {
        Timestamp now = Timestamp.now();
        DayMeal meal = DayMeal.builder()
                .recipeId(TEST_RECIPE_ID)
                .mealType(MealType.BREAKFAST)
                .time(TEST_TIME)
                .build();

        Day day = Day.builder()
                .date(now)
                .meals(Collections.singletonList(meal))
                .build();

        DietMetadata metadata = DietMetadata.builder()
                .totalDays(7)
                .fileName(TEST_FILE_NAME)
                .fileUrl(TEST_FILE_URL)
                .build();

        testDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .createdAt(now)
                .updatedAt(now)
                .days(Collections.singletonList(day))
                .metadata(metadata)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void toFirestoreMap_ShouldMapDietToMap() {
        // Act
        Map<String, Object> result = firestoreDietMapper.toFirestoreMap(testDiet);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.get("userId"));
        assertEquals(testDiet.getCreatedAt(), result.get("createdAt"));
        assertEquals(testDiet.getUpdatedAt(), result.get("updatedAt"));

        // Verify days
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        assertEquals(1, days.size());

        // Verify metadata
        Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
        assertNotNull(metadata);
        assertEquals(7, metadata.get("totalDays"));
        assertEquals(TEST_FILE_NAME, metadata.get("fileName"));
        assertEquals(TEST_FILE_URL, metadata.get("fileUrl"));
    }

    @Test
    void toDiet_WhenDocumentExists_ShouldMapToDiet() {
        // Arrange
        Map<String, Object> mealData = new HashMap<>();
        mealData.put("recipeId", TEST_RECIPE_ID);
        mealData.put("mealType", "BREAKFAST");
        mealData.put("time", TEST_TIME);

        Map<String, Object> dayData = new HashMap<>();
        dayData.put("date", Timestamp.now());
        dayData.put("meals", Collections.singletonList(mealData));

        Map<String, Object> metadataData = new HashMap<>();
        metadataData.put("totalDays", 7L);
        metadataData.put("fileName", TEST_FILE_NAME);
        metadataData.put("fileUrl", TEST_FILE_URL);

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("userId", TEST_USER_ID);
        documentData.put("createdAt", Timestamp.now());
        documentData.put("updatedAt", Timestamp.now());
        documentData.put("days", Collections.singletonList(dayData));
        documentData.put("metadata", metadataData);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(documentData);
        when(documentSnapshot.getId()).thenReturn(TEST_ID);

        // Act
        Diet result = firestoreDietMapper.toDiet(documentSnapshot);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        // Verify days
        assertNotNull(result.getDays());
        assertEquals(1, result.getDays().size());

        // Verify metadata
        assertNotNull(result.getMetadata());
        assertEquals(7, result.getMetadata().getTotalDays());
        assertEquals(TEST_FILE_NAME, result.getMetadata().getFileName());
        assertEquals(TEST_FILE_URL, result.getMetadata().getFileUrl());
    }

    @Test
    void toDiet_WhenDocumentDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        Diet result = firestoreDietMapper.toDiet(documentSnapshot);

        // Assert
        assertNull(result);
    }

    @Test
    void toDiet_WhenDataIsNull_ShouldReturnNull() {
        // Arrange
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(null);

        // Act
        Diet result = firestoreDietMapper.toDiet(documentSnapshot);

        // Assert
        assertNull(result);
    }

    @Test
    void toFirestoreMap_WhenMetadataIsNull_ShouldMapDietWithoutMetadata() {
        // Arrange
        testDiet.setMetadata(null);

        // Act
        Map<String, Object> result = firestoreDietMapper.toFirestoreMap(testDiet);

        // Assert
        assertNotNull(result);
        assertNull(result.get("metadata"));
    }

    @Test
    void toDiet_WhenMealTypeIsInvalid_ShouldHandleException() {
        // Arrange
        Map<String, Object> mealData = new HashMap<>();
        mealData.put("recipeId", TEST_RECIPE_ID);
        mealData.put("mealType", "INVALID_MEAL_TYPE");
        mealData.put("time", TEST_TIME);

        Map<String, Object> dayData = new HashMap<>();
        dayData.put("date", Timestamp.now());
        dayData.put("meals", Collections.singletonList(mealData));

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("days", Collections.singletonList(dayData));

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(documentData);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> firestoreDietMapper.toDiet(documentSnapshot));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toFirestoreMap_WhenDaysListIsEmpty_ShouldMapDietWithEmptyDays() {
        // Arrange
        testDiet.setDays(new ArrayList<>());

        // Act
        Map<String, Object> result = firestoreDietMapper.toFirestoreMap(testDiet);

        // Assert
        assertNotNull(result);
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        assertTrue(days.isEmpty());
    }

    @Test
    void toDiet_WhenTimestampsAreNull_ShouldMapDietWithNullTimestamps() {
        // Arrange
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("userId", TEST_USER_ID);
        documentData.put("createdAt", null);
        documentData.put("updatedAt", null);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(documentData);
        when(documentSnapshot.getId()).thenReturn(TEST_ID);

        // Act
        Diet result = firestoreDietMapper.toDiet(documentSnapshot);

        // Assert
        assertNotNull(result);
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
}