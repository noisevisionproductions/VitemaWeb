package com.noisevisionsoftware.nutrilog.dto.response;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.nutrilog.model.measurements.MeasurementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMeasurementsResponse {
    private String id;
    private String userId;
    private Timestamp date;
    private double height;
    private double weight;
    private double neck;
    private double biceps;
    private double chest;
    private double waist;
    private double belt;
    private double hips;
    private double thigh;
    private double calf;
    private String note;
    private int weekNumber;
    private MeasurementType measurementType;
    private MeasurementSourceType sourceType;
}
