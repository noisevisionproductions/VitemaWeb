package com.noisevisionsoftware.nutrilog.mapper.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DayMealRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DayRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DietMetadataRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DietRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DayMealResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DayResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietMetadataResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietResponse;
import com.noisevisionsoftware.nutrilog.model.diet.Day;
import com.noisevisionsoftware.nutrilog.model.diet.DayMeal;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.model.diet.DietMetadata;
import com.noisevisionsoftware.nutrilog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DietMapper {
    private final UserService userService;

    public DietResponse toResponse(Diet diet) {
        if (diet == null) return null;

        String userEmail = userService.getUserEmail(diet.getUserId());

        return DietResponse.builder()
                .id(diet.getId())
                .userId(diet.getUserId())
                .userEmail(userEmail)
                .createdAt(diet.getCreatedAt())
                .updatedAt(diet.getUpdatedAt())
                .days(diet.getDays().stream()
                        .map(this::toDayResponse)
                        .collect(Collectors.toList()))
                .metadata(diet.getMetadata() != null ? toMetadataResponse(diet.getMetadata()) : null)
                .build();
    }

    public Diet toDomain(DietRequest request) {
        return Diet.builder()
                .userId(request.getUserId())
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(request.getDays().stream()
                        .map(this::toDay)
                        .collect(Collectors.toList()))
                .metadata(request.getMetadata() != null ? toMetadata(request.getMetadata()) : null)
                .build();
    }

    private DayResponse toDayResponse(Day day) {
        return DayResponse.builder()
                .date(day.getDate())
                .meals(day.getMeals().stream()
                        .map(this::toMealResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private Day toDay(DayRequest request) {
        return Day.builder()
                .date(request.getDate())
                .meals(request.getMeals().stream()
                        .map(this::toMeal)
                        .collect(Collectors.toList()))
                .build();
    }

    private DayMealResponse toMealResponse(DayMeal meal) {
        return DayMealResponse.builder()
                .recipeId(meal.getRecipeId())
                .mealType(meal.getMealType())
                .time(meal.getTime())
                .build();
    }

    private DayMeal toMeal(DayMealRequest request) {
        return DayMeal.builder()
                .recipeId(request.getRecipeId())
                .mealType(request.getMealType())
                .time(request.getTime())
                .build();
    }

    private DietMetadataResponse toMetadataResponse(DietMetadata metadata) {
        return DietMetadataResponse.builder()
                .totalDays(metadata.getTotalDays())
                .fileName(metadata.getFileName())
                .fileUrl(metadata.getFileUrl())
                .build();
    }

    private DietMetadata toMetadata(DietMetadataRequest request) {
        return DietMetadata.builder()
                .totalDays(request.getTotalDays())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .build();
    }

    LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Timestamp convertToFirebaseTimestamp(Object dateInput) {
        try {
            if (dateInput instanceof Timestamp) {
                return (Timestamp) dateInput;
            } else if (dateInput instanceof String dateStr) {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr);
                return Timestamp.of(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                log.warn("Nieobsługiwany typ daty: {}", dateInput.getClass().getName());
                return Timestamp.now();
            }
        } catch (Exception e) {
            log.error("Błąd konwersji daty: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid date format: " + dateInput);
        }
    }
}