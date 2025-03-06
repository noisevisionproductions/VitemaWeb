package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.nutrilog.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.nutrilog.mapper.changelog.ChangelogMapper;
import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.service.ChangelogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/changelog")
@RequiredArgsConstructor
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
}