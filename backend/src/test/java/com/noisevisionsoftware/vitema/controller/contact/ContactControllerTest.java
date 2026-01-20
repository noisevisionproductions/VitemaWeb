package com.noisevisionsoftware.vitema.controller.contact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.dto.request.ContactFormRequest;
import com.noisevisionsoftware.vitema.service.contact.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(contactController)
                .build();
    }

    @Test
    void submitContactForm_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Prepare valid contact form request
        ContactFormRequest request = new ContactFormRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPhone("+48123456789");
        request.setMessage("This is a test message");

        // Execute request and verify response
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Twoja wiadomość została wysłana. Skontaktujemy się wkrótce."));

        // Verify service was called
        verify(contactService).processContactForm(any(ContactFormRequest.class));
    }

    @Test
    void submitContactForm_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Prepare valid contact form request
        ContactFormRequest request = new ContactFormRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPhone("+48123456789");
        request.setMessage("This is a test message");

        // Mock service to throw exception
        doThrow(new ExecutionException(new RuntimeException("Test exception")))
                .when(contactService).processContactForm(any(ContactFormRequest.class));

        // Execute request and verify response
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Wystąpił błąd podczas przetwarzania wiadomości."));

        // Verify service was called
        verify(contactService).processContactForm(any(ContactFormRequest.class));
    }

    @Test
    void submitContactForm_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Prepare invalid contact form request (missing required fields)
        ContactFormRequest request = new ContactFormRequest();
        // Missing name, email and message which should be required

        // Execute request and verify response
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify service was not called
        verifyNoInteractions(contactService);
    }

    @Test
    void submitContactForm_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Prepare invalid contact form request (invalid email)
        ContactFormRequest request = new ContactFormRequest();
        request.setName("Test User");
        request.setEmail("invalid-email"); // Invalid email format
        request.setMessage("This is a test message");

        // Execute request and verify response
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify service was not called
        verifyNoInteractions(contactService);
    }

    @Test
    void submitContactForm_WithInterruptedException_ShouldReturnInternalServerError() throws Exception {
        // Prepare valid contact form request
        ContactFormRequest request = new ContactFormRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setMessage("This is a test message");

        // Mock service to throw interrupted exception
        doThrow(new InterruptedException("Test interrupted"))
                .when(contactService).processContactForm(any(ContactFormRequest.class));

        // Execute request and verify response
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Wystąpił błąd podczas przetwarzania wiadomości."));

        // Verify service was called
        verify(contactService).processContactForm(any(ContactFormRequest.class));
    }
}