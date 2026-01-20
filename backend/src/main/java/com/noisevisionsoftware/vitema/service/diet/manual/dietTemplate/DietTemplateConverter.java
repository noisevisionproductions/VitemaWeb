package com.noisevisionsoftware.vitema.service.diet.manual.dietTemplate;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DietTemplateConverter {

    public DietTemplate convertFromManualDiet(
            ManualDietRequest dietRequest,
            String templateName,
            String description,
            DietTemplateCategory category,
            String createdBy
    ) {
        // Convert days from ManualDietRequest to DietTemplateDayData
        List<DietTemplateDayData> templateDays = IntStream.range(0, dietRequest.getDays().size())
                .mapToObj(dayIndex -> {
                    ParsedDay day = dietRequest.getDays().get(dayIndex);
                    return DietTemplateDayData.builder()
                            .dayNumber(dayIndex + 1)
                            .dayName("Dzień " + (dayIndex + 1))
                            .meals(convertMeals(day.getMeals()))
                            .build();
                })
                .toList();

        // Calculate average nutritional values from whole diet
        DietTemplateNutrition targetNutrition = calculateAverageNutrition(dietRequest);

        return DietTemplate.builder()
                .name(templateName)
                .description(description)
                .category(category)
                .createdBy(createdBy)
                .duration(dietRequest.getDuration())
                .mealsPerDay(dietRequest.getMealsPerDay())
                .mealTimes(dietRequest.getMealTimes())
                .mealTypes(dietRequest.getMealTypes())
                .days(templateDays)
                .targetNutrition(targetNutrition)
                .build();
    }

    private List<DietTemplateMealData> convertMeals(List<ParsedMeal> meals) {
        return meals.stream()
                .map(meal -> DietTemplateMealData.builder()
                        .name(meal.getName())
                        .mealType(meal.getMealType())
                        .time(meal.getTime())
                        .instructions(meal.getInstructions())
                        .ingredients(convertIngredients(meal.getIngredients()))
                        .nutritionalValues(meal.getNutritionalValues())
                        .photos(meal.getPhotos())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DietTemplateIngredient> convertIngredients(List<ParsedProduct> ingredients) {
        if (ingredients == null) return List.of();

        return ingredients.stream()
                .map(ingredient -> DietTemplateIngredient.builder()
                        .name(ingredient.getName())
                        .quantity(ingredient.getQuantity())
                        .unit(ingredient.getUnit())
                        .original(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }

    private DietTemplateNutrition calculateAverageNutrition(ManualDietRequest dietRequest) {
        return DietTemplateNutrition.builder()
                .targetCalories(2000.0)
                .targetProtein(150.0)
                .targetFat(80.0)
                .targetCarbs(250.0)
                .calculationMethod("ESTIMATED")
                .build();
    }
}
