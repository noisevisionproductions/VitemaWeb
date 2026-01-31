package com.noisevisionsoftware.vitema.repository.jpa.recipe;

import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, Long> {

    Optional<RecipeEntity> findByExternalId(String externalId);

    List<RecipeEntity> findAllByExternalIdIn(List<String> externalIds);

    List<RecipeEntity> findByParentRecipeId(String parentRecipeId);

    @Query("SELECT r FROM RecipeEntity r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.instructions) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<RecipeEntity> search(@Param("query") String query);

    @Query("SELECT r FROM RecipeEntity r WHERE r.isPublic = true OR r.authorId = :userId")
    Page<RecipeEntity> findAllVisible(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM RecipeEntity r WHERE (r.isPublic = true OR r.authorId = :userId) AND LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<RecipeEntity> searchVisible(@Param("query") String query, @Param("userId") String userId);

    @Query("SELECT r FROM RecipeEntity r WHERE r.externalId = :id AND (r.isPublic = true OR r.authorId = :userId)")
    Optional<RecipeEntity> findByIdAndVisible(@Param("id") String id, @Param("userId") String userId);

    @NonNull
    Page<RecipeEntity> findAll(@NonNull Pageable pageable);

    Optional<RecipeEntity> findFirstByNameIgnoreCaseOrderByCreatedAtDesc(String name);
}