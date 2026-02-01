package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    @BeforeEach
    void setUp() {
        String frontendUrl = "https://vitema.pl";
        ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
        String fromEmail = "test@vitema.pl";
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendVerificationEmail_ShouldSendEmailWithCorrectParameters() {
        // Arrange
        NewsletterSubscriber subscriber = createTestSubscriber();
        String processedHtml = "<html>Processed template</html>";

        when(templateEngine.process(eq("email/content/verification-email-content"), any(Context.class)))
                .thenReturn("<p>Email content</p>");
        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(processedHtml);

        // Act
        emailService.sendVerificationEmail(subscriber);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
        verify(templateEngine, times(2)).process(anyString(), contextCaptor.capture());

        verify(templateEngine).process(eq("email/content/verification-email-content"), any(Context.class));
        verify(templateEngine).process(eq("email/layouts/base-layout"), any(Context.class));
    }

    @Test
    void sendWelcomeEmail_ShouldSendEmailWithCorrectParameters() {
        // Arrange
        NewsletterSubscriber subscriber = createTestSubscriber();
        String processedHtml = "<html>Processed welcome template</html>";

        when(templateEngine.process(eq("email/content/welcome-email-content"), any(Context.class)))
                .thenReturn("<p>Welcome content</p>");
        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(processedHtml);

        // Act
        emailService.sendWelcomeEmail(subscriber);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
        verify(templateEngine, times(2)).process(anyString(), contextCaptor.capture());

        verify(templateEngine).process(eq("email/content/welcome-email-content"), any(Context.class));
        verify(templateEngine).process(eq("email/layouts/base-layout"), any(Context.class));
    }

    @Test
    void sendCustomEmail_ShouldSendEmailWithCorrectParameters() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String htmlContent = "<p>Test content</p>";

        // Act
        emailService.sendCustomEmail(to, subject, htmlContent);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
    }

    @Test
    void sendTemplatedEmail_ShouldSendEmailWithCorrectParameters() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String templateName = "test-template";
        Map<String, Object> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("showUnsubscribe", true);

        String processedHtml = "<html>Processed templated email</html>";

        when(templateEngine.process(eq("email/content/" + templateName + "-content"), any(Context.class)))
                .thenReturn("<p>Templated content</p>");
        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(processedHtml);

        // Act
        emailService.sendTemplatedEmail(to, subject, templateName, variables);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));

        verify(templateEngine).process(eq("email/content/" + templateName + "-content"), any(Context.class));
        verify(templateEngine).process(eq("email/layouts/base-layout"), any(Context.class));
    }

    @Test
    void sendVerificationEmail_ShouldHandleException() {
        // Arrange
        NewsletterSubscriber subscriber = createTestSubscriber();
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<p>Content</p>");
        doThrow(new RuntimeException("Test exception")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.sendVerificationEmail(subscriber));
    }

    private NewsletterSubscriber createTestSubscriber() {
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail("test@example.com");
        subscriber.setVerificationToken(UUID.randomUUID().toString());
        return subscriber;
    }
}