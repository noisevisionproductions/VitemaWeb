package com.noisevisionsoftware.nutrilog.repository.diet;

import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplate;
import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplateCategory;

import java.util.List;
import java.util.Optional;

public interface DietTemplateRepository {

    Optional<DietTemplate> findById(String id);

    List<DietTemplate> findByCreatedBy(String createdBy);

    List<DietTemplate> findByCategoryAndCreatedBy(DietTemplateCategory category, String createdBy);

    List<DietTemplate> findTopByCreatedByOrderByUsageCountDesc(String createdBy, int limit);

    List<DietTemplate> searchByNameOrDescription(String query, String createdBy, int limit);

    DietTemplate save(DietTemplate template);

    void deleteById(String id);

    void incrementUsageCount(String id);

    long countByCreatedBy(String createdBy);
}
