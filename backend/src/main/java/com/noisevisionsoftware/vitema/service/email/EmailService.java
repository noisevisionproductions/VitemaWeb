package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(NewsletterSubscriber subscriber) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + subscriber.getVerificationToken();
            String privacyPolicyUrl = frontendUrl + "/privacy-policy";

            // Przygotuj zmienne dla szablonu treści
            Map<String, Object> contentVars = new HashMap<>();
            contentVars.put("verificationUrl", verificationUrl);
            contentVars.put("subscriber", subscriber);

            // Renderuj treść emaila
            String emailContent = templateEngine.process("email/content/verification-email-content", new Context(null, contentVars));

            // Przygotuj zmienne dla szablonu bazowego
            Map<String, Object> baseVars = new HashMap<>();
            baseVars.put("emailContent", emailContent);
            baseVars.put("privacyPolicyUrl", privacyPolicyUrl);
            baseVars.put("showUnsubscribe", false);

            // Renderuj pełny email z szablonu bazowego
            String htmlContent = templateEngine.process("email/layouts/base-layout", new Context(null, baseVars));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(subscriber.getEmail());
            helper.setSubject("Potwierdź zapis do newslettera Vitema");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania emaila weryfikacyjnego", e);
            throw new RuntimeException("Błąd podczas wysyłania emaila weryfikacyjnego", e);
        }
    }

    @Async
    public void sendWelcomeEmail(NewsletterSubscriber subscriber) {
        try {
            String unsubscribeUrl = frontendUrl + "/unsubscribe?email=" + subscriber.getEmail();
            String privacyPolicyUrl = frontendUrl + "/privacy-policy";

            // Przygotuj zmienne dla szablonu treści
            Map<String, Object> contentVars = new HashMap<>();
            contentVars.put("subscriber", subscriber);

            // Renderuj treść emaila
            String emailContent = templateEngine.process("email/content/welcome-email-content", new Context(null, contentVars));

            // Przygotuj zmienne dla szablonu bazowego
            Map<String, Object> baseVars = new HashMap<>();
            baseVars.put("emailContent", emailContent);
            baseVars.put("unsubscribeUrl", unsubscribeUrl);
            baseVars.put("privacyPolicyUrl", privacyPolicyUrl);
            baseVars.put("showUnsubscribe", true);

            // Renderuj pełny email z szablonu bazowego
            String htmlContent = templateEngine.process("email/layouts/base-layout", new Context(null, baseVars));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(subscriber.getEmail());
            helper.setSubject("Witamy w newsletterze Vitema!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Błąd podczas wysyłania emaila powitalnego", e);
            throw new RuntimeException("Błąd podczas wysyłania emaila powitalnego", e);
        }
    }

    /*
     * Wysyła niestandardowy email
     * */
    @Async
    public void sendCustomEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Błąd podczas wysyłania niestandardowego emaila", e);
            throw new RuntimeException("Błąd podczas wysyłania emaila", e);
        }
    }

    /*
     * Wysyła niestandardowy email z szablonu
     * */
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            String unsubscribeUrl = frontendUrl + "/unsubscribe?email=" + to;
            String privacyPolicyUrl = frontendUrl + "/privacy-policy";

            boolean showUnsubscribe = Boolean.TRUE.equals(variables.getOrDefault("showUnsubscribe", true));

            // Renderuj treść z oryginalnego szablonu
            String emailContent = templateEngine.process("email/content/" + templateName + "-content", new Context(null, variables));

            // Przygotuj dane dla szablonu bazowego
            Map<String, Object> baseVars = new HashMap<>();
            baseVars.put("emailContent", emailContent);
            baseVars.put("unsubscribeUrl", unsubscribeUrl);
            baseVars.put("privacyPolicyUrl", privacyPolicyUrl);
            baseVars.put("showUnsubscribe", showUnsubscribe);

            // Renderuj pełny email z szablonu bazowego
            String htmlContent = templateEngine.process("email/layouts/base-layout", new Context(null, baseVars));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Błąd podczas wysyłania templatedowego emaila", e);
            throw new RuntimeException("Błąd podczas wysyłania emaila z szablonu", e);
        }
    }
}