package com.noisevisionsoftware.vitema.repository.jpa.diet;

import com.noisevisionsoftware.vitema.model.diet.template.DietTemplateCategory;
import com.noisevisionsoftware.vitema.model.diet.template.jpa.DietTemplateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DietTemplateJpaRepository extends JpaRepository<DietTemplateEntity, Long> {

    Optional<DietTemplateEntity> findByExternalId(String externalId);

    List<DietTemplateEntity> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<DietTemplateEntity> findByCategoryAndCreatedByOrderByCreatedAtDesc(
            DietTemplateCategory category,
            String createdBy
    );

    @Query("SELECT dt FROM DietTemplateEntity dt WHERE dt.createdBy = :createdBy " +
            "ORDER BY dt.usageCount DESC, dt.lastUsed DESC NULLS LAST")
    List<DietTemplateEntity> findTopByCreatedByOrderByUsageCountDesc(
            @Param("createdBy") String createdBy,
            Pageable pageable
    );

    @Query("SELECT dt FROM DietTemplateEntity dt WHERE " +
            "dt.createdBy = :createdBy AND " +
            "(LOWER(dt.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(dt.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY dt.usageCount DESC, dt.lastUsed DESC NULLS LAST")
    List<DietTemplateEntity> searchByNameOrDescription(
            @Param("query") String query,
            @Param("createdBy") String createdBy,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE DietTemplateEntity dt SET dt.usageCount = dt.usageCount + 1, " +
            "dt.lastUsed = CURRENT_TIMESTAMP WHERE dt.externalId = :externalId")
    void incrementUsageCount(@Param("externalId") String externalId);

    @Query("SELECT COUNT(dt) FROM DietTemplateEntity dt WHERE dt.createdBy = :createdBy")
    long countByCreatedBy(@Param("createdBy") String createdBy);

    List<DietTemplateEntity> findByCreatedByAndCategoryOrderByUsageCountDesc(
            String createdBy,
            DietTemplateCategory category
    );
}
