package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.EmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SingleEmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.TargetedEmailRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.ExternalRecipientRepository;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.newsletter.AdminNewsletterService;
import com.noisevisionsoftware.vitema.service.email.newsletter.ExternalRecipientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEmailServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NewsletterSubscriberRepository subscriberRepository;

    @Mock
    private ExternalRecipientRepository externalRecipientRepository;

    @Mock
    private ExternalRecipientsService externalRecipientsService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private AdminNewsletterService adminNewsletterService;

    @InjectMocks
    private AdminEmailService adminEmailService;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    @Captor
    private ArgumentCaptor<String> contentCaptor;

    private ExternalRecipient testExternalRecipient;
    private List<NewsletterSubscriber> activeSubscribers;

    @BeforeEach
    void setUp() {
        NewsletterSubscriber testSubscriber = NewsletterSubscriber.builder()
                .id(1L)
                .email("subscriber@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now().minusDays(10))
                .verified(true)
                .active(true)
                .verificationToken("test-token")
                .metadataEntries(new HashSet<>())
                .build();

        testExternalRecipient = ExternalRecipient.builder()
                .id(1L)
                .email("external@example.com")
                .name("External Recipient")
                .category("Dietetyk")
                .status("active")
                .createdAt(LocalDateTime.now().minusDays(5))
                .tags(new HashSet<>())
                .build();

        activeSubscribers = Arrays.asList(
                testSubscriber,
                NewsletterSubscriber.builder()
                        .id(2L)
                        .email("active2@example.com")
                        .role(SubscriberRole.COMPANY)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .verified(true)
                        .active(true)
                        .metadataEntries(new HashSet<>())
                        .build()
        );
    }

    // ========== sendSingleEmail Tests ==========

    @Test
    void sendSingleEmail_ShouldSendEmailWithPlainContent() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setRecipientEmail("recipient@example.com");
        request.setUseTemplate(false);
        request.setSavedTemplateId(null);
        request.setUpdateLastContactDate(false);

        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(emailService).sendCustomEmail(
                eq("recipient@example.com"),
                eq("Test Subject"),
                eq("Test Content")
        );
        verify(externalRecipientRepository, never()).findById(any());
    }

    @Test
    void sendSingleEmail_ShouldUseSavedTemplate_WhenTemplateIdProvided() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Custom Content");
        request.setRecipientEmail("recipient@example.com");
        request.setSavedTemplateId(1L);
        request.setUseTemplate(false);

        when(emailTemplateService.renderSavedTemplate(eq(1L), eq("Custom Content")))
                .thenReturn("Rendered Template Content");

        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(emailTemplateService).renderSavedTemplate(1L, "Custom Content");
        verify(emailService).sendCustomEmail(
                eq("recipient@example.com"),
                eq("Test Subject"),
                eq("Rendered Template Content")
        );
    }

    @Test
    void sendSingleEmail_ShouldUseSystemTemplate_WhenUseTemplateIsTrue() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setRecipientEmail("recipient@example.com");
        request.setUseTemplate(true);
        request.setTemplateType("promotional");
        request.setSavedTemplateId(null);

        when(emailTemplateService.applySystemTemplate(eq("Test Content"), eq("promotional")))
                .thenReturn("System Template Content");

        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(emailTemplateService).applySystemTemplate("Test Content", "promotional");
        verify(emailService).sendCustomEmail(
                eq("recipient@example.com"),
                eq("Test Subject"),
                eq("System Template Content")
        );
    }

    @Test
    void sendSingleEmail_ShouldUpdateLastContactDate_WhenExternalRecipientIdProvided() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setRecipientEmail("recipient@example.com");
        request.setExternalRecipientId(1L);
        request.setUpdateLastContactDate(true);
        request.setUseTemplate(false);

        when(externalRecipientRepository.findById(1L)).thenReturn(Optional.of(testExternalRecipient));
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenReturn(testExternalRecipient);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(externalRecipientRepository).findById(1L);
        verify(externalRecipientRepository).save(any(ExternalRecipient.class));
        ArgumentCaptor<ExternalRecipient> recipientCaptor = ArgumentCaptor.forClass(ExternalRecipient.class);
        verify(externalRecipientRepository).save(recipientCaptor.capture());
        assertNotNull(recipientCaptor.getValue().getLastContactDate());
    }

    @Test
    void sendSingleEmail_ShouldNotUpdateLastContactDate_WhenUpdateFlagIsFalse() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setRecipientEmail("recipient@example.com");
        request.setExternalRecipientId(1L);
        request.setUpdateLastContactDate(false);

        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(externalRecipientRepository, never()).findById(any());
    }

    @Test
    void sendSingleEmail_ShouldNotUpdateLastContactDate_WhenExternalRecipientNotFound() {
        // Arrange
        SingleEmailRequest request = new SingleEmailRequest();
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setRecipientEmail("recipient@example.com");
        request.setExternalRecipientId(999L);
        request.setUpdateLastContactDate(true);

        when(externalRecipientRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendSingleEmail(request);

        // Assert
        verify(externalRecipientRepository).findById(999L);
        verify(externalRecipientRepository, never()).save(any());
    }

    // ========== sendBulkEmail Tests ==========

    @Test
    void sendBulkEmail_ShouldSendToAllActiveVerifiedSubscribers() {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Bulk Subject");
        emailRequest.setContent("Bulk Content");
        emailRequest.setUseTemplate(false);

        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        adminEmailService.sendBulkEmail(emailRequest);

        // Assert
        verify(subscriberRepository).findAllByActiveTrueAndVerifiedTrue();
        verify(emailService, times(2)).sendCustomEmail(anyString(), eq("Bulk Subject"), eq("Bulk Content"));
        verify(adminNewsletterService, times(2)).updateLastEmailSent(anyString());
    }

    @Test
    void sendBulkEmail_ShouldReturnEarly_WhenNoRecipientsFound() {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Bulk Subject");
        emailRequest.setContent("Bulk Content");

        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(Collections.emptyList());

        // Act
        adminEmailService.sendBulkEmail(emailRequest);

        // Assert
        verify(subscriberRepository).findAllByActiveTrueAndVerifiedTrue();
        verify(emailService, never()).sendCustomEmail(anyString(), anyString(), anyString());
        verify(adminNewsletterService, never()).updateLastEmailSent(anyString());
    }

    @Test
    void sendBulkEmail_ShouldUseTemplate_WhenUseTemplateIsTrue() {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Bulk Subject");
        emailRequest.setContent("Bulk Content");
        emailRequest.setUseTemplate(true);
        emailRequest.setTemplateType("announcement");

        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);
        when(emailTemplateService.applySystemTemplate(eq("Bulk Content"), eq("announcement")))
                .thenReturn("Templated Bulk Content");
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        adminEmailService.sendBulkEmail(emailRequest);

        // Assert
        verify(emailTemplateService).applySystemTemplate("Bulk Content", "announcement");
        verify(emailService, times(2)).sendCustomEmail(
                anyString(),
                eq("Bulk Subject"),
                eq("Templated Bulk Content")
        );
    }

    @Test
    void sendBulkEmail_ShouldHandleException_WhenUpdatingLastEmailSentFails() {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Bulk Subject");
        emailRequest.setContent("Bulk Content");

        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doThrow(new RuntimeException("Update failed"))
                .when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act - Should not throw exception
        assertDoesNotThrow(() -> adminEmailService.sendBulkEmail(emailRequest));

        // Assert
        verify(emailService, times(2)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    // ========== sendTargetedBulkEmail Tests ==========

    @Test
    void sendTargetedBulkEmail_ShouldSendToSubscribers_WhenRecipientTypeIsSubscribers() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        request.setSubscriberFilters(null);
        request.setUseTemplate(false);

        when(subscriberRepository.findAll()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(2, result.get("sentCount"));
        assertEquals(2, result.get("subscriberCount"));
        assertNull(result.get("externalCount"));
        verify(emailService, times(2)).sendCustomEmail(anyString(), eq("Targeted Subject"), eq("Targeted Content"));
        verify(adminNewsletterService, times(2)).updateLastEmailSent(anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldSendToExternalRecipients_WhenRecipientTypeIsExternal() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        request.setExternalFilters(null);
        request.setUseTemplate(false);

        List<ExternalRecipient> externalRecipients = Arrays.asList(
                testExternalRecipient,
                ExternalRecipient.builder()
                        .id(2L)
                        .email("external2@example.com")
                        .category("Firma")
                        .status("active")
                        .createdAt(LocalDateTime.now())
                        .tags(new HashSet<>())
                        .build()
        );

        when(externalRecipientRepository.findAll()).thenReturn(externalRecipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(2, result.get("sentCount"));
        assertEquals(2, result.get("externalCount"));
        assertNull(result.get("subscriberCount"));
        verify(emailService, times(2)).sendCustomEmail(anyString(), eq("Targeted Subject"), eq("Targeted Content"));
    }

    @Test
    void sendTargetedBulkEmail_ShouldSendToMixedRecipients_WhenRecipientTypeIsMixed() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("mixed");
        request.setSubscriberFilters(null);
        request.setExternalFilters(null);
        request.setUseTemplate(false);

        List<ExternalRecipient> externalRecipients = Collections.singletonList(testExternalRecipient);

        when(subscriberRepository.findAll()).thenReturn(activeSubscribers);
        when(externalRecipientRepository.findAll()).thenReturn(externalRecipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(3, result.get("sentCount"));
        assertEquals(2, result.get("subscriberCount"));
        assertEquals(1, result.get("externalCount"));
        verify(emailService, times(3)).sendCustomEmail(anyString(), eq("Targeted Subject"), eq("Targeted Content"));
    }

    @Test
    void sendTargetedBulkEmail_ShouldReturnEmptyResult_WhenNoRecipientsFound() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        request.setSubscriberFilters(null);

        when(subscriberRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(0, result.get("sentCount"));
        assertTrue(result.get("message").toString().contains("Nie znaleziono odbiorców"));
        verify(emailService, never()).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldUseSavedTemplate_WhenTemplateIdProvided() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Custom Content");
        request.setRecipientType("subscribers");
        request.setSavedTemplateId("1");
        request.setSubscriberFilters(null);

        when(subscriberRepository.findAll()).thenReturn(activeSubscribers);
        when(emailTemplateService.renderSavedTemplate(eq(1L), eq("Custom Content")))
                .thenReturn("Rendered Template");
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        verify(emailTemplateService).renderSavedTemplate(1L, "Custom Content");
        verify(emailService, times(2)).sendCustomEmail(anyString(), eq("Targeted Subject"), eq("Rendered Template"));
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterSubscribersByActiveAndVerified_WhenNoFiltersProvided() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        request.setSubscriberFilters(null);

        NewsletterSubscriber inactiveSubscriber = NewsletterSubscriber.builder()
                .id(3L)
                .email("inactive@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now())
                .verified(true)
                .active(false)
                .metadataEntries(new HashSet<>())
                .build();

        List<NewsletterSubscriber> allSubscribers = new ArrayList<>(activeSubscribers);
        allSubscribers.add(inactiveSubscriber);

        when(subscriberRepository.findAll()).thenReturn(allSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(2, result.get("sentCount"));
        verify(emailService, times(2)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterSubscribersByRole() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        Map<String, Object> filters = new HashMap<>();
        filters.put("role", "DIETITIAN");
        request.setSubscriberFilters(filters);

        when(subscriberRepository.findAll()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService, times(1)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterExternalRecipientsByStatus() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        Map<String, Object> filters = new HashMap<>();
        filters.put("status", "active");
        request.setExternalFilters(filters);

        ExternalRecipient inactiveRecipient = ExternalRecipient.builder()
                .id(2L)
                .email("inactive@example.com")
                .category("Firma")
                .status("inactive")
                .createdAt(LocalDateTime.now())
                .tags(new HashSet<>())
                .build();

        List<ExternalRecipient> allRecipients = Arrays.asList(testExternalRecipient, inactiveRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(allRecipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService, times(1)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterExternalRecipientsByCategory() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "Dietetyk");
        request.setExternalFilters(filters);

        ExternalRecipient otherCategoryRecipient = ExternalRecipient.builder()
                .id(2L)
                .email("other@example.com")
                .category("Firma")
                .status("active")
                .createdAt(LocalDateTime.now())
                .tags(new HashSet<>())
                .build();

        List<ExternalRecipient> allRecipients = Arrays.asList(testExternalRecipient, otherCategoryRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(allRecipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService, times(1)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldUseExternalRecipientIds_WhenProvided() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        request.setExternalRecipientIds(Arrays.asList("1", "2"));

        List<ExternalRecipient> recipients = Arrays.asList(
                testExternalRecipient,
                ExternalRecipient.builder()
                        .id(2L)
                        .email("external2@example.com")
                        .category("Firma")
                        .status("active")
                        .createdAt(LocalDateTime.now())
                        .tags(new HashSet<>())
                        .build()
        );

        when(externalRecipientsService.getRecipientsByIds(Arrays.asList(1L, 2L))).thenReturn(recipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(2, result.get("sentCount"));
        verify(externalRecipientsService).getRecipientsByIds(Arrays.asList(1L, 2L));
        verify(emailService, times(2)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldUpdateExternalRecipientStatus_WhenUpdateStatusIsTrue() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        request.setExternalFilters(null);
        request.setUpdateStatus(true);
        request.setNewStatus("contacted");

        List<ExternalRecipient> recipients = Collections.singletonList(testExternalRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(recipients);
        when(externalRecipientsService.updateStatus(eq(1L), eq("contacted"))).thenReturn(testExternalRecipient);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        verify(externalRecipientsService).updateStatus(1L, "contacted");
    }

    @Test
    void sendTargetedBulkEmail_ShouldHandleException_WhenUpdatingStatusFails() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        request.setExternalFilters(null);
        request.setUpdateStatus(true);
        request.setNewStatus("contacted");

        List<ExternalRecipient> recipients = Collections.singletonList(testExternalRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(recipients);
        doThrow(new RuntimeException("Update failed"))
                .when(externalRecipientsService).updateStatus(anyLong(), anyString());
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act - Should not throw exception
        assertDoesNotThrow(() -> adminEmailService.sendTargetedBulkEmail(request));

        // Assert
        verify(emailService).sendCustomEmail(anyString(), anyString(), anyString());
    }

    // ========== renderEmailPreview Tests ==========

    @Test
    void renderEmailPreview_ShouldReturnPlainContent_WhenNoTemplate() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setContent("Preview Content");
        request.setUseTemplate(false);

        // Act
        String result = adminEmailService.renderEmailPreview(request);

        // Assert
        assertEquals("Preview Content", result);
        verify(emailTemplateService, never()).applySystemTemplate(anyString(), anyString());
        verify(emailTemplateService, never()).renderSavedTemplate(anyLong(), anyString());
    }

    @Test
    void renderEmailPreview_ShouldReturnTemplatedContent_WhenUseTemplateIsTrue() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setContent("Preview Content");
        request.setUseTemplate(true);
        request.setTemplateType("survey");

        when(emailTemplateService.applySystemTemplate(eq("Preview Content"), eq("survey")))
                .thenReturn("Templated Preview Content");

        // Act
        String result = adminEmailService.renderEmailPreview(request);

        // Assert
        assertEquals("Templated Preview Content", result);
        verify(emailTemplateService).applySystemTemplate("Preview Content", "survey");
    }

    @Test
    void renderEmailPreview_ShouldHandleNullContent() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setContent(null);
        request.setUseTemplate(false);

        // Act
        String result = adminEmailService.renderEmailPreview(request);

        // Assert
        assertNull(result);
    }

    // ========== Edge Cases and Filter Tests ==========

    @Test
    void sendTargetedBulkEmail_ShouldFilterSubscribersByActiveFlag() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        Map<String, Object> filters = new HashMap<>();
        filters.put("active", false);
        request.setSubscriberFilters(filters);

        NewsletterSubscriber inactiveSubscriber = NewsletterSubscriber.builder()
                .id(3L)
                .email("inactive@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now())
                .verified(true)
                .active(false)
                .metadataEntries(new HashSet<>())
                .build();

        List<NewsletterSubscriber> allSubscribers = new ArrayList<>(activeSubscribers);
        allSubscribers.add(inactiveSubscriber);

        when(subscriberRepository.findAll()).thenReturn(allSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService, times(1)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterSubscribersByVerifiedFlag() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        Map<String, Object> filters = new HashMap<>();
        filters.put("verified", false);
        request.setSubscriberFilters(filters);

        NewsletterSubscriber unverifiedSubscriber = NewsletterSubscriber.builder()
                .id(3L)
                .email("unverified@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now())
                .verified(false)
                .active(true)
                .metadataEntries(new HashSet<>())
                .build();

        List<NewsletterSubscriber> allSubscribers = new ArrayList<>(activeSubscribers);
        allSubscribers.add(unverifiedSubscriber);

        when(subscriberRepository.findAll()).thenReturn(allSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService, times(1)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldFilterExternalRecipientsByTags() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        Map<String, Object> filters = new HashMap<>();
        filters.put("tags", Arrays.asList("important", "vip"));
        request.setExternalFilters(filters);

        ExternalRecipient taggedRecipient = ExternalRecipient.builder()
                .id(2L)
                .email("tagged@example.com")
                .category("Firma")
                .status("active")
                .createdAt(LocalDateTime.now())
                .tags(new HashSet<>())
                .build();
        taggedRecipient.setTagList(Arrays.asList("important", "vip"));

        List<ExternalRecipient> allRecipients = Arrays.asList(testExternalRecipient, taggedRecipient);
        List<ExternalRecipient> filteredRecipients = Collections.singletonList(taggedRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(allRecipients);
        when(externalRecipientRepository.findAllByTags(Arrays.asList("important", "vip")))
                .thenReturn(filteredRecipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(externalRecipientRepository).findAllByTags(Arrays.asList("important", "vip"));
    }

    @Test
    void sendTargetedBulkEmail_ShouldHandleAllRoleFilter() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("subscribers");
        Map<String, Object> filters = new HashMap<>();
        filters.put("role", "all");
        request.setSubscriberFilters(filters);

        when(subscriberRepository.findAll()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());
        doNothing().when(adminNewsletterService).updateLastEmailSent(anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(2, result.get("sentCount"));
        verify(emailService, times(2)).sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendTargetedBulkEmail_ShouldHandleAllStatusFilter() {
        // Arrange
        TargetedEmailRequest request = new TargetedEmailRequest();
        request.setSubject("Targeted Subject");
        request.setContent("Targeted Content");
        request.setRecipientType("external");
        Map<String, Object> filters = new HashMap<>();
        filters.put("status", "all");
        request.setExternalFilters(filters);

        List<ExternalRecipient> recipients = Collections.singletonList(testExternalRecipient);

        when(externalRecipientRepository.findAll()).thenReturn(recipients);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        Map<String, Object> result = adminEmailService.sendTargetedBulkEmail(request);

        // Assert
        assertEquals(1, result.get("sentCount"));
        verify(emailService).sendCustomEmail(anyString(), anyString(), anyString());
    }
}
