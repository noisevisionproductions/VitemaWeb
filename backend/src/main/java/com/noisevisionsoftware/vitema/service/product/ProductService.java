package com.noisevisionsoftware.vitema.service.product;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.dto.request.product.ProductRequest;
import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.model.product.Product;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing products.
 * Refactored to use PostgreSQL (via ProductDatabaseService) instead of Firestore.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductDatabaseService productDatabaseService;

    /**
     * Returns GLOBAL products + CUSTOM products belonging to the specific trainer.
     */
    public List<IngredientDTO> searchProducts(String query, String trainerId) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Using database service to search by name
        List<ProductResponse> products = productDatabaseService.searchByName(query, trainerId);

        return products.stream()
                .map(this::mapResponseToIngredientDTO)
                .collect(Collectors.toList());
    }

    /**
     * Automatically determines type (GLOBAL/CUSTOM) based on who is asking.
     */
    public Product createProduct(Product product, String trainerId) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        ProductType type = ProductType.GLOBAL;
        String authorId = null;

        if (trainerId != null && !trainerId.isEmpty()) {
            type = ProductType.CUSTOM;
            authorId = trainerId;
        }

        ProductRequest request = getProductRequest(product, type);

        ProductResponse savedResponse = productDatabaseService.create(request, authorId, type);

        return mapResponseToProduct(savedResponse);
    }

    private static ProductRequest getProductRequest(Product product, ProductType type) {
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setCategory(product.getCategoryId());
        request.setUnit(product.getDefaultUnit() != null ? product.getDefaultUnit() : "g");

        // Default verification logic: Global = Verified, Custom = Unverified
        request.setVerified(type == ProductType.GLOBAL);

        if (product.getNutritionalValues() != null) {
            request.setKcal(product.getNutritionalValues().getCalories());
            request.setProtein(product.getNutritionalValues().getProtein());
            request.setFat(product.getNutritionalValues().getFat());
            request.setCarbs(product.getNutritionalValues().getCarbs());
        }
        return request;
    }

    /**
     * Retrieves a product by ID (converts String ID to Long).
     */
    public Optional<Product> getProductById(String id) {
        try {
            return productDatabaseService.findById(Long.parseLong(id))
                    .map(this::mapResponseToProduct);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * ENFORCES ownership security. A trainer cannot delete a Global product
     * or another trainer's product.
     */
    public void deleteProduct(String id, String trainerId) {
        try {
            Long dbId = Long.parseLong(id);

            Optional<ProductResponse> existingOpt = productDatabaseService.findById(dbId);

            if (existingOpt.isEmpty()) {
                throw new IllegalArgumentException("Product not found");
            }

            ProductResponse existing = existingOpt.get();

            boolean isGlobal = existing.getType() == ProductType.GLOBAL;
            boolean isOwner = trainerId != null && trainerId.equals(existing.getAuthorId());

            if (isGlobal) {
                throw new IllegalArgumentException("Cannot delete GLOBAL products");
            }

            if (!isOwner) {
                throw new IllegalArgumentException("You can only delete your own custom products");
            }

            productDatabaseService.delete(dbId);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
    }

    private IngredientDTO mapResponseToIngredientDTO(ProductResponse response) {
        return IngredientDTO.builder()
                .id(String.valueOf(response.getId()))
                .name(response.getName())
                .defaultUnit(response.getUnit())
                .nutritionalValues(NutritionalValues.builder()
                        .calories(response.getKcal())
                        .protein(response.getProtein())
                        .fat(response.getFat())
                        .carbs(response.getCarbs())
                        .build())
                .categoryId(response.getCategory())
                .type(response.getType() != null ? response.getType().name() : ProductType.GLOBAL.name())
                .build();
    }

    private Product mapResponseToProduct(ProductResponse response) {
        return Product.builder()
                .id(String.valueOf(response.getId()))
                .name(response.getName())
                .searchName(response.getName().toLowerCase())
                .defaultUnit(response.getUnit())
                .nutritionalValues(NutritionalValues.builder()
                        .calories(response.getKcal())
                        .protein(response.getProtein())
                        .fat(response.getFat())
                        .carbs(response.getCarbs())
                        .build())
                .categoryId(response.getCategory())
                .type(response.getType() != null ? response.getType() : ProductType.GLOBAL)
                .authorId(response.getAuthorId())
                .build();
    }
}