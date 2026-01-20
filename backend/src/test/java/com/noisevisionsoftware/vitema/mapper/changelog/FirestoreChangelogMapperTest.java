package com.noisevisionsoftware.vitema.mapper.changelog;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirestoreChangelogMapperTest {

    private FirestoreChangelogMapper mapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new FirestoreChangelogMapper();
    }

    @Test
    void toChangelogEntry_WithValidDocument_ShouldMapAllFields() {
        // given
        String id = "document-id-123";
        String title = "Nowa funkcjonalność";
        String description = "Dodano możliwość eksportu danych";
        Timestamp createdAt = Timestamp.now();
        String author = "jan.kowalski@example.com";
        String type = "feature";

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("createdAt", createdAt);
        data.put("author", author);
        data.put("type", type);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn(id);
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        ChangelogEntry result = mapper.toChangelogEntry(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(description, result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(author, result.getAuthor());
        assertEquals(ChangelogEntryType.FEATURE, result.getType());
    }

    @Test
    void toChangelogEntry_WithNullDocument_ShouldReturnNull() {
        // when
        ChangelogEntry result = mapper.toChangelogEntry(null);

        // then
        assertNull(result);
    }

    @Test
    void toChangelogEntry_WithNonExistentDocument_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        ChangelogEntry result = mapper.toChangelogEntry(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toChangelogEntry_WithNullData_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(null);

        // when
        ChangelogEntry result = mapper.toChangelogEntry(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toChangelogEntry_WithAllChangelogEntryTypes_ShouldMapCorrectly() {
        // given
        testTypeMapping("feature", ChangelogEntryType.FEATURE);
        testTypeMapping("fix", ChangelogEntryType.FIX);
        testTypeMapping("improvement", ChangelogEntryType.IMPROVEMENT);
    }

    private void testTypeMapping(String firestoreType, ChangelogEntryType expectedType) {
        // Setup mock data
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Test title");
        data.put("description", "Test description");
        data.put("createdAt", Timestamp.now());
        data.put("author", "author");
        data.put("type", firestoreType);

        DocumentSnapshot mockDoc = mock(DocumentSnapshot.class);
        when(mockDoc.exists()).thenReturn(true);
        when(mockDoc.getId()).thenReturn("test-id");
        when(mockDoc.getData()).thenReturn(data);

        // Map and verify
        ChangelogEntry result = mapper.toChangelogEntry(mockDoc);
        assertEquals(expectedType, result.getType());
    }

    @Test
    void toFirestoreMap_WithValidEntry_ShouldMapAllFields() {
        // given
        String title = "Naprawa błędu";
        String description = "Naprawiono problem z logowaniem";
        Timestamp createdAt = Timestamp.now();
        String author = "adam.nowak@example.com";
        ChangelogEntryType type = ChangelogEntryType.FIX;

        ChangelogEntry entry = ChangelogEntry.builder()
                .id("entry-id-123")  // id nie powinno być mapowane do mapy Firestore
                .description(description)
                .createdAt(createdAt)
                .author(author)
                .type(type)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(entry);

        // then
        assertNotNull(result);
        assertEquals(4, result.size());  // sprawdzamy, czy mamy 5 pól (bez id)
        assertEquals(description, result.get("description"));
        assertEquals(createdAt, result.get("createdAt"));
        assertEquals(author, result.get("author"));
        assertEquals("fix", result.get("type"));  // typ powinien być małymi literami
    }

    @Test
    void toFirestoreMap_WithAllChangelogEntryTypes_ShouldMapToLowercase() {
        // given
        testToFirestoreTypeMapping(ChangelogEntryType.FEATURE, "feature");
        testToFirestoreTypeMapping(ChangelogEntryType.FIX, "fix");
        testToFirestoreTypeMapping(ChangelogEntryType.IMPROVEMENT, "improvement");
    }

    private void testToFirestoreTypeMapping(ChangelogEntryType type, String expectedFirestoreType) {
        ChangelogEntry entry = ChangelogEntry.builder()
                .description("Test")
                .createdAt(Timestamp.now())
                .author("author")
                .type(type)
                .build();

        Map<String, Object> result = mapper.toFirestoreMap(entry);
        assertEquals(expectedFirestoreType, result.get("type"));
    }

    @Test
    void toFirestoreMap_DoesNotIncludeId() {
        // given
        ChangelogEntry entry = ChangelogEntry.builder()
                .id("test-id-123")
                .description("Test")
                .createdAt(Timestamp.now())
                .author("author")
                .type(ChangelogEntryType.FEATURE)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(entry);

        // then
        assertFalse(result.containsKey("id"));
    }

    @Test
    void toChangelogEntry_WithInvalidTypeCase_ShouldHandleCase() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Test");
        data.put("description", "Test");
        data.put("createdAt", Timestamp.now());
        data.put("author", "author");
        data.put("type", "fEaTuRe");  // mieszane wielkości liter

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("test-id");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        ChangelogEntry result = mapper.toChangelogEntry(documentSnapshot);

        // then
        assertEquals(ChangelogEntryType.FEATURE, result.getType());
    }

    @Test
    void toChangelogEntry_ShouldHandleNullFields() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("title", null);
        data.put("description", null);
        data.put("createdAt", Timestamp.now());
        data.put("author", null);
        data.put("type", "feature");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("test-id");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        ChangelogEntry result = mapper.toChangelogEntry(documentSnapshot);

        // then
        assertNotNull(result);
        assertNull(result.getDescription());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getAuthor());
        assertEquals(ChangelogEntryType.FEATURE, result.getType());
    }

    @Test
    void toChangelogEntry_WithNullType_ShouldThrow() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Test");
        data.put("description", "Test");
        data.put("createdAt", Timestamp.now());
        data.put("author", "author");
        data.put("type", null);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("test-id");
        when(documentSnapshot.getData()).thenReturn(data);

        // when & then
        assertThrows(NullPointerException.class, () -> mapper.toChangelogEntry(documentSnapshot));
    }
}