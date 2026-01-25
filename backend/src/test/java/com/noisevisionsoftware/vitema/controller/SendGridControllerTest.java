package com.noisevisionsoftware.vitema.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.EmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SavedTemplateRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SingleEmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.TargetedEmailRequest;
import com.noisevisionsoftware.vitema.model.newsletter.EmailTemplate;
import com.noisevisionsoftware.vitema.service.email.EmailTemplateService;
import com.noisevisionsoftware.vitema.service.email.AdminEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SendGridControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminEmailService sendGridService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private SendGridController sendGridController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sendGridController)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void sendSingleEmail_ShouldReturnOk() throws Exception {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setRecipientEmail("test@example.com");
        request.setRecipientName("Test User");
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setExternalRecipientId(123L);
        request.setTemplateType("basic");
        request.setUseTemplate(true);
        request.setCategories(List.of("test", "demo"));

        doNothing().when(sendGridService).sendSingleEmail(any(SingleEmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Wiadomość została wysłana do: test@example.com")))
                .andExpect(jsonPath("$.message", containsString("123")))
                .andExpect(jsonPath("$.success", is(true)));

        verify(sendGridService).sendSingleEmail(any(SingleEmailRequest.class));
    }

    @Test
    void sendSingleEmail_ShouldHandleExceptionAndReturnInternalServerError() throws Exception {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setRecipientEmail("test@example.com");
        request.setSubject("Test Subject");
        request.setContent("Test Content");

        doThrow(new RuntimeException("Test error")).when(sendGridService).sendSingleEmail(any(SingleEmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Wystąpił błąd")))
                .andExpect(jsonPath("$.message", containsString("Test error")))
                .andExpect(jsonPath("$.success", is(false)));

        verify(sendGridService).sendSingleEmail(any(SingleEmailRequest.class));
    }

    @Test
    void sendBulkEmail_ShouldReturnOk() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setTemplateType("promotional");
        request.setUseTemplate(true);
        request.setRecipients(List.of("user1@example.com", "user2@example.com"));
        request.setCategories(List.of("newsletter", "promo"));

        doNothing().when(sendGridService).sendBulkEmail(any(EmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Wiadomość została wysłana do wszystkich aktywnych i zweryfikowanych subskrybentów")));

        verify(sendGridService).sendBulkEmail(any(EmailRequest.class));
    }

    @Test
    void sendBulkEmail_ShouldHandleExceptionAndReturnInternalServerError() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setTemplateType("promotional");

        doThrow(new RuntimeException("Test error")).when(sendGridService).sendBulkEmail(any(EmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Wystąpił błąd")))
                .andExpect(jsonPath("$.message", containsString("Test error")));

        verify(sendGridService).sendBulkEmail(any(EmailRequest.class));
    }

    @Test
    void sendTargetedBulkEmail_ShouldReturnOk() throws Exception {
        TargetedEmailRequest request = getTargetedEmailRequest();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sent", 10);
        resultMap.put("message", "Wysłano wiadomości do 10 subskrybentów");

        when(sendGridService.sendTargetedBulkEmail(any(TargetedEmailRequest.class))).thenReturn(resultMap);

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/bulk-targeted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent", is(10)))
                .andExpect(jsonPath("$.message", containsString("Wysłano wiadomości do 10 subskrybentów")));

        verify(sendGridService).sendTargetedBulkEmail(any(TargetedEmailRequest.class));
    }

    private static TargetedEmailRequest getTargetedEmailRequest() {
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setTemplateType("promotional");
        request.setRecipientType("subscribers");
        request.setSubscriberFilters(Map.of("tags", List.of("news")));
        request.setExternalRecipientIds(List.of("123", "456"));
        request.setCategories(List.of("newsletter", "targeted"));
        request.setUpdateStatus(true);
        request.setNewStatus("processed");
        return request;
    }

    @Test
    void sendEmail_ShouldReturnOk() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setTemplateType("basic");
        request.setUseTemplate(true);
        request.setCategories(List.of("direct", "test"));

        doNothing().when(sendGridService).sendEmail(anyString(), any(EmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/send/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Wiadomość została wysłana do: test@example.com")));

        verify(sendGridService).sendEmail(eq("test@example.com"), any(EmailRequest.class));
    }

    @Test
    void getTemplates_ShouldReturnListOfTemplates() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/email/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templates", hasSize(4)))
                .andExpect(jsonPath("$.templates[0].id", is("basic")))
                .andExpect(jsonPath("$.templates[1].id", is("promotional")))
                .andExpect(jsonPath("$.templates[2].id", is("survey")))
                .andExpect(jsonPath("$.templates[3].id", is("announcement")));
    }

    @Test
    void previewEmail_ShouldReturnPreviewContent() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setTemplateType("basic");
        request.setUseTemplate(true);

        String previewContent = "<html><body>Preview content</body></html>";
        when(sendGridService.renderEmailPreview(any(EmailRequest.class))).thenReturn(previewContent);

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preview", is(previewContent)));

        verify(sendGridService).renderEmailPreview(any(EmailRequest.class));
    }

    @Test
    void getSavedTemplates_ShouldReturnListOfSavedTemplates() throws Exception {
        // Arrange
        EmailTemplate template1 = createEmailTemplate(1L, "Template 1");
        EmailTemplate template2 = createEmailTemplate(2L, "Template 2");

        when(emailTemplateService.getAllTemplates()).thenReturn(Arrays.asList(template1, template2));

        // Act & Assert
        mockMvc.perform(get("/api/admin/email/saved-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Template 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Template 2")));

        verify(emailTemplateService).getAllTemplates();
    }

    @Test
    void saveTemplate_ShouldReturnSavedTemplate() throws Exception {
        // Arrange
        SavedTemplateRequest request = new SavedTemplateRequest();
        request.setName("New Template");
        request.setSubject("New Subject");
        request.setContent("New Content");
        request.setTemplateType("promotional");
        request.setUseTemplate(true);

        EmailTemplate savedTemplate = createEmailTemplate(1L, "New Template");
        when(emailTemplateService.saveTemplate(any(SavedTemplateRequest.class))).thenReturn(savedTemplate);

        // Act & Assert
        mockMvc.perform(post("/api/admin/email/saved-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Template")));

        verify(emailTemplateService).saveTemplate(any(SavedTemplateRequest.class));
    }

    @Test
    void getTemplateById_ShouldReturnTemplate_WhenExists() throws Exception {
        // Arrange
        EmailTemplate template = createEmailTemplate(1L, "Template 1");
        when(emailTemplateService.getTemplateById(1L)).thenReturn(Optional.of(template));

        // Act & Assert
        mockMvc.perform(get("/api/admin/email/saved-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Template 1")));

        verify(emailTemplateService).getTemplateById(1L);
    }

    @Test
    void getTemplateById_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        // Arrange
        when(emailTemplateService.getTemplateById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/admin/email/saved-templates/999"))
                .andExpect(status().isNotFound());

        verify(emailTemplateService).getTemplateById(999L);
    }

    @Test
    void deleteTemplate_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(emailTemplateService).deleteTemplate(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/admin/email/saved-templates/1"))
                .andExpect(status().isOk());

        verify(emailTemplateService).deleteTemplate(1L);
    }

    private EmailTemplate createEmailTemplate(Long id, String name) {
        EmailTemplate template = new EmailTemplate();
        template.setId(id);
        template.setName(name);
        template.setSubject("Subject for " + name);
        template.setContent("Content for " + name);
        template.setDescription("Description for " + name);
        template.setUseTemplate(true);
        template.setTemplateType("promotional");
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return template;
    }
}