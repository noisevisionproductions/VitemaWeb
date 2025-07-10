package com.noisevisionsoftware.nutrilog.model.diet.template.jpa;

import com.noisevisionsoftware.nutrilog.model.meal.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "diet_template_meals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateMealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_day_id", nullable = false)
    private DietTemplateDayEntity templateDay;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(name = "time", nullable = false)
    private String time;

    @Column(name = "instructions", length = 2000)
    private String instructions;

    @Column(name = "meal_order", nullable = false)
    private int mealOrder;

    @Column(name = "calories", precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein", precision = 8, scale = 2)
    private BigDecimal protein;

    @Column(name = "fat", precision = 8, scale = 2)
    private BigDecimal fat;

    @Column(name = "carbs", precision = 8, scale = 2)
    private BigDecimal carbs;

    @Column(name = "meal_template_id")
    private String mealTemplateId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "templateMeal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<DietTemplateIngredientEntity> ingredients;

    @OneToMany(mappedBy = "templateMeal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<DietTemplateMealPhotoEntity> photos;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
