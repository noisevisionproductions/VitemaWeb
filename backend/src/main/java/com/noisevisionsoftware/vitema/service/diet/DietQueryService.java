package com.noisevisionsoftware.vitema.service.diet;

import com.noisevisionsoftware.vitema.dto.diet.*;
import com.noisevisionsoftware.vitema.model.diet.Day;
import com.noisevisionsoftware.vitema.model.diet.DayMeal;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.repository.DietRepository;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietQueryService {

    private final DietRepository dietRepository;

    public List<DietHistorySummaryDto> getTrainerDietHistory(String trainerId) {
        return dietRepository.findByUserId(trainerId).stream()
                .filter(diet -> trainerId.equals(diet.getAuthorId()))
                .sorted(Comparator.comparing(Diet::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .map(diet -> DietHistorySummaryDto.builder()
                        .id(diet.getId())
                        .name(diet.getMetadata() != null ? diet.getMetadata().getFileName() : "Dieta bez nazwy")
                        .clientName("Pacjent: " + diet.getUserId())
                        .date(diet.getCreatedAt() != null ? diet.getCreatedAt().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }

    public DietDraftDto getDietAsDraft(String dietId) {
        Diet oldDiet = dietRepository.findById(dietId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono diety: " + dietId));

        String oldName = (oldDiet.getMetadata() != null && oldDiet.getMetadata().getFileName() != null)
                ? oldDiet.getMetadata().getFileName()
                : "Nowa dieta";

        return DietDraftDto.builder()
                .dietId(null)
                .userId(oldDiet.getUserId())
                .name(oldName + " (Kopia)")
                .days(mapDaysToDto(oldDiet.getDays()))
                .build();
    }

    private List<DietDayDto> mapDaysToDto(List<Day> days) {
        if (days == null) return Collections.emptyList();

        return days.stream().map(day -> DietDayDto.builder()
                .date(day.getDate() != null ? day.getDate().toDate().toInstant().toString() : null)
                .meals(mapMealsToDto(day.getMeals()))
                .build()
        ).collect(Collectors.toList());
    }

    private List<DietMealDto> mapMealsToDto(List<DayMeal> meals) {
        if (meals == null) return Collections.emptyList();

        return meals.stream().map(meal -> DietMealDto.builder()
                .originalRecipeId(meal.getRecipeId())
                .name(meal.getName())
                .mealType(meal.getMealType() != null ? meal.getMealType().name() : null)
                .time(meal.getTime())
                .instructions(meal.getInstructions())
                .ingredients(mapIngredientsToDto(meal.getIngredients()))
                .build()
        ).collect(Collectors.toList());
    }

    private List<DietIngredientDto> mapIngredientsToDto(List<RecipeIngredient> ingredients) {
        if (ingredients == null) return Collections.emptyList();

        return ingredients.stream().map(ing -> DietIngredientDto.builder()
                .name(ing.getName())
                .quantity(ing.getQuantity())
                .unit(ing.getUnit())
                .productId(ing.getProductId() != null
                        ? String.valueOf(ing.getProductId())
                        : ing.getId())
                .categoryId(ing.getCategoryId())
                .build()
        ).collect(Collectors.toList());
    }
}