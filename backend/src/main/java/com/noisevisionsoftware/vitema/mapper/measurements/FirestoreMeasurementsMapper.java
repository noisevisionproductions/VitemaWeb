package com.noisevisionsoftware.vitema.mapper.measurements;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class FirestoreMeasurementsMapper {

    public BodyMeasurements toBodyMeasurements(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        Timestamp dateTimestamp;
        Object dateValue = data.get("date");

        try {
            if (dateValue instanceof Timestamp) {
                dateTimestamp = (Timestamp) dateValue;
            } else if (dateValue instanceof Long) {
                dateTimestamp = Timestamp.ofTimeMicroseconds((Long) dateValue);
            } else {
                log.warn("Nieprawidłowy format daty: {}, używam bieżącej daty", dateValue);
                dateTimestamp = Timestamp.now();
            }
        } catch (Exception e) {
            log.error("Błąd konwersji daty: {}", dateValue, e);
            dateTimestamp = Timestamp.now();
        }

        return BodyMeasurements.builder()
                .id(document.getId())
                .userId(getStringValue(data))
                .date(dateTimestamp)
                .height(getDoubleValue(data, "height"))
                .weight(getDoubleValue(data, "weight"))
                .neck(getDoubleValue(data, "neck"))
                .biceps(getDoubleValue(data, "biceps"))
                .chest(getDoubleValue(data, "chest"))
                .waist(getDoubleValue(data, "waist"))
                .belt(getDoubleValue(data, "belt"))
                .hips(getDoubleValue(data, "hips"))
                .thigh(getDoubleValue(data, "thigh"))
                .calf(getDoubleValue(data, "calf"))
                .note(getStringValue(data, "note", ""))
                .weekNumber(getIntValue(data))
                .measurementType(getMeasurementType(data))
                .sourceType(getSourceType(data))
                .build();
    }

    public Map<String, Object> toFirestoreMap(BodyMeasurements measurements) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", measurements.getUserId());
        data.put("date", measurements.getDate());
        data.put("height", measurements.getHeight());
        data.put("weight", measurements.getWeight());
        data.put("neck", measurements.getNeck());
        data.put("biceps", measurements.getBiceps());
        data.put("chest", measurements.getChest());
        data.put("waist", measurements.getWaist());
        data.put("belt", measurements.getBelt());
        data.put("hips", measurements.getHips());
        data.put("thigh", measurements.getThigh());
        data.put("calf", measurements.getCalf());
        data.put("note", measurements.getNote());
        data.put("weekNumber", measurements.getWeekNumber());
        data.put("measurementType", measurements.getMeasurementType().name());
        data.put("sourceType", measurements.getSourceType().name());
        return data;
    }

    private String getStringValue(Map<String, Object> data) {
        return getStringValue(data, "userId", null);
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return 0.0;
    }

    private int getIntValue(Map<String, Object> data) {
        Object value = data.get("weekNumber");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return 0;
    }

    private MeasurementType getMeasurementType(Map<String, Object> data) {
        String typeName = getStringValue(data, "measurementType", MeasurementType.WEIGHT_ONLY.name());
        try {
            return MeasurementType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return MeasurementType.WEIGHT_ONLY;
        }
    }

    private MeasurementSourceType getSourceType(Map<String, Object> data) {
        String typeName = getStringValue(data, "sourceType", MeasurementSourceType.APP.name());
        try {
            return MeasurementSourceType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return MeasurementSourceType.APP;
        }
    }
}