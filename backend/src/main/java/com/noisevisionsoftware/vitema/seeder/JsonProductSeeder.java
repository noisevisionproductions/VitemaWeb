package com.noisevisionsoftware.vitema.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.dto.request.product.ProductRequest;
import com.noisevisionsoftware.vitema.dto.seed.ProductCategoryDTO;
import com.noisevisionsoftware.vitema.dto.seed.ProductItemDTO;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.repository.jpa.ProductJpaRepository;
import com.noisevisionsoftware.vitema.service.product.ProductDatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonProductSeeder implements CommandLineRunner {

    private final ProductDatabaseService productDatabaseService;
    private final ProductJpaRepository productJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        try {
            if (productJpaRepository.count() > 0) {
                log.info("PostgreSQL products table is not empty. Skipping seed.");
                return;
            }

            log.info("Products table is empty. Starting seed process for PostgreSQL...");
            seedProductsFromJson();
            log.info("Product seeding completed successfully!");

        } catch (Exception e) {
            log.error("Error during product seeding", e);
        }
    }

    private void seedProductsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("products_seed.json");
            InputStream inputStream = resource.getInputStream();

            List<ProductCategoryDTO> categories = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            log.info("Loaded {} categories from JSON", categories.size());

            int totalProducts = 0;
            int successfulProducts = 0;

            for (ProductCategoryDTO category : categories) {
                String categoryName = category.getCategory();

                for (ProductItemDTO item : category.getItems()) {
                    totalProducts++;
                    try {
                        ProductRequest request = mapToProductRequest(item, categoryName);

                        productDatabaseService.create(request, null, ProductType.GLOBAL);

                        successfulProducts++;
                        log.debug("Seeded product: {}", item.getName());
                    } catch (Exception e) {
                        log.error("Failed to seed product: {}", item.getName(), e);
                    }
                }
            }

            log.info("Successfully seeded {}/{} products into PostgreSQL", successfulProducts, totalProducts);

        } catch (Exception e) {
            log.error("Error reading or parsing products_seed.json", e);
            throw new RuntimeException("Failed to seed products from JSON", e);
        }
    }

    private ProductRequest mapToProductRequest(ProductItemDTO item, String category) {
        return ProductRequest.builder()
                .name(item.getName())
                .category(category)
                .unit(item.getUnit())
                .kcal(item.getKcal())
                .protein(item.getProtein())
                .fat(item.getFat())
                .carbs(item.getCarbs())
                .isVerified(true)
                .build();
    }
}