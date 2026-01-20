package com.noisevisionsoftware.vitema.mapper.diet;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.*;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DietTemplateMapper {

    public DietTemplateResponse toResponse(DietTemplate template) {
        if (template == null) return null;

        return DietTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory().name())
                .categoryLabel(template.getCategory().getLabel())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt() != null ? template.getCreatedAt().toString() : null)
                .updatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().toString() : null)
                .version(template.getVersion())
                .duration(template.getDuration())
                .mealsPerDay(template.getMealsPerDay())
                .mealTimes(template.getMealTimes())
                .mealTypes(template.getMealTypes())
                .days(template.getDays() != null ?
                        template.getDays().stream().map(this::dayToResponse).collect(Collectors.toList()) : null)
                .targetNutrition(nutritionToResponse(template.getTargetNutrition()))
                .usageCount(template.getUsageCount())
                .lastUsed(template.getLastUsed() != null ? template.getLastUsed().toString() : null)
                .notes(template.getNotes())
                .totalMeals(calculateTotalMeals(template))
                .totalIngredients(calculateTotalIngredients(template))
                .hasPhotos(hasPhotos(template))
                .build();
    }

    public DietTemplate fromRequest(DietTemplateRequest request, String createdBy) {
        return DietTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(DietTemplateCategory.valueOf(request.getCategory().toUpperCase()))
                .createdBy(createdBy)
                .duration(request.getDuration())
                .mealsPerDay(request.getMealsPerDay())
                .mealTimes(request.getMealTimes())
                .mealTypes(request.getMealTypes())
                .notes(request.getNotes())
                .build();
    }

    public DietTemplate updateFromRequest(DietTemplate existing, DietTemplateRequest request) {
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCategory(DietTemplateCategory.valueOf(request.getCategory().toUpperCase()));
        existing.setDuration(request.getDuration());
        existing.setMealsPerDay(request.getMealsPerDay());
        existing.setMealTimes(request.getMealTimes());
        existing.setMealTypes(request.getMealTypes());
        existing.setNotes(request.getNotes());
        return existing;
    }

    private DietTemplateDayResponse dayToResponse(DietTemplateDayData day) {
        return DietTemplateDayResponse.builder()
                .dayNumber(day.getDayNumber())
                .dayName(day.getDayName())
                .notes(day.getNotes())
                .meals(day.getMeals() != null ?
                        day.getMeals().stream().map(this::mealToResponse).collect(Collectors.toList()) : null)
                .build();
    }

    private DietTemplateMealResponse mealToResponse(DietTemplateMealData meal) {
        return DietTemplateMealResponse.builder()
                .name(meal.getName())
                .mealType(meal.getMealType() != null ? meal.getMealType().name() : null)
                .time(meal.getTime())
                .instructions(meal.getInstructions())
                .ingredients(meal.getIngredients() != null ?
                        meal.getIngredients().stream().map(this::ingredientToResponse).collect(Collectors.toList()) : null)
                .nutritionalValues(meal.getNutritionalValues())
                .photos(meal.getPhotos())
                .mealTemplateId(meal.getMealTemplateId())
                .build();
    }

    private DietTemplateIngredientResponse ingredientToResponse(DietTemplateIngredient ingredient) {
        return DietTemplateIngredientResponse.builder()
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .original(ingredient.getOriginal())
                .categoryId(ingredient.getCategoryId())
                .hasCustomUnit(ingredient.isHasCustomUnit())
                .build();
    }

    private DietTemplateNutritionResponse nutritionToResponse(DietTemplateNutrition nutrition) {
        if (nutrition == null) return null;

        return DietTemplateNutritionResponse.builder()
                .targetCalories(nutrition.getTargetCalories())
                .targetProtein(nutrition.getTargetProtein())
                .targetFat(nutrition.getTargetFat())
                .targetCarbs(nutrition.getTargetCarbs())
                .calculationMethod(nutrition.getCalculationMethod())
                .build();
    }

    private int calculateTotalMeals(DietTemplate template) {
        if (template.getDays() == null) return 0;
        return template.getDays().stream()
                .mapToInt(day -> day.getMeals() != null ? day.getMeals().size() : 0)
                .sum();
    }

    private int calculateTotalIngredients(DietTemplate template) {
        if (template.getDays() == null) return 0;
        return template.getDays().stream()
                .flatMap(day -> day.getMeals() != null ? day.getMeals().stream() : null)
                .mapToInt(meal -> meal.getIngredients() != null ? meal.getIngredients().size() : 0)
                .sum();
    }

    private boolean hasPhotos(DietTemplate template) {
        if (template.getDays() == null) return false;
        return template.getDays().stream()
                .flatMap(day -> day.getMeals() != null ? day.getMeals().stream() : null)
                .anyMatch(meal -> meal.getPhotos() != null && !meal.getPhotos().isEmpty());
    }
}