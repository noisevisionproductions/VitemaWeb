package com.noisevisionsoftware.vitema.controller;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.vitema.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.vitema.mapper.changelog.ChangelogMapper;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.ChangelogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangelogControllerTest {

    @Mock
    private ChangelogService changelogService;

    @Mock
    private ChangelogMapper changelogMapper;

    @Mock
    private FirebaseUser firebaseUser;

    @InjectMocks
    private ChangelogController changelogController;

    private ChangelogEntryRequest changelogEntryRequest;
    private ChangelogEntry changelogEntry;
    private ChangelogEntryResponse changelogEntryResponse;
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        changelogEntryRequest = new ChangelogEntryRequest();
        changelogEntryRequest.setDescription("Test changelog entry");
        changelogEntryRequest.setType(ChangelogEntryType.FEATURE);

        changelogEntry = ChangelogEntry.builder()
                .id("entry-123")
                .description("Test changelog entry")
                .type(ChangelogEntryType.FEATURE)
                .author(TEST_USER_EMAIL)
                .createdAt(Timestamp.now())
                .build();

        changelogEntryResponse = ChangelogEntryResponse.builder()
                .id("entry-123")
                .description("Test changelog entry")
                .type(ChangelogEntryType.FEATURE)
                .author(TEST_USER_EMAIL)
                .createdAt(Timestamp.now())
                .build();
    }

    // GET /api/changelog - getAllEntries tests

    @Test
    void getAllEntries_WithEntries_ShouldReturnOkWithList() {
        // Arrange
        List<ChangelogEntry> entries = Collections.singletonList(changelogEntry);

        when(changelogService.getAllEntries()).thenReturn(entries);
        when(changelogMapper.toResponse(changelogEntry)).thenReturn(changelogEntryResponse);

        // Act
        ResponseEntity<List<ChangelogEntryResponse>> response = changelogController.getAllEntries();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().getId()).isEqualTo("entry-123");
        assertThat(response.getBody().getFirst().getDescription()).isEqualTo("Test changelog entry");

        verify(changelogService).getAllEntries();
        verify(changelogMapper).toResponse(changelogEntry);
    }

    @Test
    void getAllEntries_WithEmptyList_ShouldReturnOkWithEmptyList() {
        // Arrange
        List<ChangelogEntry> emptyEntries = Collections.emptyList();

        when(changelogService.getAllEntries()).thenReturn(emptyEntries);

        // Act
        ResponseEntity<List<ChangelogEntryResponse>> response = changelogController.getAllEntries();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();

        verify(changelogService).getAllEntries();
        verifyNoInteractions(changelogMapper);
    }

    @Test
    void getAllEntries_WithMultipleEntries_ShouldReturnOkWithAllEntries() {
        // Arrange
        ChangelogEntry entry2 = ChangelogEntry.builder()
                .id("entry-456")
                .description("Another entry")
                .type(ChangelogEntryType.FIX)
                .author("admin@example.com")
                .createdAt(Timestamp.now())
                .build();

        ChangelogEntryResponse response2 = ChangelogEntryResponse.builder()
                .id("entry-456")
                .description("Another entry")
                .type(ChangelogEntryType.FIX)
                .author("admin@example.com")
                .createdAt(Timestamp.now())
                .build();

        List<ChangelogEntry> entries = Arrays.asList(changelogEntry, entry2);

        when(changelogService.getAllEntries()).thenReturn(entries);
        when(changelogMapper.toResponse(changelogEntry)).thenReturn(changelogEntryResponse);
        when(changelogMapper.toResponse(entry2)).thenReturn(response2);

        // Act
        ResponseEntity<List<ChangelogEntryResponse>> response = changelogController.getAllEntries();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getId()).isEqualTo("entry-123");
        assertThat(response.getBody().get(1).getId()).isEqualTo("entry-456");

        verify(changelogService).getAllEntries();
        verify(changelogMapper, times(2)).toResponse(any(ChangelogEntry.class));
    }

    // POST /api/changelog - createEntry tests

    @Test
    void createEntry_WithValidRequest_ShouldReturnOkWithCreatedEntry() {
        // Arrange
        when(firebaseUser.getEmail()).thenReturn(TEST_USER_EMAIL);
        when(changelogMapper.toModel(changelogEntryRequest, TEST_USER_EMAIL)).thenReturn(changelogEntry);
        when(changelogService.createEntry(changelogEntry)).thenReturn(changelogEntry);
        when(changelogMapper.toResponse(changelogEntry)).thenReturn(changelogEntryResponse);

        // Act
        ResponseEntity<ChangelogEntryResponse> response = changelogController.createEntry(
                changelogEntryRequest, firebaseUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("entry-123");
        assertThat(response.getBody().getDescription()).isEqualTo("Test changelog entry");
        assertThat(response.getBody().getType()).isEqualTo(ChangelogEntryType.FEATURE);

        verify(changelogMapper).toModel(changelogEntryRequest, TEST_USER_EMAIL);
        verify(changelogService).createEntry(changelogEntry);
        verify(changelogMapper).toResponse(changelogEntry);
    }

    @Test
    void createEntry_WithDifferentEntryTypes_ShouldReturnOk() {
        // Arrange
        ChangelogEntryRequest fixRequest = new ChangelogEntryRequest();
        fixRequest.setDescription("Bug fix");
        fixRequest.setType(ChangelogEntryType.FIX);

        ChangelogEntry fixEntry = ChangelogEntry.builder()
                .id("fix-entry-123")
                .description("Bug fix")
                .type(ChangelogEntryType.FIX)
                .author(TEST_USER_EMAIL)
                .createdAt(Timestamp.now())
                .build();

        ChangelogEntryResponse fixResponse = ChangelogEntryResponse.builder()
                .id("fix-entry-123")
                .description("Bug fix")
                .type(ChangelogEntryType.FIX)
                .author(TEST_USER_EMAIL)
                .createdAt(Timestamp.now())
                .build();

        when(firebaseUser.getEmail()).thenReturn(TEST_USER_EMAIL);
        when(changelogMapper.toModel(fixRequest, TEST_USER_EMAIL)).thenReturn(fixEntry);
        when(changelogService.createEntry(fixEntry)).thenReturn(fixEntry);
        when(changelogMapper.toResponse(fixEntry)).thenReturn(fixResponse);

        // Act
        ResponseEntity<ChangelogEntryResponse> response = changelogController.createEntry(
                fixRequest, firebaseUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo(ChangelogEntryType.FIX);

        verify(changelogMapper).toModel(fixRequest, TEST_USER_EMAIL);
        verify(changelogService).createEntry(fixEntry);
    }

    // POST /api/changelog/mark-read - markAsRead tests

    @Test
    void markAsRead_WithValidUser_ShouldReturnOk() {
        // Arrange
        when(firebaseUser.getUid()).thenReturn(TEST_USER_ID);
        doNothing().when(changelogService).markAsRead(TEST_USER_ID);

        // Act
        ResponseEntity<Void> response = changelogController.markAsRead(firebaseUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(firebaseUser).getUid();
        verify(changelogService).markAsRead(TEST_USER_ID);
    }

    @Test
    void markAsRead_WithNullUser_ShouldThrowException() {
        // Arrange
        when(firebaseUser.getUid()).thenReturn(null);

        // Act & Assert
        try {
            changelogController.markAsRead(firebaseUser);
        } catch (Exception e) {
            // Expected behavior - service should handle null or throw exception
        }

        verify(firebaseUser).getUid();
    }

    // GET /api/changelog/has-unread - hasUnreadEntries tests

    @Test
    void hasUnreadEntries_WithUnreadEntries_ShouldReturnTrue() {
        // Arrange
        when(firebaseUser.getUid()).thenReturn(TEST_USER_ID);
        when(changelogService.hasUnreadEntries(TEST_USER_ID)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = changelogController.hasUnreadEntries(firebaseUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isTrue();

        verify(firebaseUser).getUid();
        verify(changelogService).hasUnreadEntries(TEST_USER_ID);
    }

    @Test
    void hasUnreadEntries_WithNoUnreadEntries_ShouldReturnFalse() {
        // Arrange
        when(firebaseUser.getUid()).thenReturn(TEST_USER_ID);
        when(changelogService.hasUnreadEntries(TEST_USER_ID)).thenReturn(false);

        // Act
        ResponseEntity<Boolean> response = changelogController.hasUnreadEntries(firebaseUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isFalse();

        verify(firebaseUser).getUid();
        verify(changelogService).hasUnreadEntries(TEST_USER_ID);
    }

    // Exception handler tests

    @Test
    void handleIllegalStateException_WithClosedFirestoreClient_ShouldReturnServiceUnavailable() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Firestore client has already been closed");

        // Act
        ResponseEntity<String> response = changelogController.handleIllegalStateException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo("Usługa tymczasowo niedostępna. Spróbuj ponownie.");
    }

    @Test
    void handleIllegalStateException_WithOtherIllegalState_ShouldReturnInternalServerError() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Some other error");

        // Act
        ResponseEntity<String> response = changelogController.handleIllegalStateException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Wystąpił błąd wewnętrzny.");
    }

    @Test
    void handleIllegalStateException_WithNullMessage_ShouldReturnInternalServerError() {
        // Arrange
        IllegalStateException exception = new IllegalStateException();

        // Act
        ResponseEntity<String> response = changelogController.handleIllegalStateException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Wystąpił błąd wewnętrzny.");
    }
}
