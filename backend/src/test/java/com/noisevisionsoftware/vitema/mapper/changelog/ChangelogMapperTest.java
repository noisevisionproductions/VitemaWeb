package com.noisevisionsoftware.vitema.mapper.changelog;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.vitema.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ChangelogMapperTest {

    private ChangelogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ChangelogMapper();
    }

    @Test
    void toModel_ShouldMapAllFields() {
        // given
        String description = "Dodano nową funkcjonalność";
        ChangelogEntryType type = ChangelogEntryType.FEATURE;
        String author = "testUser";

        ChangelogEntryRequest request = new ChangelogEntryRequest();
        request.setDescription(description);
        request.setType(type);

        // when
        ChangelogEntry result = mapper.toModel(request, author);

        // then
        assertNotNull(result);
        assertEquals(description, result.getDescription());
        assertEquals(type, result.getType());
        assertEquals(author, result.getAuthor());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void toResponse_WithNullType_ShouldHandleNullType() {
        // given
        ChangelogEntry entry = new ChangelogEntry();
        entry.setId("1");

        // when
        ChangelogEntryResponse result = mapper.toResponse(entry);

        // then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertNull(result.getType());
    }

    @ParameterizedTest
    @EnumSource(ChangelogEntryType.class)
    void toModel_ShouldMapAllEntryTypes(ChangelogEntryType type) {
        // given
        ChangelogEntryRequest request = new ChangelogEntryRequest();
        request.setDescription("Opis");
        request.setType(type);

        // when
        ChangelogEntry result = mapper.toModel(request, "autor");

        // then
        assertEquals(type, result.getType());
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        // given
        String id = "123";
        String description = "Poprawiono błąd w module X";
        Timestamp createdAt = Timestamp.now();
        String author = "janKowalski";
        ChangelogEntryType type = ChangelogEntryType.FIX;

        ChangelogEntry entry = new ChangelogEntry();
        entry.setId(id);
        entry.setDescription(description);
        entry.setCreatedAt(createdAt);
        entry.setAuthor(author);
        entry.setType(type);

        // when
        ChangelogEntryResponse result = mapper.toResponse(entry);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(description, result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(author, result.getAuthor());
        assertEquals(type, result.getType());
    }

    @Test
    void toModel_ShouldCreateNewTimestamp() {
        // given
        ChangelogEntryRequest request = new ChangelogEntryRequest();
        request.setDescription("Opis");
        request.setType(ChangelogEntryType.IMPROVEMENT);

        // when
        ChangelogEntry result = mapper.toModel(request, "autor");

        // then
        assertNotNull(result.getCreatedAt());
        long currentTimeSeconds = Timestamp.now().getSeconds();
        long createdAtSeconds = result.getCreatedAt().getSeconds();
        assertTrue(currentTimeSeconds - createdAtSeconds < 10,
                "Timestamp powinien być aktualny (nie starszy niż 10 sekund)");
    }

    @Test
    void toModel_WithNullAuthor_ShouldStillMapCorrectly() {
        // given
        ChangelogEntryRequest request = new ChangelogEntryRequest();
        request.setDescription("Opis");
        request.setType(ChangelogEntryType.FEATURE);

        // when
        ChangelogEntry result = mapper.toModel(request, null);

        // then
        assertNotNull(result);
        assertNull(result.getAuthor());
    }

    @Test
    void toResponse_WithNullFields_ShouldMapNullValues() {
        // given
        ChangelogEntry entry = new ChangelogEntry();

        // when
        ChangelogEntryResponse result = mapper.toResponse(entry);

        // then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getDescription());
        assertNull(result.getCreatedAt());
        assertNull(result.getAuthor());
        assertNull(result.getType());
    }

    @Test
    void toResponse_WithAllChangelogEntryTypes_ShouldMapCorrectly() {
        // given
        ChangelogEntry featureEntry = createEntry("1", ChangelogEntryType.FEATURE);
        ChangelogEntry fixEntry = createEntry("2", ChangelogEntryType.FIX);
        ChangelogEntry improvementEntry = createEntry("3", ChangelogEntryType.IMPROVEMENT);

        // when
        ChangelogEntryResponse featureResponse = mapper.toResponse(featureEntry);
        ChangelogEntryResponse fixResponse = mapper.toResponse(fixEntry);
        ChangelogEntryResponse improvementResponse = mapper.toResponse(improvementEntry);

        // then
        assertEquals(ChangelogEntryType.FEATURE, featureResponse.getType());
        assertEquals(ChangelogEntryType.FIX, fixResponse.getType());
        assertEquals(ChangelogEntryType.IMPROVEMENT, improvementResponse.getType());
    }

    private ChangelogEntry createEntry(String id, ChangelogEntryType type) {
        ChangelogEntry entry = new ChangelogEntry();
        entry.setId(id);
        entry.setDescription("Opis " + id);
        entry.setCreatedAt(Timestamp.now());
        entry.setAuthor("Autor");
        entry.setType(type);
        return entry;
    }
}