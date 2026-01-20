package com.noisevisionsoftware.vitema.repository.meal;

import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealTemplateJpaRepository extends JpaRepository<MealTemplateEntity, Long> {

    Optional<MealTemplateEntity> findByExternalId(String externalId);

    @Query("SELECT m FROM MealTemplateEntity m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.instructions) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY m.usageCount DESC, m.lastUsed DESC NULLS LAST")
    List<MealTemplateEntity> searchByNameOrInstructions(@Param("query") String query, Pageable pageable);

    @Query("SELECT m FROM MealTemplateEntity m WHERE m.nameLower LIKE LOWER(CONCAT(:query, '%')) " +
            "ORDER BY LENGTH(m.name), m.usageCount DESC")
    List<MealTemplateEntity> findByNameStartingWithIgnoreCase(@Param("query") String query, Pageable pageable);

    List<MealTemplateEntity> findTopByOrderByUsageCountDescLastUsedDesc(Pageable pageable);

    @Query("SELECT m FROM MealTemplateEntity m WHERE m.lastUsed IS NOT NULL " +
            "ORDER BY m.lastUsed DESC")
    List<MealTemplateEntity> findRecentlyUsed(Pageable pageable);

    List<MealTemplateEntity> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    @Query("SELECT m FROM MealTemplateEntity m WHERE m.mealType = :mealType " +
            "ORDER BY m.usageCount DESC")
    List<MealTemplateEntity> findByMealTypeOrderByUsageCountDesc(@Param("mealType") String mealType, Pageable pageable);

    @Modifying
    @Query("UPDATE MealTemplateEntity m SET m.usageCount = m.usageCount + 1, m.lastUsed = CURRENT_TIMESTAMP " +
            "WHERE m.externalId = :externalId")
    void incrementUsageCount(@Param("externalId") String externalId);

    @Query("SELECT COUNT(m) FROM MealTemplateEntity m WHERE m.createdBy = :createdBy")
    long countByCreatedBy(@Param("createdBy") String createdBy);

    @Query("SELECT m FROM MealTemplateEntity m WHERE " +
            "(m.isPublic = true OR m.createdBy = :userId) AND " +
            "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.instructions) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY " +
            "CASE WHEN LOWER(m.name) = LOWER(:query) THEN 1 " +
            "     WHEN LOWER(m.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "     ELSE 3 END, " +
            "m.usageCount DESC, m.lastUsed DESC NULLS LAST")
    List<MealTemplateEntity> searchAccessibleTemplates(@Param("query") String query,
                                                       @Param("userId") String userId,
                                                       Pageable pageable);
}