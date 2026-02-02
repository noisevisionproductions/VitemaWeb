package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.service.product.ProductService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing ingredients in the system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientManagementService {

    private final ProductService productService;

    /**
     * Search ingredients from the local Firestore products collection
     *
     * @param query     Search query
     * @param trainerId Trainer ID to include custom products
     * @param limit     Maximum number of results (currently not enforced, returns all matches)
     * @return List of ingredients matching the query
     */
    public List<IngredientDTO> searchIngredientsNew(String query, String trainerId, int limit) {
        try {
            List<IngredientDTO> results = productService.searchProducts(query, trainerId);

            // Apply limit if needed
            if (results.size() > limit) {
                return results.subList(0, limit);
            }

            return results;
        } catch (Exception e) {
            log.error("Error while searching ingredients", e);
            return List.of();
        }
    }

    /**
     * Search ingredients - backward compatibility with ParsedProduct
     *
     * @deprecated Use searchIngredientsNew instead
     */
    @Deprecated
    public List<ParsedProduct> searchIngredients(String query, int limit) {
        List<IngredientDTO> ingredients = searchIngredientsNew(query, null, limit);
        return ingredients.stream()
                .map(this::ingredientDTOToParsedProduct)
                .collect(Collectors.toList());
    }

    /**
     * Create ingredient - backward compatibility
     *
     * @deprecated Products should be created through ProductService
     */
    @Deprecated
    public ParsedProduct createIngredient(ParsedProduct ingredient) {
        log.warn("Using deprecated createIngredient method. Consider using ProductService instead.");
        if (ingredient.getId() == null) {
            ingredient.setId(UUID.randomUUID().toString());
        }
        return ingredient;
    }

    /**
     * Convert IngredientDTO to ParsedProduct for backward compatibility
     */
    private ParsedProduct ingredientDTOToParsedProduct(IngredientDTO dto) {
        return ParsedProduct.builder()
                .id(dto.getId())
                .name(dto.getName())
                .unit(dto.getDefaultUnit())
                .quantity(1.0)
                .original(dto.getName())
                .hasCustomUnit(false)
                .categoryId(dto.getCategoryId())
                .build();
    }

    /**
     * Validate ingredient data
     */
    public boolean validateIngredient(String name, Double quantity, String unit) {
        if (name == null || name.trim().isEmpty()) return false;
        if (quantity == null || quantity <= 0) return false;
        return unit != null && !unit.trim().isEmpty();
    }

    /**
     * Normalize unit to standard format
     */
    public String normalizeUnit(String unit) {
        if (unit == null) return "szt";

        String normalized = unit.toLowerCase().trim();

        return switch (normalized) {
            case "sztuka", "sztuki", "sz" -> "szt";
            case "gram", "gramy", "gr" -> "g";
            case "kilogram", "kilogramy", "kg" -> "kg";
            case "mililitr", "mililitry", "ml" -> "ml";
            case "litr", "litry", "l" -> "l";
            case "łyżka", "łyżki", "łyż" -> "łyżka";
            case "łyżeczka", "łyżeczki", "łyż." -> "łyżeczka";
            case "szklanka", "szklanki" -> "szklanka";
            case "porcja", "porcje" -> "porcja";
            default -> unit;
        };
    }
}