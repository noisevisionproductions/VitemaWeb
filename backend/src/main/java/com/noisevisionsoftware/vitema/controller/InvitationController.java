package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.invitation.AcceptInvitationRequest;
import com.noisevisionsoftware.vitema.dto.request.invitation.InvitationRequest;
import com.noisevisionsoftware.vitema.dto.response.MessageResponse;
import com.noisevisionsoftware.vitema.dto.response.invitation.InvitationResponse;
import com.noisevisionsoftware.vitema.mapper.invitation.InvitationMapper;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.service.UserService;
import com.noisevisionsoftware.vitema.service.invitation.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {
    private final InvitationService invitationService;
    private final InvitationMapper invitationMapper;
    private final UserService userService;

    /**
     * Creates and sends an invitation to a client.
     * Available for TRAINER and ADMIN roles.
     * <p>
     * POST /api/invitations/send
     * Body: { "email": "client@example.com" }
     *
     * @param request the invitation request containing client email
     * @return the created invitation with invitation code
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN', 'OWNER')")
    public ResponseEntity<InvitationResponse> sendInvitation(@Valid @RequestBody InvitationRequest request) {
        log.info("Creating invitation for email: {}", request.getEmail());

        Invitation invitation = invitationService.createInvitation(request.getEmail());
        InvitationResponse response = invitationMapper.toResponse(invitation);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Accepts an invitation using the provided code.
     * Available for authenticated users (mobile app).
     * <p>
     * POST /api/invitations/accept
     * Body: { "code": "TR-ABC123" }
     *
     * @param request the request containing the invitation code
     * @return success message
     */
    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        String userId = userService.getCurrentUserId();
        log.info("User {} accepting invitation with code: {}", userId, request.getCode());

        invitationService.acceptInvitation(request.getCode(), userId);

        return ResponseEntity.ok(new MessageResponse("Zaproszenie zostało zaakceptowane pomyślnie"));
    }

    /**
     * Gets all invitations created by the current trainer.
     * Available for TRAINER and ADMIN roles.
     * <p>
     * GET /api/invitations/my
     *
     * @return list of invitations
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN', 'OWNER')")
    public ResponseEntity<java.util.List<InvitationResponse>> getMyInvitations() {
        log.info("Fetching invitations for current trainer");

        java.util.List<com.noisevisionsoftware.vitema.model.invitation.Invitation> invitations = invitationService.getMyInvitations();
        java.util.List<InvitationResponse> response = invitations.stream()
                .map(invitationMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an invitation.
     * Available for TRAINER and ADMIN roles.
     * Only the trainer who created the invitation can delete it (or admin).
     * <p>
     * DELETE /api/invitations/{id}
     *
     * @param id the invitation ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN', 'OWNER')")
    public ResponseEntity<MessageResponse> deleteInvitation(@PathVariable String id) {
        log.info("Deleting invitation: {}", id);

        invitationService.deleteInvitation(id);

        return ResponseEntity.ok(new MessageResponse("Zaproszenie zostało usunięte"));
    }
}
