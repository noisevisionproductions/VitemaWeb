package com.noisevisionsoftware.vitema.model.diet.template.jpa;

import com.noisevisionsoftware.vitema.model.diet.template.DietTemplateCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "diet_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private DietTemplateCategory category;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "meals_per_day", nullable = false)
    private int mealsPerDay;

    @Column(name = "meal_times", length = 2000)
    private String mealTimesJson;

    @Column(name = "meal_types", length = 1000)
    private String mealTypesJson;

    @Column(name = "target_calories")
    private Double targetCalories;

    @Column(name = "target_protein")
    private Double targetProtein;

    @Column(name = "target_fat")
    private Double targetFat;

    @Column(name = "target_carbs")
    private Double targetCarbs;

    @Column(name = "calculation_method")
    private String calculationMethod;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "dietTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dayNumber ASC")
    private List<DietTemplateDayEntity> days;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        if (externalId == null) {
            externalId = generateExternalId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateExternalId() {
        return "dt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
