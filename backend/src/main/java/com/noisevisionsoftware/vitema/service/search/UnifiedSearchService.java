package com.noisevisionsoftware.vitema.service.search;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.dto.search.UnifiedSearchDto;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.service.product.ProductDatabaseService;
import com.noisevisionsoftware.vitema.service.product.ProductService; // Twój serwis
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedSearchService {

    private final RecipeService recipeService;
    private final ProductDatabaseService productDatabaseService;

    public List<UnifiedSearchDto> search(String query, String trainerId) {
        try {
            // 1. Szukanie przepisów w PostgreSQL
            List<UnifiedSearchDto> recipes = recipeService.searchRecipes(query).stream()
                    .map(this::mapRecipeToDto)
                    .toList();

            // 2. Szukanie produktów w PostgreSQL
            List<UnifiedSearchDto> products = productDatabaseService.searchByName(query).stream()
                    .map(this::mapProductToDto)
                    .toList();

            List<UnifiedSearchDto> combined = new ArrayList<>();
            combined.addAll(products);
            combined.addAll(recipes);
            return combined;
        } catch (Exception e) {
            log.error("Błąd Unified Search dla zapytania: {}", query, e);
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
                .build();
    }
}