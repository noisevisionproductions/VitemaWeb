package com.noisevisionsoftware.vitema.service.invitation;

import com.noisevisionsoftware.vitema.exception.*;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.InvitationRepository;
import com.noisevisionsoftware.vitema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserService userService;

    @Mock
    private InvitationEmailService invitationEmailService;

    @InjectMocks
    private InvitationService invitationService;

    private static final String TEST_TRAINER_ID = "trainer123";
    private static final String TEST_CLIENT_EMAIL = "client@example.com";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_CODE = "TR-ABC123";
    private static final String TEST_TRAINER_NAME = "Test Trainer";

    private User trainerUser;
    private User clientUser;
    private Invitation testInvitation;

    @BeforeEach
    void setUp() {
        trainerUser = User.builder()
                .id(TEST_TRAINER_ID)
                .email("trainer@example.com")
                .nickname(TEST_TRAINER_NAME)
                .role(UserRole.TRAINER)
                .build();

        clientUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_CLIENT_EMAIL)
                .role(UserRole.USER)
                .build();

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
    }

    @Nested
    @DisplayName("createInvitation Tests")
    class CreateInvitationTests {

        @Test
        @DisplayName("Should create invitation successfully for TRAINER")
        void createInvitation_WhenTrainerHasPermissions_ShouldCreateInvitation() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
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
            
            verify(userService).getCurrentUserId();
            verify(userService).getUserById(TEST_TRAINER_ID);
            verify(invitationRepository).save(any(Invitation.class));
            verify(invitationEmailService).sendInvitationEmail(eq(TEST_CLIENT_EMAIL), anyString(), eq(TEST_TRAINER_NAME));
        }

        @Test
        @DisplayName("Should create invitation successfully for ADMIN")
        void createInvitation_WhenAdminHasPermissions_ShouldCreateInvitation() {
            // Arrange
            trainerUser.setRole(UserRole.ADMIN);
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act
            Invitation result = invitationService.createInvitation(TEST_CLIENT_EMAIL);

            // Assert
            assertNotNull(result);
            verify(invitationRepository).save(any(Invitation.class));
        }

        @Test
        @DisplayName("Should throw exception when current user ID is null")
        void createInvitation_WhenCurrentUserIdIsNull_ShouldThrowException() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(null);

            // Act & Assert
            assertThrows(UnauthorizedInvitationException.class, 
                    () -> invitationService.createInvitation(TEST_CLIENT_EMAIL));
            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user has no permissions")
        void createInvitation_WhenUserHasNoPermissions_ShouldThrowException() {
            // Arrange
            trainerUser.setRole(UserRole.USER);
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);

            // Act & Assert
            assertThrows(UnauthorizedInvitationException.class, 
                    () -> invitationService.createInvitation(TEST_CLIENT_EMAIL));
            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should generate unique code when first code already exists")
        void createInvitation_WhenCodeExists_ShouldGenerateUniqueCode() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString()))
                    .thenReturn(true)
                    .thenReturn(false);
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act
            Invitation result = invitationService.createInvitation(TEST_CLIENT_EMAIL);

            // Assert
            assertNotNull(result);
            verify(invitationRepository, atLeast(2)).existsByCode(anyString());
        }

        @Test
        @DisplayName("Should set expiration date to 7 days from now")
        void createInvitation_ShouldSetExpirationTo7Days() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_TRAINER_ID)).thenReturn(trainerUser);
            when(invitationRepository.existsByCode(anyString())).thenReturn(false);
            when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
            doNothing().when(invitationEmailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // Act
            invitationService.createInvitation(TEST_CLIENT_EMAIL);

            // Assert
            ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
            verify(invitationRepository).save(captor.capture());
            
            Invitation savedInvitation = captor.getValue();
            long now = Instant.now().toEpochMilli();
            long expectedExpiration = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();
            
            assertTrue(savedInvitation.getExpiresAt() > now);
            assertTrue(Math.abs(savedInvitation.getExpiresAt() - expectedExpiration) < 1000); // 1 second tolerance
        }
    }

    @Nested
    @DisplayName("acceptInvitation Tests")
    class AcceptInvitationTests {

        @Test
        @DisplayName("Should accept valid invitation successfully")
        void acceptInvitation_WhenInvitationIsValid_ShouldAcceptInvitation() {
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
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userService).updateUser(eq(TEST_USER_ID), userCaptor.capture());
            assertEquals(TEST_TRAINER_ID, userCaptor.getValue().getTrainerId());
            
            verify(invitationRepository).update(eq(testInvitation.getId()), any(Invitation.class));
        }

        @Test
        @DisplayName("Should throw exception when invitation not found")
        void acceptInvitation_WhenInvitationNotFound_ShouldThrowException() {
            // Arrange
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(InvitationNotFoundException.class, 
                    () -> invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID));
            verify(userService, never()).updateUser(anyString(), any());
            verify(invitationRepository, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Should throw exception when invitation has expired")
        void acceptInvitation_WhenInvitationExpired_ShouldThrowException() {
            // Arrange
            long pastTime = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            testInvitation.setExpiresAt(pastTime);
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(testInvitation));

            // Act & Assert
            assertThrows(InvitationExpiredException.class, 
                    () -> invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID));
            verify(userService, never()).updateUser(anyString(), any());
            verify(invitationRepository, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Should throw exception when invitation already used")
        void acceptInvitation_WhenInvitationAlreadyUsed_ShouldThrowException() {
            // Arrange
            testInvitation.setStatus(InvitationStatus.ACCEPTED);
            when(invitationRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(testInvitation));

            // Act & Assert
            assertThrows(InvitationAlreadyUsedException.class, 
                    () -> invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID));
            verify(userService, never()).updateUser(anyString(), any());
            verify(invitationRepository, never()).update(anyString(), any());
        }
    }

    @Nested
    @DisplayName("getMyInvitations Tests")
    class GetMyInvitationsTests {

        @Test
        @DisplayName("Should return invitations for current trainer")
        void getMyInvitations_WhenTrainerHasInvitations_ShouldReturnList() {
            // Arrange
            Invitation invitation1 = Invitation.builder()
                    .id("inv1")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail("client1@example.com")
                    .code("TR-ABC111")
                    .status(InvitationStatus.PENDING)
                    .build();

            Invitation invitation2 = Invitation.builder()
                    .id("inv2")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail("client2@example.com")
                    .code("TR-ABC222")
                    .status(InvitationStatus.ACCEPTED)
                    .build();

            List<Invitation> invitations = Arrays.asList(invitation1, invitation2);
            
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(invitationRepository.findByTrainerId(TEST_TRAINER_ID)).thenReturn(invitations);

            // Act
            List<Invitation> result = invitationService.getMyInvitations();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(invitations, result);
            verify(invitationRepository).findByTrainerId(TEST_TRAINER_ID);
        }

        @Test
        @DisplayName("Should throw exception when current user ID is null")
        void getMyInvitations_WhenCurrentUserIdIsNull_ShouldThrowException() {
            // Arrange
            when(userService.getCurrentUserId()).thenReturn(null);

            // Act & Assert
            assertThrows(UnauthorizedInvitationException.class, 
                    () -> invitationService.getMyInvitations());
            verify(invitationRepository, never()).findByTrainerId(anyString());
        }
    }

    @Nested
    @DisplayName("expireOldInvitations Tests")
    class ExpireOldInvitationsTests {

        @Test
        @DisplayName("Should expire pending invitations that have passed expiration date")
        void expireOldInvitations_WhenExpiredInvitationsExist_ShouldExpireThem() {
            // Arrange
            long yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            
            Invitation expiredInv1 = Invitation.builder()
                    .id("inv1")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail("client1@example.com")
                    .code("TR-EXP001")
                    .status(InvitationStatus.PENDING)
                    .createdAt(Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli())
                    .expiresAt(yesterday)
                    .build();

            Invitation expiredInv2 = Invitation.builder()
                    .id("inv2")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail("client2@example.com")
                    .code("TR-EXP002")
                    .status(InvitationStatus.PENDING)
                    .createdAt(Instant.now().minus(10, ChronoUnit.DAYS).toEpochMilli())
                    .expiresAt(Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            List<Invitation> expiredInvitations = Arrays.asList(expiredInv1, expiredInv2);
            
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(expiredInvitations);
            doNothing().when(invitationRepository).update(anyString(), any(Invitation.class));

            // Act
            invitationService.expireOldInvitations();

            // Assert
            ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
            verify(invitationRepository, times(2)).update(anyString(), captor.capture());
            
            List<Invitation> updatedInvitations = captor.getAllValues();
            assertEquals(2, updatedInvitations.size());
            assertTrue(updatedInvitations.stream()
                    .allMatch(inv -> inv.getStatus() == InvitationStatus.EXPIRED));
        }

        @Test
        @DisplayName("Should do nothing when no expired invitations exist")
        void expireOldInvitations_WhenNoExpiredInvitations_ShouldDoNothing() {
            // Arrange
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(List.of());

            // Act
            invitationService.expireOldInvitations();

            // Assert
            verify(invitationRepository).findExpiredPendingInvitations(anyLong());
            verify(invitationRepository, never()).update(anyString(), any());
        }

        @Test
        @DisplayName("Should continue processing even if one invitation fails to update")
        void expireOldInvitations_WhenOneUpdateFails_ShouldContinueWithOthers() {
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
                    .expiresAt(Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            List<Invitation> expiredInvitations = Arrays.asList(expiredInv1, expiredInv2);
            
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenReturn(expiredInvitations);
            
            // First update succeeds, second fails
            doNothing().doThrow(new RuntimeException("Update failed"))
                    .when(invitationRepository).update(anyString(), any(Invitation.class));

            // Act
            invitationService.expireOldInvitations();

            // Assert - should attempt both updates
            verify(invitationRepository, times(2)).update(anyString(), any(Invitation.class));
        }

        @Test
        @DisplayName("Should handle exception gracefully when repository fails")
        void expireOldInvitations_WhenRepositoryFails_ShouldHandleGracefully() {
            // Arrange
            when(invitationRepository.findExpiredPendingInvitations(anyLong()))
                    .thenThrow(new RuntimeException("Firestore connection error"));

            // Act - should not throw exception
            assertDoesNotThrow(() -> invitationService.expireOldInvitations());

            // Assert
            verify(invitationRepository).findExpiredPendingInvitations(anyLong());
            verify(invitationRepository, never()).update(anyString(), any());
        }
    }
}
