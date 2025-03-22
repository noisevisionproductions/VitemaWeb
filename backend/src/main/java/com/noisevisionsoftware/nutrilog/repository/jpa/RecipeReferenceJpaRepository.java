package com.noisevisionsoftware.nutrilog.repository.jpa;

import com.noisevisionsoftware.nutrilog.model.recipe.jpa.RecipeReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeReferenceJpaRepository extends JpaRepository<RecipeReferenceEntity, String> {

    List<RecipeReferenceEntity> findByDietId(String dietId);

    List<RecipeReferenceEntity> findByRecipeId(String recipeId);

    List<RecipeReferenceEntity> findByUserId(String userId);

}
