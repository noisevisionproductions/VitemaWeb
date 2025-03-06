package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietFileInfo {
    private String fileName;
    private String fileUrl;
}