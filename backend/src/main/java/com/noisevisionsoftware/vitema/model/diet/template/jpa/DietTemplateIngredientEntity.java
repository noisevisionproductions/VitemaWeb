package com.noisevisionsoftware.vitema.model.diet.template.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_template_ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietTemplateIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_meal_id", nullable = false)
    private DietTemplateMealEntity templateMeal;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit;

    @Column(name = "original_text", length = 1000)
    private String originalText;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "has_custom_unit", nullable = false)
    private boolean hasCustomUnit;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
