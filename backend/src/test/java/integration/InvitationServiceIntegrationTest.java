package integration;

import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.vitema.VitemaApplication;
import com.noisevisionsoftware.vitema.exception.*;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.InvitationRepository;
import com.noisevisionsoftware.vitema.service.UserService;
import com.noisevisionsoftware.vitema.service.invitation.InvitationEmailService;
import com.noisevisionsoftware.vitema.service.invitation.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = VitemaApplication.class)
@ActiveProfiles("dev")
@DisplayName("Invitation Service Integration Tests")
class InvitationServiceIntegrationTest {

    @Autowired
    private InvitationService invitationService;

    @MockBean
    private InvitationRepository invitationRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private InvitationEmailService invitationEmailService;

    @MockBean
    private Firestore firestore;

    private static final String TEST_TRAINER_ID = "trainer123";
    private static final String TEST_CLIENT_EMAIL = "client@example.com";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_CODE = "TR-ABC123";

    private Invitation testInvitation;
    private User trainerUser;
    private User clientUser;

    @BeforeEach
    void setUp() {
        long now = Instant.now().toEpochMilli();
        long expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();

        testInvitation = Invitation.builder()
                .id("inv123")
                .trainerId(TEST_TRAINER_ID)
                .clientEmail(TEST_CLIENT_EMAIL)
                .code(TEST_CODE)
                .status(InvitationStatus.PENDING)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        trainerUser = User.builder()
                .id(TEST_TRAINER_ID)
                .email("trainer@example.com")
                .nickname("Test Trainer")
                .role(UserRole.TRAINER)
                .build();

        clientUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_CLIENT_EMAIL)
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("Create Invitation - Integration Tests")
    class CreateInvitationIntegrationTests {

        @Test
        @DisplayName("Should create invitation with all dependencies working together")
        void createInvitation_WithAllDependencies_ShouldSucceed() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
            when(invitationRepository.findPendingByClientEmail(TEST_CLIENT_EMAIL))
                    .thenReturn(Optional.empty());
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act
            Invitation result = invitationService.createInvitation(TEST_CLIENT_EMAIL);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_CLIENT_EMAIL, result.getClientEmail());
            assertEquals(TEST_TRAINER_ID, result.getTrainerId());
            assertEquals(InvitationStatus.PENDING, result.getStatus());
            assertNotNull(result.getCode());
            assertTrue(result.getCode().startsWith("TR-"));
            assertTrue(result.getExpiresAt() > result.getCreatedAt());

            // Verify interaction order
            verify(userService).getCurrentUserId();
            verify(userService).getUserById(TEST_TRAINER_ID);
            verify(invitationRepository).findPendingByClientEmail(TEST_CLIENT_EMAIL);
            verify(invitationRepository).save(any(Invitation.class));
            verify(invitationEmailService).sendInvitationEmail(
                    eq(TEST_CLIENT_EMAIL), 
                    anyString(), 
                    eq("Test Trainer")
            );
        }

        @Test
        @DisplayName("Should rollback invitation when email sending fails")
        void createInvitation_WhenEmailFails_ShouldRollback() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
            when(invitationRepository.findPendingByClientEmail(TEST_CLIENT_EMAIL))
                    .thenReturn(Optional.empty());
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            
            // Simulate email failure
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> invitationService.createInvitation(TEST_CLIENT_EMAIL));

            // Verify rollback happened
            assertTrue(exception.getMessage().contains("Nie udało się wysłać emaila"));
            verify(invitationRepository).save(any(Invitation.class));
            verify(invitationRepository).delete(testInvitation.getId()); // Rollback!
        }

        @Test
        @DisplayName("Should prevent duplicate pending invitations")
        void createInvitation_WhenPendingExists_ShouldThrowException() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.findPendingByClientEmail(TEST_CLIENT_EMAIL))
                    .thenReturn(Optional.of(testInvitation));

            // Act & Assert
            InvitationAlreadyExistsException exception = assertThrows(
                    InvitationAlreadyExistsException.class,
                    () -> invitationService.createInvitation(TEST_CLIENT_EMAIL)
            );

            assertTrue(exception.getMessage().contains(TEST_CLIENT_EMAIL));
            assertTrue(exception.getMessage().contains(TEST_CODE));

            // Verify invitation was never saved
            verify(invitationRepository, never()).save(any());
            verify(invitationEmailService, never()).sendInvitationEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should generate unique code after collision")
        void createInvitation_WhenCodeCollision_ShouldRetry() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.findPendingByClientEmail(TEST_CLIENT_EMAIL))
                    .thenReturn(Optional.empty());
            
            // First code exists, second doesn't
            when(invitationRepository.existsByCode(anyString()))
                    .thenReturn(true)
                    .thenReturn(false);
            
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act
            Invitation result = invitationService.createInvitation(TEST_CLIENT_EMAIL);

            // Assert
            assertNotNull(result);
            // Should have checked code existence at least twice
            verify(invitationRepository, atLeast(2)).existsByCode(anyString());
        }

        @Test
        @DisplayName("Should reject invitation creation when user lacks permissions")
        void createInvitation_WhenUserRole_ShouldThrowException() {
            // Arrange
            User regularUser = User.builder()
                    .id(TEST_TRAINER_ID)
                    .email("user@example.com")
                    .role(UserRole.USER)
                    .build();

            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(regularUser);

            // Act & Assert
            assertThrows(UnauthorizedInvitationException.class, () -> invitationService.createInvitation(TEST_CLIENT_EMAIL));

            verify(invitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Accept Invitation - Integration Tests")
    class AcceptInvitationIntegrationTests {

        @Test
        @DisplayName("Should accept invitation and assign trainer to user")
        void acceptInvitation_WithValidCode_ShouldSucceed() {
            // Arrange
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(testInvitation));
            when(userService.getUserById(TEST_USER_ID)).thenReturn(clientUser);
            when(userService.updateUser(eq(TEST_USER_ID), any(User.class))).thenReturn(clientUser);
            doNothing().when(invitationRepository).update(anyString(), any(Invitation.class));

            // Act
            Invitation result = invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(InvitationStatus.ACCEPTED, result.getStatus());

            // Verify user was updated with trainerId
            verify(userService).getUserById(TEST_USER_ID);
            verify(userService).updateUser(eq(TEST_USER_ID), any(User.class));
            verify(invitationRepository).update(eq(testInvitation.getId()), any(Invitation.class));
        }

        @Test
        @DisplayName("Should reject expired invitation")
        void acceptInvitation_WhenExpired_ShouldThrowException() {
            // Arrange - create expired invitation
            Invitation expiredInvitation = Invitation.builder()
                    .id("inv123")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail(TEST_CLIENT_EMAIL)
                    .code(TEST_CODE)
                    .status(InvitationStatus.PENDING)
                    .createdAt(Instant.now().minus(10, ChronoUnit.DAYS).toEpochMilli())
                    .expiresAt(Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(expiredInvitation));

            // Act & Assert
            assertThrows(InvitationExpiredException.class, () -> invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID));

            // Verify user was never updated
            verify(userService, never()).getUserById(anyString());
            verify(userService, never()).updateUser(anyString(), any());
            verify(invitationRepository, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Should reject already accepted invitation")
        void acceptInvitation_WhenAlreadyAccepted_ShouldThrowException() {
            // Arrange
            testInvitation.setStatus(InvitationStatus.ACCEPTED);
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(testInvitation));

            // Act & Assert
            assertThrows(InvitationAlreadyUsedException.class, () -> invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID));

            verify(userService, never()).updateUser(anyString(), any());
        }

        @Test
        @DisplayName("Should reject invitation with invalid code")
        void acceptInvitation_WithInvalidCode_ShouldThrowException() {
            // Arrange
            when(invitationRepository.findByCode("INVALID")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(InvitationNotFoundException.class, () -> invitationService.acceptInvitation("INVALID", TEST_USER_ID));
        }
    }

    @Nested
    @DisplayName("Delete Invitation - Integration Tests")
    class DeleteInvitationIntegrationTests {

        @Test
        @DisplayName("Should delete invitation when user is owner")
        void deleteInvitation_WhenOwner_ShouldSucceed() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(invitationRepository.findById("inv123")).thenReturn(Optional.of(testInvitation));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            doNothing().when(invitationRepository).delete("inv123");

            // Act
            assertDoesNotThrow(() -> invitationService.deleteInvitation("inv123"));

            // Assert
            verify(invitationRepository).delete("inv123");
        }

        @Test
        @DisplayName("Should delete invitation when user is admin")
        void deleteInvitation_WhenAdmin_ShouldSucceed() {
            // Arrange
            String adminId = "admin123";
            when(userService.getCurrentUserId()).thenReturn(adminId);
            when(invitationRepository.findById("inv123")).thenReturn(Optional.of(testInvitation));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
            doNothing().when(invitationRepository).delete("inv123");

            // Act
            assertDoesNotThrow(() -> invitationService.deleteInvitation("inv123"));

            // Assert
            verify(invitationRepository).delete("inv123");
        }

        @Test
        @DisplayName("Should reject deletion when user is not owner nor admin")
        void deleteInvitation_WhenNotOwnerNorAdmin_ShouldThrowException() {
            // Arrange
            String otherTrainerId = "trainer999";
            when(userService.getCurrentUserId()).thenReturn(otherTrainerId);
            when(invitationRepository.findById("inv123")).thenReturn(Optional.of(testInvitation));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);

            // Act & Assert
            assertThrows(UnauthorizedInvitationException.class, () -> invitationService.deleteInvitation("inv123"));

            verify(invitationRepository, never()).delete(anyString());
        }

        @Test
        @DisplayName("Should throw exception when invitation not found")
        void deleteInvitation_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(invitationRepository.findById("invalid")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(InvitationNotFoundException.class, () -> invitationService.deleteInvitation("invalid"));
        }
    }

    @Nested
    @DisplayName("Expire Old Invitations - Integration Tests")
    class ExpireOldInvitationsIntegrationTests {

        @Test
        @DisplayName("Should expire old pending invitations")
        void expireOldInvitations_WithExpiredInvitations_ShouldUpdateThem() {
            // Arrange
            Invitation expiredInv1 = Invitation.builder()
                    .id("inv1")
                    .code("TR-EXP001")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            Invitation expiredInv2 = Invitation.builder()
                    .id("inv2")
                    .code("TR-EXP002")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(List.of(expiredInv1, expiredInv2));
            doNothing().when(invitationRepository).update(anyString(), any(Invitation.class));

            // Act
            assertDoesNotThrow(() -> invitationService.expireOldInvitations());

            // Assert
            verify(invitationRepository).findExpiredPendingInvitations(anyLong());
            verify(invitationRepository, times(2)).update(anyString(), any(Invitation.class));
        }

        @Test
        @DisplayName("Should handle empty list gracefully")
        void expireOldInvitations_WithNoExpiredInvitations_ShouldDoNothing() {
            // Arrange
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(List.of());

            // Act
            assertDoesNotThrow(() -> invitationService.expireOldInvitations());

            // Assert
            verify(invitationRepository).findExpiredPendingInvitations(anyLong());
            verify(invitationRepository, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Should continue processing when one update fails")
        void expireOldInvitations_WhenOneUpdateFails_ShouldContinue() {
            // Arrange
            Invitation inv1 = Invitation.builder()
                    .id("inv1")
                    .code("TR-EXP001")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            Invitation inv2 = Invitation.builder()
                    .id("inv2")
                    .code("TR-EXP002")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(List.of(inv1, inv2));

            // First update succeeds, second fails
            doNothing()
                    .doThrow(new RuntimeException("Update failed"))
                    .when(invitationRepository).update(anyString(), any(Invitation.class));

            // Act - should not throw
            assertDoesNotThrow(() -> invitationService.expireOldInvitations());

            // Assert - both updates were attempted
            verify(invitationRepository, times(2)).update(anyString(), any(Invitation.class));
        }

        @Test
        @DisplayName("Should handle repository error gracefully")
        void expireOldInvitations_WhenRepositoryFails_ShouldNotThrow() {
            // Arrange
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenThrow(new RuntimeException("Firestore connection error"));

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> invitationService.expireOldInvitations());
        }
    }

    @Nested
    @DisplayName("Complete Flow Integration Tests")
    class CompleteFlowIntegrationTests {

        @Test
        @DisplayName("Should handle complete invitation lifecycle")
        void completeInvitationLifecycle_ShouldWorkEndToEnd() {
            // Step 1: Create invitation
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
            when(invitationRepository.findPendingByClientEmail(TEST_CLIENT_EMAIL))
                    .thenReturn(Optional.empty());
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            Invitation created = invitationService.createInvitation(TEST_CLIENT_EMAIL);
            assertNotNull(created);
            assertEquals(InvitationStatus.PENDING, created.getStatus());

            // Step 2: Get invitations list
            when(invitationRepository.findByTrainerId(TEST_TRAINER_ID))
                    .thenReturn(List.of(testInvitation));

            List<Invitation> invitations = invitationService.getMyInvitations();
            assertEquals(1, invitations.size());
            assertEquals(TEST_CODE, invitations.getFirst().getCode());

            // Step 3: Accept invitation
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(testInvitation));
            when(userService.getUserById(TEST_USER_ID)).thenReturn(clientUser);
            when(userService.updateUser(eq(TEST_USER_ID), any(User.class))).thenReturn(clientUser);
            doNothing().when(invitationRepository).update(anyString(), any(Invitation.class));

            Invitation accepted = invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID);
            assertEquals(InvitationStatus.ACCEPTED, accepted.getStatus());

            // Verify all interactions
            verify(invitationRepository).save(any(Invitation.class));
            verify(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());
            verify(invitationRepository).findByTrainerId(TEST_TRAINER_ID);
            verify(userService).updateUser(eq(TEST_USER_ID), any(User.class));
            verify(invitationRepository).update(anyString(), any(Invitation.class));
        }
    }
}
