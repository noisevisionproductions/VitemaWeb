package com.noisevisionsoftware.nutrilog.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.nutrilog.mapper.recipe.FirestoreRecipeMapper;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.model.recipe.RecipeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeRepository {
    private final Firestore firestore;
    private final FirestoreRecipeMapper firestoreRecipeMapper;
    private static final String COLLECTION_NAME = "recipes";
    private static final String REFERENCES_COLLECTION = "recipe_references";

    public Optional<Recipe> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreRecipeMapper.toRecipe(document));
        } catch (Exception e) {
            log.error("Failed to fetch recipe by id: {}", id, e);
            throw new RuntimeException("Failed to fetch recipe", e);
        }
    }

    public List<Recipe> findAllByIds(Collection<String> ids) {
        try {
            List<Recipe> recipes = new ArrayList<>();
            for (String id : ids) {
                findById(id).ifPresent(recipes::add);
            }
            return recipes;
        } catch (Exception e) {
            log.error("Failed to fetch recipes by ids", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    public Recipe update(String id, Recipe recipe) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            Map<String, Object> data = firestoreRecipeMapper.toFirestoreMap(recipe);
            docRef.update(data).get();
        } catch (Exception e) {
            log.error("Failed to update recipe: {}", id, e);
            throw new RuntimeException("Failed to update recipe", e);
        }
        return recipe;
    }

    public void save(Recipe recipe) {
        try {
            DocumentReference docRef;
            if (recipe.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(recipe.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                recipe.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreRecipeMapper.toFirestoreMap(recipe);
            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save recipe", e);
            throw new RuntimeException("Failed to save recipe", e);
        }
    }

    public void saveReference(RecipeReference reference) {
        try {
            DocumentReference docRef = firestore.collection(REFERENCES_COLLECTION).document();
            Map<String, Object> data = new HashMap<>();
            data.put("recipeId", reference.getRecipeId());
            data.put("userId", reference.getUserId());
            data.put("mealType", reference.getMealType().name());
            data.put("addedAt", reference.getAddedAt());

            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save recipe reference", e);
            throw new RuntimeException("Failed to save recipe reference", e);
        }
    }
}
