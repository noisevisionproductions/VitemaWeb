package com.noisevisionsoftware.vitema.repository.recipe;

import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeImageRepository {

    RecipeImageReference save(RecipeImageReference imageReference);

    Optional<RecipeImageReference> findByImageUrl(String imageUrl);

    void incrementReferenceCount(String imageUrl);

    int decrementReferenceCount(String imageUrl);

    void deleteByImageUrl(String imageUrl);

    List<RecipeImageReference> findAllWithZeroReferences();
}