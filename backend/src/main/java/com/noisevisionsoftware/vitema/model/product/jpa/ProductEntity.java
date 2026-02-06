package com.noisevisionsoftware.vitema.model.product.jpa;

import com.noisevisionsoftware.vitema.model.product.ProductType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id")
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    @Column(nullable = false, unique = true)
    private String name;

    @Column()
    private String category;

    @Column(length = 100)
    private String unit;

    @Column(nullable = false)
    private double kcal;

    @Column(nullable = false)
    private double protein;

    @Column(nullable = false)
    private double fat;

    @Column(nullable = false)
    private double carbs;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = true;
}
