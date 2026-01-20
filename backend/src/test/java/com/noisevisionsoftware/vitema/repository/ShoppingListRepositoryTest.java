package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.shopping.FirestoreShoppingMapper;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirestoreShoppingMapper firestoreShoppingMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    private ShoppingListRepository shoppingListRepository;

    @BeforeEach
    void setUp() {
        shoppingListRepository = new ShoppingListRepository(firestore, firestoreShoppingMapper);
    }

    @Test
    void findByDietId_WhenShoppingListExists_ShouldReturnShoppingList() throws ExecutionException, InterruptedException {
        // given
        String dietId = "test-diet-id";
        ShoppingList expectedShoppingList = createSampleShoppingList();
        List<QueryDocumentSnapshot> documents = Collections.singletonList(mock(QueryDocumentSnapshot.class));

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), eq(dietId))).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(documents);
        when(firestoreShoppingMapper.toShoppingList(any(DocumentSnapshot.class)))
                .thenReturn(expectedShoppingList);

        // when
        Optional<ShoppingList> result = shoppingListRepository.findByDietId(dietId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedShoppingList);
        verify(firestore).collection("shopping_lists");
        verify(query).get();
    }

    @Test
    void findByDietId_WhenShoppingListDoesNotExist_ShouldReturnEmpty() throws ExecutionException, InterruptedException {
        // given
        String dietId = "non-existing-diet-id";

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo(eq("dietId"), eq(dietId))).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.isEmpty()).thenReturn(true);

        // when
        Optional<ShoppingList> result = shoppingListRepository.findByDietId(dietId);

        // then
        assertThat(result).isEmpty();
        verify(firestore).collection("shopping_lists");
        verify(query).get();
    }

    @Test
    void findById_WhenShoppingListExists_ShouldReturnShoppingList() throws ExecutionException, InterruptedException {
        // given
        String id = "test-id";
        ShoppingList expectedShoppingList = createSampleShoppingList();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(id)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(firestoreShoppingMapper.toShoppingList(documentSnapshot)).thenReturn(expectedShoppingList);

        // when
        Optional<ShoppingList> result = shoppingListRepository.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedShoppingList);
        verify(firestore).collection("shopping_lists");
        verify(documentReference).get();
    }

    @Test
    void save_WhenNewShoppingList_ShouldSaveSuccessfully() throws ExecutionException, InterruptedException {
        // given
        ShoppingList shoppingList = createSampleShoppingList();
        shoppingList.setId(null);
        ShoppingList expectedShoppingList = createSampleShoppingList(); // zakładamy, że będzie zwrócony
        Map<String, Object> firestoreMap = new HashMap<>();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("new-id");
        when(firestoreShoppingMapper.toFirestoreMap(any(ShoppingList.class))).thenReturn(firestoreMap);

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
        when(documentReference.set(firestoreMap)).thenReturn(writeResultFuture);

        // Dodanie mockowania dla get() po zapisie
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreShoppingMapper.toShoppingList(documentSnapshot)).thenReturn(expectedShoppingList);

        // when
        ShoppingList result = shoppingListRepository.save(shoppingList);

        // then
        assertThat(result).isEqualTo(expectedShoppingList);
        verify(firestore).collection("shopping_lists");
        verify(documentReference).set(firestoreMap);
        assertThat(shoppingList.getId()).isEqualTo("new-id");
    }

    @Test
    void save_WhenUpdateExistingShoppingList_ShouldUpdateSuccessfully() throws ExecutionException, InterruptedException {
        // given
        ShoppingList shoppingList = createSampleShoppingList();
        ShoppingList expectedShoppingList = createSampleShoppingList(); // zakładamy, że będzie zwrócony
        Map<String, Object> firestoreMap = new HashMap<>();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(shoppingList.getId())).thenReturn(documentReference);
        when(firestoreShoppingMapper.toFirestoreMap(shoppingList)).thenReturn(firestoreMap);

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
        when(documentReference.set(firestoreMap)).thenReturn(writeResultFuture);

        // Dodanie mockowania dla get() po zapisie
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(firestoreShoppingMapper.toShoppingList(documentSnapshot)).thenReturn(expectedShoppingList);

        // when
        ShoppingList result = shoppingListRepository.save(shoppingList);

        // then
        assertThat(result).isEqualTo(expectedShoppingList);
        verify(firestore).collection("shopping_lists");
        verify(documentReference).set(firestoreMap);
    }

    @Test
    void save_WhenExceptionOccurs_ShouldThrowRuntimeException() throws ExecutionException, InterruptedException {
        // given
        ShoppingList shoppingList = createSampleShoppingList();
        Map<String, Object> firestoreMap = new HashMap<>();

        when(firestore.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(shoppingList.getId())).thenReturn(documentReference);
        when(firestoreShoppingMapper.toFirestoreMap(shoppingList)).thenReturn(firestoreMap);

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
        when(documentReference.set(firestoreMap)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // when, then
        assertThrows(RuntimeException.class, () -> shoppingListRepository.save(shoppingList));
    }

    private ShoppingList createSampleShoppingList() {
        return ShoppingList.builder()
                .id("test-id")
                .dietId("test-diet-id")
                .userId("test-user-id")
                .items(new HashMap<>())
                .createdAt(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .startDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .endDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .version(1)
                .build();
    }
}