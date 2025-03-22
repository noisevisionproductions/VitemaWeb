package com.noisevisionsoftware.nutrilog.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.nutrilog.mapper.recipe.FirestoreRecipeMapper;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirestoreRecipeRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreRecipeMapper firestoreRecipeMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private ApiFuture<Void> voidApiFuture;

    private FirestoreRecipeRepository repository;

    private static final String TEST_RECIPE_ID = "test-recipe-id";
    private static final Timestamp TEST_TIMESTAMP = Timestamp.parseTimestamp("2025-02-23T11:03:17Z");

    @BeforeEach
    void setUp() {
        repository = new FirestoreRecipeRepository(firestore, firestoreRecipeMapper);
    }

    @Test
    void findById_WhenRecipeExists_ShouldReturnRecipe() throws ExecutionException, InterruptedException {
        // given
        Recipe expectedRecipe = createTestRecipe();
        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(TEST_RECIPE_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreRecipeMapper.toRecipe(documentSnapshot)).thenReturn(expectedRecipe);

        // when
        Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

        // then
        assertThat(result)
                .isPresent()
                .contains(expectedRecipe);
    }

    @Test
    void findById_WhenRecipeDoesNotExist_ShouldReturnEmpty() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(TEST_RECIPE_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreRecipeMapper.toRecipe(documentSnapshot)).thenReturn(null);

        // when
        Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WhenFirestoreThrowsException_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(TEST_RECIPE_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when/then
        assertThatThrownBy(() -> repository.findById(TEST_RECIPE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch recipe")
                .hasCauseInstanceOf(InterruptedException.class);
    }

    @Test
    void findAllByIds_WhenRecipesExist_ShouldReturnAllRecipes() throws ExecutionException, InterruptedException {
        // given
        List<String> ids = Arrays.asList(TEST_RECIPE_ID, "test-recipe-id-2");
        Recipe recipe1 = createTestRecipe();
        Recipe recipe2 = createTestRecipe("test-recipe-id-2");

        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreRecipeMapper.toRecipe(documentSnapshot))
                .thenReturn(recipe1)
                .thenReturn(recipe2);

        // when
        List<Recipe> results = repository.findAllByIds(ids);

        // then
        assertThat(results)
                .hasSize(2)
                .containsExactly(recipe1, recipe2);
    }

    @Test
    void update_WhenSuccessful_ShouldUpdateRecipe() throws ExecutionException, InterruptedException {
        // given
        Recipe recipe = createTestRecipe();
        Map<String, Object> firestoreMap = new HashMap<>();
        firestoreMap.put("name", "Test Recipe");

        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(TEST_RECIPE_ID)).thenReturn(documentReference);
        when(firestoreRecipeMapper.toFirestoreMap(recipe)).thenReturn(firestoreMap);
        doReturn(voidApiFuture).when(documentReference).update(firestoreMap);

        // when
        repository.update(TEST_RECIPE_ID, recipe);

        // then
        verify(documentReference).update(firestoreMap);
        verify(voidApiFuture).get();
    }

    @Test
    void update_WhenFirestoreThrowsException_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        Recipe recipe = createTestRecipe();
        Map<String, Object> firestoreMap = new HashMap<>();

        when(firestore.collection("recipes")).thenReturn(collectionReference);
        when(collectionReference.document(TEST_RECIPE_ID)).thenReturn(documentReference);
        when(firestoreRecipeMapper.toFirestoreMap(recipe)).thenReturn(firestoreMap);
        doReturn(voidApiFuture).when(documentReference).update(firestoreMap);
        when(voidApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when/then
        assertThatThrownBy(() -> repository.update(TEST_RECIPE_ID, recipe))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to update recipe")
                .hasCauseInstanceOf(InterruptedException.class);
    }

    private Recipe createTestRecipe() {
        return createTestRecipe(TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe(String id) {
        return Recipe.builder()
                .id(id)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(TEST_TIMESTAMP)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();
    }
}