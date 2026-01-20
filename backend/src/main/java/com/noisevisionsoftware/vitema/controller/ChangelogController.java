package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.vitema.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.vitema.mapper.changelog.ChangelogMapper;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.ChangelogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/changelog")
@RequiredArgsConstructor
@Slf4j
public class ChangelogController {
    private final ChangelogService changelogService;
    private final ChangelogMapper changelogMapper;

    @GetMapping
    public ResponseEntity<List<ChangelogEntryResponse>> getAllEntries() {
        List<ChangelogEntry> entries = changelogService.getAllEntries();
        return ResponseEntity.ok(entries.stream()
                .map(changelogMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChangelogEntryResponse> createEntry(
            @Valid @RequestBody ChangelogEntryRequest request,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        ChangelogEntry entry = changelogMapper.toModel(request, currentUser.getEmail());
        ChangelogEntry saved = changelogService.createEntry(entry);
        return ResponseEntity.ok(changelogMapper.toResponse(saved));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal FirebaseUser currentUser) {
        changelogService.markAsRead(currentUser.getUid());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/has-unread")
    public ResponseEntity<Boolean> hasUnreadEntries(@AuthenticationPrincipal FirebaseUser currentUser) {
        return ResponseEntity.ok(changelogService.hasUnreadEntries(currentUser.getUid()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        if (e.getMessage().contains("already been closed")) {
            log.error("Firestore client has been closed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Usługa tymczasowo niedostępna. Spróbuj ponownie.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Wystąpił błąd wewnętrzny.");
    }
}