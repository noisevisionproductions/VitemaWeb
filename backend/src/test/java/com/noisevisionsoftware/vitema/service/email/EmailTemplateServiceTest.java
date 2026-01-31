package com.noisevisionsoftware.vitema.service.email;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SavedTemplateRequest;
import com.noisevisionsoftware.vitema.model.newsletter.EmailTemplate;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    @Captor
    private ArgumentCaptor<EmailTemplate> templateCaptor;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private EmailTemplate testTemplate;
    private SavedTemplateRequest testTemplateRequest;

    @BeforeEach
    void setUp() {
        // Inicjalizacja przykładowego szablonu
        LocalDateTime now = LocalDateTime.now();
        testTemplate = EmailTemplate.builder()
                .id(1L)
                .name("Testowy szablon")
                .subject("Testowy temat")
                .content("Testowa treść {{content}}")
                .description("Testowy opis")
                .useTemplate(true)
                .templateType("promotional")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Inicjalizacja przykładowego żądania
        testTemplateRequest = new SavedTemplateRequest();
        testTemplateRequest.setId("1");
        testTemplateRequest.setName("Zaktualizowany szablon");
        testTemplateRequest.setSubject("Zaktualizowany temat");
        testTemplateRequest.setContent("Zaktualizowana treść {{content}}");
        testTemplateRequest.setDescription("Zaktualizowany opis");
        testTemplateRequest.setUseTemplate(true);
        testTemplateRequest.setTemplateType("promotional");
    }

    @Test
    void getAllTemplates_ShouldReturnAllTemplates() {
        // Arrange
        List<EmailTemplate> expectedTemplates = Arrays.asList(
                testTemplate,
                EmailTemplate.builder().id(2L).name("Drugi szablon").build()
        );
        when(emailTemplateRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedTemplates);

        // Act
        List<EmailTemplate> result = emailTemplateService.getAllTemplates();

        // Assert
        assertEquals(expectedTemplates.size(), result.size(), "Powinien zwrócić wszystkie szablony");
        assertEquals(expectedTemplates, result, "Zwrócone szablony powinny być zgodne z oczekiwanymi");
        verify(emailTemplateRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getTemplateById_ShouldReturnTemplate_WhenExists() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // Act
        Optional<EmailTemplate> result = emailTemplateService.getTemplateById(1L);

        // Assert
        assertTrue(result.isPresent(), "Powinien znaleźć szablon");
        assertEquals(testTemplate, result.get(), "Zwrócony szablon powinien być zgodny z oczekiwanym");
        verify(emailTemplateRepository).findById(1L);
    }

    @Test
    void getTemplateById_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        when(emailTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<EmailTemplate> result = emailTemplateService.getTemplateById(999L);

        // Assert
        assertFalse(result.isPresent(), "Nie powinien znaleźć nieistniejącego szablonu");
        verify(emailTemplateRepository).findById(999L);
    }

    @Test
    void saveTemplate_ShouldUpdateExistingTemplate_WhenIdProvided() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmailTemplate result = emailTemplateService.saveTemplate(testTemplateRequest);

        // Assert
        verify(emailTemplateRepository).findById(1L);
        verify(emailTemplateRepository).save(templateCaptor.capture());

        EmailTemplate capturedTemplate = templateCaptor.getValue();
        assertEquals(1L, capturedTemplate.getId(), "ID powinno pozostać niezmienione");
        assertEquals("Zaktualizowany szablon", capturedTemplate.getName(), "Nazwa powinna zostać zaktualizowana");
        assertEquals("Zaktualizowany temat", capturedTemplate.getSubject(), "Temat powinien zostać zaktualizowany");
        assertEquals("Zaktualizowana treść {{content}}", capturedTemplate.getContent(), "Treść powinna zostać zaktualizowana");
        assertEquals("Zaktualizowany opis", capturedTemplate.getDescription(), "Opis powinien zostać zaktualizowany");
        assertTrue(capturedTemplate.isUseTemplate(), "UseTemplate powinno zostać zaktualizowane");
        assertEquals("promotional", capturedTemplate.getTemplateType(), "TemplateType powinno zostać zaktualizowane");
        assertNotNull(capturedTemplate.getUpdatedAt(), "Data aktualizacji powinna zostać ustawiona");
        
        // Verify the returned result
        assertNotNull(result, "Wynik nie powinien być null");
        assertEquals(1L, result.getId(), "Zwrócone ID powinno być poprawne");
        assertEquals("Zaktualizowany szablon", result.getName(), "Zwrócona nazwa powinna być zaktualizowana");
    }

    @Test
    void saveTemplate_ShouldCreateNewTemplate_WhenNoIdProvided() {
        // Arrange
        testTemplateRequest.setId(null);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenAnswer(invocation -> {
            EmailTemplate savedTemplate = invocation.getArgument(0);
            // Symulacja generowania ID przez bazę danych
            return EmailTemplate.builder()
                    .id(99L)
                    .name(savedTemplate.getName())
                    .subject(savedTemplate.getSubject())
                    .content(savedTemplate.getContent())
                    .description(savedTemplate.getDescription())
                    .useTemplate(savedTemplate.isUseTemplate())
                    .templateType(savedTemplate.getTemplateType())
                    .createdAt(savedTemplate.getCreatedAt())
                    .updatedAt(savedTemplate.getUpdatedAt())
                    .build();
        });

        // Act
        EmailTemplate result = emailTemplateService.saveTemplate(testTemplateRequest);

        // Assert
        verify(emailTemplateRepository, never()).findById(any());
        verify(emailTemplateRepository).save(templateCaptor.capture());

        EmailTemplate capturedTemplate = templateCaptor.getValue();
        assertNull(capturedTemplate.getId(), "ID nie powinno być ustawione przed zapisem");
        assertEquals("Zaktualizowany szablon", capturedTemplate.getName(), "Nazwa powinna zostać ustawiona");
        assertEquals("Zaktualizowany temat", capturedTemplate.getSubject(), "Temat powinien zostać ustawiony");
        assertEquals("Zaktualizowana treść {{content}}", capturedTemplate.getContent(), "Treść powinna zostać ustawiona");
        assertEquals("Zaktualizowany opis", capturedTemplate.getDescription(), "Opis powinien zostać ustawiony");
        assertTrue(capturedTemplate.isUseTemplate(), "UseTemplate powinno zostać ustawione");
        assertEquals("promotional", capturedTemplate.getTemplateType(), "TemplateType powinno zostać ustawione");

        assertEquals(99L, result.getId(), "ID powinno być ustawione po zapisie");
        assertNotNull(result.getCreatedAt(), "Data utworzenia powinna zostać ustawiona");
        assertNotNull(result.getUpdatedAt(), "Data aktualizacji powinna zostać ustawiona");
    }

    @Test
    void deleteTemplate_ShouldDeleteTemplate() {
        // Arrange
        Long templateId = 1L;
        doNothing().when(emailTemplateRepository).deleteById(templateId);

        // Act
        emailTemplateService.deleteTemplate(templateId);

        // Assert
        verify(emailTemplateRepository).deleteById(templateId);
    }

    @Test
    void renderSavedTemplate_ShouldReplaceContentAndApplyTemplate_WhenUseTemplateIsTrue() {
        // Arrange
        String customContent = "Niestandardowa treść";
        String expectedProcessedContent = "<html><body>Przetworzona treść</body></html>";

        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(expectedProcessedContent);

        // Act
        String result = emailTemplateService.renderSavedTemplate(1L, customContent);

        // Assert
        assertEquals(expectedProcessedContent, result, "Przetworzona treść powinna być zgodna z oczekiwaną");
        verify(emailTemplateRepository).findById(1L);
        verify(templateEngine).process(eq("email/layouts/base-layout"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertTrue(capturedContext.getVariable("emailContent").toString().contains(customContent),
                "Zmienna emailContent powinna zawierać niestandardową treść");
        assertEquals(true, capturedContext.getVariable("isPromotional"),
                "Flaga isPromotional powinna być ustawiona dla typu promotional");
        assertNotNull(capturedContext.getVariable("promotionTitle"),
                "Tytuł promocji powinien być ustawiony dla typu promotional");
    }

    @Test
    void renderSavedTemplate_ShouldReturnContentOnly_WhenUseTemplateIsFalse() {
        // Arrange
        testTemplate.setUseTemplate(false);
        String customContent = "Niestandardowa treść";
        String expectedContent = "Testowa treść Niestandardowa treść";

        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // Act
        String result = emailTemplateService.renderSavedTemplate(1L, customContent);

        // Assert
        assertEquals(expectedContent, result, "Powinna zostać zwrócona treść z podmienionym {{content}}");
        verify(emailTemplateRepository).findById(1L);
        // Nie powinno być wywołania templateEngine.process()
        verify(templateEngine, never()).process(anyString(), any(Context.class));
    }

    @Test
    void renderSavedTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(emailTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> emailTemplateService.renderSavedTemplate(999L, "Treść"));

        assertEquals("Szablon o podanym ID nie istnieje", exception.getMessage(),
                "Powinien zostać rzucony wyjątek z odpowiednim komunikatem");
        verify(emailTemplateRepository).findById(999L);
    }

    @Test
    void applySystemTemplate_ShouldApplyPromotionalTemplate() {
        // Arrange
        String content = "Testowa treść";
        String templateType = "promotional";
        String expectedProcessedContent = "<html><body>Przetworzona treść promocyjna</body></html>";

        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(expectedProcessedContent);

        // Act
        String result = emailTemplateService.applySystemTemplate(content, templateType);

        // Assert
        assertEquals(expectedProcessedContent, result, "Przetworzona treść powinna być zgodna z oczekiwaną");
        verify(templateEngine).process(eq("email/layouts/base-layout"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertEquals(content, capturedContext.getVariable("emailContent"),
                "Zmienna emailContent powinna zawierać oryginalną treść");
        assertEquals(true, capturedContext.getVariable("isPromotional"),
                "Flaga isPromotional powinna być ustawiona");
        assertNotNull(capturedContext.getVariable("promotionTitle"),
                "Tytuł promocji powinien być ustawiony");
    }

    @Test
    void applySystemTemplate_ShouldApplySurveyTemplate() {
        // Arrange
        String content = "Testowa treść";
        String templateType = "survey";
        String expectedProcessedContent = "<html><body>Przetworzona treść ankiety</body></html>";

        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(expectedProcessedContent);

        // Act
        String result = emailTemplateService.applySystemTemplate(content, templateType);

        // Assert
        assertEquals(expectedProcessedContent, result, "Przetworzona treść powinna być zgodna z oczekiwaną");
        verify(templateEngine).process(eq("email/layouts/base-layout"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertEquals(content, capturedContext.getVariable("emailContent"),
                "Zmienna emailContent powinna zawierać oryginalną treść");
        assertEquals(true, capturedContext.getVariable("isSurvey"),
                "Flaga isSurvey powinna być ustawiona");
        assertEquals("#survey", capturedContext.getVariable("surveyUrl"),
                "URL ankiety powinien być ustawiony");
    }

    @Test
    void applySystemTemplate_ShouldApplyAnnouncementTemplate() {
        // Arrange
        String content = "Testowa treść";
        String templateType = "announcement";
        String expectedProcessedContent = "<html><body>Przetworzona treść ogłoszenia</body></html>";

        when(templateEngine.process(eq("email/layouts/base-layout"), any(Context.class)))
                .thenReturn(expectedProcessedContent);

        // Act
        String result = emailTemplateService.applySystemTemplate(content, templateType);

        // Assert
        assertEquals(expectedProcessedContent, result, "Przetworzona treść powinna być zgodna z oczekiwaną");
        verify(templateEngine).process(eq("email/layouts/base-layout"), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertEquals(content, capturedContext.getVariable("emailContent"),
                "Zmienna emailContent powinna zawierać oryginalną treść");
        assertEquals(true, capturedContext.getVariable("isAnnouncement"),
                "Flaga isAnnouncement powinna być ustawiona");
        assertEquals("Ważna informacja", capturedContext.getVariable("announcementTitle"),
                "Tytuł ogłoszenia powinien być ustawiony");
    }
}