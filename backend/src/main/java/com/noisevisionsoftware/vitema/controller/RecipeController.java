package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipeImageResponse;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipesPageResponse;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeMapper;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    @GetMapping
    public ResponseEntity<RecipesPageResponse> getAllRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Recipe> recipesPage = recipeService.getAllRecipes(pageable);
        List<RecipeResponse> content = recipesPage.getContent().stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());

        RecipesPageResponse response = RecipesPageResponse.builder()
                .content(content)
                .page(recipesPage.getNumber())
                .size(recipesPage.getSize())
                .totalElements(recipesPage.getTotalElements())
                .totalPages(recipesPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable String id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipeMapper.toResponse(recipe));
        } catch (NotFoundException e) {
            log.warn("Nie znaleziono przepisu o ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/batch")
    public ResponseEntity<List<RecipeResponse>> getRecipesByIds(@RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<String> requestedIds = Arrays.asList(ids.split(","));

        List<Recipe> recipes = recipeService.getRecipesByIds(requestedIds);

        return ResponseEntity.ok(
                recipes.stream()
                        .map(recipeMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody RecipeUpdateRequest request) {
        Recipe recipe = recipeMapper.toModel(request);
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
        return ResponseEntity.ok(recipeMapper.toResponse(updatedRecipe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            log.warn("Próba usunięcia nieistniejącego przepisu: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Błąd podczas usuwania przepisu: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeImageResponse> uploadImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile image) throws BadRequestException {
        String imageUrl = recipeService.uploadImage(id, image);
        return ResponseEntity.ok(RecipeImageResponse.builder()
                .imageUrl(imageUrl)
                .build());
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(
            @PathVariable String id,
            @RequestBody RecipeImageResponse request) throws BadRequestException {
        recipeService.deleteImage(id, request.getImageUrl());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponse>> searchRecipes(@RequestParam String query) {
        List<Recipe> searchResults = recipeService.searchRecipes(query);
        return ResponseEntity.ok(
                searchResults.stream()
                        .map(recipeMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping(value = "/base64-image")
    public ResponseEntity<RecipeImageResponse> uploadBase64Image(
            @RequestBody Map<String, String> request) throws BadRequestException {
        String base64Image = request.get("imageData");
        if (base64Image == null) {
            throw new BadRequestException("Brak danych obrazu");
        }

        String imageUrl = recipeService.uploadBase64Image(base64Image);

        return ResponseEntity.ok(RecipeImageResponse.builder()
                .imageUrl(imageUrl)
                .build());
    }
}