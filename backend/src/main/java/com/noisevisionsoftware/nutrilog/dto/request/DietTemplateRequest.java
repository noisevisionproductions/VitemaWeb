package com.noisevisionsoftware.nutrilog.dto.request;

import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class DietTemplateRequest {
    private MultipartFile file;
    private int mealsPerDay;
    private String startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<MealType> mealTypes;

}