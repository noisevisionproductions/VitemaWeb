package com.noisevisionsoftware.vitema.service.email.newsletter;

import com.noisevisionsoftware.vitema.dto.request.newsletter.ExternalRecipientRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.ExternalRecipientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalRecipientsServiceTest {

    @Mock
    private ExternalRecipientRepository externalRecipientRepository;

    @InjectMocks
    private ExternalRecipientsService externalRecipientsService;

    @Captor
    private ArgumentCaptor<ExternalRecipient> recipientCaptor;

    private ExternalRecipient testRecipient;
    private ExternalRecipientRequest validRequest;

    @BeforeEach
    void setUp() {
        testRecipient = ExternalRecipient.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test Recipient")
                .category("Dietetyk")
                .status("new")
                .notes("Test notes")
                .createdAt(LocalDateTime.now().minusDays(1))
                .tags(new HashSet<>())
                .build();

        testRecipient.setTagList(Arrays.asList("tag1", "tag2"));

        validRequest = new ExternalRecipientRequest();
        validRequest.setEmail("new@example.com");
        validRequest.setName("New Recipient");
        validRequest.setCategory("Firma");
        validRequest.setTags(Arrays.asList("tag3", "tag4"));
        validRequest.setNotes("New notes");
        validRequest.setStatus("new");
    }

    @Test
    void getAllRecipients_ShouldReturnAllRecipients() {
        // Arrange
        List<ExternalRecipient> expectedRecipients = Arrays.asList(
                testRecipient,
                ExternalRecipient.builder()
                        .id(2L)
                        .email("other@example.com")
                        .name("Other Recipient")
                        .category("Firma")
                        .status("contacted")
                        .build()
        );
        when(externalRecipientRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedRecipients);

        // Act
        List<ExternalRecipient> result = externalRecipientsService.getAllRecipients();

        // Assert
        assertEquals(expectedRecipients.size(), result.size(), "Powinno zwrócić wszystkich odbiorców");
        assertEquals(expectedRecipients, result, "Powinna zwrócić tych samych odbiorców");
        verify(externalRecipientRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getRecipientsByIds_ShouldReturnRecipientsWithMatchingIds() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);
        List<ExternalRecipient> expectedRecipients = Arrays.asList(
                testRecipient,
                ExternalRecipient.builder()
                        .id(2L)
                        .email("other@example.com")
                        .name("Other Recipient")
                        .category("Firma")
                        .status("contacted")
                        .build()
        );
        when(externalRecipientRepository.findAllById(ids)).thenReturn(expectedRecipients);

        // Act
        List<ExternalRecipient> result = externalRecipientsService.getRecipientsByIds(ids);

        // Assert
        assertEquals(expectedRecipients.size(), result.size(), "Powinno zwrócić odbiorców o podanych ID");
        assertEquals(expectedRecipients, result, "Powinna zwrócić tych samych odbiorców");
        verify(externalRecipientRepository).findAllById(ids);
    }

    @Test
    void getRecipientsByIds_ShouldReturnEmptyList_WhenIdsIsNull() {
        // Act
        List<ExternalRecipient> result = externalRecipientsService.getRecipientsByIds(null);

        // Assert
        assertTrue(result.isEmpty(), "Powinno zwrócić pustą listę gdy ids jest null");
        verify(externalRecipientRepository, never()).findAllById(any());
    }

    @Test
    void getRecipientsByIds_ShouldReturnEmptyList_WhenIdsIsEmpty() {
        // Act
        List<ExternalRecipient> result = externalRecipientsService.getRecipientsByIds(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty(), "Powinno zwrócić pustą listę gdy ids jest pusta");
        verify(externalRecipientRepository, never()).findAllById(any());
    }

    @Test
    void addRecipient_ShouldAddNewRecipient_WhenEmailDoesNotExist() {
        // Arrange
        when(externalRecipientRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenAnswer(invocation -> {
            ExternalRecipient saved = invocation.getArgument(0);
            // Symulacja nadania ID przez bazę danych
            if (saved.getId() == null) {
                saved.setId(3L);
            }
            return saved;
        });

        // Act
        ExternalRecipient result = externalRecipientsService.addRecipient(validRequest);

        // Assert
        assertNotNull(result.getId(), "ID powinno być ustawione");
        assertEquals(validRequest.getEmail(), result.getEmail(), "Email powinien być ustawiony");
        assertEquals(validRequest.getName(), result.getName(), "Nazwa powinna być ustawiona");
        assertEquals(validRequest.getCategory(), result.getCategory(), "Kategoria powinna być ustawiona");
        assertEquals(validRequest.getStatus(), result.getStatus(), "Status powinien być ustawiony");
        assertEquals(validRequest.getNotes(), result.getNotes(), "Notatki powinny być ustawione");
        assertEquals(validRequest.getTags().size(), result.getTagList().size(), "Lista tagów powinna być ustawiona");
        verify(externalRecipientRepository).findByEmail(validRequest.getEmail());
        verify(externalRecipientRepository).save(any(ExternalRecipient.class));
    }

    @Test
    void addRecipient_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(externalRecipientRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(testRecipient));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> externalRecipientsService.addRecipient(validRequest));

        assertEquals("Odbiorca o podanym adresie email już istnieje", exception.getMessage(), "Powinien rzucić wyjątek z odpowiednim komunikatem");
        verify(externalRecipientRepository).findByEmail(validRequest.getEmail());
        verify(externalRecipientRepository, never()).save(any(ExternalRecipient.class));
    }

    @Test
    void bulkAddRecipients_ShouldAddMultipleRecipients() {
        // Arrange
        List<ExternalRecipientRequest> requests = Arrays.asList(
                validRequest,
                createRequestWithEmail("another@example.com")
        );

        when(externalRecipientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> result = externalRecipientsService.bulkAddRecipients(requests);

        // Assert
        assertEquals(2, result.get("added"), "Powinno dodać 2 odbiorców");
        @SuppressWarnings("unchecked")
        List<String> addedEmails = (List<String>) result.get("addedEmails");
        assertEquals(2, addedEmails.size(), "Powinno zawierać 2 emaile");
        assertTrue(addedEmails.contains(validRequest.getEmail()), "Powinno zawierać pierwszy email");
        assertTrue(addedEmails.contains("another@example.com"), "Powinno zawierać drugi email");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) result.get("errors");
        assertTrue(errors.isEmpty(), "Nie powinno być błędów");

        verify(externalRecipientRepository, times(2)).findByEmail(anyString());
        verify(externalRecipientRepository, times(2)).save(any(ExternalRecipient.class));
    }

    @Test
    void bulkAddRecipients_ShouldHandleErrors() {
        // Arrange
        List<ExternalRecipientRequest> requests = Arrays.asList(
                validRequest,
                createRequestWithEmail("existing@example.com")
        );

        when(externalRecipientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(externalRecipientRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testRecipient));
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> result = externalRecipientsService.bulkAddRecipients(requests);

        // Assert
        assertEquals(1, result.get("added"), "Powinno dodać 1 odbiorcę");
        @SuppressWarnings("unchecked")
        List<String> addedEmails = (List<String>) result.get("addedEmails");
        assertEquals(1, addedEmails.size(), "Powinno zawierać 1 email");
        assertTrue(addedEmails.contains(validRequest.getEmail()), "Powinno zawierać pierwszy email");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) result.get("errors");
        assertEquals(1, errors.size(), "Powinien być 1 błąd");
        assertEquals("existing@example.com", errors.getFirst().get("email"), "Błąd powinien dotyczyć istniejącego emaila");

        verify(externalRecipientRepository, times(2)).findByEmail(anyString());
        verify(externalRecipientRepository, times(1)).save(any(ExternalRecipient.class));
    }

    @Test
    void updateRecipient_ShouldUpdateRecipientFields() {
        // Arrange
        Long recipientId = 1L;
        ExternalRecipientRequest updateRequest = new ExternalRecipientRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setCategory("Updated Category");
        updateRequest.setTags(Arrays.asList("tag5", "tag6"));
        updateRequest.setNotes("Updated notes");

        when(externalRecipientRepository.findById(recipientId)).thenReturn(Optional.of(testRecipient));
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenReturn(testRecipient);

        // Act
        externalRecipientsService.updateRecipient(recipientId, updateRequest);

        // Assert
        verify(externalRecipientRepository).findById(recipientId);
        verify(externalRecipientRepository).save(recipientCaptor.capture());

        ExternalRecipient capturedRecipient = recipientCaptor.getValue();
        assertEquals(updateRequest.getName(), capturedRecipient.getName(), "Nazwa powinna być zaktualizowana");
        assertEquals(updateRequest.getCategory(), capturedRecipient.getCategory(), "Kategoria powinna być zaktualizowana");
        assertEquals(updateRequest.getNotes(), capturedRecipient.getNotes(), "Notatki powinny być zaktualizowane");
        assertEquals(updateRequest.getTags().size(), capturedRecipient.getTagList().size(), "Lista tagów powinna być zaktualizowana");
    }

    @Test
    void updateRecipient_ShouldThrowException_WhenRecipientNotFound() {
        // Arrange
        Long recipientId = 999L;
        ExternalRecipientRequest updateRequest = new ExternalRecipientRequest();
        updateRequest.setName("Updated Name");

        when(externalRecipientRepository.findById(recipientId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> externalRecipientsService.updateRecipient(recipientId, updateRequest));

        assertEquals("Nie znaleziono odbiorcy o podanym ID", exception.getMessage(), "Powinien rzucić wyjątek z odpowiednim komunikatem");
        verify(externalRecipientRepository).findById(recipientId);
        verify(externalRecipientRepository, never()).save(any(ExternalRecipient.class));
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        // Arrange
        Long recipientId = 1L;
        String newStatus = "contacted";

        when(externalRecipientRepository.findById(recipientId)).thenReturn(Optional.of(testRecipient));
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenReturn(testRecipient);

        // Act
        externalRecipientsService.updateStatus(recipientId, newStatus);

        // Assert
        verify(externalRecipientRepository).findById(recipientId);
        verify(externalRecipientRepository).save(recipientCaptor.capture());

        ExternalRecipient capturedRecipient = recipientCaptor.getValue();
        assertEquals(newStatus, capturedRecipient.getStatus(), "Status powinien być zaktualizowany");
        assertNotNull(capturedRecipient.getLastContactDate(), "Data ostatniego kontaktu powinna być ustawiona");
    }

    @Test
    void updateStatus_ShouldUpdateStatusWithoutContactDate_WhenNotContacted() {
        // Arrange
        Long recipientId = 1L;
        String newStatus = "pending";

        when(externalRecipientRepository.findById(recipientId)).thenReturn(Optional.of(testRecipient));
        when(externalRecipientRepository.save(any(ExternalRecipient.class))).thenReturn(testRecipient);

        // Act
        externalRecipientsService.updateStatus(recipientId, newStatus);

        // Assert
        verify(externalRecipientRepository).findById(recipientId);
        verify(externalRecipientRepository).save(recipientCaptor.capture());

        ExternalRecipient capturedRecipient = recipientCaptor.getValue();
        assertEquals(newStatus, capturedRecipient.getStatus(), "Status powinien być zaktualizowany");
        assertNull(capturedRecipient.getLastContactDate(), "Data ostatniego kontaktu nie powinna być ustawiona");
    }

    @Test
    void deleteRecipient_ShouldDeleteRecipient() {
        // Arrange
        Long recipientId = 1L;
        doNothing().when(externalRecipientRepository).deleteById(recipientId);

        // Act
        externalRecipientsService.deleteRecipient(recipientId);

        // Assert
        verify(externalRecipientRepository).deleteById(recipientId);
    }

    @Test
    void getCategories_ShouldReturnAllCategories() {
        // Arrange
        List<String> dbCategories = Arrays.asList("Dietetyk", "Firma", "Niestandardowa kategoria");
        when(externalRecipientRepository.findAllCategories()).thenReturn(dbCategories);

        // Act
        List<String> result = externalRecipientsService.getCategories();

        // Assert
        verify(externalRecipientRepository).findAllCategories();

        // Sprawdzenie czy zawiera wszystkie domyślne kategorie
        assertTrue(result.contains("Dietetyk"), "Powinno zawierać kategorię Dietetyk");
        assertTrue(result.contains("Firma"), "Powinno zawierać kategorię Firma");
        assertTrue(result.contains("Portal dietetyczny"), "Powinno zawierać kategorię Portal dietetyczny");
        assertTrue(result.contains("Potencjalny partner"), "Powinno zawierać kategorię Potencjalny partner");
        assertTrue(result.contains("Inne"), "Powinno zawierać kategorię Inne");

        // Sprawdzenie czy zawiera kategorie z bazy danych
        assertTrue(result.contains("Niestandardowa kategoria"), "Powinno zawierać niestandardową kategorię");

        // Sprawdzenie czy lista jest posortowana
        List<String> sortedResult = new ArrayList<>(result);
        Collections.sort(sortedResult);
        assertEquals(sortedResult, result, "Lista powinna być posortowana");
    }

    // Metoda pomocnicza do tworzenia żądań z różnymi emailami
    private ExternalRecipientRequest createRequestWithEmail(String email) {
        ExternalRecipientRequest request = new ExternalRecipientRequest();
        request.setEmail(email);
        request.setName("Test Recipient");
        request.setCategory("Firma");
        request.setTags(Arrays.asList("tag1", "tag2"));
        request.setNotes("Test notes");
        request.setStatus("new");
        return request;
    }
}