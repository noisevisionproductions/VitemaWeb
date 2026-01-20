package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.measurements.FirestoreMeasurementsMapper;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodyMeasurementsRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreMeasurementsMapper firestoreMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    private BodyMeasurementsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new BodyMeasurementsRepository(firestore, firestoreMapper);
    }

    @Test
    void findByUserId_ShouldReturnListOfMeasurements() throws Exception {
        // given
        String userId = "user123";

        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        documents.add(doc1);
        documents.add(doc2);

        BodyMeasurements measurement1 = createTestMeasurement("1", userId);
        BodyMeasurements measurement2 = createTestMeasurement("2", userId);

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", userId)).thenReturn(query);
        when(query.orderBy(anyString(), any(Query.Direction.class))).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreMapper.toBodyMeasurements(doc1)).thenReturn(measurement1);
        when(firestoreMapper.toBodyMeasurements(doc2)).thenReturn(measurement2);

        // when
        List<BodyMeasurements> result = repository.findByUserId(userId);

        // then
        assertEquals(2, result.size());
        assertEquals(measurement1, result.get(0));
        assertEquals(measurement2, result.get(1));

        verify(firestore).collection("bodyMeasurements");
        verify(collectionReference).whereEqualTo("userId", userId);
        verify(query).orderBy("date", Query.Direction.DESCENDING);
        verify(querySnapshotFuture).get();
        verify(firestoreMapper, times(2)).toBodyMeasurements(any(DocumentSnapshot.class));
    }

    @Test
    void findByUserId_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        String userId = "user123";

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userId", userId)).thenReturn(query);
        when(query.orderBy(anyString(), any(Query.Direction.class))).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.findByUserId(userId));
    }

    @Test
    void findById_WithExistingId_ShouldReturnOptionalWithMeasurement() throws Exception {
        // given
        String id = "measurement123";
        BodyMeasurements expectedMeasurement = createTestMeasurement(id, "user123");

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreMapper.toBodyMeasurements(documentSnapshot)).thenReturn(expectedMeasurement);

        // when
        Optional<BodyMeasurements> result = repository.findById(id);

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedMeasurement, result.get());

        verify(firestore).collection("bodyMeasurements");
        verify(collectionReference).document(id);
        verify(documentSnapshotFuture).get();
        verify(firestoreMapper).toBodyMeasurements(documentSnapshot);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmptyOptional() throws Exception {
        // given
        String id = "nonExistingId";

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreMapper.toBodyMeasurements(documentSnapshot)).thenReturn(null);

        // when
        Optional<BodyMeasurements> result = repository.findById(id);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void findById_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        String id = "measurement123";

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new ExecutionException(new RuntimeException("Test exception")));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.findById(id));
    }

    @Test
    void save_WithExistingId_ShouldUpdateDocument() throws Exception {
        // given
        String id = "existingId";
        BodyMeasurements measurements = createTestMeasurement(id, "user123");
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("weight", 75.0);

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(firestoreMapper.toFirestoreMap(measurements)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);

        // when
        repository.save(measurements);

        // then
        verify(firestore).collection("bodyMeasurements");
        verify(collectionReference).document(id);
        verify(firestoreMapper).toFirestoreMap(measurements);
        verify(documentReference).set(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void save_WithNewMeasurement_ShouldCreateNewDocumentWithGeneratedId() throws Exception {
        // given
        BodyMeasurements measurements = createTestMeasurement(null, "user123");
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("weight", 75.0);

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("generatedId");
        when(firestoreMapper.toFirestoreMap(measurements)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);

        // when
        repository.save(measurements);

        // then
        assertEquals("generatedId", measurements.getId());
        verify(firestore).collection("bodyMeasurements");
        verify(collectionReference).document();  // Bez argumentu, co oznacza wygenerowanie nowego ID
        verify(documentReference).getId();
        verify(firestoreMapper).toFirestoreMap(measurements);
        verify(documentReference).set(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void save_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        BodyMeasurements measurements = createTestMeasurement("id123", "user123");

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(firestoreMapper.toFirestoreMap(measurements)).thenReturn(new HashMap<>());
        when(documentReference.set(any())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.save(measurements));
    }

    @Test
    void delete_ShouldDeleteDocumentWithGivenId() throws Exception {
        // given
        String id = "measurementToDelete";

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(writeResultFuture);

        // when
        repository.delete(id);

        // then
        verify(firestore).collection("bodyMeasurements");
        verify(collectionReference).document(id);
        verify(documentReference).delete();
        verify(writeResultFuture).get();
    }

    @Test
    void delete_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        String id = "measurementToDelete";

        when(firestore.collection("bodyMeasurements")).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.delete(id));
    }

    // Metoda pomocnicza do tworzenia przykładowego pomiaru
    private BodyMeasurements createTestMeasurement(String id, String userId) {
        return BodyMeasurements.builder()
                .id(id)
                .userId(userId)
                .date(com.google.cloud.Timestamp.now())
                .weight(75.0)
                .height(180.0)
                .measurementType(MeasurementType.WEIGHT_ONLY)
                .sourceType(MeasurementSourceType.APP)
                .build();
    }
}