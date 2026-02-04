package com.noisevisionsoftware.vitema.service.product;

import com.noisevisionsoftware.vitema.dto.request.product.ProductRequest;
import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.model.product.jpa.ProductEntity;
import com.noisevisionsoftware.vitema.repository.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRUD for the PostgreSQL product database (ingredients). Safe delete:
 * product_id in recipe_ingredients is set to null via FK ON DELETE SET NULL.
 */
@Service
@RequiredArgsConstructor
public class ProductDatabaseService {

    private final ProductJpaRepository productJpaRepository;

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return searchByNameAndCategory(name, null);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByNameAndCategory(String name, String category) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        String trimmedName = name.trim();
        List<ProductEntity> list = (category != null && !category.isBlank())
                ? productJpaRepository.findByNameContainingIgnoreCaseAndCategory(trimmedName, category.trim())
                : productJpaRepository.findByNameContainingIgnoreCase(trimmedName);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        ProductEntity entity = ProductEntity.builder()
                .name(request.getName().trim())
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .unit(request.getUnit() != null ? request.getUnit().trim() : null)
                .kcal(request.getKcal() != null ? request.getKcal() : 0.0)
                .protein(request.getProtein() != null ? request.getProtein() : 0.0)
                .fat(request.getFat() != null ? request.getFat() : 0.0)
                .carbs(request.getCarbs() != null ? request.getCarbs() : 0.0)
                .isVerified(request.isVerified())
                .build();
        entity = productJpaRepository.save(entity);
        return toResponse(entity);
    }

    /**
     * Safe delete: FK on recipe_ingredients uses ON DELETE SET NULL, so recipes are not deleted.
     */
    @Transactional
    public void delete(Long id) {
        if (!productJpaRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productJpaRepository.deleteById(id);
    }

    private ProductResponse toResponse(ProductEntity e) {
        return ProductResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .category(e.getCategory())
                .unit(e.getUnit())
                .kcal(e.getKcal())
                .protein(e.getProtein())
                .fat(e.getFat())
                .carbs(e.getCarbs())
                .isVerified(e.isVerified())
                .build();
    }
}
