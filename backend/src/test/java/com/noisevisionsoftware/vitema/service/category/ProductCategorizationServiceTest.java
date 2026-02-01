package com.noisevisionsoftware.vitema.service.category;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCategorizationServiceTest {

    @Mock
    private FirestoreCategoryDataManager dataManager;

    @InjectMocks
    @Spy
    private ProductCategorizationService service;

    private Map<String, ProductCategoryData> testData;
    private final Timestamp now = Timestamp.now();

    @BeforeEach
    void setUp() {
        testData = new ConcurrentHashMap<>();

        ProductCategoryData mleko = ProductCategoryData.builder()
                .productName("mleko")
                .categoryId("nabiał")
                .usageCount(10)
                .variations(Arrays.asList("mleko 3.2%", "mleko pełne", "mleko świeże"))
                .lastUsed(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProductCategoryData chleb = ProductCategoryData.builder()
                .productName("chleb")
                .categoryId("pieczywo")
                .usageCount(8)
                .variations(Arrays.asList("chleb razowy", "chleb pszenny"))
                .lastUsed(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testData.put("mleko", mleko);
        testData.put("chleb", chleb);

        when(dataManager.loadData()).thenReturn(testData);
        service.init();
    }

    @Test
    void suggestCategory_shouldFindExactMatch() {
        // given
        ParsedProduct product = createParsedProduct("mleko", "mleko");

        // when
        String category = service.suggestCategory(product);

        // then
        assertEquals("nabiał", category);
    }

    @Test
    void suggestCategory_shouldFindVariationMatch() {
        // given
        ParsedProduct product = createParsedProduct("mleko 3.2%", "mleko 3.2%");

        // when
        String category = service.suggestCategory(product);

        // then
        assertEquals("nabiał", category);
    }

    @Test
    void suggestCategory_shouldFindSimilarMatch() {
        // given
        ParsedProduct product = createParsedProduct("mlekoo", "mlekoo");

        // when
        String category = service.suggestCategory(product);

        // then
        assertEquals("nabiał", category);
    }

    @Test
    void suggestCategory_shouldReturnNullWhenNoMatch() {
        // given
        ParsedProduct product = createParsedProduct("banan", "banan");

        // when
        String category = service.suggestCategory(product);

        // then
        assertNull(category);
    }

    @Test
    void updateCategorization_shouldAddNewProductCategory() {
        // given
        ParsedProduct product = createParsedProduct("banan", "banan");
        product.setCategoryId("owoce");

        // when
        service.updateCategorization(product);

        // then
        verify(service).cleanProductName("banan");
        assertTrue(testData.containsKey("banan"));
        assertEquals("owoce", testData.get("banan").getCategoryId());
        assertEquals(1, testData.get("banan").getUsageCount());
        assertTrue(testData.get("banan").getVariations().contains("banan"));
    }

    @Test
    void updateCategorization_shouldUpdateExistingProductCategory() {
        // given
        ParsedProduct product = createParsedProduct("mleko świeże 2%", "mleko świeże 2%");
        product.setCategoryId("nabiał");
        int initialUsageCount = testData.get("mleko").getUsageCount();

        // when
        service.updateCategorization(product);

        // then
        assertEquals(initialUsageCount + 1, testData.get("mleko").getUsageCount());
        assertTrue(testData.get("mleko").getVariations().contains("mleko świeże 2%"));
    }

    @Test
    void updateCategoriesInTransaction_shouldUpdateAllProducts() {
        // given
        Map<String, List<ParsedProduct>> categorizedProducts = new HashMap<>();

        ParsedProduct banan = createParsedProduct("banan", "banan");
        ParsedProduct apple = createParsedProduct("jabłko", "jabłko");
        List<ParsedProduct> owoce = Arrays.asList(banan, apple);

        categorizedProducts.put("owoce", owoce);

        // when
        service.updateCategoriesInTransaction(categorizedProducts);

        // then
        verify(service, times(2)).updateCategorization(any(ParsedProduct.class));
        verify(service).saveDataIfChanged();
    }

    @Test
    void updateCategoriesInTransaction_shouldHandleEmptyMap() {
        // given
        Map<String, List<ParsedProduct>> emptyMap = new HashMap<>();

        // when
        service.updateCategoriesInTransaction(emptyMap);

        // then
        verify(service, never()).updateCategorization(any(ParsedProduct.class));
        verify(service, never()).saveDataIfChanged();
    }

    @Test
    void calculateSimilarity_shouldReturnZeroForShortStrings() {
        // given
        String s1 = "ab";
        String s2 = "cd";

        // when
        double similarity = service.calculateSimilarity(s1, s2);

        // then
        assertEquals(0.0, similarity);
    }

    @Test
    void calculateSimilarity_shouldCalculateCorrectly() {
        // given
        String s1 = "mleko";
        String s2 = "mlekoo";

        // when
        double similarity = service.calculateSimilarity(s1, s2);

        // then
        assertTrue(similarity > 0.8);
        assertTrue(similarity < 0.9);
    }

    @Test
    void cleanProductName_shouldNormalizeString() {
        // given
        String dirtyName = "Mleko 3.2% UHT";

        // when
        String cleanName = service.cleanProductName(dirtyName);

        // then
        assertEquals("mleko 3 2 uht", cleanName);
    }

    private ParsedProduct createParsedProduct(String name, String original) {
        return ParsedProduct.builder()
                .name(name)
                .original(original)
                .quantity(1.0)
                .unit("szt")
                .build();
    }
}