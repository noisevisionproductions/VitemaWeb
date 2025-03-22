package com.noisevisionsoftware.nutrilog.repository;

import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.model.recipe.RecipeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    Optional<Recipe> findById(String id);

    Optional<Recipe> findByName(String name);

    List<Recipe> findAllByIds(Collection<String> ids);

    List<Recipe> findAll();

    Page<Recipe> findAll(Pageable pageable);

    Recipe update(String id, Recipe recipe);

    List<Recipe> search(String query);

    void delete(String id);

    Recipe save(Recipe recipe);

    List<Recipe> findByParentRecipeId(String parentId);

    // Jeśli ta metoda jest specyficzna tylko dla Firestore, można ją przenieść do FirestoreRecipeRepository
    default void saveReference(RecipeReference reference) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}