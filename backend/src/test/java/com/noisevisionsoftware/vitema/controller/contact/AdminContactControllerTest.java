package com.noisevisionsoftware.vitema.controller.contact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.exception.AdminControllerExceptionHandler;
import com.noisevisionsoftware.vitema.model.ContactMessage;
import com.noisevisionsoftware.vitema.service.contact.AdminContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminContactService adminContactService;

    @InjectMocks
    private AdminContactController adminContactController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<ContactMessage> sampleMessages;
    private ContactMessage sampleMessage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminContactController)
                .setControllerAdvice(new AdminControllerExceptionHandler())
                .build();

        // Create sample messages for testing
        sampleMessages = new ArrayList<>();

        // First message
        sampleMessage = new ContactMessage();
        sampleMessage.setId("test-id-1");
        sampleMessage.setName("John Doe");
        sampleMessage.setEmail("john@example.com");
        sampleMessage.setPhone("+48123456789");
        sampleMessage.setMessage("Test message 1");
        sampleMessage.setStatus("NEW");
        sampleMessages.add(sampleMessage);

        // Second message
        ContactMessage message2 = new ContactMessage();
        message2.setId("test-id-2");
        message2.setName("Jane Smith");
        message2.setEmail("jane@example.com");
        message2.setPhone("+48987654321");
        message2.setMessage("Test message 2");
        message2.setStatus("RESOLVED");
        sampleMessages.add(message2);
    }

    @Test
    void getAllMessages_ShouldReturnListOfMessages() throws Exception {
        // Mock service to return sample messages
        when(adminContactService.getAllContactMessages()).thenReturn(sampleMessages);

        // Execute request and verify response
        mockMvc.perform(get("/api/admin/contact/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("test-id-1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].id").value("test-id-2"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].status").value("RESOLVED"));

        // Verify service was called
        verify(adminContactService).getAllContactMessages();
    }

    @Test
    void getAllMessages_WhenServiceThrowsException_ShouldPropagateException() throws Exception {
        // Mock service to throw exception
        when(adminContactService.getAllContactMessages())
                .thenThrow(new ExecutionException(new RuntimeException("Test exception")));

        // Execute request and verify response
        mockMvc.perform(get("/api/admin/contact/messages"))
                .andExpect(status().isInternalServerError());

        // Verify service was called
        verify(adminContactService).getAllContactMessages();
    }

    @Test
    void getMessage_WhenMessageExists_ShouldReturnMessage() throws Exception {
        // Mock service to return a single message
        when(adminContactService.getContactMessage("test-id-1")).thenReturn(sampleMessage);

        // Execute request and verify response
        mockMvc.perform(get("/api/admin/contact/messages/test-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id-1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.message").value("Test message 1"))
                .andExpect(jsonPath("$.status").value("NEW"));

        // Verify service was called
        verify(adminContactService).getContactMessage("test-id-1");
    }

    @Test
    void getMessage_WhenMessageDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Mock service to return null (message not found)
        when(adminContactService.getContactMessage("non-existent-id")).thenReturn(null);

        // Execute request and verify response
        mockMvc.perform(get("/api/admin/contact/messages/non-existent-id"))
                .andExpect(status().isNotFound());

        // Verify service was called
        verify(adminContactService).getContactMessage("non-existent-id");
    }

    @Test
    void getMessage_WhenServiceThrowsException_ShouldPropagateException() throws Exception {
        // Mock service to throw exception
        when(adminContactService.getContactMessage("test-id-1"))
                .thenThrow(new ExecutionException(new RuntimeException("Test exception")));

        // Execute request and verify response
        mockMvc.perform(get("/api/admin/contact/messages/test-id-1"))
                .andExpect(status().isInternalServerError());

        // Verify service was called
        verify(adminContactService).getContactMessage("test-id-1");
    }

    @Test
    void updateMessageStatus_WithValidStatus_ShouldUpdateStatus() throws Exception {
        // Prepare status update request
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "RESOLVED");

        // Execute request and verify response
        mockMvc.perform(put("/api/admin/contact/messages/test-id-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Verify service was called
        verify(adminContactService).updateMessageStatus("test-id-1", "RESOLVED");
    }

    @Test
    void updateMessageStatus_WithMissingStatus_ShouldReturnBadRequest() throws Exception {
        // Prepare invalid request (missing status)
        Map<String, String> requestBody = new HashMap<>();
        // No status provided

        // Execute request and verify response
        mockMvc.perform(put("/api/admin/contact/messages/test-id-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status jest wymagany"));

        // Verify service was not called
        verifyNoInteractions(adminContactService);
    }

    @Test
    void updateMessageStatus_WithEmptyStatus_ShouldReturnBadRequest() throws Exception {
        // Prepare invalid request (empty status)
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "");

        // Execute request and verify response
        mockMvc.perform(put("/api/admin/contact/messages/test-id-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status jest wymagany"));

        // Verify service was not called
        verifyNoInteractions(adminContactService);
    }

    @Test
    void updateMessageStatus_WhenServiceThrowsException_ShouldPropagateException() throws Exception {
        // Prepare status update request
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "RESOLVED");

        // Mock service to throw exception
        doThrow(new ExecutionException(new RuntimeException("Test exception")))
                .when(adminContactService).updateMessageStatus("test-id-1", "RESOLVED");

        // Execute request and verify response
        mockMvc.perform(put("/api/admin/contact/messages/test-id-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());

        // Verify service was called
        verify(adminContactService).updateMessageStatus("test-id-1", "RESOLVED");
    }
}