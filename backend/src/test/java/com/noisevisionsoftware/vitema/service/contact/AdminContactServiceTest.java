package com.noisevisionsoftware.vitema.service.contact;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.model.ContactMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminContactServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotApiFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotApiFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultApiFuture;

    @InjectMocks
    private AdminContactService adminContactService;

    private ContactMessage sampleMessage;

    @BeforeEach
    void setUp() {
        // Setup sample contact message
        sampleMessage = new ContactMessage();
        sampleMessage.setId("test-id");
        sampleMessage.setName("Test User");
        sampleMessage.setEmail("test@example.com");
        sampleMessage.setMessage("Test message content");
        sampleMessage.setStatus("NEW");

        // Mock Firestore Collection
        when(firestore.collection("contact_messages")).thenReturn(collectionReference);
    }

    @Test
    void getAllContactMessages_ShouldReturnListOfMessages() throws ExecutionException, InterruptedException {
        // Setup mock documents
        List<QueryDocumentSnapshot> mockDocuments = new ArrayList<>();
        QueryDocumentSnapshot mockDocument1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot mockDocument2 = mock(QueryDocumentSnapshot.class);

        ContactMessage message1 = new ContactMessage();
        message1.setName("User 1");
        message1.setEmail("user1@example.com");
        message1.setMessage("Message 1");
        message1.setStatus("NEW");

        ContactMessage message2 = new ContactMessage();
        message2.setName("User 2");
        message2.setEmail("user2@example.com");
        message2.setMessage("Message 2");
        message2.setStatus("RESOLVED");

        when(mockDocument1.getId()).thenReturn("msg-id-1");
        when(mockDocument1.toObject(ContactMessage.class)).thenReturn(message1);

        when(mockDocument2.getId()).thenReturn("msg-id-2");
        when(mockDocument2.toObject(ContactMessage.class)).thenReturn(message2);

        mockDocuments.add(mockDocument1);
        mockDocuments.add(mockDocument2);

        // Chain mocks
        when(collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotApiFuture);
        when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(mockDocuments);

        // Execute the service method
        List<ContactMessage> result = adminContactService.getAllContactMessages();

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("msg-id-1", result.get(0).getId());
        assertEquals("User 1", result.get(0).getName());
        assertEquals("msg-id-2", result.get(1).getId());
        assertEquals("User 2", result.get(1).getName());

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).orderBy("createdAt", Query.Direction.DESCENDING);
        verify(querySnapshotApiFuture).get();
        verify(querySnapshot).getDocuments();
    }

    @Test
    void getContactMessage_WhenMessageExists_ShouldReturnMessage() throws ExecutionException, InterruptedException {
        // Setup mocks
        when(collectionReference.document("test-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("test-id");
        when(documentSnapshot.toObject(ContactMessage.class)).thenReturn(sampleMessage);

        // Execute the service method
        ContactMessage result = adminContactService.getContactMessage("test-id");

        // Verify results
        assertNotNull(result);
        assertEquals("test-id", result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document("test-id");
        verify(documentSnapshotApiFuture).get();
        verify(documentSnapshot).exists();
        verify(documentSnapshot).toObject(ContactMessage.class);
    }

    @Test
    void getContactMessage_WhenMessageDoesNotExist_ShouldReturnNull() throws ExecutionException, InterruptedException {
        // Setup mocks
        when(collectionReference.document("non-existent-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Execute the service method
        ContactMessage result = adminContactService.getContactMessage("non-existent-id");

        // Verify results
        assertNull(result);

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document("non-existent-id");
        verify(documentSnapshotApiFuture).get();
        verify(documentSnapshot).exists();
        verify(documentSnapshot, never()).toObject(any());
    }

    @Test
    void getContactMessage_WhenDocumentExistsButCannotBeConverted_ShouldReturnNull() throws ExecutionException, InterruptedException {
        // Setup mocks
        when(collectionReference.document("test-id")).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotApiFuture);
        when(documentSnapshotApiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(ContactMessage.class)).thenReturn(null);

        // Execute the service method
        ContactMessage result = adminContactService.getContactMessage("test-id");

        // Verify results
        assertNull(result);

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document("test-id");
        verify(documentSnapshotApiFuture).get();
        verify(documentSnapshot).exists();
        verify(documentSnapshot).toObject(ContactMessage.class);
    }

    @Test
    void updateMessageStatus_ShouldUpdateStatusInFirestore() throws ExecutionException, InterruptedException {
        // Setup mocks
        when(collectionReference.document("test-id")).thenReturn(documentReference);
        when(documentReference.update("status", "RESOLVED")).thenReturn(writeResultApiFuture);
        WriteResult mockWriteResult = mock(WriteResult.class);
        when(writeResultApiFuture.get()).thenReturn(mockWriteResult);

        // Execute the service method
        adminContactService.updateMessageStatus("test-id", "RESOLVED");

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document("test-id");
        verify(documentReference).update("status", "RESOLVED");
        verify(writeResultApiFuture).get();
    }

    @Test
    void updateMessageStatus_WhenFirestoreThrowsException_ShouldPropagateException() throws ExecutionException, InterruptedException {
        // Setup mocks to throw exception
        when(collectionReference.document("test-id")).thenReturn(documentReference);
        when(documentReference.update(anyString(), anyString())).thenReturn(writeResultApiFuture);
        when(writeResultApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // Execute the service method and expect exception
        assertThrows(InterruptedException.class, () ->
                adminContactService.updateMessageStatus("test-id", "RESOLVED")
        );

        // Verify interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document("test-id");
        verify(documentReference).update("status", "RESOLVED");
        verify(writeResultApiFuture).get();
    }
}