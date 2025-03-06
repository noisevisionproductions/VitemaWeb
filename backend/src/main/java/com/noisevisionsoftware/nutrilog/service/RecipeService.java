package com.noisevisionsoftware.nutrilog.service;

import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;

    private static final String RECIPES_CACHE = "recipesCache";
    private static final String RECIPES_BATCH_CACHE = "recipesBatchCache";

    @Cacheable(value = RECIPES_CACHE, key = "#id")
    public Recipe getRecipeById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + id));
    }

    @Cacheable(value = RECIPES_BATCH_CACHE, key = "#ids")
    public List<Recipe> getRecipesByIds(Collection<String> ids) {
        return recipeRepository.findAllByIds(ids);
    }

    public Recipe updateRecipe(String id, Recipe recipe) {
        getRecipeById(id);

        recipe.setId(id);
        Recipe updatedRecipe = recipeRepository.update(id, recipe);
        refreshRecipesCache();
        return updatedRecipe;
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true)
    })
    public void refreshRecipesCache() {
        log.debug("Odświeżenie cache przepisów");
    }
}