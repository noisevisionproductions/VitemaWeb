package com.noisevisionsoftware.nutrilog.service.category;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirestoreCategoryDataManagerTest {

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
    private Query query;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotApiFuture;

    @Mock
    private ApiFuture<WriteResult> writeResultApiFuture;

    private FirestoreCategoryDataManager firestoreCategoryDataManager;

    @BeforeEach
    void setUp() {
        firestoreCategoryDataManager = new FirestoreCategoryDataManager(firestore);
        when(firestore.collection(anyString())).thenReturn(collectionReference);
    }

    @Test
    void loadData_shouldReturnEmptyMap_whenCollectionIsEmpty() throws ExecutionException, InterruptedException {
        // Given
        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // When
        Map<String, ProductCategoryData> result = firestoreCategoryDataManager.loadData();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(collectionReference).get();
    }

    @Test
    void loadData_shouldReturnCategoriesMapWithNormalizedKeys_whenCollectionHasDocuments()
            throws ExecutionException, InterruptedException {
        // Given
        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);

        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);

        ProductCategoryData categoryData1 = ProductCategoryData.builder()
                .productName("marchewka")
                .categoryId("warzywa")
                .usageCount(5)
                .build();

        ProductCategoryData categoryData2 = ProductCategoryData.builder()
                .productName("jabłko")
                .categoryId("owoce")
                .usageCount(3)
                .build();

        when(doc1.toObject(ProductCategoryData.class)).thenReturn(categoryData1);
        when(doc2.toObject(ProductCategoryData.class)).thenReturn(categoryData2);

        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2));

        // When
        Map<String, ProductCategoryData> result = firestoreCategoryDataManager.loadData();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Sprawdzamy, czy klucze są znormalizowane
        assertTrue(result.containsKey("marchewka"));
        assertTrue(result.containsKey("jabłko"));
        assertEquals(categoryData1, result.get("marchewka"));
        assertEquals(categoryData2, result.get("jabłko"));
    }

    @Test
    void saveData_shouldCheckExistingDocumentsBeforeSaving() throws ExecutionException, InterruptedException {
        // Given
        Map<String, ProductCategoryData> data = Map.of(
                "testowy_produkt", ProductCategoryData.builder()
                        .productName("testowy_produkt")
                        .categoryId("kategoria1")
                        .usageCount(1)
                        .build()
        );

        // Setup batch operations
        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);

        // Mock sprawdzania istniejącego dokumentu
        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true); // Brak istniejącego dokumentu

        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // When
        firestoreCategoryDataManager.saveData(data);

        // Then
        verify(collectionReference).whereEqualTo("productName", "testowy_produkt");
        verify(writeBatch).set(any(DocumentReference.class), any(ProductCategoryData.class), eq(SetOptions.merge()));
        verify(writeBatch).commit();
    }

    @Test
    void saveData_shouldUpdateExistingDocument_whenDocumentExists() throws ExecutionException, InterruptedException {
        // Given
        Map<String, ProductCategoryData> data = Map.of(
                "istniejacy_produkt", ProductCategoryData.builder()
                        .productName("istniejacy_produkt")
                        .categoryId("kategoria1")
                        .usageCount(5)
                        .build()
        );

        // Setup batch operations
        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);

        QueryDocumentSnapshot existingDoc = mock(QueryDocumentSnapshot.class);
        when(existingDoc.getId()).thenReturn("existing_doc_id");

        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(existingDoc));

        when(collectionReference.document("existing_doc_id")).thenReturn(documentReference);

        // When
        firestoreCategoryDataManager.saveData(data);

        // Then
        verify(collectionReference).document("existing_doc_id");
        verify(writeBatch).set(eq(documentReference), any(ProductCategoryData.class), eq(SetOptions.merge()));
    }

    @Test
    void updateProduct_shouldCreateNewDocument_whenProductDoesNotExist()
            throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct newProduct = ParsedProduct.builder()
                .name("nowy_produkt")
                .original("Nowy produkt 500g")
                .quantity(500.0)
                .unit("g")
                .categoryId("kategoria1")
                .build();

        // Mock braku istniejącego dokumentu
        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);

        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);
        when(documentReference.set(anyMap(), eq(SetOptions.merge()))).thenReturn(writeResultApiFuture);

        // When
        ParsedProduct result = firestoreCategoryDataManager.updateProduct(newProduct);

        // Then
        assertEquals(newProduct, result);
        verify(documentReference).set(argThat(map -> "nowy produkt".equals(map.get("productName")) &&
                map.containsKey("createdAt") &&
                map.containsKey("usageCount") &&
                "kategoria1".equals(map.get("categoryId"))), eq(SetOptions.merge()));
    }

    @Test
    void updateProduct_shouldUpdateExistingDocument_whenProductExists()
            throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct newProduct = ParsedProduct.builder()
                .name("istniejacy_produkt")
                .original("Istniejący produkt 300g")
                .quantity(300.0)
                .unit("g")
                .categoryId("kategoria1")
                .build();

        QueryDocumentSnapshot existingDoc = mock(QueryDocumentSnapshot.class);
        when(existingDoc.getId()).thenReturn("existing_doc_id");

        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(existingDoc));

        when(collectionReference.document("existing_doc_id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        List<String> existingVariations = new ArrayList<>(List.of("stara wariacja"));
        when(documentSnapshot.get("variations")).thenReturn(existingVariations);
        when(documentReference.set(anyMap(), eq(SetOptions.merge()))).thenReturn(writeResultApiFuture);

        // When
        ParsedProduct result = firestoreCategoryDataManager.updateProduct(newProduct);

        // Then
        assertEquals(newProduct, result);
        verify(documentReference).set(argThat(map -> {
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) map.get("variations");
            return variations.contains("istniejący produkt 300g") &&
                    variations.contains("stara wariacja") &&
                    !map.containsKey("createdAt");
        }), eq(SetOptions.merge()));
    }

    @Test
    void updateProduct_shouldAddNewVariation_whenVariationDoesNotExist()
            throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct newProduct = ParsedProduct.builder()
                .name("produkt")
                .original("Nowa wariacja produktu")
                .quantity(100.0)
                .unit("g")
                .build();

        QueryDocumentSnapshot existingDoc = mock(QueryDocumentSnapshot.class);
        when(existingDoc.getId()).thenReturn("doc_id");

        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(existingDoc));

        when(collectionReference.document("doc_id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        List<String> existingVariations = new ArrayList<>(List.of("stara wariacja"));
        when(documentSnapshot.get("variations")).thenReturn(existingVariations);
        when(documentReference.set(anyMap(), eq(SetOptions.merge()))).thenReturn(writeResultApiFuture);

        // When
        firestoreCategoryDataManager.updateProduct(newProduct);

        // Then
        verify(documentReference).set(argThat(map -> {
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) map.get("variations");
            return variations.contains("nowa wariacja produktu") &&
                    variations.contains("stara wariacja") &&
                    variations.size() == 2;
        }), eq(SetOptions.merge()));
    }

    @Test
    void updateProduct_shouldNotDuplicateVariation_whenVariationAlreadyExists()
            throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct newProduct = ParsedProduct.builder()
                .name("produkt")
                .original("Istniejąca wariacja")
                .quantity(100.0)
                .unit("g")
                .build();

        QueryDocumentSnapshot existingDoc = mock(QueryDocumentSnapshot.class);
        when(existingDoc.getId()).thenReturn("doc_id");

        when(collectionReference.whereEqualTo(eq("productName"), anyString())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(existingDoc));

        when(collectionReference.document("doc_id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        List<String> existingVariations = new ArrayList<>(Arrays.asList("istniejąca wariacja", "inna wariacja"));
        when(documentSnapshot.get("variations")).thenReturn(existingVariations);
        when(documentReference.set(anyMap(), eq(SetOptions.merge()))).thenReturn(writeResultApiFuture);

        // When
        firestoreCategoryDataManager.updateProduct(newProduct);

        // Then
        verify(documentReference).set(argThat(map -> {
            @SuppressWarnings("unchecked")
            List<String> variations = (List<String>) map.get("variations");
            return variations.size() == 2 &&
                    Collections.frequency(variations, "istniejąca wariacja") == 1;
        }), eq(SetOptions.merge()));
    }

    @Test
    void loadData_shouldHandleException() throws ExecutionException, InterruptedException {
        // Given
        when(collectionReference.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // When
        Map<String, ProductCategoryData> result = firestoreCategoryDataManager.loadData();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveData_shouldHandleException() throws ExecutionException, InterruptedException {
        // Given
        Map<String, ProductCategoryData> data = Map.of(
                "product1", ProductCategoryData.builder().productName("product1").categoryId("category1").build()
        );

        when(firestore.batch()).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsApiFuture);

        when(collectionReference.whereEqualTo(anyString(), anyString())).thenReturn(query);
        when(query.limit(anyInt())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);

        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        when(writeResultsApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class,
                () -> firestoreCategoryDataManager.saveData(data));

        assertTrue(exception.getMessage().contains("Could not save category data"));
    }

    @Test
    void updateProduct_shouldHandleFirestoreException() {
        // Given
        ParsedProduct newProduct = ParsedProduct.builder()
                .name("test")
                .original("Test product")
                .build();

        when(collectionReference.whereEqualTo(anyString(), anyString())).thenReturn(query);
        when(query.limit(anyInt())).thenReturn(query);
        when(query.get()).thenThrow(new RuntimeException("Firestore error"));

        // When & Then
        Exception exception = assertThrows(RuntimeException.class,
                () -> firestoreCategoryDataManager.updateProduct(newProduct));

        assertTrue(exception.getMessage().contains("Nie udało się zaktualizować produktu"));
    }

    @Test
    void updateProduct_shouldHandleSpecialCharactersInProductName() throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct productWithSpecialChars = ParsedProduct.builder()
                .name("test!@#$%^&*()product")
                .original("Test product with special chars")
                .categoryId("test_category")
                .build();

        when(collectionReference.whereEqualTo(anyString(), anyString())).thenReturn(query);
        when(query.limit(anyInt())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);
        when(documentReference.set(anyMap(), any())).thenReturn(writeResultApiFuture);

        // When & Then
        assertDoesNotThrow(() -> firestoreCategoryDataManager.updateProduct(productWithSpecialChars));

        verify(documentReference).set(argThat(map -> {
            String productName = (String) map.get("productName");
            return productName != null && !productName.contains("!@#$%^&*()");
        }), eq(SetOptions.merge()));
    }

    @Test
    void updateProduct_shouldNormalizeProductNameCorrectly() throws ExecutionException, InterruptedException {
        // Given
        ParsedProduct productWithNumbers = ParsedProduct.builder()
                .name("Mleko 3,2% 1l")
                .original("Mleko 3,2% 1l")
                .categoryId("nabiał")
                .build();

        when(collectionReference.whereEqualTo(anyString(), anyString())).thenReturn(query);
        when(query.limit(anyInt())).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);
        when(documentReference.set(anyMap(), any())).thenReturn(writeResultApiFuture);

        // When
        firestoreCategoryDataManager.updateProduct(productWithNumbers);

        // Then
        verify(documentReference).set(argThat(map -> {
            String productName = (String) map.get("productName");
            return "mleko".equals(productName);
        }), eq(SetOptions.merge()));
    }
}