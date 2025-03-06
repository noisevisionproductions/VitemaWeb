package com.noisevisionsoftware.nutrilog.controller.diet;

import com.noisevisionsoftware.nutrilog.dto.request.category.BulkCategoryRequest;
import com.noisevisionsoftware.nutrilog.dto.request.category.ParseProductsRequest;
import com.noisevisionsoftware.nutrilog.dto.request.category.UpdateCategoriesRequest;
import com.noisevisionsoftware.nutrilog.dto.request.category.UpdateProductRequest;
import com.noisevisionsoftware.nutrilog.model.shopping.category.Category;
import com.noisevisionsoftware.nutrilog.service.category.FirestoreCategoryDataManager;
import com.noisevisionsoftware.nutrilog.service.category.ProductCategorizationService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsingResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.ProductParsingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/diets/categorization")
@RequiredArgsConstructor
@Slf4j
public class DietCategorizationController {

    private final ProductCategorizationService categorizationService;
    private final FirestoreCategoryDataManager firestoreCategoryDataManager;
    private final ProductParsingService productParsingService;
    private final List<Category> defaultCategories;

    @PostMapping("/parse")
    public ResponseEntity<List<ParsedProduct>> parseProducts(@RequestBody @Valid ParseProductsRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ParsedProduct> parsedProducts = new ArrayList<>();

        for (String productText : request.getProducts()) {
            try {
                ParsingResult result = productParsingService.parseProduct(productText);
                ParsedProduct product;

                if (result.isSuccess() && result.getProduct() != null) {
                    product = result.getProduct();
                } else {
                    product = ParsedProduct.builder()
                            .id(UUID.randomUUID().toString())
                            .name(productText)
                            .quantity(1.0)
                            .unit("szt")
                            .original(productText)
                            .hasCustomUnit(false)
                            .categoryId(null)
                            .build();
                }

                String categoryId = categorizationService.suggestCategory(product);
                product.setCategoryId(categoryId);

                parsedProducts.add(product);
            } catch (Exception e) {
                log.error("Błąd podczas parsowania produktu: {}", productText, e);
                ParsedProduct defaultProduct = ParsedProduct.builder()
                        .id(UUID.randomUUID().toString())
                        .name(productText)
                        .quantity(1.0)
                        .unit("szt")
                        .original(productText)
                        .hasCustomUnit(false)
                        .categoryId(null)
                        .build();
                parsedProducts.add(defaultProduct);
            }
        }

        return ResponseEntity.ok(parsedProducts);
    }

    @PutMapping("/product")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @RequestBody UpdateProductRequest request
    ) {
        try {
            if (request.getOldProduct() == null || request.getNewProduct() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Brak wymaganych danych produktu"
                        ));
            }

            if (request.getNewProduct().getOriginal() == null || request.getNewProduct().getOriginal().isEmpty()) {
                request.getNewProduct().setOriginal(request.getOldProduct().getOriginal());
            }

            ParsedProduct updatedProduct = firestoreCategoryDataManager.updateProduct(
                    request.getOldProduct(),
                    request.getNewProduct()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "product", updatedProduct,
                    "message", "Produkt został zaktualizowany"
            ));
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji produktu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Wystąpił błąd podczas aktualizacji produktu: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCategories(
            @RequestBody UpdateCategoriesRequest request
    ) {
        try {
            Map<String, List<ParsedProduct>> products = request.getCategorizedProducts();

            if (products == null || products.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Brak produktów do kategoryzacji"));
            }

            categorizationService.updateCategoriesInTransaction(products);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Zaktualizowano " + products.values().stream().mapToInt(List::size).sum() + " produktów"
            ));
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji kategorii", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Wystąpił błąd podczas aktualizacji kategorii: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(defaultCategories);
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, String>> suggestCategory(@RequestBody ParsedProduct product) {
        try {
            String categoryId = categorizationService.suggestCategory(product);
            return ResponseEntity.ok(Map.of("categoryId", Objects.requireNonNullElse(categoryId, "uncategorized")));
        } catch (Exception e) {
            log.error("Error suggesting category for product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/suggest/bulk")
    public ResponseEntity<Map<String, String>> bulkSuggestCategories(
            @RequestBody BulkCategoryRequest request) {
        try {
            Map<String, String> suggestions = new HashMap<>();

            for (ParsedProduct product : request.getProducts()) {
                String categoryId = categorizationService.suggestCategory(product);
                String key = product.getOriginal() != null ? product.getOriginal() : product.getName();
                suggestions.put(key, categoryId);
            }

            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error suggesting categories in bulk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}