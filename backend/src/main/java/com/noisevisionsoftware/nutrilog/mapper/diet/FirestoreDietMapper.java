package com.noisevisionsoftware.nutrilog.mapper.diet;

import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.nutrilog.model.diet.*;
import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FirestoreDietMapper {

    public Map<String, Object> toFirestoreMap(Diet diet) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", diet.getUserId());
        data.put("createdAt", diet.getCreatedAt());
        data.put("updatedAt", diet.getUpdatedAt());
        data.put("days", diet.getDays().stream()
                .map(this::dayToMap)
                .collect(Collectors.toList()));
        data.put("metadata", metadataToMap(diet.getMetadata()));
        return data;
    }

    private Map<String, Object> metadataToMap(DietMetadata metadata) {
        if (metadata == null) return null;
        Map<String, Object> data = new HashMap<>();
        data.put("totalDays", metadata.getTotalDays());
        data.put("fileName", metadata.getFileName());
        data.put("fileUrl", metadata.getFileUrl());
        return data;
    }

    private Map<String, Object> dayToMap(Day day) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", day.getDate());
        data.put("meals", day.getMeals().stream()
                .map(this::mealToMap)
                .collect(Collectors.toList()));
        return data;
    }

    private Map<String, Object> mealToMap(DayMeal meal) {
        Map<String, Object> data = new HashMap<>();
        data.put("recipeId", meal.getRecipeId());
        data.put("mealType", meal.getMealType().name());
        data.put("time", meal.getTime());
        return data;
    }

    @SuppressWarnings("unchecked")
    public Diet toDiet(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return Diet.builder()
                .id(document.getId())
                .userId(getStringValue(data))
                .createdAt(getTimestampValue(data, "createdAt"))
                .updatedAt(getTimestampValue(data, "updatedAt"))
                .days(toDays((List<Map<String, Object>>) data.get("days")))
                .metadata(toMetadata((Map<String, Object>) data.get("metadata")))
                .build();
    }

    // Metody pomocnicze do bezpiecznego pobierania wartości
    private String getStringValue(Map<String, Object> data) {
        Object value = data.get("userId");
        return value instanceof String ? (String) value : null;
    }

    private Timestamp getTimestampValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof Timestamp ? (Timestamp) value : null;
    }

    private List<Day> toDays(List<Map<String, Object>> daysData) {
        if (daysData == null) return new ArrayList<>();
        return daysData.stream()
                .map(this::toDay)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Day toDay(Map<String, Object> dayData) {
        return Day.builder()
                .date(getTimestampValue(dayData, "date"))
                .meals(toMeals((List<Map<String, Object>>) dayData.get("meals")))
                .build();
    }

    private List<DayMeal> toMeals(List<Map<String, Object>> mealsData) {
        if (mealsData == null) return new ArrayList<>();
        return mealsData.stream()
                .map(this::toMeal)
                .collect(Collectors.toList());
    }

    private DayMeal toMeal(Map<String, Object> mealData) {
        return DayMeal.builder()
                .recipeId((String) mealData.get("recipeId"))
                .mealType(MealType.valueOf((String) mealData.get("mealType")))
                .time((String) mealData.get("time"))
                .build();
    }

    private DietMetadata toMetadata(Map<String, Object> metadataData) {
        if (metadataData == null) return null;
        return DietMetadata.builder()
                .totalDays(((Long) metadataData.get("totalDays")).intValue())
                .fileName((String) metadataData.get("fileName"))
                .fileUrl((String) metadataData.get("fileUrl"))
                .build();
    }
}