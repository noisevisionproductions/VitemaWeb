package com.noisevisionsoftware.nutrilog.mapper.measurements;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.BodyMeasurementsRequest;
import com.noisevisionsoftware.nutrilog.dto.response.BodyMeasurementsResponse;
import com.noisevisionsoftware.nutrilog.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.nutrilog.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.nutrilog.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BodyMeasurementsMapper {

    public BodyMeasurementsResponse toResponse(BodyMeasurements measurements) {
        return BodyMeasurementsResponse.builder()
                .id(measurements.getId())
                .userId(measurements.getUserId())
                .date(measurements.getDate())
                .height(measurements.getHeight())
                .weight(measurements.getWeight())
                .neck(measurements.getNeck())
                .biceps(measurements.getBiceps())
                .chest(measurements.getChest())
                .waist(measurements.getWaist())
                .belt(measurements.getBelt())
                .hips(measurements.getHips())
                .thigh(measurements.getThigh())
                .calf(measurements.getCalf())
                .note(measurements.getNote())
                .weekNumber(measurements.getWeekNumber())
                .measurementType(measurements.getMeasurementType())
                .sourceType(measurements.getSourceType())
                .build();
    }

    public BodyMeasurements toModel(BodyMeasurementsRequest request, String userId) {
        return BodyMeasurements.builder()
                .userId(userId)
                .date(request.getDate())
                .height(request.getHeight())
                .weight(request.getWeight())
                .neck(request.getNeck())
                .biceps(request.getBiceps())
                .chest(request.getChest())
                .waist(request.getWaist())
                .belt(request.getBelt())
                .hips(request.getHips())
                .thigh(request.getThigh())
                .calf(request.getCalf())
                .note(request.getNote())
                .weekNumber(calculateWeekNumber(request.getDate()))
                .measurementType(request.getMeasurementType())
                .sourceType(MeasurementSourceType.APP)
                .build();
    }

    private int calculateWeekNumber(Timestamp timestamp) {
        return DateUtils.getWeekNumber(timestamp);
    }
}