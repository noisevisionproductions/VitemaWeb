package com.noisevisionsoftware.vitema.service.invitation;

import com.noisevisionsoftware.vitema.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending invitation-related emails.
 * This is a placeholder implementation with logging.
 * You can integrate it with JavaMail, SendGrid, or another email provider later.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationEmailService {

    private final EmailService emailService;

    /**
     * Sends an invitation email to a client with the pairing code.
     *
     * @param to          the recipient's email address
     * @param code        the unique invitation code
     * @param trainerName the name of the trainer sending the invitation
     */
    public void sendInvitationEmail(String to, String code, String trainerName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("trainerName", trainerName);
        variables.put("showUnsubscribe", false);

        emailService.sendTemplatedEmail(
                to,
                "Zaproszenie od trenera " + trainerName,
                "invitation-email",
                variables
        );
    }
}
