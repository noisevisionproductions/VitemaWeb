package com.noisevisionsoftware.vitema.service.invitation;

import com.noisevisionsoftware.vitema.exception.*;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.InvitationRepository;
import com.noisevisionsoftware.vitema.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final InvitationEmailService invitationEmailService;
    
    private static final int CODE_LENGTH = 6;
    private static final String CODE_PREFIX = "TR-";
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int EXPIRATION_DAYS = 7;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Creates a new invitation for a client.
     * Only users with ADMIN or TRAINER role can create invitations.
     * 
     * @param clientEmail the email address of the client to invite
     * @return the created invitation
     * @throws UnauthorizedInvitationException if the current user is not authorized
     * @throws InvitationAlreadyExistsException if pending invitation already exists for this email
     */
    public Invitation createInvitation(String clientEmail) {
        // Get current trainer ID
        String trainerId = userService.getCurrentUserId();
        if (trainerId == null) {
            throw new UnauthorizedInvitationException("Nie można zidentyfikować użytkownika");
        }

        // Check if trainer has permissions
        User trainer = userService.getUserById(trainerId);
        if (!hasInvitationPermission(trainer.getRole())) {
            throw new UnauthorizedInvitationException("Brak uprawnień do tworzenia zaproszeń");
        }

        // Check for duplicate - prevent multiple pending invitations for same email
        invitationRepository.findPendingByClientEmail(clientEmail).ifPresent(existingInvitation -> {
            throw new InvitationAlreadyExistsException(
                    "Zaproszenie dla adresu " + clientEmail + " już istnieje (kod: " + existingInvitation.getCode() + ")"
            );
        });

        // Generate unique code
        String code = generateUniqueCode();

        // Calculate expiration timestamp (7 days from now)
        long now = Instant.now().toEpochMilli();
        long expiresAt = Instant.now().plus(EXPIRATION_DAYS, ChronoUnit.DAYS).toEpochMilli();

        // Create invitation
        Invitation invitation = Invitation.builder()
                .trainerId(trainerId)
                .clientEmail(clientEmail)
                .code(code)
                .status(InvitationStatus.PENDING)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        // Save to Firestore
        Invitation savedInvitation = invitationRepository.save(invitation);

        // Try to send invitation email - if it fails, rollback the saved invitation
        try {
            String trainerName = trainer.getNickname() != null ? trainer.getNickname() : trainer.getEmail();
            invitationEmailService.sendInvitationEmail(clientEmail, code, trainerName);
            log.info("Invitation created: code={}, trainerId={}, clientEmail={}", code, trainerId, clientEmail);
        } catch (Exception e) {
            // Rollback: Delete the invitation from database if email sending fails
            log.error("Email sending failed, rolling back invitation: id={}, code={}", savedInvitation.getId(), code, e);
            try {
                invitationRepository.delete(savedInvitation.getId());
                log.info("Successfully rolled back invitation: id={}", savedInvitation.getId());
            } catch (Exception rollbackException) {
                log.error("Failed to rollback invitation after email error: id={}", savedInvitation.getId(), rollbackException);
            }
            // Re-throw the exception so the frontend gets an error
            throw new RuntimeException("Nie udało się wysłać emaila z zaproszeniem. Spróbuj ponownie.", e);
        }

        return savedInvitation;
    }

    /**
     * Accepts an invitation using the provided code and assigns the trainer to the user.
     * 
     * @param code the invitation code
     * @param userId the ID of the user accepting the invitation
     * @return the accepted invitation
     * @throws InvitationNotFoundException if invitation is not found
     * @throws InvitationExpiredException if invitation has expired
     * @throws InvitationAlreadyUsedException if invitation has already been used
     */
    public Invitation acceptInvitation(String code, String userId) {
        // Find invitation by code
        Invitation invitation = invitationRepository.findByCode(code)
                .orElseThrow(() -> new InvitationNotFoundException("Zaproszenie o kodzie " + code + " nie zostało znalezione"));

        // Check if invitation has expired
        long now = Instant.now().toEpochMilli();
        if (now > invitation.getExpiresAt()) {
            throw new InvitationExpiredException("Zaproszenie wygasło");
        }

        // Check if invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationAlreadyUsedException("Zaproszenie zostało już użyte");
        }

        // Assign trainer to user
        User user = userService.getUserById(userId);
        user.setTrainerId(invitation.getTrainerId());
        userService.updateUser(userId, user);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.update(invitation.getId(), invitation);

        log.info("Invitation accepted: code={}, userId={}, trainerId={}", code, userId, invitation.getTrainerId());

        return invitation;
    }

    /**
     * Generates a unique invitation code.
     * Format: TR-XXXXXX (6 random alphanumeric characters)
     * 
     * @return unique invitation code
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = CODE_PREFIX + generateRandomString();
            attempts++;
            
            if (attempts >= maxAttempts) {
                log.error("Failed to generate unique code after {} attempts", maxAttempts);
                throw new RuntimeException("Nie udało się wygenerować unikalnego kodu");
            }
        } while (invitationRepository.existsByCode(code));

        return code;
    }

    /**
     * Generates a random alphanumeric string.
     *
     * @return random string
     */
    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(InvitationService.CODE_LENGTH);
        for (int i = 0; i < InvitationService.CODE_LENGTH; i++) {
            sb.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Gets all invitations for the current trainer.
     * 
     * @return list of invitations
     */
    public java.util.List<Invitation> getMyInvitations() {
        String trainerId = userService.getCurrentUserId();
        if (trainerId == null) {
            throw new UnauthorizedInvitationException("Nie można zidentyfikować użytkownika");
        }

        return invitationRepository.findByTrainerId(trainerId);
    }

    /**
     * Deletes an invitation.
     * Only the trainer who created the invitation (or admin) can delete it.
     * 
     * @param invitationId the ID of the invitation to delete
     * @throws InvitationNotFoundException if invitation is not found
     * @throws UnauthorizedInvitationException if user doesn't have permission to delete
     */
    public void deleteInvitation(String invitationId) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedInvitationException("Nie można zidentyfikować użytkownika");
        }

        // Find the invitation
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Zaproszenie o ID " + invitationId + " nie zostało znalezione"));

        // Check if user has permission to delete (must be the trainer who created it, or admin)
        boolean isOwner = invitation.getTrainerId().equals(currentUserId);
        boolean isAdmin = userService.isCurrentUserAdminOrOwner();

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedInvitationException("Nie masz uprawnień do usunięcia tego zaproszenia");
        }

        // Delete the invitation
        invitationRepository.delete(invitationId);
        log.info("Invitation deleted: id={}, code={}, deletedBy={}", invitationId, invitation.getCode(), currentUserId);
    }

    /**
     * Automatically expires old invitations.
     * This method runs daily at 2:00 AM and updates all pending invitations 
     * that have passed their expiration date to EXPIRED status.
     * 
     * Scheduled using cron expression: "0 0 2 * * ?" (second minute hour day month weekday)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireOldInvitations() {
        log.info("Starting automatic expiration of old invitations");
        
        try {
            long currentTime = Instant.now().toEpochMilli();
            
            // Find all expired pending invitations
            List<Invitation> expiredInvitations = invitationRepository.findExpiredPendingInvitations(currentTime);
            
            if (expiredInvitations.isEmpty()) {
                log.info("No expired invitations found");
                return;
            }
            
            // Update each expired invitation to EXPIRED status
            int successCount = 0;
            int failureCount = 0;
            
            for (Invitation invitation : expiredInvitations) {
                try {
                    invitation.setStatus(InvitationStatus.EXPIRED);
                    invitationRepository.update(invitation.getId(), invitation);
                    successCount++;
                    log.debug("Expired invitation: id={}, code={}, clientEmail={}", 
                            invitation.getId(), invitation.getCode(), invitation.getClientEmail());
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to expire invitation: id={}, code={}", 
                            invitation.getId(), invitation.getCode(), e);
                }
            }
            
            log.info("Expired {} invitations successfully (failures: {})", successCount, failureCount);
            
        } catch (Exception e) {
            log.error("Error during automatic invitation expiration", e);
        }
    }

    /**
     * Checks if the user role has permission to create invitations.
     * 
     * @param role the user role
     * @return true if has permission, false otherwise
     */
    private boolean hasInvitationPermission(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.TRAINER || role == UserRole.OWNER;
    }
}
