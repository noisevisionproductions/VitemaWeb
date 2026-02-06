package com.noisevisionsoftware.vitema.service.product;

import com.noisevisionsoftware.vitema.dto.request.product.ProductRequest;
import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.model.product.jpa.ProductEntity;
import com.noisevisionsoftware.vitema.repository.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDatabaseService {

    private final ProductJpaRepository productJpaRepository;

    @Transactional(readOnly = true)
    public Optional<ProductResponse> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(this::toResponse);
    }

    /**
     * Search by Name (with Trainer Context).
     * Returns Global products + Custom products for that specific trainer.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name, String trainerId) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        String trimmedName = name.trim();

        // If no trainerId, return only GLOBAL products
        if (trainerId == null || trainerId.isBlank()) {
            return productJpaRepository.findByNameContainingIgnoreCaseAndType(trimmedName, ProductType.GLOBAL)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        // If trainerId exists, return GLOBAL + their CUSTOM products
        return productJpaRepository.searchProductsForTrainer(trimmedName, trainerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Backward compatibility: Search by Name (Global only).
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return searchByName(name, null);
    }

    /**
     * Search by Name AND Category (Global only).
     * Added safety filter to ensure we don't leak private custom products here.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByNameAndCategory(String name, String category) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        return productJpaRepository.findByNameContainingIgnoreCaseAndCategory(name.trim(), category.trim())
                .stream()
                .filter(entity -> entity.getType() == ProductType.GLOBAL)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse create(ProductRequest request, String authorId, ProductType type) {
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
                .authorId(authorId)
                .type(type)
                .build();

        entity = productJpaRepository.save(entity);
        return toResponse(entity);
    }

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
                .authorId(e.getAuthorId())
                .type(e.getType())
                .build();
    }
}