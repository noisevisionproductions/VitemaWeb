package com.noisevisionsoftware.vitema.repository.jpa.recipe;

import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeImageReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeImageReferenceJpaRepository extends JpaRepository<RecipeImageReferenceEntity, Long> {

    Optional<RecipeImageReferenceEntity> findByImageUrl(String imageUrl);

    @Modifying
    @Query("UPDATE RecipeImageReferenceEntity r SET r.referenceCount = r.referenceCount + 1 WHERE r.imageUrl = :imageUrl")
    void incrementReferenceCount(@Param("imageUrl") String imageUrl);

    @Modifying
    @Query("UPDATE RecipeImageReferenceEntity r SET r.referenceCount = r.referenceCount - 1 WHERE r.imageUrl = :imageUrl AND r.referenceCount > 0")
    void decrementReferenceCount(@Param("imageUrl") String imageUrl);

    @Query("SELECT r.referenceCount FROM RecipeImageReferenceEntity r WHERE r.imageUrl = :imageUrl")
    Optional<Integer> findReferenceCount(@Param("imageUrl") String imageUrl);

    List<RecipeImageReferenceEntity> findByReferenceCount(int referenceCount);

    void deleteByImageUrl(String imageUrl);
}