package com.noisevisionsoftware.nutrilog.model.diet.template.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "diet_template_days")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietTemplateDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_template_id", nullable = false)
    private DietTemplateEntity dietTemplate;

    @Column(name = "day_number", nullable = false)
    private int dayNumber;

    @Column(name = "day_name")
    private String dayName;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "templateDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("mealOrder ASC")
    private List<DietTemplateMealEntity> meals;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

