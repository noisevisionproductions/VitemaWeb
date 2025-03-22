package com.noisevisionsoftware.nutrilog.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.mapper.recipe.FirestoreRecipeMapper;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.model.recipe.RecipeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FirestoreRecipeRepository {
    private final Firestore firestore;
    private final FirestoreRecipeMapper firestoreRecipeMapper;

    private static final String COLLECTION_NAME = "recipes_old";
    private static final String REFERENCES_COLLECTION = "recipe_references_old";

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
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Recipe> recipes = new ArrayList<>();
            List<List<String>> batches = batchIds(new ArrayList<>(ids));

            for (List<String> batch : batches) {
                CollectionReference recipesRef = firestore.collection(COLLECTION_NAME);
                Query query = recipesRef.whereIn(FieldPath.documentId(), batch);
                QuerySnapshot querySnapshot = query.get().get();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Recipe recipe = firestoreRecipeMapper.toRecipe(document);
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                }
            }

            return recipes;
        } catch (Exception e) {
            log.error("Failed to fetch recipes by ids", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    public List<Recipe> findAll() {
        try {
            log.info("Pobieranie wszystkich przepisów");
            CollectionReference recipesRef = firestore.collection(COLLECTION_NAME);
            QuerySnapshot querySnapshot = recipesRef.get().get();

            List<Recipe> recipes = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Recipe recipe = firestoreRecipeMapper.toRecipe(document);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }

            log.info("Pobrano {} przepisów", recipes.size());
            return recipes;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich przepisów", e);
            throw new RuntimeException("Failed to fetch all recipes", e);
        }
    }

    public Page<Recipe> findAll(Pageable pageable) {
        try {
            CollectionReference recipesRef = firestore.collection(COLLECTION_NAME);

            long total = recipesRef.get().get().size();

            Query query = applySorting(recipesRef, pageable.getSort());

            query = query.offset((int) pageable.getOffset()).limit(pageable.getPageSize());

            QuerySnapshot querySnapshot = query.get().get();

            List<Recipe> recipes = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Recipe recipe = firestoreRecipeMapper.toRecipe(document);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }

            return new PageImpl<>(recipes, pageable, total);
        } catch (Exception e) {
            log.error("Failed to fetch all recipes", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    public Recipe update(String id, Recipe recipe) {
        try {
            DocumentReference docRef;
            if (recipe.getId() != null && !recipe.getId().isEmpty()) {
                docRef = firestore.collection(COLLECTION_NAME).document(recipe.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
            }

            Map<String, Object> data = firestoreRecipeMapper.toFirestoreMap(recipe);
            docRef.update(data).get();
        } catch (Exception e) {
            log.error("Failed to update recipe: {}", id, e);
            throw new RuntimeException("Failed to update recipe", e);
        }
        return recipe;
    }

    public List<Recipe> search(String query) {
        try {
            CollectionReference recipesRef = firestore.collection(COLLECTION_NAME);
            QuerySnapshot querySnapshot = recipesRef.get().get();

            List<Recipe> allRecipes = querySnapshot.getDocuments().stream()
                    .map(firestoreRecipeMapper::toRecipe)
                    .filter(Objects::nonNull)
                    .toList();

            String queryLower = query.toLowerCase();
            return allRecipes.stream()
                    .filter(recipe ->
                            recipe.getName().toLowerCase().contains(queryLower) ||
                                    (recipe.getInstructions() != null && recipe.getInstructions().toLowerCase().contains(queryLower)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search recipes", e);
            throw new RuntimeException("Failed to search recipes", e);
        }
    }

    public void delete(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            docRef.delete().get();
        } catch (Exception e) {
            log.error("Failed to delete recipe: {}", id, e);
            throw new RuntimeException("Failed to delete recipe", e);
        }
    }

    public Recipe save(Recipe recipe) {
        try {
            DocumentReference docRef;
            if (recipe.getId() != null && !recipe.getId().isEmpty()) {
                docRef = firestore.collection(COLLECTION_NAME).document(recipe.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                recipe.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreRecipeMapper.toFirestoreMap(recipe);
            docRef.set(data).get();
            return recipe;
        } catch (Exception e) {
            log.error("Failed to save recipe", e);
            throw new RuntimeException("Failed to save recipe", e);
        }
    }

    public void saveReference(RecipeReference reference) {
        try {
            DocumentReference docRef;
            if (reference.getId() != null && !reference.getId().isEmpty()) {
                docRef = firestore.collection(REFERENCES_COLLECTION).document(reference.getId());
            } else {
                docRef = firestore.collection(REFERENCES_COLLECTION).document();
                reference.setId(docRef.getId());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", reference.getId());
            data.put("recipeId", reference.getRecipeId());
            data.put("dietId", reference.getDietId());
            data.put("userId", reference.getUserId());
            data.put("mealType", reference.getMealType().name());
            data.put("addedAt", reference.getAddedAt());

            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save recipe reference", e);
            throw new RuntimeException("Failed to save recipe reference", e);
        }
    }

    public List<Recipe> findByParentRecipeId(String parentId) {
        try {
            CollectionReference recipesRef = firestore.collection(COLLECTION_NAME);
            Query query = recipesRef.whereEqualTo("parentRecipeId", parentId);
            QuerySnapshot querySnapshot = query.get().get();

            List<Recipe> recipes = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Recipe recipe = firestoreRecipeMapper.toRecipe(document);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }

            return recipes;
        } catch (Exception e) {
            log.error("Failed to find recipes by parent id: {}", parentId, e);
            throw new RuntimeException("Failed to find recipes by parent id", e);
        }
    }

    private Query applySorting(CollectionReference reference, Sort sort) {
        Query query = reference;
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                Query.Direction direction = order.getDirection().isAscending() ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;
                query = query.orderBy(order.getProperty(), direction);
            }
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }
        return query;
    }

    private <T> List<List<T>> batchIds(List<T> items) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 10) {
            batches.add(items.subList(i, Math.min(i + 10, items.size())));
        }
        return batches;
    }
}
