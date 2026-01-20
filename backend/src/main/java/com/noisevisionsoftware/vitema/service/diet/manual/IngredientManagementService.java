package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.service.external.OpenFoodFactsService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service odpowiedzialny za zarządzanie składnikami w systemie
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientManagementService {

    private final OpenFoodFactsService openFoodFactsService;
    private final ProductCategorizationService categorizationService;

    // Podstawowe składniki jako fallback
    private static final String[] COMMON_INGREDIENTS = {
            "mleko 3,2%", "jajka", "mąka pszenna", "masło", "cukier", "sól", "pieprz",
            "kurczak", "wołowina", "wieprzowina", "ryż", "makaron", "ziemniaki",
            "pomidory", "cebula", "czosnek", "marchew", "kapusta", "sałata",
            "oliwa z oliwek", "olej rzepakowy", "ser żółty", "ser biały", "jogurt naturalny",
            "chleb", "bułka", "płatki owsiane", "banan", "jabłko", "pomarańcza",
            "brokuły", "papryka", "ogórek", "kaszanka", "kiełbasa", "szynka"
    };

    /**
     * Wyszukuje składniki w zewnętrznych i lokalnych źródłach
     */
    public List<ParsedProduct> searchIngredients(String query, int limit) {
        try {
            // Najpierw szukaj w zewnętrznych źródłach
            List<ParsedProduct> externalResults = searchExternalIngredients(query, limit);

            // Jeśli nie ma wystarczająco wyników, uzupełnij lokalnymi
            if (externalResults.size() < limit) {
                List<ParsedProduct> localResults = searchLocalIngredients(query, limit - externalResults.size());
                externalResults.addAll(localResults);
            }

            return externalResults;
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania składników", e);
            // W przypadku błędu, zwróć lokalne wyniki
            return searchLocalIngredients(query, limit);
        }
    }

    /**
     * Tworzy nowy składnik z automatycznym ID
     */
    public ParsedProduct createIngredient(ParsedProduct ingredient) {
        if (ingredient.getId() == null) {
            ingredient.setId(UUID.randomUUID().toString());
        }

        // Automatyczna kategoryzacja jeśli brak kategorii
        if (ingredient.getCategoryId() == null) {
            try {
                String categoryId = categorizationService.suggestCategory(ingredient);
                ingredient.setCategoryId(categoryId);
            } catch (Exception e) {
                log.warn("Nie udało się automatycznie skategoryzować składnika: {}", ingredient.getName(), e);
            }
        }

        return ingredient;
    }

    /**
     * Tworzy składnik z podstawowych informacji
     */
    public ParsedProduct createBasicIngredient(String name, String unit, Double quantity) {
        ParsedProduct ingredient = ParsedProduct.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .original(name)
                .unit(unit != null ? unit : "szt")
                .quantity(quantity != null ? quantity : 1.0)
                .hasCustomUnit(false)
                .build();

        return createIngredient(ingredient);
    }

    /**
     * Waliduje składnik przed zapisem
     */
    public boolean validateIngredient(ParsedProduct ingredient) {
        if (ingredient == null) return false;
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) return false;
        if (ingredient.getQuantity() == null || ingredient.getQuantity() <= 0) return false;
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) return false;
        return true;
    }

    /**
     * Normalizuje składnik (poprawia formatowanie, jednostki itp.)
     */
    public ParsedProduct normalizeIngredient(ParsedProduct ingredient) {
        if (ingredient == null) return null;

        // Normalizuj nazwę
        if (ingredient.getName() != null) {
            ingredient.setName(ingredient.getName().trim());
        }

        // Normalizuj jednostkę
        if (ingredient.getUnit() != null) {
            ingredient.setUnit(normalizeUnit(ingredient.getUnit()));
        }

        if (ingredient.getQuantity() == null || ingredient.getQuantity() <= 0) {
            ingredient.setQuantity(1.0);
        }

        return ingredient;
    }

    // Metody pomocnicze (private)

    private List<ParsedProduct> searchExternalIngredients(String query, int limit) {
        List<ParsedProduct> results = openFoodFactsService.searchIngredients(query, limit);

        // Dodaj kategorie do produktów które ich nie mają
        for (ParsedProduct product : results) {
            if (product.getCategoryId() == null) {
                try {
                    String categoryId = categorizationService.suggestCategory(product);
                    product.setCategoryId(categoryId);
                } catch (Exception e) {
                    log.debug("Nie udało się skategoryzować produktu: {}", product.getName());
                }
            }
        }

        return results;
    }

    private List<ParsedProduct> searchLocalIngredients(String query, int limit) {
        List<ParsedProduct> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (String ingredient : COMMON_INGREDIENTS) {
            if (ingredient.toLowerCase().contains(lowerQuery) && results.size() < limit) {
                ParsedProduct baseProduct = ParsedProduct.builder()
                        .name(ingredient)
                        .original(ingredient)
                        .build();

                ParsedProduct product = ParsedProduct.builder()
                        .id(UUID.randomUUID().toString())
                        .name(ingredient)
                        .quantity(1.0)
                        .unit(getDefaultUnit(ingredient))
                        .original(ingredient)
                        .hasCustomUnit(false)
                        .categoryId(categorizationService.suggestCategory(baseProduct))
                        .build();

                results.add(product);
            }
        }

        return results;
    }

    private String getDefaultUnit(String ingredientName) {
        String lower = ingredientName.toLowerCase();

        if (lower.contains("mleko") || lower.contains("olej") || lower.contains("oliwa")) {
            return "ml";
        }
        if (lower.contains("mąka") || lower.contains("cukier") || lower.contains("sól")) {
            return "g";
        }
        if (lower.contains("jajka") || lower.contains("jajko")) {
            return "szt";
        }
        if (lower.contains("masło") || lower.contains("ser")) {
            return "g";
        }
        if (lower.contains("mięso") || lower.contains("kurczak") || lower.contains("wołowina") || lower.contains("wieprzowina")) {
            return "g";
        }

        return "szt"; // domyślna jednostka
    }

    private String normalizeUnit(String unit) {
        if (unit == null) return "szt";

        String normalized = unit.toLowerCase().trim();

        // Mapowanie popularnych jednostek
        switch (normalized) {
            case "sztuka":
            case "sztuki":
            case "sz":
                return "szt";
            case "gram":
            case "gramy":
            case "gr":
                return "g";
            case "kilogram":
            case "kilogramy":
            case "kg":
                return "kg";
            case "mililitr":
            case "mililitry":
            case "ml":
                return "ml";
            case "litr":
            case "litry":
            case "l":
                return "l";
            case "łyżka":
            case "łyżki":
            case "łyż":
                return "łyżka";
            case "łyżeczka":
            case "łyżeczki":
            case "łyż.":
                return "łyżeczka";
            case "szklanka":
            case "szklanki":
                return "szklanka";
            case "porcja":
            case "porcje":
                return "porcja";
            default:
                return unit;
        }
    }
}