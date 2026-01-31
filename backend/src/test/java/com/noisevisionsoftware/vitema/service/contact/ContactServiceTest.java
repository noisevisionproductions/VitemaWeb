package com.noisevisionsoftware.vitema.service.contact;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.noisevisionsoftware.vitema.dto.request.ContactFormRequest;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private EmailService emailService;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeResultApiFuture;

    @Mock
    private WriteResult writeResult;

    @InjectMocks
    private ContactService contactService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> firestoreDataCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> adminEmailVariablesCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> userEmailVariablesCaptor;

    private ContactFormRequest sampleRequest;
    private final String adminEmail = "admin@example.com";
    private final String frontendUrl = "https://vitema.example.com";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Set values for @Value-annotated fields using ReflectionTestUtils
        ReflectionTestUtils.setField(contactService, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(contactService, "frontendUrl", frontendUrl);

        // Setup sample contact form request
        sampleRequest = new ContactFormRequest();
        sampleRequest.setName("John Doe");
        sampleRequest.setEmail("john.doe@example.com");
        sampleRequest.setPhone("+48123456789");
        sampleRequest.setMessage("This is a test message from John Doe.");

        // Mock Firestore behavior
        when(firestore.collection("contact_messages")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.set(any(Map.class))).thenReturn(writeResultApiFuture);
        try {
            when(writeResultApiFuture.get()).thenReturn(writeResult);
        } catch (Exception e) {
            fail("Exception during mock setup: " + e.getMessage());
        }
    }

    @Test
    void processContactForm_ShouldSaveToFirestoreAndSendEmails() throws ExecutionException, InterruptedException {
        // Current date for verification purposes
        Timestamp beforeTest = Timestamp.now();

        // Execute the service method
        contactService.processContactForm(sampleRequest);

        // Verify Firestore interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document();
        verify(documentReference).set(firestoreDataCaptor.capture());
        verify(writeResultApiFuture).get();

        // Verify captured Firestore data
        Map<String, Object> savedData = firestoreDataCaptor.getValue();
        assertEquals("John Doe", savedData.get("name"));
        assertEquals("john.doe@example.com", savedData.get("email"));
        assertEquals("+48123456789", savedData.get("phone"));
        assertEquals("This is a test message from John Doe.", savedData.get("message"));
        assertEquals("NEW", savedData.get("status"));
        assertTrue(savedData.containsKey("createdAt"));

        // Verify timestamp is recent (created during the test)
        Timestamp createdAt = (Timestamp) savedData.get("createdAt");
        Timestamp afterTest = Timestamp.now();
        assertTrue(createdAt.compareTo(beforeTest) >= 0);
        assertTrue(createdAt.compareTo(afterTest) <= 0);

        // Verify admin email
        verify(emailService).sendTemplatedEmail(
                eq(adminEmail),
                eq("Nowa wiadomość kontaktowa: John Doe"),
                eq("contact-notification"),
                adminEmailVariablesCaptor.capture()
        );

        // Verify admin email variables
        Map<String, Object> adminVariables = adminEmailVariablesCaptor.getValue();
        assertEquals("John Doe", adminVariables.get("name"));
        assertEquals("john.doe@example.com", adminVariables.get("email"));
        assertEquals("+48123456789", adminVariables.get("phone"));
        assertEquals("This is a test message from John Doe.", adminVariables.get("message"));
        assertEquals(false, adminVariables.get("showUnsubscribe"));

        // Verify user confirmation email
        verify(emailService).sendTemplatedEmail(
                eq("john.doe@example.com"),
                eq("Dziękujemy za kontakt - Vitema"),
                eq("contact-confirmation"),
                userEmailVariablesCaptor.capture()
        );

        // Verify user email variables
        Map<String, Object> userVariables = userEmailVariablesCaptor.getValue();
        assertEquals("John Doe", userVariables.get("name"));
        assertEquals(false, userVariables.get("showUnsubscribe"));
        assertEquals("https://vitema.example.com/newsletter", userVariables.get("newsletterUrl"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void processContactForm_WhenFirestoreThrowsException_ShouldPropagateException() throws ExecutionException, InterruptedException {
        // Setup mock to throw exception
        when(writeResultApiFuture.get()).thenThrow(new InterruptedException("Test exception"));

        // Execute the method and expect exception
        assertThrows(InterruptedException.class, () ->
                contactService.processContactForm(sampleRequest)
        );

        // Verify Firestore interactions
        verify(firestore).collection("contact_messages");
        verify(collectionReference).document();
        verify(documentReference).set(any(Map.class));

        // Verify no emails were sent
        verifyNoInteractions(emailService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void processContactForm_WithMinimalData_ShouldProcessSuccessfully() throws ExecutionException, InterruptedException {
        // Create request with minimal required data
        ContactFormRequest minimalRequest = new ContactFormRequest();
        minimalRequest.setName("Jane Smith");
        minimalRequest.setEmail("jane.smith@example.com");
        minimalRequest.setMessage("Minimal message");
        // Note: Phone is optional and not set

        // Execute the service method
        contactService.processContactForm(minimalRequest);

        // Verify Firestore interactions
        verify(firestore).collection("contact_messages");
        verify(documentReference).set(firestoreDataCaptor.capture());

        // Verify saved data
        Map<String, Object> savedData = firestoreDataCaptor.getValue();
        assertEquals("Jane Smith", savedData.get("name"));
        assertEquals("jane.smith@example.com", savedData.get("email"));
        assertNull(savedData.get("phone")); // Phone should be null
        assertEquals("Minimal message", savedData.get("message"));

        // Verify emails were sent
        verify(emailService, times(2)).sendTemplatedEmail(
                anyString(), anyString(), anyString(), any(Map.class)
        );
    }

    @Test
    void processContactForm_WithMaximalData_ShouldProcessSuccessfully() throws ExecutionException, InterruptedException {
        ContactFormRequest maxRequest = getContactFormRequest();

        // Execute the service method
        contactService.processContactForm(maxRequest);

        // Verify Firestore interactions
        verify(firestore).collection("contact_messages");
        verify(documentReference).set(firestoreDataCaptor.capture());

        // Verify saved data - focusing on length of message
        Map<String, Object> savedData = firestoreDataCaptor.getValue();
        assertEquals("Max User", savedData.get("name"));
        String savedMessage = (String) savedData.get("message");
        assertTrue(savedMessage.length() > 300); // Checking it's a long message
        assertTrue(savedMessage.contains("Łódź")); // Checking special characters are preserved

        // Verify emails were sent with the correct data
        verify(emailService).sendTemplatedEmail(
                eq(adminEmail),
                eq("Nowa wiadomość kontaktowa: Max User"),
                eq("contact-notification"),
                adminEmailVariablesCaptor.capture()
        );

        Map<String, Object> adminVariables = adminEmailVariablesCaptor.getValue();
        assertEquals(maxRequest.getMessage(), adminVariables.get("message"));
    }

    private static ContactFormRequest getContactFormRequest() {
        ContactFormRequest maxRequest = new ContactFormRequest();
        maxRequest.setName("Max User");
        maxRequest.setEmail("max.user@example.com");
        maxRequest.setPhone("+48987654321");
        maxRequest.setMessage("This is a very long message that includes special characters and Unicode: " +
                "Łódź, śnieg, źródło, żaba - Polish characters. " +
                "Here are some emojis: 😊🚀🔥. " +
                "And here's a long paragraph of text to make sure the system handles large messages properly. " +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        return maxRequest;
    }

    @Test
    void processContactForm_ShouldCreateTimestampWhenProcessing() throws ExecutionException, InterruptedException {
        // Execute the service method
        contactService.processContactForm(sampleRequest);

        // Capture the saved data
        verify(documentReference).set(firestoreDataCaptor.capture());
        Map<String, Object> savedData = firestoreDataCaptor.getValue();

        // Verify timestamp is created and is a Timestamp object
        assertTrue(savedData.containsKey("createdAt"));
        assertInstanceOf(Timestamp.class, savedData.get("createdAt"));
    }

    // Current Date and Time (UTC): 2025-04-03 15:48:51
    // Current User's AuthPage: noisevisionproductions
    @Test
    void processContactForm_ShouldUseCurrentTimeForTimestamp() throws ExecutionException, InterruptedException {
        // Note: Exact timestamp verification is challenging due to execution time differences
        // We'll verify that the timestamp is created within a reasonable time window

        // Get a timestamp before the test
        Timestamp before = Timestamp.now();

        // Execute the service method
        contactService.processContactForm(sampleRequest);

        // Get a timestamp after the test
        Timestamp after = Timestamp.now();

        // Capture the saved data
        verify(documentReference).set(firestoreDataCaptor.capture());
        Map<String, Object> savedData = firestoreDataCaptor.getValue();
        Timestamp createdAt = (Timestamp) savedData.get("createdAt");

        // Verify timestamp is between before and after
        assertTrue(createdAt.compareTo(before) >= 0, "Timestamp should be after or equal to the 'before' timestamp");
        assertTrue(createdAt.compareTo(after) <= 0, "Timestamp should be before or equal to the 'after' timestamp");
    }

    @Test
    void processContactForm_ShouldFormatUserConfirmationEmailCorrectly() throws ExecutionException, InterruptedException {
        // Execute the service method
        contactService.processContactForm(sampleRequest);

        // Verify user confirmation email
        verify(emailService).sendTemplatedEmail(
                eq(sampleRequest.getEmail()),
                eq("Dziękujemy za kontakt - Vitema"),
                eq("contact-confirmation"),
                userEmailVariablesCaptor.capture()
        );

        // Verify newsletter URL is formatted correctly
        Map<String, Object> userVariables = userEmailVariablesCaptor.getValue();
        assertEquals(frontendUrl + "/newsletter", userVariables.get("newsletterUrl"));
    }
}