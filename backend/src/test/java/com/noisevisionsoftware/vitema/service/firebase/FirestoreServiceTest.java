package com.noisevisionsoftware.vitema.service.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirestoreServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private DocumentReference documentReference;

    @InjectMocks
    private FirestoreService firestoreService;

    private static final String TEST_DIET_ID = "test-diet-id";

    @Test
    void deleteRelatedData_ShouldDeleteAllRelatedData() throws ExecutionException, InterruptedException {
        // given
        List<DocumentSnapshot> documents = new ArrayList<>();
        documents.add(documentSnapshot);
        documents.add(documentSnapshot);

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), anyString())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        doReturn(documents).when(querySnapshot).getDocuments();
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(documentSnapshot.getReference()).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<Void> mockVoidFuture = mock(ApiFuture.class);
        doReturn(mockVoidFuture).when(documentReference).delete();
        when(firestore.document("diets/" + TEST_DIET_ID)).thenReturn(documentReference);
        doReturn(null).when(mockVoidFuture).get();

        // when
        firestoreService.deleteRelatedData(TEST_DIET_ID);

        // then
        verify(firestore).collection("shopping_lists");
        verify(firestore).collection("recipe_references");
        verify(firestore).document("diets/" + TEST_DIET_ID);
        verify(documentReference, times(5)).delete();
    }

    @Test
    void deleteRelatedData_WhenCollectionDeleteFails_ShouldThrowException() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection("shopping_lists")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), anyString())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenThrow(new ExecutionException(new InterruptedException("Test exception")));

        // when/then
        assertThatThrownBy(() -> firestoreService.deleteRelatedData(TEST_DIET_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete Firestore data")
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete collection")
                .cause()
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(InterruptedException.class)
                .hasMessage("Test exception");
    }

    @Test
    void deleteRelatedData_WhenDietDeleteFails_ShouldThrowException() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), anyString())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        doReturn(new ArrayList<DocumentSnapshot>()).when(querySnapshot).getDocuments();
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);

        when(firestore.document("diets/" + TEST_DIET_ID)).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<Void> mockVoidFuture = mock(ApiFuture.class);
        doReturn(mockVoidFuture).when(documentReference).delete();
        doThrow(new ExecutionException(new RuntimeException("Test exception")))
                .when(mockVoidFuture).get();

        // when/then
        assertThatThrownBy(() -> firestoreService.deleteRelatedData(TEST_DIET_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete Firestore data");
    }

    @Test
    void deleteRelatedData_WhenNoDocumentsFound_ShouldOnlyDeleteDiet() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), anyString())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        doReturn(new ArrayList<DocumentSnapshot>()).when(querySnapshot).getDocuments();

        when(firestore.document("diets/" + TEST_DIET_ID)).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<Void> mockVoidFuture = mock(ApiFuture.class);
        doReturn(mockVoidFuture).when(documentReference).delete();
        doReturn(null).when(mockVoidFuture).get();

        // when
        firestoreService.deleteRelatedData(TEST_DIET_ID);

        // then
        verify(firestore).collection("shopping_lists");
        verify(firestore).collection("recipe_references");
        verify(firestore).document("diets/" + TEST_DIET_ID);
        verify(documentReference, times(1)).delete();
    }

    @Test
    void deleteRelatedData_WhenDocumentDeleteFails_ShouldThrowException() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), anyString())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);

        List<DocumentSnapshot> documents = new ArrayList<>();
        documents.add(documentSnapshot);
        doReturn(documents).when(querySnapshot).getDocuments();
        when(documentSnapshot.getReference()).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<Void> mockVoidFuture = mock(ApiFuture.class);
        doReturn(mockVoidFuture).when(documentReference).delete();
        doThrow(new ExecutionException(new RuntimeException("Test exception")))
                .when(mockVoidFuture).get();

        // when/then
        assertThatThrownBy(() -> firestoreService.deleteRelatedData(TEST_DIET_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete Firestore data")
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete collection")
                .cause()
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");
    }
}