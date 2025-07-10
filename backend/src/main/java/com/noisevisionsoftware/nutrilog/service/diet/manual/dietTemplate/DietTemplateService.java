package com.noisevisionsoftware.nutrilog.service.diet.manual.dietTemplate;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.DietTemplateResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.DietTemplateStatsResponse;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.mapper.diet.DietTemplateMapper;
import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplate;
import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplateCategory;
import com.noisevisionsoftware.nutrilog.repository.diet.DietTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietTemplateService {

    private final DietTemplateMapper dietTemplateMapper;
    private final DietTemplateRepository dietTemplateRepository;
    private final DietTemplateConverter dietTemplateConverter;

    public DietTemplate saveTemplate(DietTemplate template) {
        if (template.getId() == null) {
            template.setId(UUID.randomUUID().toString());
            template.setCreatedAt(Timestamp.now());
            template.setVersion(1);
            template.setUsageCount(0);
        }
        template.setUpdatedAt(Timestamp.now());

        return dietTemplateRepository.save(template);
    }

    public void incrementUsageCount(String templateId) {
        Optional<DietTemplate> templateOpt = dietTemplateRepository.findById(templateId);
        if (templateOpt.isPresent()) {
            DietTemplate t = templateOpt.get();
            t.setUsageCount(t.getUsageCount() + 1);
            t.setLastUsed(Timestamp.now());
            saveTemplate(t);
        }
    }

    @Transactional(readOnly = true)
    public List<DietTemplateResponse> getAllTemplatesForUser(String userId) {
        List<DietTemplate> templates = dietTemplateRepository.findByCreatedBy(userId);
        return templates.stream()
                .map(dietTemplateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DietTemplateResponse getTemplateById(String id) {
        DietTemplate template = dietTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Szablon nie został znaleziony: " + id));
        return dietTemplateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<DietTemplateResponse> getTemplatesByCategory(String categoryStr, String userId) {
        DietTemplateCategory category = DietTemplateCategory.valueOf(categoryStr.toUpperCase());
        List<DietTemplate> templates = dietTemplateRepository.findByCategoryAndCreatedBy(category, userId);
        return templates.stream()
                .map(dietTemplateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DietTemplateResponse> getMostUsedTemplates(String userId, int limit) {
        List<DietTemplate> templates = dietTemplateRepository.findTopByCreatedByOrderByUsageCountDesc(userId, limit);
        return templates.stream()
                .map(dietTemplateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DietTemplateResponse> searchTemplates(String query, String userId, int limit) {
        List<DietTemplate> templates = dietTemplateRepository.searchByNameOrDescription(query, userId, limit);
        return templates.stream()
                .map(dietTemplateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DietTemplateResponse createTemplate(DietTemplateRequest request, String userId) {
        DietTemplate template = dietTemplateMapper.fromRequest(request, userId);
        DietTemplate saved = saveTemplate(template);
        return dietTemplateMapper.toResponse(saved);
    }

    @Transactional
    public DietTemplateResponse createTemplateFromManualDiet(DietTemplateRequest request, String userId) {
        if (request.getDietData() == null) {
            throw new IllegalArgumentException("Dane diety są wymagane");
        }

        DietTemplate template = dietTemplateConverter.convertFromManualDiet(
                request.getDietData(),
                request.getName(),
                request.getDescription(),
                DietTemplateCategory.valueOf(request.getCategory().toUpperCase()),
                userId
        );

        if (request.getNotes() != null) {
            template.setNotes(request.getNotes());
        }

        DietTemplate saved = saveTemplate(template);
        return dietTemplateMapper.toResponse(saved);
    }

    @Transactional
    public DietTemplateResponse updateTemplate(String id, DietTemplateRequest request, String userId) {
        DietTemplate existing = dietTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Szablon nie został znaleziony: " + id));

        if (!existing.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Brak uprawnień do edycji tego szablonu");
        }

        DietTemplate updated = dietTemplateMapper.updateFromRequest(existing, request);
        DietTemplate saved = saveTemplate(updated);
        return dietTemplateMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTemplate(String id, String userId) {
        DietTemplate template = dietTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Szablon nie został znaleziony: " + id));

        if (!template.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Brak uprawnień do usunięcia tego szablonu");
        }

        dietTemplateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DietTemplateStatsResponse getTemplateStats(String userId) {
        List<DietTemplate> allTemplates = dietTemplateRepository.findByCreatedBy(userId);

        Map<String, Long> byCategory = allTemplates.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.counting()
                ));

        DietTemplate mostUsed = allTemplates.stream()
                .max(Comparator.comparing(DietTemplate::getUsageCount))
                .orElse(null);

        DietTemplate newest = allTemplates.stream()
                .max(Comparator.comparing(DietTemplate::getCreatedAt))
                .orElse(null);

        long totalUsage = allTemplates.stream()
                .mapToLong(DietTemplate::getUsageCount)
                .sum();

        return DietTemplateStatsResponse.builder()
                .totalTemplates(allTemplates.size())
                .templatesByCategory(byCategory)
                .mostUsedTemplate(mostUsed != null ? dietTemplateMapper.toResponse(mostUsed) : null)
                .newestTemplate(newest != null ? dietTemplateMapper.toResponse(newest) : null)
                .totalUsageCount(totalUsage)
                .build();
    }
}
