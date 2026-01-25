package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.EmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SingleEmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.TargetedEmailRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.ExternalRecipientRepository;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.newsletter.AdminNewsletterService;
import com.noisevisionsoftware.vitema.service.email.newsletter.ExternalRecipientsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmailService {

    private final EmailService emailService;

    private final NewsletterSubscriberRepository subscriberRepository;
    private final ExternalRecipientRepository externalRecipientRepository;
    private final ExternalRecipientsService externalRecipientsService;
    private final EmailTemplateService emailTemplateService;
    private final AdminNewsletterService adminNewsletterService;

    /**
     * Wysyła pojedynczy email do wskazanego adresata
     */
    @Transactional
    public void sendSingleEmail(SingleEmailRequest request) {
        String emailContent = prepareContent(
                request.getSavedTemplateId() != null ? String.valueOf(request.getSavedTemplateId()) : null,
                request.getContent(),
                request.isUseTemplate(),
                request.getTemplateType()
        );

        emailService.sendCustomEmail(request.getRecipientEmail(), request.getSubject(), emailContent);

        if (request.getExternalRecipientId() != null && request.isUpdateLastContactDate()) {
            externalRecipientRepository.findById(request.getExternalRecipientId())
                    .ifPresent(recipient -> {
                        recipient.setLastContactDate(LocalDateTime.now());
                        externalRecipientRepository.save(recipient);
                    });
        }
    }

    /**
     * Wysyła wiadomość do wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    @Transactional
    public void sendBulkEmail(EmailRequest emailRequest) {
        List<String> recipients = subscriberRepository.findAllByActiveTrueAndVerifiedTrue()
                .stream()
                .map(NewsletterSubscriber::getEmail)
                .toList();

        if (recipients.isEmpty()) {
            log.warn("Nie znaleziono odbiorców dla masowego emaila");
            return;
        }

        String emailContent = prepareContent(null, emailRequest.getContent(), emailRequest.isUseTemplate(), emailRequest.getTemplateType());

        for (String email : recipients) {
            emailService.sendCustomEmail(email, emailRequest.getSubject(), emailContent);
        }

        updateLastEmailSentForSubscribers(recipients);
    }

    /**
     * Wysyła wiadomość do wybranych odbiorców (targetowanie)
     */
    @Transactional
    public Map<String, Object> sendTargetedBulkEmail(TargetedEmailRequest request) {
        Map<String, Object> result = new HashMap<>();

        TargetedRecipientsResult recipients = collectTargetedRecipients(request, result);

        if (recipients.allEmails().isEmpty()) {
            log.warn("Nie znaleziono odbiorców spełniających kryteria");
            result.put("message", "Nie znaleziono odbiorców spełniających kryteria");
            result.put("sentCount", 0);
            return result;
        }

        String emailContent = prepareContent(
                request.getSavedTemplateId(),
                request.getContent(),
                request.isUseTemplate(),
                request.getTemplateType()
        );

        for (String email : recipients.allEmails()) {
            emailService.sendCustomEmail(email, request.getSubject(), emailContent);
        }

        updateRecipientsStatus(request, recipients);

        result.put("message", "Zlecono wysyłkę do " + recipients.allEmails().size() + " odbiorców");
        result.put("sentCount", recipients.allEmails().size());

        return result;
    }

    /**
     * Renderuje podgląd treści wiadomości
     */
    public String renderEmailPreview(EmailRequest request) {
        return prepareContent(null, request.getContent(), request.isUseTemplate(), request.getTemplateType());
    }

    private String prepareContent(String templateId, String content, boolean useTemplate, String templateType) {
        if (templateId != null && !templateId.isEmpty()) {
            return emailTemplateService.renderSavedTemplate(
                    Long.valueOf(templateId),
                    content
            );
        } else if (useTemplate) {
            return emailTemplateService.applySystemTemplate(content, templateType);
        } else {
            return content;
        }
    }

    private void updateLastEmailSentForSubscribers(List<String> emails) {
        for (String email : emails) {
            try {
                adminNewsletterService.updateLastEmailSent(email);
            } catch (Exception e) {
                log.error("Nie udało się zaktualizować daty ostatniego maila dla {}", email, e);
            }
        }
    }

    private void updateRecipientsStatus(TargetedEmailRequest request, TargetedRecipientsResult recipients) {
        if ("subscribers".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType())) {
            updateLastEmailSentForSubscribers(recipients.subscriberEmails());
        }

        if (("external".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType()))
                && request.isUpdateStatus()) {
            for (Long id : recipients.externalIds()) {
                try {
                    externalRecipientsService.updateStatus(id, request.getNewStatus());
                } catch (Exception e) {
                    log.error("Nie udało się zaktualizować statusu dla external id {}", id, e);
                }
            }
        }
    }

    private TargetedRecipientsResult collectTargetedRecipients(TargetedEmailRequest request, Map<String, Object> result) {
        List<String> allEmails = new ArrayList<>();
        List<String> subscriberEmails = new ArrayList<>();
        List<Long> externalIds = new ArrayList<>();

        if ("subscribers".equals(request.getRecipientType()) || "mixed".equals(request.getRecipientType())) {
            List<NewsletterSubscriber> subscribers = getFilteredSubscribers(request.getSubscriberFilters());
            subscriberEmails = subscribers.stream()
                    .map(NewsletterSubscriber::getEmail)
                    .toList();

            allEmails.addAll(subscriberEmails);
            result.put("subscriberCount", subscriberEmails.size());
        }

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

    private boolean matchesSubscriberFilters(NewsletterSubscriber subscriber, Map<String, Object> filters) {
        if (filters.containsKey("active")) {
            boolean activeFilter = (boolean) filters.get("active");
            if (subscriber.isActive() != activeFilter) return false;
        } else if (!subscriber.isActive()) {
            return false;
        }

        if (filters.containsKey("verified")) {
            boolean verifiedFilter = (boolean) filters.get("verified");
            if (subscriber.isVerified() != verifiedFilter) return false;
        } else if (!subscriber.isVerified()) {
            return false;
        }

        if (filters.containsKey("role") && filters.get("role") != null) {
            String roleFilter = (String) filters.get("role");
            return "all".equals(roleFilter) || roleFilter.equals(subscriber.getRole().toString());
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private List<ExternalRecipient> getFilteredExternalRecipients(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return externalRecipientRepository.findAll();
        }

        List<ExternalRecipient> result = externalRecipientRepository.findAll();

        if (filters.containsKey("status") && filters.get("status") != null) {
            String statusFilter = (String) filters.get("status");
            if (!"all".equals(statusFilter)) {
                result = result.stream()
                        .filter(r -> statusFilter.equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        if (filters.containsKey("category") && filters.get("category") != null) {
            String categoryFilter = (String) filters.get("category");
            if (!"all".equals(categoryFilter)) {
                result = result.stream()
                        .filter(r -> categoryFilter.equals(r.getCategory()))
                        .collect(Collectors.toList());
            }
        }

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

    private record TargetedRecipientsResult(
            List<String> allEmails,
            List<String> subscriberEmails,
            List<Long> externalIds
    ) {
    }
}