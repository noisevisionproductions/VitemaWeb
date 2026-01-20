package com.noisevisionsoftware.vitema.mapper.measurements;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.BodyMeasurementsRequest;
import com.noisevisionsoftware.vitema.dto.response.BodyMeasurementsResponse;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import com.noisevisionsoftware.vitema.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BodyMeasurementsMapperTest {

    private BodyMeasurementsMapper mapper;

    @Mock
    private DateUtils dateUtils;

    @BeforeEach
    void setUp() {
        mapper = new BodyMeasurementsMapper();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        // given
        BodyMeasurements measurements = createBodyMeasurements();

        // when
        BodyMeasurementsResponse response = mapper.toResponse(measurements);

        // then
        assertNotNull(response);
        assertEquals(measurements.getId(), response.getId());
        assertEquals(measurements.getUserId(), response.getUserId());
        assertEquals(measurements.getDate(), response.getDate());
        assertEquals(measurements.getHeight(), response.getHeight());
        assertEquals(measurements.getWeight(), response.getWeight());
        assertEquals(measurements.getNeck(), response.getNeck());
        assertEquals(measurements.getBiceps(), response.getBiceps());
        assertEquals(measurements.getChest(), response.getChest());
        assertEquals(measurements.getWaist(), response.getWaist());
        assertEquals(measurements.getBelt(), response.getBelt());
        assertEquals(measurements.getHips(), response.getHips());
        assertEquals(measurements.getThigh(), response.getThigh());
        assertEquals(measurements.getCalf(), response.getCalf());
        assertEquals(measurements.getNote(), response.getNote());
        assertEquals(measurements.getWeekNumber(), response.getWeekNumber());
        assertEquals(measurements.getMeasurementType(), response.getMeasurementType());
        assertEquals(measurements.getSourceType(), response.getSourceType());
    }

    @Test
    void toModel_ShouldMapAllFieldsAndCalculateWeekNumber() {
        // given
        String userId = "user123";
        BodyMeasurementsRequest request = createBodyMeasurementsRequest();
        Timestamp date = request.getDate();

        // when
        BodyMeasurements model = mapper.toModel(request, userId);

        // then
        assertNotNull(model);
        assertEquals(userId, model.getUserId());
        assertEquals(date, model.getDate());
        assertEquals(request.getHeight(), model.getHeight());
        assertEquals(request.getWeight(), model.getWeight());
        assertEquals(request.getNeck(), model.getNeck());
        assertEquals(request.getBiceps(), model.getBiceps());
        assertEquals(request.getChest(), model.getChest());
        assertEquals(request.getWaist(), model.getWaist());
        assertEquals(request.getBelt(), model.getBelt());
        assertEquals(request.getHips(), model.getHips());
        assertEquals(request.getThigh(), model.getThigh());
        assertEquals(request.getCalf(), model.getCalf());
        assertEquals(request.getNote(), model.getNote());
        assertEquals(request.getMeasurementType(), model.getMeasurementType());
        assertEquals(MeasurementSourceType.APP, model.getSourceType());

        // Sprawdzamy czy numer tygodnia został obliczony
        assertTrue(model.getWeekNumber() > 0);
    }

    @Test
    void toModel_ShouldSetSourceTypeToAPP() {
        // given
        String userId = "user456";
        BodyMeasurementsRequest request = createBodyMeasurementsRequest();

        // when
        BodyMeasurements model = mapper.toModel(request, userId);

        // then
        assertEquals(MeasurementSourceType.APP, model.getSourceType());
    }

    @Test
    void toModel_WithWeightOnly_ShouldSetMeasurementTypeCorrectly() {
        // given
        String userId = "user789";
        BodyMeasurementsRequest request = createBodyMeasurementsRequest();
        request.setMeasurementType(MeasurementType.WEIGHT_ONLY);

        // when
        BodyMeasurements model = mapper.toModel(request, userId);

        // then
        assertEquals(MeasurementType.WEIGHT_ONLY, model.getMeasurementType());
    }

    @Test
    void toModel_WithFullBody_ShouldSetMeasurementTypeCorrectly() {
        // given
        String userId = "user101112";
        BodyMeasurementsRequest request = createBodyMeasurementsRequest();
        request.setMeasurementType(MeasurementType.FULL_BODY);

        // when
        BodyMeasurements model = mapper.toModel(request, userId);

        // then
        assertEquals(MeasurementType.FULL_BODY, model.getMeasurementType());
    }

    @Test
    void toResponse_WithNullValues_ShouldHandleNullValues() {
        // given
        BodyMeasurements measurements = new BodyMeasurements();

        // when
        BodyMeasurementsResponse response = mapper.toResponse(measurements);

        // then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getDate());
        assertEquals(0.0, response.getHeight());
        assertEquals(0.0, response.getWeight());
        assertEquals(0.0, response.getNeck());
        assertEquals(0.0, response.getBiceps());
        assertEquals(0.0, response.getChest());
        assertEquals(0.0, response.getWaist());
        assertEquals(0.0, response.getBelt());
        assertEquals(0.0, response.getHips());
        assertEquals(0.0, response.getThigh());
        assertEquals(0.0, response.getCalf());
        assertNull(response.getNote());
        assertEquals(0, response.getWeekNumber());
        assertNull(response.getMeasurementType());
        assertNull(response.getSourceType());
    }

    // Metody pomocnicze
    private BodyMeasurements createBodyMeasurements() {
        return BodyMeasurements.builder()
                .id("measurement123")
                .userId("user123")
                .date(Timestamp.now())
                .height(180.5)
                .weight(75.2)
                .neck(38.0)
                .biceps(35.5)
                .chest(100.0)
                .waist(85.0)
                .belt(88.0)
                .hips(95.0)
                .thigh(55.0)
                .calf(38.0)
                .note("Test measurement")
                .weekNumber(12)
                .measurementType(MeasurementType.FULL_BODY)
                .sourceType(MeasurementSourceType.APP)
                .build();
    }

    private BodyMeasurementsRequest createBodyMeasurementsRequest() {
        BodyMeasurementsRequest request = new BodyMeasurementsRequest();
        request.setDate(Timestamp.now());
        request.setHeight(180.5);
        request.setWeight(75.2);
        request.setNeck(38.0);
        request.setBiceps(35.5);
        request.setChest(100.0);
        request.setWaist(85.0);
        request.setBelt(88.0);
        request.setHips(95.0);
        request.setThigh(55.0);
        request.setCalf(38.0);
        request.setNote("Test measurement request");
        request.setMeasurementType(MeasurementType.FULL_BODY);
        return request;
    }
}