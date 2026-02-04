package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.google.cloud.storage.*;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeJpaConverter;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeJpaRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeImageRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// Dodaj import AccessDeniedException (może być ze Spring Security lub Twój własny)
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final Storage storage;
    private final RecipeJpaRepository recipeJpaRepository;
    private final RecipeJpaConverter recipeJpaConverter;
    private final UserService userService;

    @Value("${firebase.storage.bucket-name}")
    private String storageBucket;

    private static final String RECIPES_CACHE = "recipesCache";
    private static final String RECIPES_BATCH_CACHE = "recipesBatchCache";
    private static final String RECIPES_PAGE_CACHE = "recipesPageCache";

    private void verifyOwnership(Recipe recipe) {
        String currentUserId = userService.getCurrentUserId();
        boolean isAdminOrOwner = userService.isCurrentUserAdminOrOwner();


        if (!isAdminOrOwner && (currentUserId == null || !currentUserId.equals(recipe.getAuthorId()))) {
            throw new AccessDeniedException("Nie masz uprawnień do edycji lub usunięcia tego przepisu.");
        }
    }

    @Cacheable(value = RECIPES_CACHE, key = "#id")
    public Recipe getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + id));

        String currentUserId = userService.getCurrentUserId();
        boolean isAdminOrOwner = userService.isCurrentUserAdminOrOwner();

        if (!recipe.isPublic() &&
                (currentUserId == null || !currentUserId.equals(recipe.getAuthorId())) &&
                !isAdminOrOwner) {
            throw new NotFoundException("Recipe not found or access denied");
        }

        return recipe;
    }

    @Cacheable(value = RECIPES_BATCH_CACHE, key = "#ids")
    public List<Recipe> getRecipesByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return recipeRepository.findAllByIds(ids);
    }

    @Cacheable(value = RECIPES_PAGE_CACHE, key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort, @userService.getCurrentUserId()}")
    public Page<Recipe> getAllRecipes(Pageable pageable) {
        String currentUserId = userService.getCurrentUserId();
        Page<RecipeEntity> entitiesPage = recipeJpaRepository.findAllVisible(currentUserId, pageable);

        return entitiesPage.map(recipeJpaConverter::toModel);
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, key = "#id"),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public Recipe updateRecipe(String id, Recipe recipe) {
        Recipe existingRecipe = getRecipeById(id);

        verifyOwnership(existingRecipe);

        recipe.setId(id);

        if (recipe.getAuthorId() == null) {
            recipe.setAuthorId(existingRecipe.getAuthorId());
        }

        return recipeRepository.update(id, recipe);
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, key = "#id"),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public void deleteRecipe(String id) {
        Recipe recipe = getRecipeById(id);

        verifyOwnership(recipe);

        if (recipe.getPhotos() != null && !recipe.getPhotos().isEmpty()) {
            for (String photoUrl : recipe.getPhotos()) {
                try {
                    recipeImageRepository.decrementReferenceCount(photoUrl);
                } catch (Exception e) {
                    log.error("Błąd podczas aktualizacji referencji zdjęcia: {}", photoUrl, e);
                }
            }
        }

        recipeRepository.delete(id);
        cleanupOrphanedImages();
    }

    @Async
    protected void cleanupOrphanedImages() {
        List<RecipeImageReference> orphanedImages = recipeImageRepository.findAllWithZeroReferences();
        for (RecipeImageReference image : orphanedImages) {
            try {
                if (image.getStoragePath() != null) {
                    BlobId blobId = BlobId.of(storageBucket, image.getStoragePath());
                    storage.delete(blobId);
                    log.info("Usunięto osierocone zdjęcie: {}", image.getImageUrl());
                }
                recipeImageRepository.deleteByImageUrl(image.getImageUrl());
            } catch (Exception e) {
                log.error("Błąd podczas usuwania osieroconego zdjęcia: {}", image.getImageUrl(), e);
            }
        }
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public Recipe createRecipe(Recipe recipe) {
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(Timestamp.now());
        }

        String currentUserId = userService.getCurrentUserId();
        if (currentUserId != null) {
            recipe.setAuthorId(currentUserId);
        }

        if (!userService.isCurrentUserAdminOrOwner()) {
            recipe.setPublic(false);
        }

        return recipeRepository.save(recipe);
    }

    public Recipe findOrCreateRecipe(Recipe recipe) {
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            return createRecipe(recipe);
        }

        // TODO: search not only by name, but for example ingredient
        Optional<Recipe> existingRecipe = recipeRepository.findByName(recipe.getName().trim());

        if (existingRecipe.isPresent()) {
            Recipe existing = existingRecipe.get();

            Recipe updatedRecipe = Recipe.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .instructions(existing.getInstructions())
                    .createdAt(existing.getCreatedAt())
                    .photos(existing.getPhotos() != null ? new ArrayList<>(existing.getPhotos()) : new ArrayList<>())
                    .nutritionalValues(existing.getNutritionalValues())
                    .parentRecipeId(existing.getParentRecipeId())
                    .ingredients(existing.getIngredients())
                    .authorId(existing.getAuthorId())
                    .isPublic(existing.isPublic())
                    .build();

            boolean shouldUpdate = false;

            if ((updatedRecipe.getInstructions() == null || updatedRecipe.getInstructions().isEmpty()) &&
                    recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                updatedRecipe.setInstructions(recipe.getInstructions());
                shouldUpdate = true;
            } else if (updatedRecipe.getInstructions() != null && recipe.getInstructions() != null &&
                    recipe.getInstructions().length() > updatedRecipe.getInstructions().length()) {
                updatedRecipe.setInstructions(recipe.getInstructions());
                shouldUpdate = true;
            }

            if (updatedRecipe.getNutritionalValues() == null && recipe.getNutritionalValues() != null) {
                updatedRecipe.setNutritionalValues(recipe.getNutritionalValues());
                shouldUpdate = true;
            }

            if (recipe.getPhotos() != null && !recipe.getPhotos().isEmpty()) {
                List<String> combinedPhotos = new ArrayList<>();
                if (updatedRecipe.getPhotos() != null) combinedPhotos.addAll(updatedRecipe.getPhotos());
                for (String photo : recipe.getPhotos()) {
                    if (!combinedPhotos.contains(photo)) {
                        combinedPhotos.add(photo);
                        shouldUpdate = true;
                    }
                }
                updatedRecipe.setPhotos(combinedPhotos);
            }

            if (shouldUpdate) {
                try {
                    verifyOwnership(existing);
                    return recipeRepository.update(updatedRecipe.getId(), updatedRecipe);
                } catch (AccessDeniedException e) {
                    return existing;
                }
            }

            return existing;
        } else {
            log.info("Tworzenie nowego przepisu: {}", recipe.getName());
            return createRecipe(recipe);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, key = "#id"),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public String uploadImage(String id, MultipartFile image) throws BadRequestException {
        Recipe recipe = getRecipeById(id);

        verifyOwnership(recipe);

        try {
            String originalFilename = Optional.ofNullable(image.getOriginalFilename()).orElse("image.jpg");
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;
            String objectName = String.format("recipes/%s/images/%s", id, filename);

            BlobId blobId = BlobId.of(storageBucket, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(image.getContentType())
                    .setMetadata(Map.of("cacheControl", "public, max-age=31536000", "contentDisposition", "inline"))
                    .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                    .build();
            storage.create(blobInfo, image.getBytes());
            String imageUrl = String.format("https://storage.googleapis.com/%s/%s", storageBucket, objectName);

            Optional<RecipeImageReference> existingRef = recipeImageRepository.findByImageUrl(imageUrl);
            if (existingRef.isPresent()) {
                recipeImageRepository.incrementReferenceCount(imageUrl);
            } else {
                RecipeImageReference newRef = RecipeImageReference.builder()
                        .imageUrl(imageUrl).storagePath(objectName).referenceCount(1).build();
                recipeImageRepository.save(newRef);
            }

            List<String> photos = new ArrayList<>(recipe.getPhotos() != null ? recipe.getPhotos() : new ArrayList<>());
            photos.add(imageUrl);
            recipe.setPhotos(photos);

            recipeRepository.update(id, recipe);

            return imageUrl;
        } catch (IOException e) {
            log.error("Błąd podczas przesyłania obrazu", e);
            throw new BadRequestException("Nie udało się przesłać obrazu: " + e.getMessage());
        }
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, key = "#id"),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public void deleteImage(String id, String imageUrl) throws BadRequestException {
        Recipe recipe = getRecipeById(id);

        verifyOwnership(recipe);

        if (recipe.getPhotos() == null || !recipe.getPhotos().contains(imageUrl)) {
            throw new BadRequestException("Podany obraz nie istnieje dla tego przepisu");
        }

        if (imageUrl.contains("storage.googleapis.com")) {
            try {
                String objectPath = imageUrl.split(storageBucket + "/")[1];
                BlobId blobId = BlobId.of(storageBucket, objectPath);
                storage.delete(blobId);
            } catch (Exception e) {
                log.error("Błąd podczas usuwania obrazu z Cloud Storage", e);
            }
        }
        List<String> updatedPhotos = new ArrayList<>(recipe.getPhotos());
        updatedPhotos.remove(imageUrl);
        recipe.setPhotos(updatedPhotos);
        recipeRepository.update(id, recipe);
    }

    public List<Recipe> searchRecipes(String query) {
        String currentUserId = userService.getCurrentUserId();
        List<RecipeEntity> entities = recipeJpaRepository.searchVisible(query, currentUserId);
        return entities.stream()
                .map(recipeJpaConverter::toModel)
                .collect(Collectors.toList());
    }

    public String uploadBase64Image(String base64Image) throws BadRequestException {
        try {
            if (base64Image == null || !base64Image.startsWith("data:image/")) {
                throw new BadRequestException("Nieprawidłowe dane obrazu");
            }

            String[] parts = base64Image.split(",");
            String imageData = parts.length > 1 ? parts[1] : "";

            String contentType = "image/jpeg";
            if (parts[0].contains("png")) {
                contentType = "image/png";
            } else if (parts[0].contains("gif")) {
                contentType = "image/gif";
            }

            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            String extension = ".jpg";
            if (contentType.equals("image/png")) {
                extension = ".png";
            } else if (contentType.equals("image/gif")) {
                extension = ".gif";
            }

            UUID uuid = UUID.randomUUID();
            String filename = uuid + extension;

            String objectName = "temp-recipes/images/" + filename;

            BlobId blobId = BlobId.of(storageBucket, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setMetadata(Map.of(
                            "cacheControl", "public, max-age=31536000",
                            "contentDisposition", "inline"
                    ))
                    .setAcl(new ArrayList<>(Collections.singletonList(
                            Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
                    )))
                    .build();

            storage.create(blobInfo, imageBytes);

            return String.format("https://storage.googleapis.com/%s/%s",
                    storageBucket, objectName);
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania obrazu base64", e);
            throw new BadRequestException("Nie udało się przesłać obrazu: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTempImages() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            long cutoffTime = calendar.getTimeInMillis();

            Iterable<Blob> blobs = storage.list(
                    storageBucket,
                    Storage.BlobListOption.prefix("temp-recipes/images/")
            ).iterateAll();

            for (Blob blob : blobs) {
                String name = blob.getName();

                try {
                    String fileName = name.substring(name.lastIndexOf('/') + 1);
                    if (fileName.contains("_")) {
                        String timestampStr = fileName.split("_")[0];
                        long timestamp = Long.parseLong(timestampStr);

                        if (timestamp < cutoffTime) {
                            blob.delete();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Problem z przetwarzaniem pliku {}: {}", name, e.getMessage());
                }
            }

            log.info("Zakończono czyszczenie tymczasowych zdjęć");
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia tymczasowych zdjęć", e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public void refreshRecipesCache() {
        log.debug("Odświeżenie cache przepisów");
    }
}