package integration.service.email;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class EmailServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        JavaMailSenderImpl sender = getJavaMailSender();

        when(templateEngine.process(anyString(), any())).thenReturn("<html><body>Test email content</body></html>");

        emailService = new EmailService(sender, templateEngine);
        String frontendUrl = "https://test-vitema.pl";
        ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
        String fromEmail = "test@vitema.pl";
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);

        greenMail.reset();
    }

    private static JavaMailSenderImpl getJavaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("localhost");
        sender.setPort(3025);

        sender.setUsername("");
        sender.setPassword("");

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.starttls.required", "false");
        props.put("mail.debug", "true");
        sender.setJavaMailProperties(props);
        return sender;
    }

    @Test
    void sendVerificationEmail_ShouldSendEmailCorrectly() throws MessagingException {
        // Arrange
        NewsletterSubscriber subscriber = createTestSubscriber();

        // Act
        emailService.sendVerificationEmail(subscriber);

        // Wait for message to arrive
        assertTrue(greenMail.waitForIncomingEmail(5000, 1),
                "Wiadomość e-mail powinna dotrzeć w ciągu 5 sekund");

        // Assert
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length, "Powinna zostać wysłana dokładnie jedna wiadomość");

        MimeMessage receivedMessage = receivedMessages[0];
        assertEquals(subscriber.getEmail(), receivedMessage.getAllRecipients()[0].toString(),
                "Email powinien być wysłany na prawidłowy adres");
        assertEquals("Potwierdź zapis do newslettera Vitema", receivedMessage.getSubject(),
                "Email powinien mieć prawidłowy temat");
    }

    @Test
    void sendWelcomeEmail_ShouldSendEmailCorrectly() throws MessagingException {
        // Arrange
        NewsletterSubscriber subscriber = createTestSubscriber();

        // Act
        emailService.sendWelcomeEmail(subscriber);

        // Wait for message to arrive
        assertTrue(greenMail.waitForIncomingEmail(5000, 1),
                "Wiadomość e-mail powinna dotrzeć w ciągu 5 sekund");

        // Assert
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length, "Powinna zostać wysłana dokładnie jedna wiadomość");

        MimeMessage receivedMessage = receivedMessages[0];
        assertEquals(subscriber.getEmail(), receivedMessage.getAllRecipients()[0].toString(),
                "Email powinien być wysłany na prawidłowy adres");
        assertEquals("Witamy w newsletterze Vitema!", receivedMessage.getSubject(),
                "Email powinien mieć prawidłowy temat");
    }

    @Test
    void sendCustomEmail_ShouldSendEmailCorrectly() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Custom Email";
        String htmlContent = "<html><body><h1>Test Content</h1></body></html>";

        // Act
        emailService.sendCustomEmail(to, subject, htmlContent);

        // Wait for message to arrive
        assertTrue(greenMail.waitForIncomingEmail(5000, 1),
                "Wiadomość e-mail powinna dotrzeć w ciągu 5 sekund");

        // Assert
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length, "Powinna zostać wysłana dokładnie jedna wiadomość");

        MimeMessage receivedMessage = receivedMessages[0];
        assertEquals(to, receivedMessage.getAllRecipients()[0].toString(),
                "Email powinien być wysłany na prawidłowy adres");
        assertEquals(subject, receivedMessage.getSubject(),
                "Email powinien mieć prawidłowy temat");

        String content = GreenMailUtil.getBody(receivedMessage);
        assertTrue(content.contains("Test Content"),
                "Treść emaila powinna zawierać oczekiwaną zawartość");
    }

    @Test
    void sendTemplatedEmail_ShouldSendEmailCorrectly() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Templated Email";
        String templateName = "welcome-email";
        Map<String, Object> variables = new HashMap<>();
        variables.put("testVariable", "testValue");

        // Act
        emailService.sendTemplatedEmail(to, subject, templateName, variables);

        // Wait for message to arrive
        assertTrue(greenMail.waitForIncomingEmail(5000, 1),
                "Wiadomość e-mail powinna dotrzeć w ciągu 5 sekund");

        // Assert
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length, "Powinna zostać wysłana dokładnie jedna wiadomość");

        MimeMessage receivedMessage = receivedMessages[0];
        assertEquals(to, receivedMessage.getAllRecipients()[0].toString(),
                "Email powinien być wysłany na prawidłowy adres");
        assertEquals(subject, receivedMessage.getSubject(),
                "Email powinien mieć prawidłowy temat");
    }

    private NewsletterSubscriber createTestSubscriber() {
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail("test@example.com");
        subscriber.setVerificationToken(UUID.randomUUID().toString());
        return subscriber;
    }
}