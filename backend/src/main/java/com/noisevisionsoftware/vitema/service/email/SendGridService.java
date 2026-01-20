package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.EmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SingleEmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.TargetedEmailRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.ExternalRecipientRepository;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.newsletter.AdminNewsletterService;
import com.noisevisionsoftware.vitema.service.newsletter.ExternalRecipientsService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.base-url:https://vitema.pl}")
    private String baseUrl;

    private final NewsletterSubscriberRepository subscriberRepository;
    private final ExternalRecipientRepository externalRecipientRepository;
    private final ExternalRecipientsService externalRecipientsService;
    private final EmailTemplateService emailTemplateService;
    private final AdminNewsletterService adminNewsletterService;

    /*
     * Wysyła pojedynczy email do wskazanego adresata z opcjonalnym powiązaniem z ExternalRecipient
     * */
    @Transactional
    public void sendSingleEmail(SingleEmailRequest request) throws IOException {
        String emailContent = "";

        if (request.getSavedTemplateId() != null) {
            emailContent = emailTemplateService.renderSavedTemplate(
                    request.getSavedTemplateId(),
                    request.getContent()
            );
        } else if (request.isUseTemplate()) {
            emailContent = emailTemplateService.applySystemTemplate(request.getContent(), request.getTemplateType());
        } else {
            emailContent = request.getContent();
        }

        Mail mail = createMailWithDirectRecipient(
                request.getSubject(),
                emailContent,
                request.getRecipientEmail(),
                request.getCategories()
        );

        if (request.getRecipientName() != null && !request.getRecipientName().isEmpty()) {
            Personalization personalization = mail.getPersonalization().getFirst();
            Email to = personalization.getTos().getFirst();
            to.setName(request.getRecipientName());
        }

        sendEmail(mail);

        if (request.getExternalRecipientId() != null && request.isUpdateLastContactDate()) {
            ExternalRecipient recipient = externalRecipientRepository.findById(request.getExternalRecipientId()).orElse(null);

            if (recipient != null) {
                recipient.setLastContactDate(LocalDateTime.now());
                externalRecipientRepository.save(recipient);
            } else {
                log.warn("External recipient with ID {} not found", request.getExternalRecipientId());
            }
        }
    }

    /**
     * Wysyła wiadomość do wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    @Transactional
    public void sendBulkEmail(EmailRequest emailRequest) throws IOException {
        List<String> recipients = subscriberRepository.findAllByActiveTrueAndVerifiedTrue()
                .stream()
                .map(NewsletterSubscriber::getEmail)
                .toList();

        if (recipients.isEmpty()) {
            log.warn("No recipients found for bulk email");
            return;
        }

        String emailContent = prepareEmailContent(emailRequest);
        Mail mail = createMailWithBcc(emailRequest.getSubject(), emailContent, recipients, emailRequest.getCategories());
        sendEmail(mail);
        updateLastEmailSentForSubscribers(recipients);
    }

    /**
     * Wysyła wiadomość do wybranych odbiorców (subskrybenci, zewnętrzni, mieszani)
     */
    @Transactional
    public Map<String, Object> sendTargetedBulkEmail(TargetedEmailRequest request) throws IOException {
        // Przygotowanie wyniku
        Map<String, Object> result = new HashMap<>();

        // Zbierz odbiorców
        TargetedRecipientsResult recipients = collectTargetedRecipients(request, result);

        if (recipients.emails().isEmpty()) {
            log.warn("No recipients found for targeted bulk email");
            result.put("message", "Nie znaleziono odbiorców spełniających kryteria");
            result.put("sentCount", 0);
            return result;
        }

        // Przygotuj treść emaila
        String emailContent = prepareEmailContent(request);

        // Wyślij email
        Mail mail = createMailWithBcc(request.getSubject(), emailContent, recipients.emails(), request.getCategories());
        sendEmail(mail);

        // Aktualizuj statusy
        updateRecipientsStatus(request, recipients);

        // Przygotuj wynik
        result.put("message", "Wiadomość została wysłana do " + recipients.emails().size() + " odbiorców");
        result.put("sentCount", recipients.emails().size());

        return result;
    }

    /**
     * Wysyła pojedynczy email do wskazanego adresata
     */
    public void sendEmail(String to, EmailRequest emailRequest) throws IOException {
        String emailContent = prepareEmailContent(emailRequest);
        Mail mail = createMailWithDirectRecipient(emailRequest.getSubject(), emailContent, to, emailRequest.getCategories());
        sendEmail(mail);
    }

    /**
     * Renderuje pogląd treści wiadomości z użyciem szablonu
     */
    public String renderEmailPreview(EmailRequest emailRequest) {
        return prepareEmailContent(emailRequest);
    }

    // Metody pomocnicze

    /**
     * Przygotowuje treść emaila z uwzględnieniem szablonu, jeśli wybrano
     */
    private String prepareEmailContent(EmailRequest emailRequest) {
        if (emailRequest.isUseTemplate()) {
            return emailTemplateService.applySystemTemplate(emailRequest.getContent(), emailRequest.getTemplateType());
        } else {
            return emailRequest.getContent();
        }
    }

    /**
     * Przygotowuje treść emaila z użyciem zapisanego szablonu lub szablonu systemowego
     */
    private String prepareEmailContent(TargetedEmailRequest request) {
        if (request.getSavedTemplateId() != null && !request.getSavedTemplateId().isEmpty()) {
            return emailTemplateService.renderSavedTemplate(
                    Long.valueOf(request.getSavedTemplateId()),
                    request.getContent()
            );
        } else {
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setSubject(request.getSubject());
            emailRequest.setContent(request.getContent());
            emailRequest.setUseTemplate(request.isUseTemplate());
            emailRequest.setTemplateType(request.getTemplateType());
            emailRequest.setCategories(request.getCategories());

            return prepareEmailContent(emailRequest);
        }
    }

    /**
     * Tworzy obiekt Mail z odbiorcami w BCC
     */
    private Mail createMailWithBcc(String subject, String content, List<String> recipients, List<String> categories) {
        Mail mail = new Mail();
        Email from = new Email(fromEmail, fromName);
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(new Content("text/html", content));

        addCategories(mail, categories);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(fromEmail));
        recipients.forEach(recipient -> personalization.addBcc(new Email(recipient)));
        mail.addPersonalization(personalization);

        return mail;
    }

    /**
     * Tworzy obiekt Mail z bezpośrednim odbiorcą
     */
    private Mail createMailWithDirectRecipient(String subject, String content, String recipient, List<String> categories) {
        Mail mail = new Mail();
        Email from = new Email(fromEmail, fromName);
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(new Content("text/html", content));

        addCategories(mail, categories);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(recipient));
        mail.addPersonalization(personalization);

        return mail;
    }

    /**
     * Dodaje kategorie do maila
     */
    private void addCategories(Mail mail, List<String> categories) {
        if (categories != null && !categories.isEmpty()) {
            categories.forEach(mail::addCategory);
        }
    }

    /**
     * Wysyła email przez sendgrid API
     */
    private void sendEmail(Mail mail) throws IOException {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 300) {
            log.error("Failed to send email. Status Code: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            throw new IOException("Failed to send email: " + response.getBody());
        }
    }

    /**
     * Aktualizuje datę ostatniego emaila dla subskrybentów
     */
    private void updateLastEmailSentForSubscribers(List<String> emails) {
        for (String email : emails) {
            try {
                adminNewsletterService.updateLastEmailSent(email);
            } catch (Exception e) {
                log.error("Failed to update last email sent for subscriber {}", email, e);
            }
        }
    }

    /**
     * Aktualizuje statusy odbiorców po wysłaniu
     */
    private void updateRecipientsStatus(TargetedEmailRequest request, TargetedRecipientsResult recipients) {
        if ("subscribers".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType())) {
            updateLastEmailSentForSubscribers(recipients.subscriberEmails());
        }

        if (("external".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType()))
                && request.isUpdateStatus()) {
            updateExternalRecipientsStatus(recipients.externalIds(), request.getNewStatus());
        }
    }

    /**
     * Aktualizuje statusy zewnętrznych odbiorców
     */
    private void updateExternalRecipientsStatus(List<Long> externalIds, String newStatus) {
        for (Long id : externalIds) {
            try {
                externalRecipientsService.updateStatus(id, newStatus);
            } catch (Exception e) {
                log.error("Failed to update status for external recipient {}", id, e);
            }
        }
    }

    /**
     * Zbiera odbiorców docelowych emaila
     */
    private TargetedRecipientsResult collectTargetedRecipients(TargetedEmailRequest request, Map<String, Object> result) {
        List<String> allEmails = new ArrayList<>();
        List<String> subscriberEmails = new ArrayList<>();
        List<Long> externalIds = new ArrayList<>();

        // Zbierz subskrybentów
        if ("subscribers".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType())) {
            List<NewsletterSubscriber> subscribers = getFilteredSubscribers(request.getSubscriberFilters());
            subscriberEmails = subscribers.stream()
                    .map(NewsletterSubscriber::getEmail)
                    .toList();

            allEmails.addAll(subscriberEmails);
            result.put("subscriberCount", subscriberEmails.size());
        }

        // Zbierz zewnętrznych odbiorców
        if ("external".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType())) {
            List<ExternalRecipient> externalRecipients = getExternalRecipients(request);

            List<String> externalEmails = externalRecipients.stream()
                    .map(ExternalRecipient::getEmail)
                    .toList();

            externalIds = externalRecipients.stream()
                    .map(ExternalRecipient::getId)
                    .toList();

            allEmails.addAll(externalEmails);
            result.put("externalCount", externalEmails.size());
        }

        return new TargetedRecipientsResult(allEmails, subscriberEmails, externalIds);
    }

    /**
     * Pobiera zewnętrznych odbiorców na podstawie kryteriów
     */
    private List<ExternalRecipient> getExternalRecipients(TargetedEmailRequest request) {
        if (request.getExternalRecipientIds() != null && !request.getExternalRecipientIds().isEmpty()) {
            List<Long> ids = request.getExternalRecipientIds().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            return externalRecipientsService.getRecipientsByIds(ids);
        } else {
            return getFilteredExternalRecipients(request.getExternalFilters());
        }
    }

    /**
     * Filtrowanie subskrybentów na podstawie przekazanych filtrów
     */
    private List<NewsletterSubscriber> getFilteredSubscribers(Map<String, Object> filters) {
        List<NewsletterSubscriber> allSubscribers = subscriberRepository.findAll();

        if (filters == null || filters.isEmpty()) {
            return allSubscribers.stream()
                    .filter(NewsletterSubscriber::isActive)
                    .filter(NewsletterSubscriber::isVerified)
                    .collect(Collectors.toList());
        }

        return allSubscribers.stream()
                .filter(subscriber -> matchesSubscriberFilters(subscriber, filters))
                .collect(Collectors.toList());
    }

    /**
     * Sprawdza, czy subskrybent pasuje do filtrów
     */
    private boolean matchesSubscriberFilters(NewsletterSubscriber subscriber, Map<String, Object> filters) {
        // Domyślnie wymagamy aktywnych
        if (filters.containsKey("active")) {
            boolean activeFilter = (boolean) filters.get("active");
            if (subscriber.isActive() != activeFilter) return false;
        } else if (!subscriber.isActive()) {
            return false;
        }

        // Domyślnie wymagamy zweryfikowanych
        if (filters.containsKey("verified")) {
            boolean verifiedFilter = (boolean) filters.get("verified");
            if (subscriber.isVerified() != verifiedFilter) return false;
        } else if (!subscriber.isVerified()) {
            return false;
        }

        // Filtrowanie po roli
        if (filters.containsKey("role") && filters.get("role") != null) {
            String roleFilter = (String) filters.get("role");
            return "all".equals(roleFilter) || roleFilter.equals(subscriber.getRole().toString());
        }

        return true;
    }

    /**
     * Filtrowanie zewnętrznych odbiorców na podstawie przekazanych filtrów
     */
    @SuppressWarnings("unchecked")
    private List<ExternalRecipient> getFilteredExternalRecipients(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return externalRecipientRepository.findAll();
        }

        List<ExternalRecipient> result = externalRecipientRepository.findAll();

        // Filtrowanie po statusie
        if (filters.containsKey("status") && filters.get("status") != null) {
            String statusFilter = (String) filters.get("status");
            if (!"all".equals(statusFilter)) {
                result = result.stream()
                        .filter(r -> statusFilter.equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // Filtrowanie po kategorii
        if (filters.containsKey("category") && filters.get("category") != null) {
            String categoryFilter = (String) filters.get("category");
            if (!"all".equals(categoryFilter)) {
                result = result.stream()
                        .filter(r -> categoryFilter.equals(r.getCategory()))
                        .collect(Collectors.toList());
            }
        }

        // Filtrowanie po tagach
        if (filters.containsKey("tags") && filters.get("tags") != null) {
            List<String> tagFilters = (List<String>) filters.get("tags");
            if (!tagFilters.isEmpty()) {
                List<ExternalRecipient> tagFilteredRecipients = externalRecipientRepository.findAllByTags(tagFilters);
                result = result.stream()
                        .filter(tagFilteredRecipients::contains)
                        .collect(Collectors.toList());
            }
        }

        return result;
    }

    /**
     * Klasa pomocnicza przechowująca wynik zbierania odbiorców
     */
    private record TargetedRecipientsResult(
            List<String> emails,
            List<String> subscriberEmails,
            List<Long> externalIds
    ) {
    }
}