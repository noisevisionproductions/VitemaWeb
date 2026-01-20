package com.noisevisionsoftware.vitema.model.recipe.jpa;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_references")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeReferenceEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "diet_id", nullable = false)
    private String dietId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "meal_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
}
