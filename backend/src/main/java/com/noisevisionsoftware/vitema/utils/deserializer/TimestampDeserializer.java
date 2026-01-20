package com.noisevisionsoftware.vitema.utils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.Timestamp;

import java.io.IOException;

public class TimestampDeserializer extends JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            JsonNode node = p.getCodec().readTree(p);

            // Obsługa formatu {_seconds, _nanoseconds}
            if (node.has("_seconds") && node.has("_nanoseconds")) {
                long seconds = node.get("_seconds").asLong();
                int nanos = node.get("_nanoseconds").asInt();
                return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
            }

            // Obsługa formatu {seconds, nanoseconds} - ten format jest przekazywany z frontendu
            else if (node.has("seconds") && node.has("nanoseconds")) {
                long seconds = node.get("seconds").asLong();
                int nanos = node.get("nanoseconds").asInt();
                return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
            }

            // Obsługa formatu timestamp w milisekundach (liczba)
            else if (node.isNumber()) {
                long millis = node.asLong();
                return Timestamp.ofTimeSecondsAndNanos(
                        millis / 1000,
                        (int) ((millis % 1000) * 1_000_000)
                );
            }

            // Obsługa daty jako string w formacie ISO
            else if (node.isTextual()) {
                String dateText = node.asText();
                // Prosty przypadek: timestamp w milisekundach jako string
                if (dateText.matches("\\d+")) {
                    long millis = Long.parseLong(dateText);
                    return Timestamp.ofTimeSecondsAndNanos(
                            millis / 1000,
                            (int) ((millis % 1000) * 1_000_000)
                    );
                }

                try {
                    java.time.Instant instant = java.time.Instant.parse(dateText);
                    return Timestamp.ofTimeSecondsAndNanos(
                            instant.getEpochSecond(),
                            instant.getNano()
                    );
                } catch (Exception e) {
                    // Ignoruj, spróbujemy innych formatów
                }
            }

            throw new IllegalArgumentException("Nierozpoznany format Timestamp");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value for Timestamp", e);
        }
    }
}