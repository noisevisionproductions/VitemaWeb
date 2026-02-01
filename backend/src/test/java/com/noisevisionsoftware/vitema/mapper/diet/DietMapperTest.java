package com.noisevisionsoftware.vitema.mapper.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.DayMealRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DayRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietMetadataRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.DayMealResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.DayResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.DietResponse;
import com.noisevisionsoftware.vitema.model.diet.*;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DietMapperTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DietMapper dietMapper;

    private Diet testDiet;
    private DietRequest testDietRequest;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_USER_EMAIL = "test@example.com";
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

        DayMealRequest mealRequest = DayMealRequest.builder()
                .recipeId(TEST_RECIPE_ID)
                .mealType(MealType.BREAKFAST)
                .time(TEST_TIME)
                .build();

        DayRequest dayRequest = DayRequest.builder()
                .date(Timestamp.now())
                .meals(Collections.singletonList(mealRequest))
                .build();


        DietMetadataRequest metadataRequest = DietMetadataRequest.builder()
                .totalDays(7)
                .fileName(TEST_FILE_NAME)
                .fileUrl(TEST_FILE_URL)
                .build();

        testDietRequest = DietRequest.builder()
                .userId(TEST_USER_ID)
                .days(Collections.singletonList(dayRequest))
                .metadata(metadataRequest)
                .build();
    }

    @Test
    void toResponse_WhenDietIsNull_ShouldReturnNull() {
        // Act
        DietResponse response = dietMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toResponse_WhenDietIsValid_ShouldMapCorrectly() {
        // Arrange
        when(userService.getUserEmail(TEST_USER_ID)).thenReturn(TEST_USER_EMAIL);

        // Act
        DietResponse response = dietMapper.toResponse(testDiet);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_USER_ID, response.getUserId());
        assertEquals(TEST_USER_EMAIL, response.getUserEmail());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        // Verify metadata
        assertNotNull(response.getMetadata());
        assertEquals(7, response.getMetadata().getTotalDays());
        assertEquals(TEST_FILE_NAME, response.getMetadata().getFileName());
        assertEquals(TEST_FILE_URL, response.getMetadata().getFileUrl());

        // Verify days
        assertNotNull(response.getDays());
        assertEquals(1, response.getDays().size());

        // Verify meal
        DayResponse dayResponse = response.getDays().getFirst();
        assertNotNull(dayResponse.getMeals());
        assertEquals(1, dayResponse.getMeals().size());

        DayMealResponse mealResponse = dayResponse.getMeals().getFirst();
        assertEquals(TEST_RECIPE_ID, mealResponse.getRecipeId());
        assertEquals(MealType.BREAKFAST, mealResponse.getMealType());
        assertEquals(TEST_TIME, mealResponse.getTime());
    }

    @Test
    void toDomain_WhenRequestIsValid_ShouldMapCorrectly() {
        // Act
        Diet diet = dietMapper.toDomain(testDietRequest);

        // Assert
        assertNotNull(diet);
        assertEquals(TEST_USER_ID, diet.getUserId());
        assertNotNull(diet.getCreatedAt());
        assertNotNull(diet.getUpdatedAt());

        // Verify metadata
        assertNotNull(diet.getMetadata());
        assertEquals(7, diet.getMetadata().getTotalDays());
        assertEquals(TEST_FILE_NAME, diet.getMetadata().getFileName());
        assertEquals(TEST_FILE_URL, diet.getMetadata().getFileUrl());

        // Verify days
        assertNotNull(diet.getDays());
        assertEquals(1, diet.getDays().size());

        // Verify meal
        Day day = diet.getDays().getFirst();
        assertNotNull(day.getMeals());
        assertEquals(1, day.getMeals().size());

        DayMeal meal = day.getMeals().getFirst();
        assertEquals(TEST_RECIPE_ID, meal.getRecipeId());
        assertEquals(MealType.BREAKFAST, meal.getMealType());
        assertEquals(TEST_TIME, meal.getTime());
    }

    @Test
    void toResponse_WhenMetadataIsNull_ShouldMapWithoutMetadata() {
        // Arrange
        testDiet.setMetadata(null);
        when(userService.getUserEmail(TEST_USER_ID)).thenReturn(TEST_USER_EMAIL);

        // Act
        DietResponse response = dietMapper.toResponse(testDiet);

        // Assert
        assertNotNull(response);
        assertNull(response.getMetadata());
    }

    @Test
    void toDomain_WhenMetadataIsNull_ShouldMapWithoutMetadata() {
        // Arrange
        testDietRequest.setMetadata(null);

        // Act
        Diet diet = dietMapper.toDomain(testDietRequest);

        // Assert
        assertNotNull(diet);
        assertNull(diet.getMetadata());
    }

    @Test
    void toResponse_WhenDaysListIsEmpty_ShouldMapWithEmptyDays() {
        // Arrange
        testDiet.setDays(Collections.emptyList());
        when(userService.getUserEmail(TEST_USER_ID)).thenReturn(TEST_USER_EMAIL);

        // Act
        DietResponse response = dietMapper.toResponse(testDiet);

        // Assert
        assertNotNull(response);
        assertTrue(response.getDays().isEmpty());
    }

    @Test
    void timestampToLocalDateTime_ShouldConvertCorrectly() {
        // Arrange
        Timestamp timestamp = Timestamp.now();
        LocalDateTime expected = timestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Act
        LocalDateTime result = dietMapper.timestampToLocalDateTime(timestamp);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void toResponse_ShouldCallUserService() {
        // Arrange
        when(userService.getUserEmail(TEST_USER_ID)).thenReturn(TEST_USER_EMAIL);

        // Act
        dietMapper.toResponse(testDiet);

        // Verify
        verify(userService).getUserEmail(TEST_USER_ID);
    }
}