package com.noisevisionsoftware.vitema.mapper.measurements;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirestoreMeasurementsMapperTest {

    private FirestoreMeasurementsMapper mapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new FirestoreMeasurementsMapper();
    }

    @Test
    void toBodyMeasurements_WithValidDocument_ShouldMapAllFields() {
        // given
        String id = "measurement-id-123";
        String userId = "user-id-456";
        Timestamp date = Timestamp.now();
        double height = 180.5;
        double weight = 75.2;
        double neck = 38.0;
        double biceps = 35.5;
        double chest = 100.0;
        double waist = 85.0;
        double belt = 88.0;
        double hips = 95.0;
        double thigh = 55.0;
        double calf = 38.0;
        String note = "Pomiar testowy";
        int weekNumber = 12;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("date", date);
        data.put("height", height);
        data.put("weight", weight);
        data.put("neck", neck);
        data.put("biceps", biceps);
        data.put("chest", chest);
        data.put("waist", waist);
        data.put("belt", belt);
        data.put("hips", hips);
        data.put("thigh", thigh);
        data.put("calf", calf);
        data.put("note", note);
        data.put("weekNumber", weekNumber);
        data.put("measurementType", "FULL_BODY");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn(id);
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(date, result.getDate());
        assertEquals(height, result.getHeight());
        assertEquals(weight, result.getWeight());
        assertEquals(neck, result.getNeck());
        assertEquals(biceps, result.getBiceps());
        assertEquals(chest, result.getChest());
        assertEquals(waist, result.getWaist());
        assertEquals(belt, result.getBelt());
        assertEquals(hips, result.getHips());
        assertEquals(thigh, result.getThigh());
        assertEquals(calf, result.getCalf());
        assertEquals(note, result.getNote());
        assertEquals(weekNumber, result.getWeekNumber());
        assertEquals(MeasurementType.FULL_BODY, result.getMeasurementType());
        assertEquals(MeasurementSourceType.APP, result.getSourceType());
    }

    @Test
    void toBodyMeasurements_WithNullDocument_ShouldReturnNull() {
        // when
        BodyMeasurements result = mapper.toBodyMeasurements(null);

        // then
        assertNull(result);
    }

    @Test
    void toBodyMeasurements_WithNonExistentDocument_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toBodyMeasurements_WithNullData_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(null);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toBodyMeasurements_WithLongTimestamp_ShouldConvertToTimestamp() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", 1614556800000000L); // Przykładowa wartość Long dla timestamp
        data.put("measurementType", "WEIGHT_ONLY");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNotNull(result);
        assertNotNull(result.getDate());
        assertEquals(1614556800000000L, result.getDate().getSeconds() * 1000000 + result.getDate().getNanos() / 1000);
    }

    @Test
    void toBodyMeasurements_WithInvalidDateFormat_ShouldUseCurrentTimestamp() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", "invalid-date-format");
        data.put("measurementType", "WEIGHT_ONLY");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        long beforeTestTime = System.currentTimeMillis() / 1000;

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        long afterTestTime = System.currentTimeMillis() / 1000;

        // then
        assertNotNull(result);
        assertNotNull(result.getDate());
        long resultTime = result.getDate().getSeconds();
        assertTrue(resultTime >= beforeTestTime && resultTime <= afterTestTime,
                "Timestamp powinien być ustawiony na aktualny czas");
    }

    @Test
    void toBodyMeasurements_WithNullDate_ShouldUseCurrentTimestamp() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", null);
        data.put("measurementType", "WEIGHT_ONLY");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        long beforeTestTime = System.currentTimeMillis() / 1000;

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        long afterTestTime = System.currentTimeMillis() / 1000;

        // then
        assertNotNull(result);
        assertNotNull(result.getDate());
        long resultTime = result.getDate().getSeconds();
        assertTrue(resultTime >= beforeTestTime && resultTime <= afterTestTime,
                "Timestamp powinien być ustawiony na aktualny czas");
    }

    @Test
    void toBodyMeasurements_WithDifferentNumericTypes_ShouldConvertCorrectly() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", Timestamp.now());
        data.put("height", 180L);           // Long
        data.put("weight", 75);             // Integer
        data.put("neck", 38.5);             // Double
        data.put("weekNumber", 12L);        // Long zamiast Integer
        data.put("measurementType", "WEIGHT_ONLY");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(180.0, result.getHeight());
        assertEquals(75.0, result.getWeight());
        assertEquals(38.5, result.getNeck());
        assertEquals(12, result.getWeekNumber());
    }

    @Test
    void toBodyMeasurements_WithInvalidMeasurementType_ShouldDefaultToWeightOnly() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", Timestamp.now());
        data.put("measurementType", "INVALID_TYPE");
        data.put("sourceType", "APP");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(MeasurementType.WEIGHT_ONLY, result.getMeasurementType());
    }

    @Test
    void toBodyMeasurements_WithInvalidSourceType_ShouldDefaultToApp() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        data.put("date", Timestamp.now());
        data.put("measurementType", "WEIGHT_ONLY");
        data.put("sourceType", "INVALID_SOURCE");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("id123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        BodyMeasurements result = mapper.toBodyMeasurements(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(MeasurementSourceType.APP, result.getSourceType());
    }

    @Test
    void toFirestoreMap_WithValidMeasurement_ShouldMapAllFields() {
        // given
        String userId = "user-id-789";
        Timestamp date = Timestamp.now();
        double height = 175.0;
        double weight = 70.5;
        double neck = 37.0;
        double biceps = 33.0;
        double chest = 98.0;
        double waist = 82.0;
        double belt = 86.0;
        double hips = 93.0;
        double thigh = 53.0;
        double calf = 36.0;
        String note = "Notatka testowa";
        int weekNumber = 15;
        MeasurementType measurementType = MeasurementType.FULL_BODY;
        MeasurementSourceType sourceType = MeasurementSourceType.GOOGLE_SHEET;

        BodyMeasurements measurements = BodyMeasurements.builder()
                .id("measurement-id-789")  // ID nie powinno być mapowane do mapy Firestore
                .userId(userId)
                .date(date)
                .height(height)
                .weight(weight)
                .neck(neck)
                .biceps(biceps)
                .chest(chest)
                .waist(waist)
                .belt(belt)
                .hips(hips)
                .thigh(thigh)
                .calf(calf)
                .note(note)
                .weekNumber(weekNumber)
                .measurementType(measurementType)
                .sourceType(sourceType)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(measurements);

        // then
        assertNotNull(result);
        assertEquals(16, result.size());  // Wszystkie pola oprócz id
        assertEquals(userId, result.get("userId"));
        assertEquals(date, result.get("date"));
        assertEquals(height, result.get("height"));
        assertEquals(weight, result.get("weight"));
        assertEquals(neck, result.get("neck"));
        assertEquals(biceps, result.get("biceps"));
        assertEquals(chest, result.get("chest"));
        assertEquals(waist, result.get("waist"));
        assertEquals(belt, result.get("belt"));
        assertEquals(hips, result.get("hips"));
        assertEquals(thigh, result.get("thigh"));
        assertEquals(calf, result.get("calf"));
        assertEquals(note, result.get("note"));
        assertEquals(weekNumber, result.get("weekNumber"));
        assertEquals(measurementType.name(), result.get("measurementType"));
        assertEquals(sourceType.name(), result.get("sourceType"));

        // ID nie powinno być w mapie
        assertFalse(result.containsKey("id"));
    }

    @Test
    void toFirestoreMap_WithMissingFields_ShouldMapAvailableFields() {
        // given
        BodyMeasurements measurements = BodyMeasurements.builder()
                .userId("user123")
                .date(Timestamp.now())
                .weight(70.0) // Tylko waga jest ustawiona
                .measurementType(MeasurementType.WEIGHT_ONLY)
                .sourceType(MeasurementSourceType.APP)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(measurements);

        // then
        assertNotNull(result);
        assertEquals("user123", result.get("userId"));
        assertEquals(70.0, result.get("weight"));
        assertEquals(0.0, result.get("height")); // Domyślne wartości dla nieustawionych pól
        assertEquals(MeasurementType.WEIGHT_ONLY.name(), result.get("measurementType"));
        assertEquals(MeasurementSourceType.APP.name(), result.get("sourceType"));
    }
}