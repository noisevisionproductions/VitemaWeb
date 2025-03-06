package com.noisevisionsoftware.nutrilog.utils.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.cloud.Timestamp;

import java.io.IOException;

public class TimestampSerializer extends JsonSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp timestamp, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("_seconds", timestamp.getSeconds());
        gen.writeNumberField("_nanoseconds", timestamp.getNanos());
        gen.writeEndObject();
    }
}