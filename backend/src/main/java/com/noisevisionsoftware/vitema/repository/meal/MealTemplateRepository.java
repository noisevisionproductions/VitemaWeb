package com.noisevisionsoftware.vitema.repository.meal;

import com.noisevisionsoftware.vitema.model.meal.MealTemplate;

import java.util.List;
import java.util.Optional;

public interface MealTemplateRepository {

    Optional<MealTemplate> findById(String id);

    List<MealTemplate> searchByName(String query, int limit);

    List<MealTemplate> findTopByUsageCount(int limit);

    List<MealTemplate> findRecentlyUsed(int limit);

    MealTemplate save(MealTemplate template);

    void delete(String id);

    void incrementUsageCount(String id);

    List<MealTemplate> searchAccessibleTemplates(String query, String userId, int limit);
}