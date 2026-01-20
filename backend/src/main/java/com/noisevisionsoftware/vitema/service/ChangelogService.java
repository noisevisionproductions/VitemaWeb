package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.repository.ChangelogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangelogService {
    private final ChangelogRepository changelogRepository;

    @Cacheable(value = "changelogCache", key = "'all'", unless = "#result == null")
    public List<ChangelogEntry> getAllEntries() {
        return changelogRepository.findAll();
    }

    @CacheEvict(value = "changelogCache", allEntries = true)
    public ChangelogEntry createEntry(ChangelogEntry entry) {
        changelogRepository.save(entry);
        return entry;
    }

    public boolean hasUnreadEntries(String userId) {
        Timestamp lastRead = changelogRepository.getLastReadTimestamp(userId);
        List<ChangelogEntry> entries = getAllEntries();

        return entries.stream()
                .anyMatch(entry -> entry.getCreatedAt().compareTo(lastRead) > 0);
    }

    public void markAsRead(String userId) {
        changelogRepository.updateUserSettings(userId, Timestamp.now());
    }
}