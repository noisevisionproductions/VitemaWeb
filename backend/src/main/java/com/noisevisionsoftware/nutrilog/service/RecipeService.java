package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.google.cloud.storage.*;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final Storage storage;

    @Value("${firebase.storage.bucket-name}")
    private String storageBucket;

    private static final String RECIPES_CACHE = "recipesCache";
    private static final String RECIPES_BATCH_CACHE = "recipesBatchCache";
    private static final String RECIPES_PAGE_CACHE = "recipesPageCache";

    @Cacheable(value = RECIPES_CACHE, key = "#id")
    public Recipe getRecipeById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + id));
    }

    @Cacheable(value = RECIPES_BATCH_CACHE, key = "#ids")
    public List<Recipe> getRecipesByIds(Collection<String> ids) {
        return recipeRepository.findAllByIds(ids);
    }

    @Cacheable(value = RECIPES_PAGE_CACHE, key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    @Caching(evict = {
            @CacheEvict(value = RECIPES_CACHE, key = "#id"),
            @CacheEvict(value = RECIPES_BATCH_CACHE, allEntries = true),
            @CacheEvict(value = RECIPES_PAGE_CACHE, allEntries = true)
    })
    public Recipe updateRecipe(String id, Recipe recipe) {
        getRecipeById(id);

        recipe.setId(id);

        return recipeRepository.update(id, recipe);
    }

    public Recipe createRecipe(Recipe recipe) {
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(Timestamp.now());
        }

        return recipeRepository.save(recipe);
    }

    public Recipe findOrCreateRecipe(Recipe recipe) {
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            return createRecipe(recipe);
        }

        Optional<Recipe> existingRecipe = recipeRepository.findByName(recipe.getName().trim());

        if (existingRecipe.isPresent()) {
            Recipe existing = existingRecipe.get();
            log.debug("Znaleziono istniejący przepis: {} (id: {})", existing.getName(), existing.getId());

            boolean shouldUpdate = false;

            // Jeśli nowy przepis ma instrukcje, a stary nie ma, lub nowe są dłuższe
            if ((existing.getInstructions() == null || existing.getInstructions().isEmpty()) &&
                    recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                existing.setInstructions(recipe.getInstructions());
                shouldUpdate = true;
            } else if (existing.getInstructions() != null && recipe.getInstructions() != null &&
                    recipe.getInstructions().length() > existing.getInstructions().length()) {
                // Jeśli nowe instrukcje są bardziej szczegółowe (dłuższe)
                existing.setInstructions(recipe.getInstructions());
                shouldUpdate = true;
            }

            // Jeśli nowy przepis ma wartości odżywcze, a stary nie ma
            if (existing.getNutritionalValues() == null && recipe.getNutritionalValues() != null) {
                existing.setNutritionalValues(recipe.getNutritionalValues());
                shouldUpdate = true;
            }

            if (recipe.getPhotos() != null && !recipe.getPhotos().isEmpty()) {
                List<String> combinedPhotos = new ArrayList<>();

                if (existing.getPhotos() != null) {
                    combinedPhotos.addAll(existing.getPhotos());
                }

                for (String photo : recipe.getPhotos()) {
                    if (!combinedPhotos.contains(photo)) {
                        combinedPhotos.add(photo);
                        shouldUpdate = true;
                    }
                }

                existing.setPhotos(combinedPhotos);
            }

            if (shouldUpdate) {
                log.debug("Aktualizacja istniejącego przepisu: {}", existing.getId());
                return recipeRepository.update(existing.getId(), existing);
            }

            return existing;
        } else {
            log.debug("Tworzenie nowego przepisu: {}", recipe.getName());
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

        try {
            String originalFilename = Optional.ofNullable(image.getOriginalFilename())
                    .orElse("image.jpg");
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            String objectName = String.format("recipes/%s/images/%s", id, filename);

            BlobId blobId = BlobId.of(storageBucket, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(image.getContentType())
                    .setMetadata(Map.of(
                            "cacheControl", "public, max-age=31536000",
                            "contentDisposition", "inline"
                    ))
                    .setAcl(new ArrayList<>(Collections.singletonList(
                            Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
                    )))
                    .build();

            storage.create(blobInfo, image.getBytes());

            String imageUrl = String.format("https://storage.googleapis.com/%s/%s",
                    storageBucket, objectName);

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
        return recipeRepository.search(query);
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

            String filename = UUID.randomUUID() + extension;

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
        log.info("Rozpoczęcie czyszczenia tymczasowych zdjęć...");
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
                            log.debug("Usunięto plik: {}", name);
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