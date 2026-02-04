package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.dto.response.product.ProductResponse;
import com.noisevisionsoftware.vitema.model.product.Product;
import com.noisevisionsoftware.vitema.service.product.ProductDatabaseService;
import com.noisevisionsoftware.vitema.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductDatabaseService productDatabaseService;

    /** Search PostgreSQL product database (for recipe ingredients). */
    @GetMapping("/db/search")
    public ResponseEntity<List<ProductResponse>> searchDb(
            @RequestParam String query,
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productDatabaseService.searchByNameAndCategory(query, category));
        }
        return ResponseEntity.ok(productDatabaseService.searchByName(query));
    }

    @GetMapping("/search")
    public ResponseEntity<List<IngredientDTO>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) String trainerId) {
        log.info("Searching products with query: {}, trainerId: {}", query, trainerId);
        List<IngredientDTO> results = productService.searchProducts(query, trainerId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @RequestParam(required = false) String trainerId) {
        log.info("Creating product: {}, trainerId: {}", product.getName(), trainerId);
        Product created = productService.createProduct(product, trainerId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @RequestParam String trainerId) {
        productService.deleteProduct(id, trainerId);
        return ResponseEntity.noContent().build();
    }
}
