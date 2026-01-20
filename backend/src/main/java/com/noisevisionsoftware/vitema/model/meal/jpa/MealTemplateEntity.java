package com.noisevisionsoftware.vitema.model.meal.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meal_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_lower", nullable = false)
    private String nameLower;

    @Column(length = 5000)
    private String instructions;

    @Column(name = "meal_type")
    private String mealType;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    private String category;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    // Wartości odżywcze
    @Column(precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(precision = 8, scale = 2)
    private BigDecimal protein;

    @Column(precision = 8, scale = 2)
    private BigDecimal fat;

    @Column(precision = 8, scale = 2)
    private BigDecimal carbs;

    @OneToMany(mappedBy = "mealTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<MealTemplatePhotoEntity> photos;

    @OneToMany(mappedBy = "mealTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<MealTemplateIngredientEntity> ingredients;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (externalId == null) {
            externalId = generateExternalId();
        }
        nameLower = name != null ? name.toLowerCase() : "";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        nameLower = name != null ? name.toLowerCase() : "";
    }

    private String generateExternalId() {
        return "mt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}