package com.noisevisionsoftware.vitema.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.noisevisionsoftware.vitema.dto.seed.ProductCategoryDTO;
import com.noisevisionsoftware.vitema.dto.seed.ProductItemDTO;
import com.noisevisionsoftware.vitema.model.product.Product;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.repository.ProductRepository;
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

    private final ProductRepository productRepository;
    private final Firestore firestore;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        try {
            // Check if products collection is empty
            if (!isProductsCollectionEmpty()) {
                log.info("Products collection is not empty. Skipping seed.");
                return;
            }

            log.info("Products collection is empty. Starting seed process...");
            seedProductsFromJson();
            log.info("Product seeding completed successfully!");

        } catch (Exception e) {
            log.error("Error during product seeding", e);
            // Don't throw exception - allow application to start even if seeding fails
        }
    }

    private boolean isProductsCollectionEmpty() {
        try {
            QuerySnapshot snapshot = firestore.collection("products").limit(1).get().get();
            return snapshot.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if products collection is empty", e);
            return false; // Assume not empty to avoid accidental re-seeding
        }
    }

    private void seedProductsFromJson() {
        try {
            // Read JSON file from resources
            ClassPathResource resource = new ClassPathResource("products_seed.json");
            InputStream inputStream = resource.getInputStream();

            // Parse JSON to list of categories
            List<ProductCategoryDTO> categories = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            log.info("Loaded {} categories from JSON", categories.size());

            int totalProducts = 0;
            int successfulProducts = 0;

            // Process each category
            for (ProductCategoryDTO category : categories) {
                String categoryId = category.getCategory();
                log.info("Processing category: {} with {} items", categoryId, category.getItems().size());

                for (ProductItemDTO item : category.getItems()) {
                    totalProducts++;
                    try {
                        Product product = mapToProduct(item, categoryId);
                        productRepository.save(product);
                        successfulProducts++;
                        log.debug("Seeded product: {}", product.getName());
                    } catch (Exception e) {
                        log.error("Failed to seed product: {}", item.getName(), e);
                    }
                }
            }

            log.info("Successfully seeded {}/{} products", successfulProducts, totalProducts);

        } catch (Exception e) {
            log.error("Error reading or parsing products_seed.json", e);
            throw new RuntimeException("Failed to seed products from JSON", e);
        }
    }

    private Product mapToProduct(ProductItemDTO item, String categoryId) {
        // Normalize name for search
        String searchName = item.getName().toLowerCase().trim();

        // Create nutritional values
        NutritionalValues nutritionalValues = NutritionalValues.builder()
                .calories(item.getKcal())
                .protein(item.getProtein())
                .fat(item.getFat())
                .carbs(item.getCarbs())
                .build();

        return Product.builder()
                .name(item.getName())
                .searchName(searchName)
                .defaultUnit(item.getUnit())
                .nutritionalValues(nutritionalValues)
                .type(ProductType.GLOBAL)
                .authorId(null)  // GLOBAL products have no author
                .categoryId(categoryId)
                .createdAt(System.currentTimeMillis())
                .build();
    }
}
