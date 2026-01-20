package com.noisevisionsoftware.vitema.repository.jpa.recipe;

import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeReferenceJpaRepository extends JpaRepository<RecipeReferenceEntity, String> {

    List<RecipeReferenceEntity> findByDietId(String dietId);

    List<RecipeReferenceEntity> findByRecipeId(String recipeId);

    List<RecipeReferenceEntity> findByUserId(String userId);

}
