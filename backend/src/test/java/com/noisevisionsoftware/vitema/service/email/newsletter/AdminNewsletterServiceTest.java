package com.noisevisionsoftware.vitema.service.email.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
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
    void getAllActiveSubscribers_ShouldReturnEmptyList_WhenNoSubscribers() {
        // Arrange
        when(subscriberRepository.findAllByActiveTrueAndVerifiedTrue()).thenReturn(Collections.emptyList());

        // Act
        List<NewsletterSubscriber> result = adminNewsletterService.getAllActiveSubscribers();

        // Assert
        assertTrue(result.isEmpty(), "Powinna zwrócić pustą listę");
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
    void getAllSubscribers_ShouldReturnEmptyList_WhenNoSubscribers() {
        // Arrange
        when(subscriberRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<NewsletterSubscriber> result = adminNewsletterService.getAllSubscribers();

        // Assert
        assertTrue(result.isEmpty(), "Powinna zwrócić pustą listę");
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
    void activateSubscriber_ShouldDoNothing_WhenSubscriberDoesNotExist() {
        // Arrange
        when(subscriberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        adminNewsletterService.activateSubscriber(999L);

        // Assert
        verify(subscriberRepository).findById(999L);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
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
    void verifySubscriberManually_ShouldDoNothing_WhenSubscriberDoesNotExist() {
        // Arrange
        when(subscriberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        adminNewsletterService.verifySubscriberManually(999L);

        // Assert
        verify(subscriberRepository).findById(999L);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
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
    void updateSubscriberMetadata_ShouldInitializeMetadataEntries_WhenNull() {
        // Arrange
        NewsletterSubscriber subscriberWithNullMetadata = NewsletterSubscriber.builder()
                .id(5L)
                .email("nullmetadata@example.com")
                .role(SubscriberRole.DIETITIAN)
                .createdAt(LocalDateTime.now())
                .verified(true)
                .active(true)
                .metadataEntries(null)
                .build();

        when(subscriberRepository.findById(5L)).thenReturn(Optional.of(subscriberWithNullMetadata));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(subscriberWithNullMetadata);

        Map<String, String> newMetadata = Map.of("key", "value");

        // Act
        adminNewsletterService.updateSubscriberMetadata(5L, newMetadata);

        // Assert
        verify(subscriberRepository).findById(5L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        assertNotNull(capturedSubscriber.getMetadataEntries(), "MetadataEntries powinny być zainicjalizowane");
        Map<String, String> metadata = capturedSubscriber.getMetadata();
        assertEquals("value", metadata.get("key"), "Metadata powinna zawierać nową wartość");
    }

    @Test
    void updateSubscriberMetadata_ShouldMergeMetadata_WhenMetadataExists() {
        // Arrange
        Map<String, String> existingMetadata = new HashMap<>();
        existingMetadata.put("existingKey", "existingValue");
        testSubscriber.setMetadata(existingMetadata);

        when(subscriberRepository.findById(1L)).thenReturn(Optional.of(testSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber);

        Map<String, String> newMetadata = Map.of("newKey", "newValue");

        // Act
        adminNewsletterService.updateSubscriberMetadata(1L, newMetadata);

        // Assert
        verify(subscriberRepository).findById(1L);
        verify(subscriberRepository).save(subscriberCaptor.capture());

        NewsletterSubscriber capturedSubscriber = subscriberCaptor.getValue();
        Map<String, String> metadata = capturedSubscriber.getMetadata();
        assertEquals("existingValue", metadata.get("existingKey"), "Istniejąca metadata powinna pozostać");
        assertEquals("newValue", metadata.get("newKey"), "Nowa metadata powinna być dodana");
    }

    @Test
    void updateSubscriberMetadata_ShouldDoNothing_WhenSubscriberDoesNotExist() {
        // Arrange
        when(subscriberRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, String> newMetadata = Map.of("key", "value");

        // Act
        adminNewsletterService.updateSubscriberMetadata(999L, newMetadata);

        // Assert
        verify(subscriberRepository).findById(999L);
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
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
    void updateLastEmailSent_ShouldDoNothing_WhenSubscriberDoesNotExist() {
        // Arrange
        when(subscriberRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        adminNewsletterService.updateLastEmailSent("nonexistent@example.com");

        // Assert
        verify(subscriberRepository).findByEmail("nonexistent@example.com");
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
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

    @Test
    void getNewsletterStats_ShouldReturnEmptyStats_WhenNoSubscribers() {
        // Arrange
        when(subscriberRepository.count()).thenReturn(0L);
        when(subscriberRepository.countVerifiedSubscribers()).thenReturn(0L);
        when(subscriberRepository.countActiveSubscribers()).thenReturn(0L);
        when(subscriberRepository.countActiveVerifiedSubscribers()).thenReturn(0L);
        when(subscriberRepository.countByRole()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = adminNewsletterService.getNewsletterStats();

        // Assert
        assertEquals(0L, stats.get("total"), "Całkowita liczba subskrybentów powinna być 0");
        assertEquals(0L, stats.get("verified"), "Liczba zweryfikowanych subskrybentów powinna być 0");
        assertEquals(0L, stats.get("active"), "Liczba aktywnych subskrybentów powinna być 0");
        assertEquals(0L, stats.get("activeVerified"), "Liczba aktywnych i zweryfikowanych subskrybentów powinna być 0");

        @SuppressWarnings("unchecked")
        Map<SubscriberRole, Long> roleDistribution = (Map<SubscriberRole, Long>) stats.get("roleDistribution");
        assertTrue(roleDistribution.isEmpty(), "Rozkład ról powinien być pusty");

        verify(subscriberRepository).count();
        verify(subscriberRepository).countVerifiedSubscribers();
        verify(subscriberRepository).countActiveSubscribers();
        verify(subscriberRepository).countActiveVerifiedSubscribers();
        verify(subscriberRepository).countByRole();
    }

    @Test
    void getNewsletterStats_ShouldHandleEmptyRoleDistribution() {
        // Arrange
        long totalCount = 5;
        long verifiedCount = 3;
        long activeCount = 2;
        long activeVerifiedCount = 1;

        when(subscriberRepository.count()).thenReturn(totalCount);
        when(subscriberRepository.countVerifiedSubscribers()).thenReturn(verifiedCount);
        when(subscriberRepository.countActiveSubscribers()).thenReturn(activeCount);
        when(subscriberRepository.countActiveVerifiedSubscribers()).thenReturn(activeVerifiedCount);
        when(subscriberRepository.countByRole()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = adminNewsletterService.getNewsletterStats();

        // Assert
        assertEquals(totalCount, stats.get("total"));
        assertEquals(verifiedCount, stats.get("verified"));
        assertEquals(activeCount, stats.get("active"));
        assertEquals(activeVerifiedCount, stats.get("activeVerified"));

        @SuppressWarnings("unchecked")
        Map<SubscriberRole, Long> roleDistribution = (Map<SubscriberRole, Long>) stats.get("roleDistribution");
        assertTrue(roleDistribution.isEmpty(), "Rozkład ról powinien być pusty");
    }
}
