package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.diet.DietMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreDietMapper firestoreDietMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> documentFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private Query query;

    @InjectMocks
    private DietRepository dietRepository;

    private Diet testDiet;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        testDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(new ArrayList<>())
                .metadata(DietMetadata.builder().build())
                .build();
        testDiet.setId(TEST_ID);
        testDiet.setUserId(TEST_USER_ID);
    }

    @Test
    void save_ShouldSaveDietSuccessfully() {
        // Arrange
        Map<String, Object> firestoreMap = new HashMap<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenReturn(firestoreMap);
        when(documentReference.set(firestoreMap)).thenReturn(writeFuture);
        when(documentReference.getId()).thenReturn(TEST_ID);

        // Act
        Diet savedDiet = dietRepository.save(testDiet);

        // Assert
        assertNotNull(savedDiet);
        assertEquals(TEST_ID, savedDiet.getId());
        verify(documentReference).set(firestoreMap);
    }

    @Test
    void findById_ShouldReturnDietWhenExists() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentFuture);
        when(documentFuture.get()).thenReturn(documentSnapshot);
        when(firestoreDietMapper.toDiet(documentSnapshot)).thenReturn(testDiet);

        // Act
        Optional<Diet> result = dietRepository.findById(TEST_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_ID, result.get().getId());
    }

    @Test
    void findByUserId_ShouldReturnListOfDiets() throws Exception {
        // Arrange
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", TEST_USER_ID)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        // Act
        List<Diet> results = dietRepository.findByUserId(TEST_USER_ID);

        // Assert
        assertNotNull(results);
        verify(collectionReference).whereEqualTo("userId", TEST_USER_ID);
    }

    @Test
    void delete_ShouldDeleteDietSuccessfully() {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(writeFuture);

        // Act & Assert
        assertDoesNotThrow(() -> dietRepository.delete(TEST_ID));
        verify(documentReference).delete();
    }

    @Test
    void findAll_ShouldReturnAllDiets() throws Exception {
        // Arrange
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        // Act
        List<Diet> results = dietRepository.findAll();

        // Assert
        assertNotNull(results);
        verify(collectionReference).get();
    }

    @Test
    void findAllPaginated_ShouldReturnPaginatedDiets() throws Exception {
        // Arrange
        int page = 0;
        int size = 10;
        List<QueryDocumentSnapshot> documents = new ArrayList<>();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.offset(0)).thenReturn(query);
        when(query.limit(size)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        // Act
        List<Diet> results = dietRepository.findAllPaginated(page, size);

        // Assert
        assertNotNull(results);
        verify(query).limit(size);
        verify(query).offset(0);
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenSaveFails() {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenThrow(new RuntimeException("Mapping failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dietRepository.save(testDiet));
    }

    @Test
    void findById_ShouldReturnEmpty_WhenDietNotFound() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentFuture);
        when(documentFuture.get()).thenReturn(documentSnapshot);
        when(firestoreDietMapper.toDiet(documentSnapshot)).thenReturn(null);

        // Act
        Optional<Diet> result = dietRepository.findById(TEST_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void update_ShouldUpdateDietSuccessfully() throws Exception {
        // Arrange
        Map<String, Object> firestoreMap = new HashMap<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenReturn(firestoreMap);
        when(documentReference.update(firestoreMap)).thenReturn(writeFuture);

        // Act
        Diet updatedDiet = dietRepository.update(TEST_ID, testDiet);

        // Assert
        assertNotNull(updatedDiet);
        assertEquals(TEST_ID, updatedDiet.getId());
        verify(documentReference).update(firestoreMap);
        verify(writeFuture).get();
    }

    @Test
    void update_ShouldThrowRuntimeException_WhenUpdateFails() throws Exception {
        // Arrange
        Map<String, Object> firestoreMap = new HashMap<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenReturn(firestoreMap);
        when(documentReference.update(firestoreMap)).thenReturn(writeFuture);
        when(writeFuture.get()).thenThrow(new InterruptedException("Update failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dietRepository.update(TEST_ID, testDiet));
    }

    @Test
    void update_ShouldSetIdOnReturnedDiet() {
        // Arrange
        Diet dietWithoutId = Diet.builder()
                .userId(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(new ArrayList<>())
                .metadata(DietMetadata.builder().build())
                .build();

        Map<String, Object> firestoreMap = new HashMap<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenReturn(firestoreMap);
        when(documentReference.update(firestoreMap)).thenReturn(writeFuture);

        // Act
        Diet updatedDiet = dietRepository.update(TEST_ID, dietWithoutId);

        // Assert
        assertNotNull(updatedDiet);
        assertEquals(TEST_ID, updatedDiet.getId());
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenWriteFutureFails() throws Exception {
        // Arrange
        Map<String, Object> firestoreMap = new HashMap<>();
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(firestoreDietMapper.toFirestoreMap(any(Diet.class))).thenReturn(firestoreMap);
        when(documentReference.set(firestoreMap)).thenReturn(writeFuture);
        when(writeFuture.get()).thenThrow(new InterruptedException("Write failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.save(testDiet));
        assertEquals("Failed to save diet", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void findById_ShouldThrowRuntimeException_WhenDocumentFutureFails() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentFuture);
        when(documentFuture.get()).thenThrow(new InterruptedException("Fetch failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.findById(TEST_ID));
        assertEquals("Failed to fetch diet", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void findByUserId_ShouldReturnListWithActualDocuments() throws Exception {
        // Arrange
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").userId(TEST_USER_ID).build();
        Diet diet2 = Diet.builder().id("diet2").userId(TEST_USER_ID).build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", TEST_USER_ID)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(diet2);

        // Act
        List<Diet> results = dietRepository.findByUserId(TEST_USER_ID);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(diet1, results.get(0));
        assertEquals(diet2, results.get(1));
    }

    @Test
    void findByUserId_ShouldFilterOutNullDiets() throws Exception {
        // Arrange
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").userId(TEST_USER_ID).build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", TEST_USER_ID)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(null);

        // Act
        List<Diet> results = dietRepository.findByUserId(TEST_USER_ID);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(diet1, results.get(0));
    }

    @Test
    void findByUserId_ShouldThrowRuntimeException_WhenQueryFails() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", TEST_USER_ID)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenThrow(new InterruptedException("Query failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.findByUserId(TEST_USER_ID));
        assertEquals("Failed to fetch diets by userId", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void delete_ShouldThrowRuntimeException_WhenDeleteFails() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(TEST_ID)).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(writeFuture);
        when(writeFuture.get()).thenThrow(new InterruptedException("Delete failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.delete(TEST_ID));
        assertEquals("Failed to delete diet", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void findAll_ShouldReturnListWithActualDocuments() throws Exception {
        // Arrange
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").build();
        Diet diet2 = Diet.builder().id("diet2").build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(diet2);

        // Act
        List<Diet> results = dietRepository.findAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(diet1, results.get(0));
        assertEquals(diet2, results.get(1));
    }

    @Test
    void findAll_ShouldFilterOutNullDiets() throws Exception {
        // Arrange
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(null);

        // Act
        List<Diet> results = dietRepository.findAll();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(diet1, results.get(0));
    }

    @Test
    void findAll_ShouldThrowRuntimeException_WhenQueryFails() throws Exception {
        // Arrange
        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenThrow(new InterruptedException("Query failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.findAll());
        assertEquals("Failed to fetch all diets", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void findAllPaginated_ShouldReturnListWithActualDocuments() throws Exception {
        // Arrange
        int page = 1;
        int size = 5;
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").build();
        Diet diet2 = Diet.builder().id("diet2").build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.offset(page * size)).thenReturn(query);
        when(query.limit(size)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(diet2);

        // Act
        List<Diet> results = dietRepository.findAllPaginated(page, size);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(diet1, results.get(0));
        assertEquals(diet2, results.get(1));
        verify(query).offset(5);
        verify(query).limit(5);
    }

    @Test
    void findAllPaginated_ShouldFilterOutNullDiets() throws Exception {
        // Arrange
        int page = 0;
        int size = 10;
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        List<QueryDocumentSnapshot> documents = Arrays.asList(doc1, doc2);
        Diet diet1 = Diet.builder().id("diet1").build();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.offset(0)).thenReturn(query);
        when(query.limit(size)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreDietMapper.toDiet(doc1)).thenReturn(diet1);
        when(firestoreDietMapper.toDiet(doc2)).thenReturn(null);

        // Act
        List<Diet> results = dietRepository.findAllPaginated(page, size);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(diet1, results.get(0));
    }

    @Test
    void findAllPaginated_ShouldThrowRuntimeException_WhenQueryFails() throws Exception {
        // Arrange
        int page = 0;
        int size = 10;

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.offset(0)).thenReturn(query);
        when(query.limit(size)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenThrow(new InterruptedException("Query failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dietRepository.findAllPaginated(page, size));
        assertEquals("Failed to fetch paginated diets", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void findAllPaginated_ShouldCalculateOffsetCorrectly() throws Exception {
        // Arrange
        int page = 2;
        int size = 20;
        List<QueryDocumentSnapshot> documents = new ArrayList<>();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.offset(40)).thenReturn(query);
        when(query.limit(size)).thenReturn(query);
        when(query.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        // Act
        List<Diet> results = dietRepository.findAllPaginated(page, size);

        // Assert
        assertNotNull(results);
        verify(query).offset(40);
        verify(query).limit(20);
    }
}