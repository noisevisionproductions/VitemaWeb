package com.noisevisionsoftware.vitema.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.cloud.Timestamp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {JacksonConfig.class})
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void objectMapper_ShouldBeConfiguredCorrectly() {
        assertNotNull(objectMapper);
    }

    @Test
    void objectMapper_ShouldHaveFailOnUnknownPropertiesDisabled() {
        assertFalse(objectMapper.getDeserializationConfig()
                .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    void objectMapper_ShouldHaveJavaTimeModuleRegistered() {
        // JavaTimeModule is registered with the logical module ID "jackson-datatype-jsr310"
        // rather than the fully qualified class name
        assertTrue(objectMapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"),
                "JavaTimeModule should be registered. Found modules: " + objectMapper.getRegisteredModuleIds());
    }

    @Test
    void objectMapper_ShouldHaveWriteDatesAsTimestampsDisabled() {
        assertFalse(objectMapper.getSerializationConfig()
                .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void objectMapper_ShouldHaveTimestampSerializerRegistered() throws Exception {
        // Create a test Timestamp
        Timestamp testTimestamp = Timestamp.ofTimeSecondsAndNanos(
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), 0);

        // Serialize the timestamp
        String json = objectMapper.writeValueAsString(testTimestamp);

        // Verify it contains the expected fields
        assertNotNull(json);
        assertTrue(json.contains("_seconds"));
        assertTrue(json.contains("_nanoseconds"));
    }

    @Test
    void objectMapper_ShouldHaveTimestampDeserializerRegistered() throws Exception {
        // Create JSON with Timestamp format
        String json = "{\"_seconds\":1609459200,\"_nanoseconds\":0}";

        // Deserialize the timestamp
        Timestamp timestamp = objectMapper.readValue(json, Timestamp.class);

        // Verify it was deserialized correctly
        assertNotNull(timestamp);
        assertEquals(1609459200L, timestamp.getSeconds());
        assertEquals(0, timestamp.getNanos());
    }

    @Test
    void objectMapper_ShouldDeserializeTimestampWithSecondsAndNanosecondsFormat() throws Exception {
        // Test format with "seconds" and "nanoseconds" (frontend format)
        String json = "{\"seconds\":1609459200,\"nanoseconds\":500000000}";

        Timestamp timestamp = objectMapper.readValue(json, Timestamp.class);

        assertNotNull(timestamp);
        assertEquals(1609459200L, timestamp.getSeconds());
        assertEquals(500000000, timestamp.getNanos());
    }

    @Test
    void objectMapper_ShouldHandleUnknownProperties() throws Exception {
        // JSON with unknown properties should not fail
        String json = "{\"knownField\":\"value\",\"unknownField\":\"shouldBeIgnored\"}";

        // Create a simple test class
        TestClass result = objectMapper.readValue(json, TestClass.class);

        assertNotNull(result);
        assertEquals("value", result.knownField);
    }

    @Test
    void objectMapper_ShouldSerializeLocalDateTimeAsISOString() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        String json = objectMapper.writeValueAsString(dateTime);

        assertNotNull(json);
        // Should be ISO string format, not timestamp
        assertTrue(json.contains("2024-01-01"));
        assertFalse(json.matches("\\d+"));
    }

    // Helper class for testing unknown properties
    static class TestClass {
        public String knownField;
    }
}
