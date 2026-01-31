package com.noisevisionsoftware.vitema.controller.diet;

import com.noisevisionsoftware.vitema.dto.request.category.BulkCategoryRequest;
import com.noisevisionsoftware.vitema.dto.request.category.ParseProductsRequest;
import com.noisevisionsoftware.vitema.dto.request.category.UpdateCategoriesRequest;
import com.noisevisionsoftware.vitema.dto.request.category.UpdateProductRequest;
import com.noisevisionsoftware.vitema.model.shopping.category.Category;
import com.noisevisionsoftware.vitema.service.category.FirestoreCategoryDataManager;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsingResult;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ProductParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietCategorizationControllerTest {

    @Mock
    private ProductCategorizationService categorizationService;

    @Mock
    private FirestoreCategoryDataManager firestoreCategoryDataManager;

    @Mock
    private ProductParsingService productParsingService;

    @Mock
    private List<Category> defaultCategories;

    @InjectMocks
    private DietCategorizationController controller;

    private ParsedProduct testProduct;
    private ParseProductsRequest parseProductsRequest;
    private UpdateProductRequest updateProductRequest;
    private UpdateCategoriesRequest updateCategoriesRequest;
    private BulkCategoryRequest bulkCategoryRequest;
    private Category testCategory;
    private ParsingResult parsingResult;

    private static final String TEST_PRODUCT_NAME = "Mleko 2% 1L";
    private static final String TEST_CATEGORY_ID = "dairy";
    private static final String TEST_PRODUCT_ID = "product-123";

    @BeforeEach
    void setUp() {
        testProduct = ParsedProduct.builder()
                .id(TEST_PRODUCT_ID)
                .name("Mleko")
                .quantity(1.0)
                .unit("L")
                .original(TEST_PRODUCT_NAME)
                .hasCustomUnit(false)
                .categoryId(TEST_CATEGORY_ID)
                .build();

        parseProductsRequest = new ParseProductsRequest();
        parseProductsRequest.setProducts(Arrays.asList(TEST_PRODUCT_NAME, "Chleb 500g"));

        updateProductRequest = new UpdateProductRequest(testProduct, testProduct);

        updateCategoriesRequest = new UpdateCategoriesRequest();
        Map<String, List<ParsedProduct>> categorizedProducts = new HashMap<>();
        categorizedProducts.put(TEST_CATEGORY_ID, Collections.singletonList(testProduct));
        updateCategoriesRequest.setCategorizedProducts(categorizedProducts);

        bulkCategoryRequest = new BulkCategoryRequest(Arrays.asList(testProduct));

        testCategory = new Category();
        testCategory.setId(TEST_CATEGORY_ID);
        testCategory.setName("Nabiał");

        parsingResult = new ParsingResult();
        parsingResult.setSuccess(true);
        parsingResult.setProduct(testProduct);
    }

    // POST /api/diets/categorization/parse - parseProducts tests

    @Test
    void parseProducts_WithValidProducts_ShouldReturnParsedProductsWithCategories() {
        // Arrange
        when(productParsingService.parseProduct(anyString())).thenReturn(parsingResult);
        when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn(TEST_CATEGORY_ID);

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(TEST_CATEGORY_ID, response.getBody().get(0).getCategoryId());
        verify(productParsingService, times(2)).parseProduct(anyString());
        verify(categorizationService, times(2)).suggestCategory(any(ParsedProduct.class));
    }

    @Test
    void parseProducts_WithEmptyProductList_ShouldReturnEmptyList() {
        // Arrange
        parseProductsRequest.setProducts(Collections.emptyList());

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verifyNoInteractions(productParsingService);
        verifyNoInteractions(categorizationService);
    }

    @Test
    void parseProducts_WithNullProductList_ShouldReturnEmptyList() {
        // Arrange
        parseProductsRequest.setProducts(null);

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verifyNoInteractions(productParsingService);
        verifyNoInteractions(categorizationService);
    }

    @Test
    void parseProducts_WhenParsingFails_ShouldReturnDefaultProduct() {
        // Arrange
        ParsingResult failedResult = new ParsingResult();
        failedResult.setSuccess(false);
        failedResult.setProduct(null);
        when(productParsingService.parseProduct(anyString())).thenReturn(failedResult);
        when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn(null);

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        ParsedProduct defaultProduct = response.getBody().get(0);
        assertEquals(TEST_PRODUCT_NAME, defaultProduct.getName());
        assertEquals(1.0, defaultProduct.getQuantity());
        assertEquals("szt", defaultProduct.getUnit());
        assertFalse(defaultProduct.isHasCustomUnit());
        verify(productParsingService, times(2)).parseProduct(anyString());
    }

    @Test
    void parseProducts_WhenParsingThrowsException_ShouldReturnDefaultProduct() {
        // Arrange
        when(productParsingService.parseProduct(anyString()))
                .thenThrow(new RuntimeException("Parsing error"));

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        ParsedProduct defaultProduct = response.getBody().get(0);
        assertEquals(TEST_PRODUCT_NAME, defaultProduct.getName());
        assertNull(defaultProduct.getCategoryId());
        verify(productParsingService, times(2)).parseProduct(anyString());
    }

    @Test
    void parseProducts_WithCategorizationServiceReturningNull_ShouldSetNullCategoryId() {
        // Arrange
        when(productParsingService.parseProduct(anyString())).thenReturn(parsingResult);
        when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn(null);

        // Act
        ResponseEntity<List<ParsedProduct>> response = controller.parseProducts(parseProductsRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertNull(response.getBody().get(0).getCategoryId());
        verify(categorizationService, times(2)).suggestCategory(any(ParsedProduct.class));
    }

    // PUT /api/diets/categorization/product - updateProduct tests

    @Test
    void updateProduct_WithValidRequest_ShouldReturnUpdatedProduct() {
        // Arrange
        when(firestoreCategoryDataManager.updateProduct(any(ParsedProduct.class)))
                .thenReturn(testProduct);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(testProduct, response.getBody().get("product"));
        assertEquals("Produkt został zaktualizowany", response.getBody().get("message"));
        verify(firestoreCategoryDataManager).updateProduct(testProduct);
    }

    @Test
    void updateProduct_WithNullOldProduct_ShouldReturnBadRequest() {
        // Arrange
        updateProductRequest.setOldProduct(null);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Brak wymaganych danych produktu", response.getBody().get("message"));
        verifyNoInteractions(firestoreCategoryDataManager);
    }

    @Test
    void updateProduct_WithNullNewProduct_ShouldReturnBadRequest() {
        // Arrange
        updateProductRequest.setNewProduct(null);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Brak wymaganych danych produktu", response.getBody().get("message"));
        verifyNoInteractions(firestoreCategoryDataManager);
    }

    @Test
    void updateProduct_WithNullOriginalInNewProduct_ShouldCopyFromOldProduct() {
        // Arrange
        ParsedProduct newProductWithoutOriginal = ParsedProduct.builder()
                .id(TEST_PRODUCT_ID)
                .name("Updated Name")
                .quantity(2.0)
                .unit("L")
                .original(null)
                .categoryId(TEST_CATEGORY_ID)
                .build();
        updateProductRequest.setNewProduct(newProductWithoutOriginal);

        when(firestoreCategoryDataManager.updateProduct(any(ParsedProduct.class)))
                .thenReturn(newProductWithoutOriginal);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_PRODUCT_NAME, newProductWithoutOriginal.getOriginal());
        verify(firestoreCategoryDataManager).updateProduct(newProductWithoutOriginal);
    }

    @Test
    void updateProduct_WithBothOriginalFieldsNull_ShouldUseNewProductName() {
        // Arrange
        ParsedProduct oldProductWithoutOriginal = ParsedProduct.builder()
                .id(TEST_PRODUCT_ID)
                .name("Old Name")
                .original(null)
                .build();
        ParsedProduct newProductWithoutOriginal = ParsedProduct.builder()
                .id(TEST_PRODUCT_ID)
                .name("New Name")
                .original(null)
                .build();
        updateProductRequest.setOldProduct(oldProductWithoutOriginal);
        updateProductRequest.setNewProduct(newProductWithoutOriginal);

        when(firestoreCategoryDataManager.updateProduct(any(ParsedProduct.class)))
                .thenReturn(newProductWithoutOriginal);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Name", newProductWithoutOriginal.getOriginal());
        verify(firestoreCategoryDataManager).updateProduct(newProductWithoutOriginal);
    }

    @Test
    void updateProduct_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Update failed";
        when(firestoreCategoryDataManager.updateProduct(any(ParsedProduct.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateProduct(updateProductRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains(errorMessage));
        verify(firestoreCategoryDataManager).updateProduct(any(ParsedProduct.class));
    }

    // POST /api/diets/categorization/update - updateCategories tests

    @Test
    void updateCategories_WithValidRequest_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(categorizationService).updateCategoriesInTransaction(anyMap());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateCategories(updateCategoriesRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("Zaktualizowano 1 produktów"));
        verify(categorizationService).updateCategoriesInTransaction(anyMap());
    }

    @Test
    void updateCategories_WithMultipleCategories_ShouldReturnCorrectCount() {
        // Arrange
        Map<String, List<ParsedProduct>> categorizedProducts = new HashMap<>();
        categorizedProducts.put("dairy", Arrays.asList(testProduct, testProduct));
        categorizedProducts.put("bread", Collections.singletonList(testProduct));
        updateCategoriesRequest.setCategorizedProducts(categorizedProducts);

        doNothing().when(categorizationService).updateCategoriesInTransaction(anyMap());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateCategories(updateCategoriesRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("Zaktualizowano 3 produktów"));
        verify(categorizationService).updateCategoriesInTransaction(anyMap());
    }

    @Test
    void updateCategories_WithNullProducts_ShouldReturnBadRequest() {
        // Arrange
        updateCategoriesRequest.setCategorizedProducts(null);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateCategories(updateCategoriesRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Brak produktów do kategoryzacji", response.getBody().get("message"));
        verifyNoInteractions(categorizationService);
    }

    @Test
    void updateCategories_WithEmptyProducts_ShouldReturnBadRequest() {
        // Arrange
        updateCategoriesRequest.setCategorizedProducts(Collections.emptyMap());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateCategories(updateCategoriesRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Brak produktów do kategoryzacji", response.getBody().get("message"));
        verifyNoInteractions(categorizationService);
    }

    @Test
    void updateCategories_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Transaction failed";
        doThrow(new RuntimeException(errorMessage))
                .when(categorizationService).updateCategoriesInTransaction(anyMap());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateCategories(updateCategoriesRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains(errorMessage));
        verify(categorizationService).updateCategoriesInTransaction(anyMap());
    }

    // GET /api/diets/categorization/categories - getCategories tests

    @Test
    void getCategories_ShouldReturnDefaultCategories() {
        // Act
        ResponseEntity<List<Category>> response = controller.getCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(defaultCategories, response.getBody());
    }

    @Test
    void getCategories_WhenEmpty_ShouldReturnEmptyList() {
        // Act
        ResponseEntity<List<Category>> response = controller.getCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // POST /api/diets/categorization/suggest - suggestCategory tests

    @Test
    void suggestCategory_WithValidProduct_ShouldReturnCategoryId() {
        // Arrange
        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenReturn(TEST_CATEGORY_ID);

        // Act
        ResponseEntity<Map<String, String>> response = controller.suggestCategory(testProduct);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_CATEGORY_ID, response.getBody().get("categoryId"));
        verify(categorizationService).suggestCategory(testProduct);
    }

    @Test
    void suggestCategory_WhenServiceReturnsNull_ShouldReturnUncategorized() {
        // Arrange
        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = controller.suggestCategory(testProduct);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("uncategorized", response.getBody().get("categoryId"));
        verify(categorizationService).suggestCategory(testProduct);
    }

    @Test
    void suggestCategory_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Categorization error";
        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<Map<String, String>> response = controller.suggestCategory(testProduct);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("error"));
        verify(categorizationService).suggestCategory(testProduct);
    }

    // POST /api/diets/categorization/suggest/bulk - bulkSuggestCategories tests

    @Test
    void bulkSuggestCategories_WithValidProducts_ShouldReturnCategorySuggestions() {
        // Arrange
        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenReturn(TEST_CATEGORY_ID);

        // Act
        ResponseEntity<Map<String, String>> response = controller.bulkSuggestCategories(bulkCategoryRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(TEST_CATEGORY_ID, response.getBody().get(TEST_PRODUCT_NAME));
        verify(categorizationService).suggestCategory(testProduct);
    }

    @Test
    void bulkSuggestCategories_WithMultipleProducts_ShouldReturnAllSuggestions() {
        // Arrange
        ParsedProduct product2 = ParsedProduct.builder()
                .name("Chleb")
                .original("Chleb 500g")
                .build();
        bulkCategoryRequest.setProducts(Arrays.asList(testProduct, product2));

        when(categorizationService.suggestCategory(testProduct))
                .thenReturn(TEST_CATEGORY_ID);
        when(categorizationService.suggestCategory(product2))
                .thenReturn("bread");

        // Act
        ResponseEntity<Map<String, String>> response = controller.bulkSuggestCategories(bulkCategoryRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(TEST_CATEGORY_ID, response.getBody().get(TEST_PRODUCT_NAME));
        assertEquals("bread", response.getBody().get("Chleb 500g"));
        verify(categorizationService, times(2)).suggestCategory(any(ParsedProduct.class));
    }

    @Test
    void bulkSuggestCategories_WithProductWithoutOriginal_ShouldUseNameAsKey() {
        // Arrange
        ParsedProduct productWithoutOriginal = ParsedProduct.builder()
                .name("Chleb")
                .original(null)
                .build();
        bulkCategoryRequest.setProducts(Collections.singletonList(productWithoutOriginal));

        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenReturn("bread");

        // Act
        ResponseEntity<Map<String, String>> response = controller.bulkSuggestCategories(bulkCategoryRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("bread", response.getBody().get("Chleb"));
        verify(categorizationService).suggestCategory(productWithoutOriginal);
    }

    @Test
    void bulkSuggestCategories_WithEmptyProductList_ShouldReturnEmptyMap() {
        // Arrange
        bulkCategoryRequest.setProducts(Collections.emptyList());

        // Act
        ResponseEntity<Map<String, String>> response = controller.bulkSuggestCategories(bulkCategoryRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verifyNoInteractions(categorizationService);
    }

    @Test
    void bulkSuggestCategories_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Bulk categorization failed";
        when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<Map<String, String>> response = controller.bulkSuggestCategories(bulkCategoryRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("error"));
        verify(categorizationService).suggestCategory(testProduct);
    }
}
