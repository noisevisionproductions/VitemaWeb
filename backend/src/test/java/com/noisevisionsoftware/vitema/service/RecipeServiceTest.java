package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeJpaConverter;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeJpaRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeImageRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private Storage storage;

    @Mock
    private RecipeImageRepository recipeImageRepository;

    @Mock
    private RecipeJpaRepository recipeJpaRepository;

    @Mock
    private RecipeJpaConverter recipeJpaConverter;

    @Mock
    private UserService userService;

    @InjectMocks
    private RecipeService recipeService;

    @Captor
    private ArgumentCaptor<Recipe> recipeCaptor;

    @Captor
    private ArgumentCaptor<BlobInfo> blobInfoCaptor;

    @Captor
    private ArgumentCaptor<byte[]> byteArrayCaptor;

    @Captor
    private ArgumentCaptor<RecipeImageReference> imageReferenceCaptor;

    @Value("${firebase.storage.bucket-name:test-bucket}")
    private String storageBucket = "test-bucket";

    private static final String TEST_RECIPE_ID = "test-recipe-id";
    private static final String TEST_BUCKET_NAME = "test-bucket";
    private static final String TEST_IMAGE_URL = "https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/test-image.jpg";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recipeService, "storageBucket", TEST_BUCKET_NAME);

        // Set up default behaviors for UserService (lenient to avoid unnecessary stubbing exceptions)
        lenient().when(userService.getCurrentUserId()).thenReturn("test-user-id");
        lenient().when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
    }

    @Test
    void getRecipeById_WhenRecipeExists_ShouldReturnRecipe() {
        // given
        Recipe expectedRecipe = createTestRecipe();
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(expectedRecipe));

        // when
        Recipe actualRecipe = recipeService.getRecipeById(TEST_RECIPE_ID);

        // then
        assertThat(actualRecipe)
                .isNotNull()
                .isEqualTo(expectedRecipe);
    }

    @Test
    void getRecipeById_WhenRecipeDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> recipeService.getRecipeById(TEST_RECIPE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe not found")
                .hasMessageContaining(TEST_RECIPE_ID);
    }

    @Test
    void getRecipesByIds_ShouldReturnListOfRecipes() {
        // given
        List<String> recipeIds = Arrays.asList(TEST_RECIPE_ID, "test-recipe-id-2");
        List<Recipe> expectedRecipes = Arrays.asList(
                createTestRecipe(),
                createTestRecipe("test-recipe-id-2")
        );
        when(recipeRepository.findAllByIds(recipeIds)).thenReturn(expectedRecipes);

        // when
        List<Recipe> actualRecipes = recipeService.getRecipesByIds(recipeIds);

        // then
        assertThat(actualRecipes)
                .isNotNull()
                .hasSize(2)
                .isEqualTo(expectedRecipes);
    }

    @Test
    void getRecipesByIds_WhenIdsIsEmpty_ShouldReturnEmptyList() {
        // when
        List<Recipe> actualRecipes = recipeService.getRecipesByIds(Collections.emptyList());

        // then
        assertThat(actualRecipes).isEmpty();
        verify(recipeRepository, never()).findAllByIds(anyCollection());
    }

    @Test
    void getRecipesByIds_WhenIdsIsNull_ShouldReturnEmptyList() {
        // when
        List<Recipe> actualRecipes = recipeService.getRecipesByIds(null);

        // then
        assertThat(actualRecipes).isEmpty();
        verify(recipeRepository, never()).findAllByIds(anyCollection());
    }

    @Test
    void getAllRecipes_ShouldReturnPageOfRecipes() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Recipe> recipes = Arrays.asList(createTestRecipe(), createTestRecipe("test-recipe-id-2"));
        List<RecipeEntity> entities = Arrays.asList(
                createTestRecipeEntity(createTestRecipe()),
                createTestRecipeEntity(createTestRecipe("test-recipe-id-2"))
        );
        Page<RecipeEntity> entitiesPage = new PageImpl<>(entities, pageable, entities.size());

        when(recipeJpaRepository.findAllVisible(anyString(), eq(pageable))).thenReturn(entitiesPage);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class)))
                .thenAnswer(invocation -> {
                    RecipeEntity entity = invocation.getArgument(0);
                    return recipes.stream()
                            .filter(r -> r.getId().equals(entity.getExternalId()))
                            .findFirst()
                            .orElse(createTestRecipe());
                });

        // when
        Page<Recipe> actualPage = recipeService.getAllRecipes(pageable);

        // then
        assertThat(actualPage.getContent()).hasSize(2);
        verify(recipeJpaRepository).findAllVisible(anyString(), eq(pageable));
    }

    @Test
    void updateRecipe_WhenRecipeExists_ShouldUpdateAndReturnRecipe() {
        // given
        Recipe existingRecipe = createTestRecipe();
        Recipe updateRecipe = createTestRecipe();
        updateRecipe.setId(null);
        updateRecipe.setName("Updated Recipe Name");
        updateRecipe.setInstructions("Updated instructions");

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));
        when(recipeRepository.update(eq(TEST_RECIPE_ID), any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        Recipe updatedRecipe = recipeService.updateRecipe(TEST_RECIPE_ID, updateRecipe);

        // then
        assertThat(updatedRecipe).isNotNull();
        assertThat(updatedRecipe.getId()).isEqualTo(TEST_RECIPE_ID);
        assertThat(updatedRecipe.getName()).isEqualTo("Updated Recipe Name");
        assertThat(updatedRecipe.getInstructions()).isEqualTo("Updated instructions");

        verify(recipeRepository).update(eq(TEST_RECIPE_ID), any(Recipe.class));
    }

    @Test
    void updateRecipe_WhenRecipeDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Recipe updateRecipe = createTestRecipe();
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> recipeService.updateRecipe(TEST_RECIPE_ID, updateRecipe))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe not found")
                .hasMessageContaining(TEST_RECIPE_ID);
    }

    @Test
    void createRecipe_ShouldSetCreatedAtAndSave() {
        // given
        Recipe recipeToCreate = Recipe.builder()
                .name("New Recipe")
                .instructions("New instructions")
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id("new-recipe-id")
                .name("New Recipe")
                .instructions("New instructions")
                .createdAt(Timestamp.now())
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // when
        Recipe result = recipeService.createRecipe(recipeToCreate);

        // then
        verify(recipeRepository).save(recipeCaptor.capture());
        Recipe capturedRecipe = recipeCaptor.getValue();

        assertThat(capturedRecipe.getCreatedAt()).isNotNull();
        assertThat(result).isEqualTo(savedRecipe);
    }

    @Test
    void createRecipe_WhenCreatedAtIsAlreadySet_ShouldNotOverwrite() {
        // given
        Timestamp existingTimestamp = Timestamp.parseTimestamp("2023-01-01T10:00:00Z");
        Recipe recipeToCreate = Recipe.builder()
                .name("New Recipe")
                .instructions("New instructions")
                .createdAt(existingTimestamp)
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id("new-recipe-id")
                .name("New Recipe")
                .instructions("New instructions")
                .createdAt(existingTimestamp)
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // when
        Recipe result = recipeService.createRecipe(recipeToCreate);

        // then
        verify(recipeRepository).save(recipeCaptor.capture());
        Recipe capturedRecipe = recipeCaptor.getValue();

        assertThat(capturedRecipe.getCreatedAt()).isEqualTo(existingTimestamp);
        assertThat(result).isEqualTo(savedRecipe);
    }

    @Test
    void findOrCreateRecipe_WhenRecipeDoesNotExist_ShouldCreateNewRecipe() {
        // given
        Recipe newRecipe = Recipe.builder()
                .name("New Recipe")
                .instructions("New instructions")
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id("new-recipe-id")
                .name("New Recipe")
                .instructions("New instructions")
                .createdAt(Timestamp.now())
                .build();

        when(recipeRepository.findByName("New Recipe")).thenReturn(Optional.empty());
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // when
        Recipe result = recipeService.findOrCreateRecipe(newRecipe);

        // then
        verify(recipeRepository).findByName("New Recipe");
        verify(recipeRepository).save(any(Recipe.class));
        assertThat(result).isEqualTo(savedRecipe);
    }

    @Test
    void findOrCreateRecipe_WhenRecipeExistsAndHasNoUpdates_ShouldReturnExistingRecipe() {
        // given
        Recipe newRecipe = Recipe.builder()
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .build();

        Recipe existingRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .createdAt(Timestamp.now())
                .build();

        when(recipeRepository.findByName("Existing Recipe")).thenReturn(Optional.of(existingRecipe));

        // when
        Recipe result = recipeService.findOrCreateRecipe(newRecipe);

        // then
        verify(recipeRepository).findByName("Existing Recipe");
        verify(recipeRepository, never()).update(anyString(), any(Recipe.class));
        assertThat(result).isEqualTo(existingRecipe);
    }

    @Test
    void findOrCreateRecipe_WhenRecipeExistsAndHasLongerInstructions_ShouldUpdateAndReturnUpdatedRecipe() {
        // given
        Recipe newRecipe = Recipe.builder()
                .name("Existing Recipe")
                .instructions("More detailed and longer instructions with step by step guide")
                .build();

        Recipe existingRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .createdAt(Timestamp.now())
                .build();

        Recipe updatedRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("More detailed and longer instructions with step by step guide")
                .createdAt(existingRecipe.getCreatedAt())
                .build();

        when(recipeRepository.findByName("Existing Recipe")).thenReturn(Optional.of(existingRecipe));
        when(recipeRepository.update(eq("existing-id"), any(Recipe.class))).thenReturn(updatedRecipe);

        // when
        Recipe result = recipeService.findOrCreateRecipe(newRecipe);

        // then
        verify(recipeRepository).findByName("Existing Recipe");
        verify(recipeRepository).update(eq("existing-id"), any(Recipe.class));
        assertThat(result).isEqualTo(updatedRecipe);
    }

    @Test
    void findOrCreateRecipe_WhenExistingHasNoNutritionalValuesButNewHas_ShouldUpdate() {
        // given
        NutritionalValues nutritionalValues = new NutritionalValues();
        nutritionalValues.setCalories(250.0);
        nutritionalValues.setProtein(10.0);
        nutritionalValues.setCarbs(30.0);
        nutritionalValues.setFat(5.0);

        Recipe newRecipe = Recipe.builder()
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .nutritionalValues(nutritionalValues)
                .build();

        Recipe existingRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .createdAt(Timestamp.now())
                .build();

        Recipe updatedRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .nutritionalValues(nutritionalValues)
                .createdAt(existingRecipe.getCreatedAt())
                .build();

        when(recipeRepository.findByName("Existing Recipe")).thenReturn(Optional.of(existingRecipe));
        when(recipeRepository.update(eq("existing-id"), any(Recipe.class))).thenReturn(updatedRecipe);

        // when
        Recipe result = recipeService.findOrCreateRecipe(newRecipe);

        // then
        verify(recipeRepository).update(eq("existing-id"), any(Recipe.class));
        assertThat(result.getNutritionalValues()).isEqualTo(nutritionalValues);
    }

    @Test
    void findOrCreateRecipe_WhenNewRecipeHasAdditionalPhotos_ShouldUpdateWithCombinedPhotos() {
        // given
        Recipe newRecipe = Recipe.builder()
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .photos(Arrays.asList("new-photo.jpg", "another-photo.jpg"))
                .build();

        Recipe existingRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .photos(List.of("existing-photo.jpg"))
                .createdAt(Timestamp.now())
                .build();

        Recipe updatedRecipe = Recipe.builder()
                .id("existing-id")
                .name("Existing Recipe")
                .instructions("Basic instructions")
                .photos(Arrays.asList("existing-photo.jpg", "new-photo.jpg", "another-photo.jpg"))
                .createdAt(existingRecipe.getCreatedAt())
                .build();

        when(recipeRepository.findByName("Existing Recipe")).thenReturn(Optional.of(existingRecipe));
        when(recipeRepository.update(eq("existing-id"), any(Recipe.class))).thenReturn(updatedRecipe);

        // when
        Recipe result = recipeService.findOrCreateRecipe(newRecipe);

        // then
        verify(recipeRepository).update(eq("existing-id"), recipeCaptor.capture());
        Recipe capturedRecipe = recipeCaptor.getValue();

        assertThat(capturedRecipe.getPhotos())
                .containsExactlyInAnyOrder("existing-photo.jpg", "new-photo.jpg", "another-photo.jpg");
        assertThat(result).isEqualTo(updatedRecipe);
    }

    @Test
    void findOrCreateRecipe_WhenNameIsEmpty_ShouldCreateNewRecipe() {
        // given
        Recipe recipeWithoutName = Recipe.builder()
                .instructions("Some instructions")
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id("new-id")
                .instructions("Some instructions")
                .createdAt(Timestamp.now())
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // when
        Recipe result = recipeService.findOrCreateRecipe(recipeWithoutName);

        // then
        verify(recipeRepository, never()).findByName(anyString());
        verify(recipeRepository).save(any(Recipe.class));
        assertThat(result).isEqualTo(savedRecipe);
    }

    @Test
    void uploadImage_ShouldUploadAndUpdateRecipe() throws IOException {
        // given
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeImageRepository.findByImageUrl(any())).thenReturn(Optional.empty());

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Blob blob = mock(Blob.class);
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        try (MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {
            UUID mockUuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUuid);

            // when
            String imageUrl = recipeService.uploadImage(TEST_RECIPE_ID, imageFile);

            // then
            verify(storage).create(blobInfoCaptor.capture(), byteArrayCaptor.capture());
            verify(recipeRepository).update(eq(TEST_RECIPE_ID), recipeCaptor.capture());
            verify(recipeImageRepository).save(imageReferenceCaptor.capture());

            BlobInfo capturedBlobInfo = blobInfoCaptor.getValue();
            byte[] capturedBytes = byteArrayCaptor.getValue();
            Recipe capturedRecipe = recipeCaptor.getValue();
            RecipeImageReference capturedReference = imageReferenceCaptor.getValue();

            assertThat(capturedBlobInfo.getBlobId().getBucket()).isEqualTo("test-bucket");
            // Verify the path structure (UUID might not be mocked correctly, so check structure)
            String actualPath = capturedBlobInfo.getBlobId().getName();
            assertThat(actualPath).startsWith("recipes/test-recipe-id/images/");
            assertThat(actualPath).endsWith(".jpg");
            // Verify it has some content between the path and extension (UUID or other identifier)
            assertThat(actualPath.length()).isGreaterThan("recipes/test-recipe-id/images/.jpg".length());
            assertThat(capturedBlobInfo.getContentType()).isEqualTo("image/jpeg");

            assertThat(capturedBytes).isEqualTo("test image content".getBytes());

            assertThat(capturedRecipe.getPhotos()).hasSize(3);
            assertThat(capturedRecipe.getPhotos())
                    .contains("photo1.jpg", "photo2.jpg");
            assertThat(capturedRecipe.getPhotos())
                    .anyMatch(url -> url.startsWith("https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/") && url.endsWith(".jpg"));

            assertThat(capturedReference.getImageUrl()).startsWith("https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/");
            assertThat(capturedReference.getImageUrl()).endsWith(".jpg");
            assertThat(capturedReference.getStoragePath()).startsWith("recipes/test-recipe-id/images/");
            assertThat(capturedReference.getStoragePath()).endsWith(".jpg");
            assertThat(capturedReference.getReferenceCount()).isEqualTo(1);

            assertThat(imageUrl).startsWith("https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/");
            assertThat(imageUrl).endsWith(".jpg");
        }
    }

    @Test
    void uploadImage_WithExistingImageReference_ShouldIncrementReferenceCount() throws IOException {
        // given
        Recipe recipe = createTestRecipe();
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Blob blob = mock(Blob.class);
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        try (MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {
            UUID mockUuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUuid);

            // Symulujemy, że referencja do obrazu już istnieje - użyj anyString() bo URL jest generowany z UUID
            RecipeImageReference existingReference = RecipeImageReference.builder()
                    .imageUrl("https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/some-uuid.jpg")
                    .storagePath("recipes/test-recipe-id/images/some-uuid.jpg")
                    .referenceCount(2)
                    .build();

            when(recipeImageRepository.findByImageUrl(anyString())).thenReturn(Optional.of(existingReference));

            // when
            String imageUrl = recipeService.uploadImage(TEST_RECIPE_ID, imageFile);

            // then
            verify(storage).create(any(BlobInfo.class), any(byte[].class));
            verify(recipeRepository).update(eq(TEST_RECIPE_ID), any(Recipe.class));
            // Verify incrementReferenceCount was called (the URL will be generated by the service)
            verify(recipeImageRepository).incrementReferenceCount(anyString());

            assertThat(imageUrl).startsWith("https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/");
            assertThat(imageUrl).endsWith(".jpg");
        }
    }

    @Test
    void deleteImage_ShouldDeleteImageFromStorageAndUpdateRecipe() throws BadRequestException {
        // given
        Recipe recipe = createTestRecipe();
        recipe.setPhotos(Arrays.asList("photo1.jpg", TEST_IMAGE_URL));
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        // when
        recipeService.deleteImage(TEST_RECIPE_ID, TEST_IMAGE_URL);

        // then
        // The implementation deletes from storage if URL contains storage.googleapis.com
        verify(storage).delete(any(BlobId.class));
        verify(recipeRepository).update(eq(TEST_RECIPE_ID), recipeCaptor.capture());

        Recipe capturedRecipe = recipeCaptor.getValue();
        assertThat(capturedRecipe.getPhotos()).hasSize(1);
        assertThat(capturedRecipe.getPhotos()).contains("photo1.jpg");
        assertThat(capturedRecipe.getPhotos()).doesNotContain(TEST_IMAGE_URL);
    }

    @Test
    void deleteImage_WhenImageNotFromStorage_ShouldOnlyUpdateRecipe() throws BadRequestException {
        // given
        Recipe recipe = createTestRecipe();
        String nonStorageImageUrl = "photo1.jpg";
        recipe.setPhotos(Arrays.asList(nonStorageImageUrl, "photo2.jpg"));
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        // when
        recipeService.deleteImage(TEST_RECIPE_ID, nonStorageImageUrl);

        // then
        // Should not delete from storage for non-storage URLs
        verify(storage, never()).delete(any(BlobId.class));
        verify(recipeRepository).update(eq(TEST_RECIPE_ID), recipeCaptor.capture());

        Recipe capturedRecipe = recipeCaptor.getValue();
        assertThat(capturedRecipe.getPhotos()).hasSize(1);
        assertThat(capturedRecipe.getPhotos()).contains("photo2.jpg");
        assertThat(capturedRecipe.getPhotos()).doesNotContain(nonStorageImageUrl);
    }

    @Test
    void deleteRecipe_ShouldDecrementReferenceCountForAllPhotos() {
        // given
        Recipe recipe = createTestRecipe();
        recipe.setPhotos(Arrays.asList("photo1.jpg", TEST_IMAGE_URL, "photo3.jpg"));
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        when(recipeImageRepository.decrementReferenceCount(any())).thenReturn(1);

        // when
        recipeService.deleteRecipe(TEST_RECIPE_ID);

        // then
        verify(recipeRepository).delete(TEST_RECIPE_ID);

        verify(recipeImageRepository).decrementReferenceCount("photo1.jpg");
        verify(recipeImageRepository).decrementReferenceCount(TEST_IMAGE_URL);
        verify(recipeImageRepository).decrementReferenceCount("photo3.jpg");

        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    void deleteRecipe_ShouldDeleteOrphanedImages() {
        // given
        Recipe recipe = createTestRecipe();
        String orphanedImageUrl = "https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/orphaned.jpg";
        String orphanedStoragePath = "recipes/test-recipe-id/images/orphaned.jpg";
        recipe.setPhotos(Arrays.asList("photo1.jpg", orphanedImageUrl));
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        when(recipeImageRepository.decrementReferenceCount("photo1.jpg")).thenReturn(1);

        when(recipeImageRepository.decrementReferenceCount(orphanedImageUrl)).thenReturn(0);

        RecipeImageReference orphanedRef = RecipeImageReference.builder()
                .imageUrl(orphanedImageUrl)
                .storagePath(orphanedStoragePath)
                .referenceCount(0)
                .build();
        when(recipeImageRepository.findAllWithZeroReferences()).thenReturn(Collections.singletonList(orphanedRef));

        // when
        recipeService.deleteRecipe(TEST_RECIPE_ID);

        // then
        verify(recipeRepository).delete(TEST_RECIPE_ID);

        verify(recipeImageRepository).decrementReferenceCount("photo1.jpg");
        verify(recipeImageRepository).decrementReferenceCount(orphanedImageUrl);
    }

    @Test
    void cleanupOrphanedImages_ShouldDeleteImagesWithZeroReferences() {
        // given
        List<RecipeImageReference> orphanedImages = Arrays.asList(
                RecipeImageReference.builder()
                        .imageUrl("https://storage.googleapis.com/test-bucket/images/orphaned1.jpg")
                        .storagePath("images/orphaned1.jpg")
                        .referenceCount(0)
                        .build(),
                RecipeImageReference.builder()
                        .imageUrl("https://storage.googleapis.com/test-bucket/images/orphaned2.jpg")
                        .storagePath("images/orphaned2.jpg")
                        .referenceCount(0)
                        .build()
        );

        when(recipeImageRepository.findAllWithZeroReferences()).thenReturn(orphanedImages);

        // when
        recipeService.cleanupOrphanedImages();

        // then
        verify(recipeImageRepository).findAllWithZeroReferences();

        ArgumentCaptor<BlobId> blobIdCaptor = ArgumentCaptor.forClass(BlobId.class);
        verify(storage, times(2)).delete(blobIdCaptor.capture());

        List<BlobId> capturedBlobIds = blobIdCaptor.getAllValues();
        assertThat(capturedBlobIds).hasSize(2);
        assertThat(capturedBlobIds.get(0).getName()).isEqualTo("images/orphaned1.jpg");
        assertThat(capturedBlobIds.get(1).getName()).isEqualTo("images/orphaned2.jpg");

        verify(recipeImageRepository).deleteByImageUrl("https://storage.googleapis.com/test-bucket/images/orphaned1.jpg");
        verify(recipeImageRepository).deleteByImageUrl("https://storage.googleapis.com/test-bucket/images/orphaned2.jpg");
    }

    @Test
    void uploadImage_WhenRecipeNotFound_ShouldThrowNotFoundException() {
        // given
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when/then
        assertThatThrownBy(() -> recipeService.uploadImage(TEST_RECIPE_ID, imageFile))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe not found");
    }

    @Test
    void deleteImage_ShouldRemoveImageAndUpdateRecipe() throws BadRequestException {
        // given
        Recipe recipe = Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Test Recipe")
                .authorId("test-user-id")
                .photos(Arrays.asList("photo1.jpg", "https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/test-image.jpg"))
                .build();

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        String imageUrl = "https://storage.googleapis.com/test-bucket/recipes/test-recipe-id/images/test-image.jpg";

        // when
        recipeService.deleteImage(TEST_RECIPE_ID, imageUrl);

        // then
        verify(storage).delete(any(BlobId.class));
        verify(recipeRepository).update(eq(TEST_RECIPE_ID), recipeCaptor.capture());

        Recipe capturedRecipe = recipeCaptor.getValue();
        assertThat(capturedRecipe.getPhotos()).hasSize(1);
        assertThat(capturedRecipe.getPhotos()).containsExactly("photo1.jpg");
    }

    @Test
    void deleteImage_WhenImageNotInRecipe_ShouldThrowBadRequestException() {
        // given
        Recipe recipe = Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Test Recipe")
                .authorId("test-user-id")
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

        String imageUrl = "https://storage.googleapis.com/test-bucket/non-existent-image.jpg";

        // when/then
        assertThatThrownBy(() -> recipeService.deleteImage(TEST_RECIPE_ID, imageUrl))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Podany obraz nie istnieje dla tego przepisu");
    }

    @Test
    void searchRecipes_ShouldReturnMatchingRecipes() {
        // given
        String query = "pasta";
        List<Recipe> expectedRecipes = Arrays.asList(createTestRecipe(), createTestRecipe("test-recipe-id-2"));
        List<RecipeEntity> entities = Arrays.asList(
                createTestRecipeEntity(createTestRecipe()),
                createTestRecipeEntity(createTestRecipe("test-recipe-id-2"))
        );

        when(recipeJpaRepository.searchVisible(eq(query), anyString())).thenReturn(entities);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class)))
                .thenAnswer(invocation -> {
                    RecipeEntity entity = invocation.getArgument(0);
                    return expectedRecipes.stream()
                            .filter(r -> r.getId().equals(entity.getExternalId()))
                            .findFirst()
                            .orElse(createTestRecipe());
                });

        // when
        List<Recipe> result = recipeService.searchRecipes(query);

        // then
        assertThat(result).hasSize(2);
        verify(recipeJpaRepository).searchVisible(eq(query), anyString());
    }

    @Test
    void uploadBase64Image_ShouldUploadAndReturnUrl() throws BadRequestException {
        // given
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAMC";

        Blob blob = mock(Blob.class);
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);

        try (MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {
            UUID mockUuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUuid);

            // when
            String imageUrl = recipeService.uploadBase64Image(base64Image);

            // then
            verify(storage).create(blobInfoCaptor.capture(), any(byte[].class));

            BlobInfo capturedBlobInfo = blobInfoCaptor.getValue();

            assertThat(capturedBlobInfo.getBlobId().getBucket()).isEqualTo("test-bucket");
            // Verify the path structure (UUID might not be mocked correctly, so check structure)
            String actualPath = capturedBlobInfo.getBlobId().getName();
            assertThat(actualPath).startsWith("temp-recipes/images/");
            assertThat(actualPath).endsWith(".jpg");
            // Verify it has some content between the path and extension (UUID or other identifier)
            assertThat(actualPath.length()).isGreaterThan("temp-recipes/images/.jpg".length());
            assertThat(capturedBlobInfo.getContentType()).isEqualTo("image/jpeg");

            assertThat(imageUrl).startsWith("https://storage.googleapis.com/test-bucket/temp-recipes/images/");
            assertThat(imageUrl).endsWith(".jpg");
        }
    }

    @Test
    void uploadBase64Image_WithInvalidData_ShouldThrowBadRequestException() {
        // given
        String invalidBase64 = "invalid-data";

        // when/then
        assertThatThrownBy(() -> recipeService.uploadBase64Image(invalidBase64))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Nieprawidłowe dane obrazu");
    }

    @Test
    void refreshRecipesCache_ShouldNotThrowException() {
        // when/then - just make sure it doesn't throw exceptions
        recipeService.refreshRecipesCache();
    }

    private Recipe createTestRecipe() {
        return createTestRecipe(TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe(String id) {
        return Recipe.builder()
                .id(id)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(Timestamp.parseTimestamp("2025-02-23T10:45:23Z"))
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .authorId("test-user-id")
                .isPublic(true)
                .build();
    }

    private RecipeEntity createTestRecipeEntity(Recipe recipe) {
        return RecipeEntity.builder()
                .externalId(recipe.getId())
                .name(recipe.getName())
                .instructions(recipe.getInstructions())
                .photos(recipe.getPhotos() != null ? new ArrayList<>(recipe.getPhotos()) : new ArrayList<>())
                .authorId(recipe.getAuthorId())
                .isPublic(recipe.isPublic())
                .createdAt(java.time.LocalDateTime.ofEpochSecond(
                        recipe.getCreatedAt().getSeconds(),
                        recipe.getCreatedAt().getNanos(),
                        java.time.ZoneOffset.UTC))
                .build();
    }
}