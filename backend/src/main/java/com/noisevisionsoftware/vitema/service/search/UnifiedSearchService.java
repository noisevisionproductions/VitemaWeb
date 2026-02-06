package com.noisevisionsoftware.vitema.service.search;

import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.dto.search.UnifiedSearchDto;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.service.product.ProductDatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedSearchService {

    private final RecipeService recipeService;
    private final ProductDatabaseService productDatabaseService;

    /**
     * Performs a combined search for recipes and products.
     * Updated to include trainer-specific custom products from PostgreSQL.
     */
    public List<UnifiedSearchDto> search(String query, String trainerId) {
        try {
            // 1. Search recipes (PostgreSQL)
            List<UnifiedSearchDto> recipes = recipeService.searchRecipes(query).stream()
                    .map(this::mapRecipeToDto)
                    .toList();

            // 2. Search products (PostgreSQL) - Now passing trainerId for ownership filtering
            List<UnifiedSearchDto> products = productDatabaseService.searchByName(query, trainerId).stream()
                    .map(this::mapProductToDto)
                    .toList();

            List<UnifiedSearchDto> combined = new ArrayList<>();
            combined.addAll(products);
            combined.addAll(recipes);

            log.info("Unified search for query '{}' found {} recipes and {} products",
                    query, recipes.size(), products.size());
            return combined;
        } catch (Exception e) {
            log.error("Error in Unified Search for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    private UnifiedSearchDto mapRecipeToDto(Recipe recipe) {
        return UnifiedSearchDto.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .type(UnifiedSearchDto.SearchResultType.RECIPE)
                .nutritionalValues(recipe.getNutritionalValues())
                .photos(recipe.getPhotos())
                .build();
    }

    private UnifiedSearchDto mapProductToDto(ProductResponse product) {
        return UnifiedSearchDto.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .type(UnifiedSearchDto.SearchResultType.PRODUCT)
                .unit(product.getUnit())
                .nutritionalValues(NutritionalValues.builder()
                        .calories(product.getKcal())
                        .protein(product.getProtein())
                        .fat(product.getFat())
                        .carbs(product.getCarbs())
                        .build())
                .build();
    }
}