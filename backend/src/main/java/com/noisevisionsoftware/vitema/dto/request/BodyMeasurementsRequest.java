package com.noisevisionsoftware.vitema.dto.request;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodyMeasurementsRequest {
    @NotNull
    private Timestamp date;

    @Positive
    private double height;

    @Positive
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

    @NotNull
    private MeasurementType measurementType;
}