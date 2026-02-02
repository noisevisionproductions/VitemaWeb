package com.noisevisionsoftware.vitema.service.product;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.model.product.Product;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public List<IngredientDTO> searchProducts(String query, String trainerId) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.searchProducts(query, trainerId);
        return products.stream()
                .map(this::productToIngredientDTO)
                .collect(Collectors.toList());
    }

    public Product createProduct(Product product, String trainerId) {
        // Validate required fields
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getDefaultUnit() == null || product.getDefaultUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Default unit is required");
        }

        // If trainer is creating it, force CUSTOM type
        if (trainerId != null && !trainerId.isEmpty()) {
            product.setType(ProductType.CUSTOM);
            product.setAuthorId(trainerId);
        }

        // If no type specified, default to GLOBAL (admin only should do this)
        if (product.getType() == null) {
            product.setType(ProductType.GLOBAL);
        }

        // Generate searchName for efficient searching
        product.setSearchName(product.getName().toLowerCase().trim());

        return productRepository.save(product);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public void deleteProduct(String id, String trainerId) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = productOpt.get();

        // Only allow deletion of CUSTOM products by their author
        if (product.getType() == ProductType.CUSTOM) {
            if (!product.getAuthorId().equals(trainerId)) {
                throw new IllegalArgumentException("Cannot delete product created by another trainer");
            }
        } else {
            throw new IllegalArgumentException("Cannot delete GLOBAL products");
        }

        productRepository.delete(id);
    }

    private IngredientDTO productToIngredientDTO(Product product) {
        return IngredientDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .defaultUnit(product.getDefaultUnit())
                .nutritionalValues(product.getNutritionalValues())
                .categoryId(product.getCategoryId())
                .type(product.getType().name())
                .build();
    }
}
