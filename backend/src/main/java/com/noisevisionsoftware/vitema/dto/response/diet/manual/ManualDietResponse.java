package com.noisevisionsoftware.vitema.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManualDietResponse {

    private String dietId;
    private String message;

}
