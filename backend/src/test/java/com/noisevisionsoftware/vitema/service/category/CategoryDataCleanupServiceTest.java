package com.noisevisionsoftware.vitema.service.category;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CategoryDataCleanupService Tests")
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

    @InjectMocks
    private CategoryDataCleanupService categoryDataCleanupService;

    private static final String COLLECTION_NAME = "product_categories";

    @BeforeEach
    void setUp() {
        when(firestore.collection(COLLECTION_NAME)).thenReturn(collectionReference);
    }

    @Nested
    @DisplayName("cleanupDuplicates")
    class CleanupDuplicatesTests {

        @Test
        @DisplayName("Should not remove any documents when no duplicates exist")
        void givenNoDuplicates_When_CleanupDuplicates_Then_NoDocumentsRemoved() throws Exception {
            // Given
            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Marchewka", null, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "Jabłko", null, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            verify(writeBatch, never()).delete(any(DocumentReference.class));
            verify(writeBatch, never()).update(any(DocumentReference.class), anyMap());
        }

        @Test
        @DisplayName("Should remove duplicates and keep the newest document")
        void givenDuplicates_When_CleanupDuplicates_Then_RemoveDuplicatesAndKeepNewest() throws Exception {
            // Given
            Timestamp olderTime = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp newerTime = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Marchewka", newerTime,
                    Arrays.asList("marchewka", "marchew"), 5L);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "marchewka", olderTime,
                    List.of("marchewka 500g"), 3L);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            ArgumentCaptor<DocumentReference> deleteCaptor = ArgumentCaptor.forClass(DocumentReference.class);
            verify(writeBatch).delete(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue().getId()).isEqualTo("doc2");

            ArgumentCaptor<DocumentReference> updateCaptor = ArgumentCaptor.forClass(DocumentReference.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(updateCaptor.capture(), updateMapCaptor.capture());

            assertThat(updateCaptor.getValue().getId()).isEqualTo("doc1");
            Map<String, Object> updates = updateMapCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) updates.get("variations");
            assertThat(variations).containsExactlyInAnyOrder("marchewka", "marchew", "marchewka 500g");
            assertThat(updates.get("usageCount")).isEqualTo(8);
            assertThat(updates.get("updatedAt")).isNotNull();
        }

        @Test
        @DisplayName("Should merge variations from all duplicates without duplicates")
        void givenDuplicatesWithOverlappingVariations_When_CleanupDuplicates_Then_MergeVariationsWithoutDuplicates() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);
            Timestamp time3 = Timestamp.ofTimeSecondsAndNanos(3000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Mleko", time3,
                    Arrays.asList("mleko", "mleko 3,2%"), 10L);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "mleko", time2,
                    Arrays.asList("mleko", "mleko 1l"), 5L);
            QueryDocumentSnapshot doc3 = createDocumentSnapshot("doc3", "MLEKO", time1,
                    List.of("mleko 500ml"), 2L);

            setupQuerySnapshot(Arrays.asList(doc1, doc2, doc3));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) updates.get("variations");
            assertThat(variations).containsExactlyInAnyOrder("mleko", "mleko 3,2%", "mleko 1l", "mleko 500ml");
            assertThat(variations).doesNotHaveDuplicates();
            assertThat(updates.get("usageCount")).isEqualTo(17);
        }

        @Test
        @DisplayName("Should sum usageCount from all duplicates")
        void givenDuplicatesWithUsageCounts_When_CleanupDuplicates_Then_SumAllUsageCounts() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Chleb", time2, null, 15L);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "chleb", time1, null, 7L);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            assertThat(updates.get("usageCount")).isEqualTo(22);
        }

        @Test
        @DisplayName("Should handle documents with null usageCount")
        void givenDuplicatesWithNullUsageCount_When_CleanupDuplicates_Then_HandleNullGracefully() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Masło", time2, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "masło", time1, null, 5L);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            assertThat(updates.get("usageCount")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle documents with null updatedAt by keeping them last")
        void givenDuplicatesWithNullUpdatedAt_When_CleanupDuplicates_Then_KeepDocumentWithTimestamp() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Ser", null, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "ser", time1, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            ArgumentCaptor<DocumentReference> deleteCaptor = ArgumentCaptor.forClass(DocumentReference.class);
            verify(writeBatch).delete(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue().getId()).isEqualTo("doc1");
        }

        @Test
        @DisplayName("Should commit batch when batch size reaches limit")
        void givenManyDuplicates_When_CleanupDuplicates_Then_CommitBatchWhenLimitReached() throws Exception {
            // Given
            List<QueryDocumentSnapshot> documents = new ArrayList<>();
            Timestamp baseTime = Timestamp.ofTimeSecondsAndNanos(1000, 0);

            // Create enough duplicates to trigger batch commit (450 operations limit)
            // Each duplicate group: 1 update + (N-1) deletes = N operations
            // To trigger commit, we need at least 450 operations in one group
            // Let's create a group with 451 duplicates: 1 update + 450 deletes = 451 operations
            // This will trigger a commit after 450 operations, then continue with the remaining
            for (int i = 0; i < 451; i++) {
                documents.add(createDocumentSnapshot("doc" + i, "Produkt",
                        Timestamp.ofTimeSecondsAndNanos(baseTime.getSeconds() + i, 0), null, null));
            }

            setupQuerySnapshot(documents);

            WriteBatch batch1 = mock(WriteBatch.class);
            WriteBatch batch2 = mock(WriteBatch.class);
            lenient().when(firestore.batch()).thenReturn(batch1, batch2);

            @SuppressWarnings("unchecked")
            ApiFuture<List<WriteResult>> commitFuture1 = mock(ApiFuture.class);
            @SuppressWarnings("unchecked")
            ApiFuture<List<WriteResult>> commitFuture2 = mock(ApiFuture.class);
            lenient().when(batch1.commit()).thenReturn(commitFuture1);
            lenient().when(batch2.commit()).thenReturn(commitFuture2);
            lenient().when(commitFuture1.get()).thenReturn(Collections.emptyList());
            lenient().when(commitFuture2.get()).thenReturn(Collections.emptyList());

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            // First batch should commit when reaching 450 operations (after 450 deletes)
            // Second batch should commit at the end with remaining operations
            verify(batch1, atLeast(1)).commit();
            verify(batch2, atLeast(1)).commit();
        }

        @Test
        @DisplayName("Should handle multiple duplicate groups")
        void givenMultipleDuplicateGroups_When_CleanupDuplicates_Then_ProcessAllGroups() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Marchewka", time2, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "marchewka", time1, null, null);
            QueryDocumentSnapshot doc3 = createDocumentSnapshot("doc3", "Jabłko", time2, null, null);
            QueryDocumentSnapshot doc4 = createDocumentSnapshot("doc4", "jabłko", time1, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2, doc3, doc4));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            verify(writeBatch, times(2)).delete(any(DocumentReference.class));
            verify(writeBatch, times(2)).update(any(DocumentReference.class), anyMap());
        }

        @Test
        @DisplayName("Should normalize product names when grouping")
        void givenProductsWithDifferentFormats_When_CleanupDuplicates_Then_GroupByNormalizedName() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            // These should be grouped together after normalization
            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Mleko 3,2% 1l", time2, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "mleko 500ml", time1, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            verify(writeBatch).delete(any(DocumentReference.class));
            verify(writeBatch).update(any(DocumentReference.class), anyMap());
        }

        @Test
        @DisplayName("Should handle documents with null productName")
        void givenDocumentsWithNullProductName_When_CleanupDuplicates_Then_SkipThem() throws Exception {
            // Given
            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Marchewka", null, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", null, null, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            verify(writeBatch, never()).delete(any(DocumentReference.class));
            verify(writeBatch, never()).update(any(DocumentReference.class), anyMap());
        }

        @Test
        @DisplayName("Should handle variations as non-String list items")
        void givenVariationsWithNonStringItems_When_CleanupDuplicates_Then_IgnoreNonStringItems() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Produkt", time2,
                    Arrays.asList("wariacja1", "wariacja2"), null);

            QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
            DocumentReference ref2 = mock(DocumentReference.class);
            when(doc2.getId()).thenReturn("doc2");
            when(doc2.getString("productName")).thenReturn("produkt");
            when(doc2.getTimestamp("updatedAt")).thenReturn(time1);
            when(doc2.get("variations")).thenReturn(Arrays.asList("wariacja3", 123, "wariacja4"));
            when(doc2.getLong("usageCount")).thenReturn(null);
            when(doc2.getReference()).thenReturn(ref2);
            when(ref2.getId()).thenReturn("doc2");

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) updates.get("variations");
            assertThat(variations).containsExactlyInAnyOrder("wariacja1", "wariacja2", "wariacja3", "wariacja4");
            assertThat(variations).hasSize(4);
        }

        @Test
        @DisplayName("Should throw RuntimeException when Firestore query fails")
        void givenFirestoreQueryFailure_When_CleanupDuplicates_Then_ThrowRuntimeException() throws Exception {
            // Given
            when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
            when(querySnapshotApiFuture.get()).thenThrow(new ExecutionException("Firestore error", null));

            // When & Then
            assertThatThrownBy(() -> categoryDataCleanupService.cleanupDuplicates())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Nie udało się wyczyścić duplikatów");
        }

        @Test
        @DisplayName("Should throw RuntimeException when batch commit fails")
        void givenBatchCommitFailure_When_CleanupDuplicates_Then_ThrowRuntimeException() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Produkt", time2, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "produkt", time1, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));

            when(firestore.batch()).thenReturn(writeBatch);
            @SuppressWarnings("unchecked")
            ApiFuture<List<WriteResult>> commitFuture = mock(ApiFuture.class);
            when(writeBatch.commit()).thenReturn(commitFuture);
            when(commitFuture.get()).thenThrow(new ExecutionException("Commit error", null));

            // When & Then
            assertThatThrownBy(() -> categoryDataCleanupService.cleanupDuplicates())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Nie udało się wyczyścić duplikatów");
        }

        @Test
        @DisplayName("Should handle empty collection")
        void givenEmptyCollection_When_CleanupDuplicates_Then_CompleteWithoutErrors() throws Exception {
            // Given
            setupQuerySnapshot(Collections.emptyList());
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            verify(writeBatch, never()).delete(any(DocumentReference.class));
            verify(writeBatch, never()).update(any(DocumentReference.class), anyMap());
        }

        @Test
        @DisplayName("Should handle variations as null")
        void givenDuplicatesWithNullVariations_When_CleanupDuplicates_Then_HandleNullGracefully() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Produkt", time2, null, null);
            QueryDocumentSnapshot doc2 = createDocumentSnapshot("doc2", "produkt", time1, null, null);

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) updates.get("variations");
            assertThat(variations).isEmpty();
        }

        @Test
        @DisplayName("Should handle variations as non-List object")
        void givenVariationsAsNonList_When_CleanupDuplicates_Then_HandleGracefully() throws Exception {
            // Given
            Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
            Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

            QueryDocumentSnapshot doc1 = createDocumentSnapshot("doc1", "Produkt", time2, null, null);

            QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
            DocumentReference ref2 = mock(DocumentReference.class);
            when(doc2.getId()).thenReturn("doc2");
            when(doc2.getString("productName")).thenReturn("produkt");
            when(doc2.getTimestamp("updatedAt")).thenReturn(time1);
            when(doc2.get("variations")).thenReturn("not a list");
            when(doc2.getLong("usageCount")).thenReturn(null);
            when(doc2.getReference()).thenReturn(ref2);
            when(ref2.getId()).thenReturn("doc2");

            setupQuerySnapshot(Arrays.asList(doc1, doc2));
            setupWriteBatch();

            // When
            categoryDataCleanupService.cleanupDuplicates();

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(writeBatch).update(any(DocumentReference.class), updateMapCaptor.capture());

            Map<String, Object> updates = updateMapCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) updates.get("variations");
            assertThat(variations).isEmpty();
        }
    }

    // Helper methods

    private QueryDocumentSnapshot createDocumentSnapshot(String id, String productName, Timestamp updatedAt,
                                                         List<String> variations, Long usageCount) {
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        DocumentReference ref = mock(DocumentReference.class);

        when(doc.getId()).thenReturn(id);
        when(doc.getString("productName")).thenReturn(productName);
        when(doc.getTimestamp("updatedAt")).thenReturn(updatedAt);
        when(doc.get("variations")).thenReturn(variations);
        when(doc.getLong("usageCount")).thenReturn(usageCount);
        when(doc.getReference()).thenReturn(ref);
        when(ref.getId()).thenReturn(id);

        return doc;
    }

    private void setupQuerySnapshot(List<QueryDocumentSnapshot> documents) throws Exception {
        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
    }

    private void setupWriteBatch() throws Exception {
        lenient().when(firestore.batch()).thenReturn(writeBatch);
        @SuppressWarnings("unchecked")
        ApiFuture<List<WriteResult>> commitFuture = mock(ApiFuture.class);
        lenient().when(writeBatch.commit()).thenReturn(commitFuture);
        lenient().when(commitFuture.get()).thenReturn(Collections.emptyList());
    }
}
