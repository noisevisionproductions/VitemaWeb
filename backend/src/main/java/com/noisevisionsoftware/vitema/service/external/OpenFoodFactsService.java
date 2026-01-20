package com.noisevisionsoftware.vitema.service.external;

import com.noisevisionsoftware.vitema.dto.external.OpenFoodFactsProduct;
import com.noisevisionsoftware.vitema.dto.external.OpenFoodFactsResponse;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.QuantityInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OpenFoodFactsService {

    private final RestTemplate restTemplate;

    @Autowired
    public OpenFoodFactsService(@Qualifier("openFoodFactsRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "ingredientsSearchCache", key = "#query + '_' + #limit")
    public List<ParsedProduct> searchIngredients(String query, int limit) {
        try {
            String uri = UriComponentsBuilder
                    .fromPath("/cgi/search.pl")
                    .queryParam("search_terms", query)
                    .queryParam("search_simple", "1")
                    .queryParam("action", "process")
                    .queryParam("json", "1")
                    .queryParam("page_size", Math.min(limit, 50))
                    .queryParam("fields", "product_name,product_name_pl,brands,categories,quantity")
                    .build()
                    .toUriString();

            log.debug("Wyszukiwanie składników w Open Food Facts: {}", query);

            OpenFoodFactsResponse response = restTemplate.getForObject(uri, OpenFoodFactsResponse.class);

            if (response == null || response.getProducts() == null) {
                log.warn("Brak wyników z Open Food Facts dla zapytania: {}", query);
                return new ArrayList<>();
            }

            return response.getProducts().stream()
                    .limit(limit)
                    .map(this::convertToIngredient)
                    .toList();
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania w Open Food Facts dla zapytania: {}", query);
            return new ArrayList<>();
        }
    }

    private ParsedProduct convertToIngredient(OpenFoodFactsProduct product) {
        String name = getPolishName(product);
        String originalName = buildOriginalName(product, name);

        QuantityInfo quantityInfo = parseQuantityFromOpenFoodFacts(product.getQuantity());

        return ParsedProduct.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .quantity(quantityInfo.getQuantity() != null ? quantityInfo.getQuantity() : 1.0)
                .unit(quantityInfo.getPotentialUnit() != null ? quantityInfo.getPotentialUnit() : "szt")
                .original(originalName)
                .hasCustomUnit(false)
                .categoryId(null)
                .build();
    }

    private String getPolishName(OpenFoodFactsProduct product) {
        if (product.getProductNamePl() != null && !product.getProductNamePl().trim().isEmpty()) {
            return product.getProductNamePl().trim();
        }

        if (product.getProductName() != null && !product.getProductName().trim().isEmpty()) {
            return product.getProductName().trim();
        }

        return "Nieznany produkt";
    }

    private String buildOriginalName(OpenFoodFactsProduct product, String name) {
        StringBuilder builder = new StringBuilder(name);

        if (product.getBrands() != null && !product.getBrands().trim().isEmpty()) {
            builder.append(" (").append(product.getBrands().trim()).append(")");
        }

        if (product.getQuantity() != null && !product.getQuantity().trim().isEmpty()) {
            builder.append(" - ").append(product.getQuantity().trim());
        }

        return builder.toString();
    }

    private QuantityInfo parseQuantityFromOpenFoodFacts(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return new QuantityInfo(1.0, "szt");
        }

        // Wzorce dla różnych formatów ilości
        Pattern[] patterns = {
                Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(g|kg|ml|l)"),
                Pattern.compile("(\\d+)\\s*(szt|sztuk|sztuki)"),
                Pattern.compile("(\\d+(?:[.,]\\d+)?)"),
        };

        String cleanQuantity = quantityStr.toLowerCase().trim();

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(cleanQuantity);
            if (matcher.find()) {
                try {
                    double quantity = Double.parseDouble(matcher.group(1).replace(',', '.'));
                    String unit = matcher.groupCount() > 1 ? matcher.group(2) : "szt";

                    // Normalizacja jednostek
                    unit = normalizeUnit(unit);

                    return new QuantityInfo(quantity, unit);
                } catch (NumberFormatException e) {
                    log.warn("Nie można sparsować ilości: {}", quantityStr);
                }
            }
        }

        return new QuantityInfo(1.0, "szt");
    }

    private String normalizeUnit(String unit) {
        return switch (unit.toLowerCase()) {
            case "sztuk", "sztuki" -> "szt";
            case "kg" -> "kg";
            case "g" -> "g";
            case "l" -> "l";
            case "ml" -> "ml";
            default -> unit;
        };
    }
}
