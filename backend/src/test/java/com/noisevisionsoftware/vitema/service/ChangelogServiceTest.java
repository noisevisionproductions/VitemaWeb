package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import com.noisevisionsoftware.vitema.repository.ChangelogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangelogServiceTest {

    @Mock
    private ChangelogRepository changelogRepository;

    @InjectMocks
    private ChangelogService changelogService;

    private ChangelogEntry testEntry1;
    private ChangelogEntry testEntry2;
    private String testUserId;
    private Timestamp olderTimestamp;

    @BeforeEach
    void setUp() {
        testUserId = "user123";

        olderTimestamp = Timestamp.ofTimeSecondsAndNanos(1614556800, 0); // 2021-03-01
        Timestamp newerTimestamp = Timestamp.ofTimeSecondsAndNanos(1646092800, 0); // 2022-03-01

        testEntry1 = ChangelogEntry.builder()
                .id("entry1")
                .description("Dodano nową funkcję")
                .createdAt(olderTimestamp)
                .author("admin")
                .type(ChangelogEntryType.FEATURE)
                .build();

        testEntry2 = ChangelogEntry.builder()
                .id("entry2")
                .description("Naprawiono błąd w aplikacji")
                .createdAt(newerTimestamp)
                .author("admin")
                .type(ChangelogEntryType.FIX)
                .build();
    }

    @Test
    void getAllEntries_ShouldReturnAllChangelogEntries() {
        // given
        List<ChangelogEntry> expectedEntries = Arrays.asList(testEntry1, testEntry2);
        when(changelogRepository.findAll()).thenReturn(expectedEntries);

        // when
        List<ChangelogEntry> result = changelogService.getAllEntries();

        // then
        assertThat(result).isEqualTo(expectedEntries);
        verify(changelogRepository).findAll();
    }

    @Test
    void createEntry_ShouldSaveAndReturnEntry() {
        // given
        ChangelogEntry newEntry = ChangelogEntry.builder()
                .description("Ulepszono wydajność aplikacji")
                .createdAt(Timestamp.now())
                .author("developer")
                .type(ChangelogEntryType.IMPROVEMENT)
                .build();

        // when
        ChangelogEntry result = changelogService.createEntry(newEntry);

        // then
        assertThat(result).isEqualTo(newEntry);
        verify(changelogRepository).save(newEntry);
    }

    @Test
    void hasUnreadEntries_WhenHasNewerEntries_ShouldReturnTrue() {
        // given
        // Użytkownik ostatnio czytał przed nowszym wpisem
        when(changelogRepository.getLastReadTimestamp(testUserId)).thenReturn(olderTimestamp);
        when(changelogRepository.findAll()).thenReturn(Arrays.asList(testEntry1, testEntry2));

        // when
        boolean result = changelogService.hasUnreadEntries(testUserId);

        // then
        assertThat(result).isTrue();
        verify(changelogRepository).getLastReadTimestamp(testUserId);
    }

    @Test
    void hasUnreadEntries_WhenNoNewerEntries_ShouldReturnFalse() {
        // given
        Timestamp veryNewTimestamp = Timestamp.ofTimeSecondsAndNanos(1677628800, 0); // 2023-03-01
        when(changelogRepository.getLastReadTimestamp(testUserId)).thenReturn(veryNewTimestamp);
        when(changelogRepository.findAll()).thenReturn(Arrays.asList(testEntry1, testEntry2));

        // when
        boolean result = changelogService.hasUnreadEntries(testUserId);

        // then
        assertThat(result).isFalse();
        verify(changelogRepository).getLastReadTimestamp(testUserId);
    }

    @Test
    void markAsRead_ShouldUpdateTimestampInRepository() {
        // when
        changelogService.markAsRead(testUserId);

        // then
        verify(changelogRepository).updateUserSettings(eq(testUserId), any(Timestamp.class));
    }
}