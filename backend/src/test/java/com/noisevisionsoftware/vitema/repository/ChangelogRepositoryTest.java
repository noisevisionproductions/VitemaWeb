package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.changelog.FirestoreChangelogMapper;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangelogRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreChangelogMapper firestoreMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private CollectionReference userSettingsCollection;

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

    private ChangelogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ChangelogRepository(firestore, firestoreMapper);
    }

    @Test
    void findAll_ShouldReturnListOfChangelogEntries() throws Exception {
        // given
        when(firestore.collection("changelog")).thenReturn(collectionReference);

        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        documents.add(doc1);
        documents.add(doc2);

        ChangelogEntry entry1 = createTestEntry("1");
        ChangelogEntry entry2 = createTestEntry("2");

        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreMapper.toChangelogEntry(doc1)).thenReturn(entry1);
        when(firestoreMapper.toChangelogEntry(doc2)).thenReturn(entry2);

        // when
        List<ChangelogEntry> result = repository.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals(entry1, result.get(0));
        assertEquals(entry2, result.get(1));

        verify(firestore).collection("changelog");
        verify(collectionReference).orderBy("createdAt", Query.Direction.DESCENDING);
        verify(querySnapshotFuture).get();
        verify(firestoreMapper, times(2)).toChangelogEntry(any(DocumentSnapshot.class));
    }

    @Test
    void findAll_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        when(firestore.collection("changelog")).thenReturn(collectionReference);
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.findAll());
    }

    @Test
    void save_ShouldAddNewChangelogEntry() throws Exception {
        // given
        when(firestore.collection("changelog")).thenReturn(collectionReference);

        ChangelogEntry entry = createTestEntry(null);
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("title", "Test Entry");

        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("generatedId");
        when(firestoreMapper.toFirestoreMap(entry)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);

        // when
        repository.save(entry);

        // then
        assertEquals("generatedId", entry.getId());
        verify(collectionReference).document();
        verify(documentReference).getId();
        verify(firestoreMapper).toFirestoreMap(entry);
        verify(documentReference).set(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void save_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        when(firestore.collection("changelog")).thenReturn(collectionReference);

        ChangelogEntry entry = createTestEntry(null);

        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("generatedId");
        when(firestoreMapper.toFirestoreMap(entry)).thenReturn(new HashMap<>());
        when(documentReference.set(any())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.save(entry));
    }

    @Test
    void updateUserSettings_ShouldUpdateLastReadTimestamp() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "user123";
        Timestamp lastRead = Timestamp.now();

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<SetOptions> optionsCaptor = ArgumentCaptor.forClass(SetOptions.class);
        when(documentReference.set(dataCaptor.capture(), optionsCaptor.capture())).thenReturn(writeResultFuture);

        // when
        repository.updateUserSettings(userId, lastRead);

        // then
        assertEquals(lastRead, dataCaptor.getValue().get("lastChangelogRead"));
        assertEquals(SetOptions.merge(), optionsCaptor.getValue());
        verify(writeResultFuture).get();
    }

    @Test
    void updateUserSettings_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "user123";
        Timestamp lastRead = Timestamp.now();

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);
        when(documentReference.set(any(), any(SetOptions.class))).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new ExecutionException(new RuntimeException("Test exception")));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.updateUserSettings(userId, lastRead));
    }

    @Test
    void getLastReadTimestamp_WhenDocumentExists_ShouldReturnTimestamp() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "user123";
        Timestamp expectedTimestamp = Timestamp.now();

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.get("lastChangelogRead")).thenReturn(expectedTimestamp);

        // when
        Timestamp result = repository.getLastReadTimestamp(userId);

        // then
        assertEquals(expectedTimestamp, result);
    }

    @Test
    void getLastReadTimestamp_WhenDocumentDoesNotExist_ShouldReturnEpochTimestamp() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "nonExistingUser";

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        Timestamp result = repository.getLastReadTimestamp(userId);

        // then
        assertEquals(0, result.getSeconds());
        assertEquals(0, result.getNanos());
    }

    @Test
    void getLastReadTimestamp_WhenFieldIsNull_ShouldReturnEpochTimestamp() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "userWithNoLastRead";

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.get("lastChangelogRead")).thenReturn(null);

        // when
        Timestamp result = repository.getLastReadTimestamp(userId);

        // then
        assertEquals(0, result.getSeconds());
        assertEquals(0, result.getNanos());
    }

    @Test
    void getLastReadTimestamp_WhenExceptionOccurs_ShouldThrowRuntimeException() throws Exception {
        // given
        when(firestore.collection("userSettings")).thenReturn(userSettingsCollection);

        String userId = "user123";

        when(userSettingsCollection.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> repository.getLastReadTimestamp(userId));
    }

    // Metoda pomocnicza do tworzenia przykładowego wpisu w dzienniku zmian
    private ChangelogEntry createTestEntry(String id) {
        return ChangelogEntry.builder()
                .id(id)
                .description("This is a test entry description")
                .createdAt(Timestamp.now())
                .author("test-author")
                .type(ChangelogEntryType.FEATURE)
                .build();
    }
}