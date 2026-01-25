package com.noisevisionsoftware.vitema.service.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import com.noisevisionsoftware.vitema.service.email.newsletter.AdminNewsletterService;
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
class AdminNewsletterServiceTest {

    @Mock
    private NewsletterSubscriberRepository subscriberRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminNewsletterService adminNewsletterService;

    @Captor
    private ArgumentCaptor<NewsletterSubscriber> subscriberCaptor;

    private NewsletterSubscriber testSubscriber;
    private List<NewsletterSubscriber> activeSubscribers;

    @BeforeEach
    void setUp() {
        // Przygotowanie testowego subskrybenta
        testSubscriber = NewsletterSubscriber.builder()
                .id(1L)
                .email("test@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now().minusDays(10))
                .verified(true)
                .active(true)
                .verificationToken("test-token")
                .metadataEntries(new HashSet<>())
                .build();

        // Przygotowanie listy aktywnych subskrybentów
        activeSubscribers = Arrays.asList(
                testSubscriber,
                NewsletterSubscriber.builder()
                        .id(2L)
                        .email("active@example.com")
                        .role(SubscriberRole.COMPANY)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .verified(true)
                        .active(true)
                        .build()
        );
    }

    @Test
    void getAllActiveSubscribers_ShouldReturnAllActiveAndVerifiedSubscribers() {
        // Arrange
        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);

        // Act
        List<NewsletterSubscriber> result = adminNewsletterService.getAllActiveSubscribers();

        // Assert
        assertEquals(activeSubscribers.size(), result.size(), "Powinno zwrócić wszystkich aktywnych subskrybentów");
        assertEquals(activeSubscribers, result, "Powinna zwrócić tych samych subskrybentów");
        verify(subscriberRepository).findAllByActiveTrueAndVerifiedTrue();
    }

    @Test
    void getAllSubscribers_ShouldReturnAllSubscribers() {
        // Arrange
        List<NewsletterSubscriber> allSubscribers = new ArrayList<>(activeSubscribers);
        allSubscribers.add(NewsletterSubscriber.builder()
                .id(3L)
                .email("inactive@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now().minusDays(20))
                .verified(true)
                .active(false)
                .build());

        when(subscriberRepository.findAll()).thenReturn(allSubscribers);

        // Act
        List<NewsletterSubscriber> result = adminNewsletterService.getAllSubscribers();

        // Assert
        assertEquals(allSubscribers.size(), result.size(), "Powinno zwrócić wszystkich subskrybentów");
        assertEquals(allSubscribers, result, "Powinna zwrócić tych samych subskrybentów");
        verify(subscriberRepository).findAll();
    }

    @Test
    void deactivateSubscriber_ShouldDeactivateSubscriber_WhenSubscriberExists() {
        // Arrange
        when(subscriberRepository.findById(1L)).thenReturn(Optional.of(testSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber);

        // Act
        adminNewsletterService.deactivateSubscriber(1L);

        // Assert
        verify(subscriberRepository).findById(1L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        assertFalse(capturedSubscriber.isActive(), "Subskrybent powinien być dezaktywowany");
    }

    @Test
    void deactivateSubscriber_ShouldDoNothing_WhenSubscriberDoesNotExist() {
        // Arrange
        when(subscriberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        adminNewsletterService.deactivateSubscriber(999L);

        // Assert
        verify(subscriberRepository).findById(999L);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }

    @Test
    void activateSubscriber_ShouldActivateSubscriber_WhenSubscriberExists() {
        // Arrange
        NewsletterSubscriber inactiveSubscriber = NewsletterSubscriber.builder()
                .id(3L)
                .email("inactive@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now().minusDays(20))
                .verified(true)
                .active(false)
                .build();

        when(subscriberRepository.findById(3L)).thenReturn(Optional.of(inactiveSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(inactiveSubscriber);

        // Act
        adminNewsletterService.activateSubscriber(3L);

        // Assert
        verify(subscriberRepository).findById(3L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        assertTrue(capturedSubscriber.isActive(), "Subskrybent powinien być aktywowany");
    }

    @Test
    void verifySubscriberManually_ShouldVerifySubscriber_WhenSubscriberExists() {
        // Arrange
        NewsletterSubscriber unverifiedSubscriber = NewsletterSubscriber.builder()
                .id(4L)
                .email("unverified@example.com")
                .role(SubscriberRole.COMPANY)
                .createdAt(LocalDateTime.now().minusDays(2))
                .verified(false)
                .active(true)
                .build();

        when(subscriberRepository.findById(4L)).thenReturn(Optional.of(unverifiedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(unverifiedSubscriber);

        // Act
        adminNewsletterService.verifySubscriberManually(4L);

        // Assert
        verify(subscriberRepository).findById(4L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        assertTrue(capturedSubscriber.isVerified(), "Subskrybent powinien być zweryfikowany");
        assertNotNull(capturedSubscriber.getVerifiedAt(), "Data weryfikacji powinna być ustawiona");
    }

    @Test
    void updateSubscriberMetadata_ShouldAddNewMetadata_WhenSubscriberExists() {
        // Arrange
        when(subscriberRepository.findById(1L)).thenReturn(Optional.of(testSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber);

        Map<String, String> newMetadata = Map.of(
                "preferredLanguage", "pl",
                "dietType", "vegetarian"
        );

        // Act
        adminNewsletterService.updateSubscriberMetadata(1L, newMetadata);

        // Assert
        verify(subscriberRepository).findById(1L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        Map<String, String> metadata = capturedSubscriber.getMetadata();
        assertEquals("pl", metadata.get("preferredLanguage"), "Metadata powinna zawierać preferredLanguage");
        assertEquals("vegetarian", metadata.get("dietType"), "Metadata powinna zawierać dietType");
    }

    @Test
    void updateLastEmailSent_ShouldUpdateLastEmailSentDate_WhenSubscriberExists() {
        // Arrange
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber);

        // Act
        adminNewsletterService.updateLastEmailSent("test@example.com");

        // Assert
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        assertNotNull(capturedSubscriber.getLastEmailSent(), "Data ostatniego emaila powinna być ustawiona");
    }

    @Test
    void deleteSubscriber_ShouldDeleteSubscriber() {
        // Arrange
        doNothing().when(subscriberRepository).deleteById(1L);

        // Act
        adminNewsletterService.deleteSubscriber(1L);

        // Assert
        verify(subscriberRepository).deleteById(1L);
    }

    @Test
    void sendBulkEmail_ShouldSendEmailToAllActiveSubscribers() {
        // Arrange
        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        for (NewsletterSubscriber subscriber : activeSubscribers) {
            when(subscriberRepository.findByEmail(subscriber.getEmail())).thenReturn(Optional.of(subscriber));
        }

        String subject = "Test Subject";
        String content = "Test Content";

        // Act
        adminNewsletterService.sendBulkEmail(subject, content);

        // Assert
        verify(subscriberRepository).findAllByActiveTrueAndVerifiedTrue();
        verify(emailService, times(activeSubscribers.size())).sendCustomEmail(anyString(), eq(subject), eq(content));

        // Weryfikacja dla każdego subskrybenta
        for (NewsletterSubscriber subscriber : activeSubscribers) {
            verify(subscriberRepository).findByEmail(subscriber.getEmail());
        }

        // Weryfikacja zapisu dla każdego subskrybenta
        verify(subscriberRepository, times(activeSubscribers.size())).save(any(NewsletterSubscriber.class));
    }

    @Test
    void sendBulkEmail_ShouldThrowRuntimeException_WhenEmailServiceFails() {
        // Arrange
        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(activeSubscribers);
        doThrow(new RuntimeException("Email service failure")).when(emailService)
                .sendCustomEmail(anyString(), anyString(), anyString());

        String subject = "Test Subject";
        String content = "Test Content";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminNewsletterService.sendBulkEmail(subject, content);
        });

        assertEquals("Błąd podczas wysyłania masowego emaila", exception.getMessage());
        verify(subscriberRepository).findAllByActiveTrueAndVerifiedTrue();
        verify(emailService).sendCustomEmail(anyString(), eq(subject), eq(content));
    }

    @Test
    void getNewsletterStats_ShouldReturnStatistics() {
        // Arrange
        long totalCount = 10;
        long verifiedCount = 8;
        long activeCount = 7;
        long activeVerifiedCount = 6;

        when(subscriberRepository.count()).thenReturn(totalCount);
        when(subscriberRepository.countVerifiedSubscribers()).thenReturn(verifiedCount);
        when(subscriberRepository.countActiveSubscribers()).thenReturn(activeCount);
        when(subscriberRepository.countActiveVerifiedSubscribers()).thenReturn(activeVerifiedCount);

        List<Object[]> roleCounts = Arrays.asList(
                new Object[]{SubscriberRole.DIETITIAN, 7L},
                new Object[]{SubscriberRole.COMPANY, 3L}
        );
        when(subscriberRepository.countByRole()).thenReturn(roleCounts);

        // Act
        Map<String, Object> stats = adminNewsletterService.getNewsletterStats();

        // Assert
        assertEquals(totalCount, stats.get("total"), "Całkowita liczba subskrybentów powinna być poprawna");
        assertEquals(verifiedCount, stats.get("verified"), "Liczba zweryfikowanych subskrybentów powinna być poprawna");
        assertEquals(activeCount, stats.get("active"), "Liczba aktywnych subskrybentów powinna być poprawna");
        assertEquals(activeVerifiedCount, stats.get("activeVerified"), "Liczba aktywnych i zweryfikowanych subskrybentów powinna być poprawna");

        @SuppressWarnings("unchecked")
        Map<SubscriberRole, Long> roleDistribution = (Map<SubscriberRole, Long>) stats.get("roleDistribution");
        assertEquals(7L, roleDistribution.get(SubscriberRole.DIETITIAN), "Liczba dietetyków powinna być poprawna");
        assertEquals(3L, roleDistribution.get(SubscriberRole.COMPANY), "Liczba firm powinna być poprawna");

        verify(subscriberRepository).count();
        verify(subscriberRepository).countVerifiedSubscribers();
        verify(subscriberRepository).countActiveSubscribers();
        verify(subscriberRepository).countActiveVerifiedSubscribers();
        verify(subscriberRepository).countByRole();
    }
}