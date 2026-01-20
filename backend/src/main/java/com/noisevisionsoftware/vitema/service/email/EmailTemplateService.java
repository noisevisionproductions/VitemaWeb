package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SavedTemplateRequest;
import com.noisevisionsoftware.vitema.model.newsletter.EmailTemplate;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateEngine templateEngine;

    /*
     * Pobiera wszystkie zapisane szablony email
     * */
    @Transactional(readOnly = true)
    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAllByOrderByCreatedAtDesc();
    }

    /*
     * Pobiera szablon emaila po jego ID
     * */
    @Transactional(readOnly = true)
    public Optional<EmailTemplate> getTemplateById(Long id) {
        return emailTemplateRepository.findById(id);
    }

    /*
     * Zapisuje nowy szablon emaila
     * */
    @Transactional
    public EmailTemplate saveTemplate(SavedTemplateRequest request) {
        EmailTemplate template;
        LocalDateTime now = LocalDateTime.now();

        if (request.getId() != null) {
            // Aktualizacja istniejącego szablonu
            template = emailTemplateRepository.findById(Long.valueOf(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Szablon o podanym ID nie istnieje"));

            template.setName(request.getName());
            template.setSubject(request.getSubject());
            template.setContent(request.getContent());
            template.setDescription(request.getDescription());
            template.setUseTemplate(request.isUseTemplate());
            template.setTemplateType(request.getTemplateType());
            template.setUpdatedAt(now);
        } else {
            // Tworzenie nowego szablonu
            template = EmailTemplate.builder()
                    .name(request.getName())
                    .subject(request.getSubject())
                    .content(request.getContent())
                    .description(request.getDescription())
                    .useTemplate(request.isUseTemplate())
                    .templateType(request.getTemplateType())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        return emailTemplateRepository.save(template);
    }

    /*
     * Usuwa szablon emaila
     * */
    @Transactional
    public void deleteTemplate(Long id) {
        emailTemplateRepository.deleteById(id);
    }

    /*
     * Renderuje treść emaila z zużyciem zapisanego szablonu
     * */
    public String renderSavedTemplate(Long templateId, String customContent) {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Szablon o podanym ID nie istnieje"));

        String content = template.getContent();

        if (customContent != null && !customContent.isEmpty()) {
            content = content.replace("{{content}}", customContent);
        }

        if (template.isUseTemplate()) {
            return applySystemTemplate(content, template.getTemplateType());
        } else {
            return content;
        }
    }

    /*
     * Stosuje systemowy szablon do treści emaila
     * */
    protected String applySystemTemplate(String content, String templateType) {
        Context context = new Context();
        context.setVariable("emailContent", content);

        Map<String, Object> additionalParams = new HashMap<>();

        switch (templateType) {
            case "promotional":
                additionalParams.put("isPromotional", true);
                additionalParams.put("promotionTitle", "Specjalna oferta!");
                additionalParams.put("promotionSubtitle", "Sprawdź nasze najnowsze promocje.");
                break;
            case "survey":
                additionalParams.put("isSurvey", true);
                additionalParams.put("surveyUrl", "#survey");
                break;
            case "announcement":
                additionalParams.put("isAnnouncement", true);
                additionalParams.put("announcementTitle", "Ważna informacja");
                break;
            default:
                break;
        }

        additionalParams.forEach(context::setVariable);

        return templateEngine.process("email/layouts/base-layout", context);
    }
}
