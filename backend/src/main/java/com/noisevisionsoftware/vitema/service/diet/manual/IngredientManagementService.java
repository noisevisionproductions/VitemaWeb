package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing ingredients.
 * Delegates to ProductService for PostgreSQL operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientManagementService {

    private final ProductService productService;

    /**
     * Search ingredients from PostgreSQL via ProductService
     */
    public List<IngredientDTO> searchIngredientsNew(String query, String trainerId, int limit) {
        try {
            List<IngredientDTO> results = productService.searchProducts(query, trainerId);

            if (results.size() > limit) {
                return results.subList(0, limit);
            }

            return results;
        } catch (Exception e) {
            log.error("Error while searching ingredients", e);
            return List.of();
        }
    }
}