package com.noisevisionsoftware.vitema.service.category;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryDataCleanupServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotApiFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private WriteBatch writeBatch;

    @Mock
    private ApiFuture<List<WriteResult>> writeResultsApiFuture;

    @Mock
    private DocumentReference documentReference;

    @InjectMocks
    private CategoryDataCleanupService categoryDataCleanupService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> updateDataCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(firestore.collection("product_categories")).thenReturn(collectionReference);
    }

    @Test
    @DisplayName("Powinien zakończyć się sukcesem gdy brak duplikatów")
    void cleanupDuplicates_shouldCompleteSuccessfully_whenNoDuplicates() throws ExecutionException, InterruptedException {
        // Given
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("marchewka");
        when(doc2.getString("productName")).thenReturn("jabłko");

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        lenient().when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        lenient().when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> categoryDataCleanupService.cleanupDuplicates());

        // Then
        verify(collectionReference).get();
        // batch.commit() jest wywoływane tylko gdy batchSize > 0, więc może nie być wywołane
        verify(writeBatch, never()).update(any(), any());
        verify(writeBatch, never()).delete(any());
    }

    @Test
    @DisplayName("Powinien usunąć duplikaty i zachować najnowszy dokument")
    void cleanupDuplicates_shouldRemoveDuplicatesAndKeepNewest() throws ExecutionException, InterruptedException {
        // Given
        Timestamp olderTimestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);
        Timestamp newerTimestamp = Timestamp.ofTimeSecondsAndNanos(2000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("marchewka");
        when(doc1.getTimestamp("updatedAt")).thenReturn(newerTimestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn(Arrays.asList("wariacja1", "wariacja2"));

        when(doc2.getString("productName")).thenReturn("marchewka");
        when(doc2.getTimestamp("updatedAt")).thenReturn(olderTimestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Arrays.asList("wariacja2", "wariacja3"));

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch).update(eq(doc1Ref), updateDataCaptor.capture());
        verify(writeBatch).delete(eq(doc2Ref));
        verify(writeBatch).commit();

        Map<String, Object> updateData = updateDataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<String> variations = (List<String>) updateData.get("variations");
        assertEquals(8, ((Number) updateData.get("usageCount")).intValue()); // 5 + 3
        assertTrue(variations.contains("wariacja1"));
        assertTrue(variations.contains("wariacja2"));
        assertTrue(variations.contains("wariacja3"));
        assertEquals(3, variations.size()); // Bez duplikatów
        assertNotNull(updateData.get("updatedAt"));
    }

    @Test
    @DisplayName("Powinien obsłużyć duplikaty z null timestamp")
    void cleanupDuplicates_shouldHandleDuplicatesWithNullTimestamp() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(2L);
        when(doc1.get("variations")).thenReturn(Collections.singletonList("wariacja1"));

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(null);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(null);
        when(doc2.get("variations")).thenReturn(null);

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch).update(eq(doc1Ref), any());
        verify(writeBatch).delete(eq(doc2Ref));
    }

    @Test
    @DisplayName("Powinien zsumować usageCount z wszystkich duplikatów")
    void cleanupDuplicates_shouldSumUsageCountFromAllDuplicates() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
        Timestamp timestamp2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);
        Timestamp timestamp3 = Timestamp.ofTimeSecondsAndNanos(3000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);
        DocumentReference doc3Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc3 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp1);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(10L);
        when(doc1.get("variations")).thenReturn(Collections.emptyList());

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp2);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(5L);
        when(doc2.get("variations")).thenReturn(Collections.emptyList());

        when(doc3.getString("productName")).thenReturn("produkt");
        when(doc3.getTimestamp("updatedAt")).thenReturn(timestamp3);
        when(doc3.getReference()).thenReturn(doc3Ref);
        when(doc3.getLong("usageCount")).thenReturn(7L);
        when(doc3.get("variations")).thenReturn(Collections.emptyList());

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then - doc3 powinien być zachowany (najnowszy timestamp)
        verify(writeBatch).update(eq(doc3Ref), updateDataCaptor.capture());
        verify(writeBatch).delete(eq(doc1Ref));
        verify(writeBatch).delete(eq(doc2Ref));

        Map<String, Object> updateData = updateDataCaptor.getValue();
        assertEquals(22, ((Number) updateData.get("usageCount")).intValue()); // 10 + 5 + 7
    }

    @Test
    @DisplayName("Powinien połączyć wszystkie wariacje bez duplikatów")
    void cleanupDuplicates_shouldMergeAllVariationsWithoutDuplicates() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(0L);
        when(doc1.get("variations")).thenReturn(Arrays.asList("wariacja1", "wariacja2"));

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(0L);
        when(doc2.get("variations")).thenReturn(Arrays.asList("wariacja2", "wariacja3", "wariacja4"));

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch).update(eq(doc1Ref), updateDataCaptor.capture());

        Map<String, Object> updateData = updateDataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<String> variations = (List<String>) updateData.get("variations");
        assertEquals(4, variations.size());
        assertTrue(variations.contains("wariacja1"));
        assertTrue(variations.contains("wariacja2"));
        assertTrue(variations.contains("wariacja3"));
        assertTrue(variations.contains("wariacja4"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wiele grup produktów jednocześnie")
    void cleanupDuplicates_shouldHandleMultipleProductGroups() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);
        DocumentReference doc3Ref = mock(DocumentReference.class);
        DocumentReference doc4Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc3 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc4 = mock(QueryDocumentSnapshot.class);

        // Grupa 1: marchewka
        when(doc1.getString("productName")).thenReturn("marchewka");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn(Collections.emptyList());

        when(doc2.getString("productName")).thenReturn("marchewka");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Collections.emptyList());

        // Grupa 2: jabłko
        when(doc3.getString("productName")).thenReturn("jabłko");
        when(doc3.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc3.getReference()).thenReturn(doc3Ref);
        when(doc3.getLong("usageCount")).thenReturn(2L);
        when(doc3.get("variations")).thenReturn(Collections.emptyList());

        when(doc4.getString("productName")).thenReturn("jabłko");
        when(doc4.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc4.getReference()).thenReturn(doc4Ref);
        when(doc4.getLong("usageCount")).thenReturn(1L);
        when(doc4.get("variations")).thenReturn(Collections.emptyList());

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2, doc3, doc4));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch, times(2)).update(any(), any()); // Dla każdej grupy
        verify(writeBatch, times(2)).delete(any()); // Usunięcie duplikatów
    }

    @Test
    @DisplayName("Powinien obsłużyć batch size limit i commitować wielokrotnie")
    void cleanupDuplicates_shouldHandleBatchSizeLimit() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);
        List<QueryDocumentSnapshot> documents = new ArrayList<>();

        // Tworzymy dużo duplikatów, aby przekroczyć limit batch (450 operacji)
        for (int i = 0; i < 300; i++) {
            DocumentReference doc1Ref = mock(DocumentReference.class);
            DocumentReference doc2Ref = mock(DocumentReference.class);
            QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
            QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

            when(doc1.getString("productName")).thenReturn("produkt" + i);
            when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
            when(doc1.getReference()).thenReturn(doc1Ref);
            when(doc1.getLong("usageCount")).thenReturn(1L);
            when(doc1.get("variations")).thenReturn(Collections.emptyList());

            when(doc2.getString("productName")).thenReturn("produkt" + i);
            when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
            when(doc2.getReference()).thenReturn(doc2Ref);
            when(doc2.getLong("usageCount")).thenReturn(1L);
            when(doc2.get("variations")).thenReturn(Collections.emptyList());

            documents.add(doc1);
            documents.add(doc2);
        }

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        WriteBatch batch1 = mock(WriteBatch.class);
        WriteBatch batch2 = mock(WriteBatch.class);
        ApiFuture<List<WriteResult>> future1 = mock(ApiFuture.class);
        ApiFuture<List<WriteResult>> future2 = mock(ApiFuture.class);

        when(firestore.batch()).thenReturn(batch1, batch2);
        when(batch1.commit()).thenReturn(future1);
        when(batch2.commit()).thenReturn(future2);
        when(future1.get()).thenReturn(Collections.emptyList());
        when(future2.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(batch1, atLeastOnce()).commit();
        verify(batch2, atLeastOnce()).commit();
    }

    @Test
    @DisplayName("Powinien rzucić RuntimeException gdy wystąpi błąd")
    void cleanupDuplicates_shouldThrowRuntimeException_whenErrorOccurs() throws ExecutionException, InterruptedException {
        // Given
        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryDataCleanupService.cleanupDuplicates());

        assertTrue(exception.getMessage().contains("Nie udało się wyczyścić duplikatów"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Powinien znormalizować nazwy produktów przed grupowaniem")
    void cleanupDuplicates_shouldNormalizeProductNames() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        // Różne warianty nazwy produktu, które powinny być znormalizowane do tego samego
        when(doc1.getString("productName")).thenReturn("Mleko 3,2% 1l");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn(Collections.emptyList());

        when(doc2.getString("productName")).thenReturn("mleko 500ml");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Collections.emptyList());

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then - powinny być traktowane jako duplikaty po normalizacji
        verify(writeBatch).update(any(), any());
        verify(writeBatch).delete(any());
    }

    @Test
    @DisplayName("Powinien pominąć dokumenty bez productName")
    void cleanupDuplicates_shouldSkipDocumentsWithoutProductName() throws ExecutionException, InterruptedException {
        // Given
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc2.getString("productName")).thenReturn(null);

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        lenient().when(firestore.batch()).thenReturn(writeBatch);
        lenient().when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        lenient().when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch, never()).update(any(), any());
        verify(writeBatch, never()).delete(any());
    }

    @Test
    @DisplayName("Powinien obsłużyć wariacje jako nie-List")
    void cleanupDuplicates_shouldHandleNonListVariations() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn("nie jest listą"); // Nieprawidłowy typ

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Arrays.asList("wariacja1"));

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> categoryDataCleanupService.cleanupDuplicates());

        // Then
        verify(writeBatch).update(eq(doc1Ref), updateDataCaptor.capture());
        Map<String, Object> updateData = updateDataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<String> variations = (List<String>) updateData.get("variations");
        assertTrue(variations.contains("wariacja1"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wariacje z nie-String elementami")
    void cleanupDuplicates_shouldHandleVariationsWithNonStringElements() throws ExecutionException, InterruptedException {
        // Given
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn(Arrays.asList("wariacja1", 123, "wariacja2")); // Mieszane typy

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(timestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Collections.emptyList());

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> categoryDataCleanupService.cleanupDuplicates());

        // Then
        verify(writeBatch).update(eq(doc1Ref), updateDataCaptor.capture());
        Map<String, Object> updateData = updateDataCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<String> variations = (List<String>) updateData.get("variations");
        assertTrue(variations.contains("wariacja1"));
        assertTrue(variations.contains("wariacja2"));
        assertFalse(variations.contains(123)); // Nie-String elementy powinny być pominięte
    }

    @Test
    @DisplayName("Powinien zaktualizować updatedAt na najnowszą datę")
    void cleanupDuplicates_shouldUpdateUpdatedAtToCurrentTime() throws ExecutionException, InterruptedException {
        // Given
        Timestamp beforeTest = Timestamp.now();
        Timestamp oldTimestamp = Timestamp.ofTimeSecondsAndNanos(1000, 0);

        DocumentReference doc1Ref = mock(DocumentReference.class);
        DocumentReference doc2Ref = mock(DocumentReference.class);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        when(doc1.getString("productName")).thenReturn("produkt");
        when(doc1.getTimestamp("updatedAt")).thenReturn(oldTimestamp);
        when(doc1.getReference()).thenReturn(doc1Ref);
        when(doc1.getLong("usageCount")).thenReturn(5L);
        when(doc1.get("variations")).thenReturn(Collections.emptyList());

        when(doc2.getString("productName")).thenReturn("produkt");
        when(doc2.getTimestamp("updatedAt")).thenReturn(oldTimestamp);
        when(doc2.getReference()).thenReturn(doc2Ref);
        when(doc2.getLong("usageCount")).thenReturn(3L);
        when(doc2.get("variations")).thenReturn(Collections.emptyList());

        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);
        when(writeResultsApiFuture.get()).thenReturn(Collections.emptyList());

        // When
        categoryDataCleanupService.cleanupDuplicates();

        // Then
        verify(writeBatch).update(eq(doc1Ref), updateDataCaptor.capture());
        Map<String, Object> updateData = updateDataCaptor.getValue();
        Timestamp updatedAt = (Timestamp) updateData.get("updatedAt");
        assertNotNull(updatedAt);
        assertTrue(updatedAt.compareTo(beforeTest) >= 0);
    }

    @Test
    @DisplayName("Powinien poprawnie znormalizować nazwę produktu - test metody normalizeProductName")
    void normalizeProductName_shouldNormalizeCorrectly() throws Exception {
        // Given
        Method normalizeMethod = CategoryDataCleanupService.class.getDeclaredMethod(
                "normalizeProductName", String.class);
        normalizeMethod.setAccessible(true);

        // When & Then
        assertEquals("mleko", normalizeMethod.invoke(categoryDataCleanupService, "Mleko 3,2% 1l"));
        assertEquals("marchewka", normalizeMethod.invoke(categoryDataCleanupService, "Marchewka 500g"));
        assertEquals("jabłko", normalizeMethod.invoke(categoryDataCleanupService, "Jabłko 2 szt"));
        assertEquals("", normalizeMethod.invoke(categoryDataCleanupService, (String) null));
        assertEquals("chleb", normalizeMethod.invoke(categoryDataCleanupService, "Chleb!@#$%^&*()"));
        assertEquals("produkt testowy", normalizeMethod.invoke(categoryDataCleanupService, "Produkt   Testowy"));
    }

    @Test
    @DisplayName("Powinien poprawnie wyciągnąć wariacje z dokumentu - test metody getVariationsFromDocument")
    void getVariationsFromDocument_shouldExtractVariationsCorrectly() throws Exception {
        // Given
        Method getVariationsMethod = CategoryDataCleanupService.class.getDeclaredMethod(
                "getVariationsFromDocument", DocumentSnapshot.class);
        getVariationsMethod.setAccessible(true);

        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        // Test z poprawną listą Stringów
        when(doc.get("variations")).thenReturn(Arrays.asList("wariacja1", "wariacja2"));
        @SuppressWarnings("unchecked")
        List<String> result1 = (List<String>) getVariationsMethod.invoke(categoryDataCleanupService, doc);
        assertEquals(2, result1.size());
        assertTrue(result1.contains("wariacja1"));
        assertTrue(result1.contains("wariacja2"));

        // Test z null
        when(doc.get("variations")).thenReturn(null);
        @SuppressWarnings("unchecked")
        List<String> result2 = (List<String>) getVariationsMethod.invoke(categoryDataCleanupService, doc);
        assertTrue(result2.isEmpty());

        // Test z nie-List
        when(doc.get("variations")).thenReturn("nie jest listą");
        @SuppressWarnings("unchecked")
        List<String> result3 = (List<String>) getVariationsMethod.invoke(categoryDataCleanupService, doc);
        assertTrue(result3.isEmpty());

        // Test z listą zawierającą nie-String elementy
        when(doc.get("variations")).thenReturn(Arrays.asList("wariacja1", 123, "wariacja2"));
        @SuppressWarnings("unchecked")
        List<String> result4 = (List<String>) getVariationsMethod.invoke(categoryDataCleanupService, doc);
        assertEquals(2, result4.size());
        assertTrue(result4.contains("wariacja1"));
        assertTrue(result4.contains("wariacja2"));
    }
}
