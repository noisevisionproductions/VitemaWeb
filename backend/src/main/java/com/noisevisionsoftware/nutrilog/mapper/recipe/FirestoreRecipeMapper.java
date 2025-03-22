package com.noisevisionsoftware.nutrilog.mapper.recipe;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FirestoreRecipeMapper {

    public Map<String, Object> toFirestoreMap(Recipe recipe) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", recipe.getName());
        data.put("instructions", recipe.getInstructions());
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(Timestamp.now());
        }
        data.put("createdAt", recipe.getCreatedAt());
        data.put("updatedAt", Timestamp.now());
        data.put("photos", recipe.getPhotos() != null ? recipe.getPhotos() : new ArrayList<>());
        data.put("nutritionalValues", nutritionalValuesToMap(recipe.getNutritionalValues()));
        data.put("parentRecipeId", recipe.getParentRecipeId());
        return data;
    }

    private Map<String, Object> nutritionalValuesToMap(NutritionalValues values) {
        if (values == null) return null;
        Map<String, Object> data = new HashMap<>();
        data.put("calories", values.getCalories());
        data.put("protein", values.getProtein());
        data.put("fat", values.getFat());
        data.put("carbs", values.getCarbs());
        return data;
    }

    @SuppressWarnings("unchecked")
    public Recipe toRecipe(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return Recipe.builder()
                .id(document.getId())
                .name((String) data.get("name"))
                .instructions((String) data.get("instructions"))
                .createdAt((Timestamp) data.get("createdAt"))
                .photos(getPhotos(data))
                .nutritionalValues(toNutritionalValues((Map<String, Object>) data.get("nutritionalValues")))
                .parentRecipeId((String) data.get("parentRecipeId"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> getPhotos(Map<String, Object> data) {
        List<String> photos = (List<String>) data.get("photos");
        return photos != null ? new ArrayList<>(photos) : new ArrayList<>();
    }

    private NutritionalValues toNutritionalValues(Map<String, Object> data) {
        if (data == null) return null;

        return NutritionalValues.builder()
                .calories(getDoubleValue(data, "calories"))
                .protein(getDoubleValue(data, "protein"))
                .fat(getDoubleValue(data, "fat"))
                .carbs(getDoubleValue(data, "carbs"))
                .build();
    }

    private double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return 0.0;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return 0.0;
    }
}