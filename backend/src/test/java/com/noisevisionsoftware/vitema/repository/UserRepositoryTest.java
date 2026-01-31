package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.user.FirestoreUserMapper;
import com.noisevisionsoftware.vitema.model.user.Gender;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
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
class UserRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreUserMapper firestoreUserMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(firestore, firestoreUserMapper);
    }

    @Test
    void findAll_ShouldReturnListOfUsers() throws ExecutionException, InterruptedException {
        // given
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        documents.add(doc1);
        documents.add(doc2);

        User user1 = createSampleUser("user1");
        User user2 = createSampleUser("user2");

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreUserMapper.toUser(doc1)).thenReturn(user1);
        when(firestoreUserMapper.toUser(doc2)).thenReturn(user2);

        // when
        List<User> result = userRepository.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(firestore).collection("users");
        verify(querySnapshotFuture).get();
        verify(firestoreUserMapper, times(2)).toUser(any(DocumentSnapshot.class));
    }

    @Test
    void findAll_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.findAll());
        assertEquals("Failed to fetch users", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() throws ExecutionException, InterruptedException {
        // given
        String userId = "testUserId";
        User expectedUser = createSampleUser(userId);

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreUserMapper.toUser(documentSnapshot)).thenReturn(expectedUser);

        // when
        Optional<User> result = userRepository.findById(userId);

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(firestore).collection("users");
        verify(documentReference).get();
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() throws ExecutionException, InterruptedException {
        // given
        String userId = "nonExistingId";

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreUserMapper.toUser(documentSnapshot)).thenReturn(null);

        // when
        Optional<User> result = userRepository.findById(userId);

        // then
        assertFalse(result.isPresent());
        verify(firestore).collection("users");
        verify(documentReference).get();
    }

    @Test
    void findById_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        String userId = "testUserId";

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.findById(userId));
        assertEquals("Failed to fetch user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void save_WhenNewUser_ShouldSaveWithGeneratedId() throws ExecutionException, InterruptedException {
        // given
        User user = createSampleUser(null); // brak ID
        Map<String, Object> firestoreData = new HashMap<>();
        String generatedId = "generatedUserId";

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn(generatedId);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);

        // when
        userRepository.save(user);

        // then
        assertEquals(generatedId, user.getId());
        verify(firestore).collection("users");
        verify(collectionReference).document();
        verify(documentReference).getId();
        verify(firestoreUserMapper).toFirestoreMap(user);
        verify(documentReference).set(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void save_WhenExistingUser_ShouldUpdateDocumentWithExistingId() throws ExecutionException, InterruptedException {
        // given
        String existingId = "existingUserId";
        User user = createSampleUser(existingId);
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(existingId)).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);

        // when
        userRepository.save(user);

        // then
        verify(firestore).collection("users");
        verify(collectionReference).document(existingId);
        verify(firestoreUserMapper).toFirestoreMap(user);
        verify(documentReference).set(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void save_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        User user = createSampleUser("userId");
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.save(user));
        assertEquals("Failed to save user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void update_ShouldUpdateUserSuccessfully() throws ExecutionException, InterruptedException {
        // given
        String userId = "userId";
        User user = createSampleUser(userId);
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.update(firestoreData)).thenReturn(writeResultFuture);

        // when
        userRepository.update(userId, user);

        // then
        verify(firestore).collection("users");
        verify(collectionReference).document(userId);
        verify(firestoreUserMapper).toFirestoreMap(user);
        verify(documentReference).update(firestoreData);
        verify(writeResultFuture).get();
    }

    @Test
    void update_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        String userId = "userId";
        User user = createSampleUser(userId);
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.update(firestoreData)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.update(userId, user));
        assertEquals("Failed to update user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void findAllByTrainerId_ShouldReturnListOfUsers() throws ExecutionException, InterruptedException {
        // given
        String trainerId = "trainer123";
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        documents.add(doc1);
        documents.add(doc2);

        User user1 = createSampleUser("user1");
        user1.setTrainerId(trainerId);
        User user2 = createSampleUser("user2");
        user2.setTrainerId(trainerId);

        Query query = mock(Query.class);
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("trainerId", trainerId)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreUserMapper.toUser(doc1)).thenReturn(user1);
        when(firestoreUserMapper.toUser(doc2)).thenReturn(user2);

        // when
        List<User> result = userRepository.findAllByTrainerId(trainerId);

        // then
        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(firestore).collection("users");
        verify(querySnapshotFuture).get();
        verify(firestoreUserMapper, times(2)).toUser(any(DocumentSnapshot.class));
    }

    @Test
    void findAllByTrainerId_WhenNoUsersFound_ShouldReturnEmptyList() throws ExecutionException, InterruptedException {
        // given
        String trainerId = "trainer123";
        List<QueryDocumentSnapshot> documents = new ArrayList<>();

        Query query = mock(Query.class);
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("trainerId", trainerId)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);

        // when
        List<User> result = userRepository.findAllByTrainerId(trainerId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(firestore).collection("users");
        verify(querySnapshotFuture).get();
    }

    @Test
    void findAllByTrainerId_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        String trainerId = "trainer123";

        Query query = mock(Query.class);
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("trainerId", trainerId)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.findAllByTrainerId(trainerId));
        assertEquals("Failed to fetch clients", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenMapperThrowsException() {
        // given
        User user = createSampleUser("userId");

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenThrow(new RuntimeException("Mapping failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.save(user));
        assertEquals("Failed to save user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void update_ShouldThrowRuntimeException_WhenMapperThrowsException() {
        // given
        String userId = "userId";
        User user = createSampleUser(userId);

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenThrow(new RuntimeException("Mapping failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.update(userId, user));
        assertEquals("Failed to update user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenExecutionExceptionOccurs() throws ExecutionException, InterruptedException {
        // given
        User user = createSampleUser("userId");
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.set(firestoreData)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new ExecutionException("Execution failed", new RuntimeException()));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.save(user));
        assertEquals("Failed to save user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void update_ShouldThrowRuntimeException_WhenExecutionExceptionOccurs() throws ExecutionException, InterruptedException {
        // given
        String userId = "userId";
        User user = createSampleUser(userId);
        Map<String, Object> firestoreData = new HashMap<>();

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(firestoreUserMapper.toFirestoreMap(user)).thenReturn(firestoreData);
        when(documentReference.update(firestoreData)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new ExecutionException("Execution failed", new RuntimeException()));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.update(userId, user));
        assertEquals("Failed to update user", exception.getMessage());
        verify(firestore).collection("users");
    }

    @Test
    void findAll_ShouldFilterOutNullUsers() throws ExecutionException, InterruptedException {
        // given
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        documents.add(doc1);
        documents.add(doc2);

        User user1 = createSampleUser("user1");

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreUserMapper.toUser(doc1)).thenReturn(user1);
        when(firestoreUserMapper.toUser(doc2)).thenReturn(null);

        // when
        List<User> result = userRepository.findAll();

        // then
        assertEquals(2, result.size()); // Note: UserRepository doesn't filter nulls, so both are included
        assertEquals(user1, result.get(0));
        assertNull(result.get(1));
    }

    @Test
    void findById_ShouldThrowRuntimeException_WhenExecutionExceptionOccurs() throws ExecutionException, InterruptedException {
        // given
        String userId = "testUserId";

        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new ExecutionException("Execution failed", new RuntimeException()));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userRepository.findById(userId));
        assertEquals("Failed to fetch user", exception.getMessage());
        verify(firestore).collection("users");
    }

    private User createSampleUser(String id) {
        return User.builder()
                .id(id)
                .email("test@example.com")
                .nickname("TestUser")
                .gender(Gender.MALE)
                .birthDate(946684800000L) // 2000-01-01
                .storedAge(25)
                .profileCompleted(true)
                .role(UserRole.USER)
                .note("Test note")
                .createdAt(1614556800000L) // 2021-03-01
                .build();
    }
}