package com.noisevisionsoftware.vitema.service.email.newsletter;

import com.noisevisionsoftware.vitema.dto.request.newsletter.SubscriptionRequest;
import com.noisevisionsoftware.vitema.exception.TooManyRequestsException;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscribersMetadata;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.EmailService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicNewsletterServiceTest {

    @Mock
    private NewsletterSubscriberRepository subscriberRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PublicNewsletterService publicNewsletterService;

    @Captor
    private ArgumentCaptor<NewsletterSubscriber> subscriberCaptor;

    private SubscriptionRequest validRequest;
    private NewsletterSubscriber existingUnverifiedSubscriber;
    private NewsletterSubscriber existingVerifiedSubscriber;

    @BeforeEach
    void setUp() {
        validRequest = new SubscriptionRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setRole("dietetyk");

        existingUnverifiedSubscriber = NewsletterSubscriber.create("test@example.com", SubscriberRole.DIETITIAN);
        existingUnverifiedSubscriber.setId(1L);
        existingUnverifiedSubscriber.setVerified(false);
        existingUnverifiedSubscriber.setLastEmailSent(LocalDateTime.now().minusHours(1));

        existingVerifiedSubscriber = NewsletterSubscriber.create("verified@example.com", SubscriberRole.DIETITIAN);
        existingVerifiedSubscriber.setId(2L);
        existingVerifiedSubscriber.setVerified(true);
        existingVerifiedSubscriber.setVerifiedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void subscribe_ShouldCreateNewSubscriber_WhenEmailDoesNotExist() {
        // Arrange
        when(subscriberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(any(NewsletterSubscriber.class));

        // Act
        publicNewsletterService.subscribe(validRequest);

        // Assert
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(subscriberCaptor.capture());
        verify(emailService).sendVerificationEmail(any(NewsletterSubscriber.class));

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertEquals("test@example.com", captured.getEmail());
        assertEquals(SubscriberRole.DIETITIAN, captured.getRole());
        assertFalse(captured.isVerified());
        assertTrue(captured.isActive());
        assertNotNull(captured.getVerificationToken());
        assertNotNull(captured.getLastEmailSent());
    }

    @Test
    void subscribe_ShouldCreateNewSubscriberWithCompanyRole_WhenRoleIsNotDietetyk() {
        // Arrange
        SubscriptionRequest companyRequest = new SubscriptionRequest();
        companyRequest.setEmail("company@example.com");
        companyRequest.setRole("firma");

        when(subscriberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(any(NewsletterSubscriber.class));

        // Act
        publicNewsletterService.subscribe(companyRequest);

        // Assert
        verify(subscriberRepository).findByEmail("company@example.com");
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertEquals("company@example.com", captured.getEmail());
        assertEquals(SubscriberRole.COMPANY, captured.getRole());
    }

    @Test
    void subscribe_ShouldResendVerificationEmail_WhenSubscriberExistsButIsNotVerified() {
        // Arrange
        when(subscriberRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUnverifiedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingUnverifiedSubscriber);
        doNothing().when(emailService).sendVerificationEmail(any(NewsletterSubscriber.class));

        // Act
        publicNewsletterService.subscribe(validRequest);

        // Assert
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(subscriberCaptor.capture());
        verify(emailService).sendVerificationEmail(any(NewsletterSubscriber.class));

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertNotNull(captured.getLastEmailSent());
    }

    @Test
    void subscribe_ShouldThrowException_WhenSubscriberExistsAndIsVerified() {
        // Arrange
        SubscriptionRequest verifiedRequest = new SubscriptionRequest();
        verifiedRequest.setEmail("verified@example.com");
        verifiedRequest.setRole("dietetyk");

        when(subscriberRepository.findByEmail(anyString())).thenReturn(Optional.of(existingVerifiedSubscriber));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> publicNewsletterService.subscribe(verifiedRequest));

        assertEquals("Email już istnieje w bazie newslettera", exception.getMessage());
        verify(subscriberRepository).findByEmail("verified@example.com");
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(emailService, never()).sendVerificationEmail(any(NewsletterSubscriber.class));
    }

    @Test
    void subscribe_ShouldThrowException_WhenEmailSentRecently() {
        // Arrange
        NewsletterSubscriber recentEmailSubscriber = NewsletterSubscriber.create("recent@example.com", SubscriberRole.DIETITIAN);
        recentEmailSubscriber.setLastEmailSent(LocalDateTime.now().minusMinutes(5)); // Email wysłany 5 minut temu

        SubscriptionRequest recentRequest = new SubscriptionRequest();
        recentRequest.setEmail("recent@example.com");
        recentRequest.setRole("dietetyk");

        when(subscriberRepository.findByEmail(anyString())).thenReturn(Optional.of(recentEmailSubscriber));

        // Act & Assert
        TooManyRequestsException exception = assertThrows(TooManyRequestsException.class, () -> publicNewsletterService.subscribe(recentRequest));

        assertTrue(exception.getMessage().contains("Wysłano już mail weryfikacyjny"));
        verify(subscriberRepository).findByEmail("recent@example.com");
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(emailService, never()).sendVerificationEmail(any(NewsletterSubscriber.class));
    }

    @Test
    void verifySubscriberAndGet_ShouldVerifySubscriber_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        when(subscriberRepository.findByVerificationToken(token)).thenReturn(Optional.of(existingUnverifiedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingUnverifiedSubscriber);
        doNothing().when(emailService).sendWelcomeEmail(any(NewsletterSubscriber.class));

        // Act
        NewsletterSubscriber result = publicNewsletterService.verifySubscriberAndGet(token);

        // Assert
        assertNotNull(result);
        verify(subscriberRepository).findByVerificationToken(token);
        verify(subscriberRepository).save(subscriberCaptor.capture());
        verify(emailService).sendWelcomeEmail(any(NewsletterSubscriber.class));

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertTrue(captured.isVerified());
        assertNotNull(captured.getVerifiedAt());
    }

    @Test
    void verifySubscriberAndGet_ShouldReturnSubscriber_WhenAlreadyVerified() {
        // Arrange
        String token = "already-verified-token";
        when(subscriberRepository.findByVerificationToken(token)).thenReturn(Optional.of(existingVerifiedSubscriber));

        // Act
        NewsletterSubscriber result = publicNewsletterService.verifySubscriberAndGet(token);

        // Assert
        assertNotNull(result);
        verify(subscriberRepository).findByVerificationToken(token);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(emailService, never()).sendWelcomeEmail(any(NewsletterSubscriber.class));
    }

    @Test
    void verifySubscriberAndGet_ShouldReturnNull_WhenTokenNotFound() {
        // Arrange
        String token = "invalid-token";
        when(subscriberRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        // Act
        NewsletterSubscriber result = publicNewsletterService.verifySubscriberAndGet(token);

        // Assert
        assertNull(result);
        verify(subscriberRepository).findByVerificationToken(token);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(emailService, never()).sendWelcomeEmail(any(NewsletterSubscriber.class));
    }

    @Test
    void verifySubscriberAndGet_ShouldReturnNull_WhenExceptionOccurs() {
        // Arrange
        String token = "error-token";
        when(subscriberRepository.findByVerificationToken(token)).thenReturn(Optional.of(existingUnverifiedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenThrow(new RuntimeException("Test exception"));

        // Act
        NewsletterSubscriber result = publicNewsletterService.verifySubscriberAndGet(token);

        // Assert
        assertNull(result);
        verify(subscriberRepository).findByVerificationToken(token);
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
        verify(emailService, never()).sendWelcomeEmail(any(NewsletterSubscriber.class));
    }

    @Test
    void unsubscribe_ShouldDeactivateSubscriber_WhenEmailExists() {
        // Arrange
        String email = "test@example.com";
        when(subscriberRepository.findByEmail(email)).thenReturn(Optional.of(existingVerifiedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingVerifiedSubscriber);

        // Act
        boolean result = publicNewsletterService.unsubscribe(email);

        // Assert
        assertTrue(result);
        verify(subscriberRepository).findByEmail(email);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertFalse(captured.isActive());
    }

    @Test
    void unsubscribe_ShouldReturnFalse_WhenEmailNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(subscriberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = publicNewsletterService.unsubscribe(email);

        // Assert
        assertFalse(result);
        verify(subscriberRepository).findByEmail(email);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }

    @Test
    void updateSubscriberMetadata_ShouldUpdateMetadata_WhenSubscriberExists() {
        // Arrange
        Long subscriberId = 1L;
        Map<String, String> newMetadata = Map.of(
                "preferredTopic", "nutrition",
                "language", "pl"
        );

        NewsletterSubscriber subscriberWithMetadata = NewsletterSubscriber.create("metadata@example.com", SubscriberRole.DIETITIAN);
        subscriberWithMetadata.setId(subscriberId);
        subscriberWithMetadata.setMetadataEntries(new HashSet<>());

        when(subscriberRepository.findById(subscriberId)).thenReturn(Optional.of(subscriberWithMetadata));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(subscriberWithMetadata);

        // Act
        publicNewsletterService.updateSubscriberMetadata(subscriberId, newMetadata);

        // Assert
        verify(subscriberRepository).findById(subscriberId);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber captured = subscriberCaptor.getValue();
        assertEquals(2, captured.getMetadataEntries().size());

        // Konwertuj metadane do mapy dla łatwiejszego sprawdzenia
        Map<String, String> capturedMetadata = new HashMap<>();
        for (NewsletterSubscribersMetadata meta : captured.getMetadataEntries()) {
            capturedMetadata.put(meta.getKey(), meta.getValue());
        }

        assertEquals("nutrition", capturedMetadata.get("preferredTopic"));
        assertEquals("pl", capturedMetadata.get("language"));
    }

    @Test
    void updateSubscriberMetadata_ShouldThrowException_WhenSubscriberNotFound() {
        // Arrange
        Long subscriberId = 999L;
        Map<String, String> newMetadata = Map.of("key", "value");

        when(subscriberRepository.findById(subscriberId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> publicNewsletterService.updateSubscriberMetadata(subscriberId, newMetadata));

        assertEquals("Subskrybent o podanym ID nie istnieje", exception.getMessage());
        verify(subscriberRepository).findById(subscriberId);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }
}