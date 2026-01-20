package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.utils.excelParser.config.ExcelParserConfig;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsingResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelParserServiceTest {

    @Mock
    private ProductParsingService productParsingService;

    @Mock
    private ProductCategorizationService categorizationService;

    @Mock
    private ExcelParserConfig excelParserConfig;

    @InjectMocks
    private ExcelParserService excelParserService;

    private MultipartFile createMockExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("DietTemplate");

        // Wiersz nagłówkowy
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Lp.");
        headerRow.createCell(1).setCellValue("Nazwa posiłku");
        headerRow.createCell(2).setCellValue("Przygotowanie");
        headerRow.createCell(3).setCellValue("Składniki");
        headerRow.createCell(4).setCellValue("Wartości odżywcze (kcal,białko,tłuszcz,węglowodany)");

        // Dane posiłków
        // Posiłek 1
        Row mealRow1 = sheet.createRow(1);
        mealRow1.createCell(0).setCellValue(1);
        mealRow1.createCell(1).setCellValue("Owsianka z owocami");
        mealRow1.createCell(2).setCellValue("Ugotować płatki na mleku, dodać owoce");
        mealRow1.createCell(3).setCellValue("50g płatki owsiane, 200ml mleko 2%, 1 banan");
        mealRow1.createCell(4).setCellValue("350,15,7,60");

        // Posiłek 2
        Row mealRow2 = sheet.createRow(2);
        mealRow2.createCell(0).setCellValue(2);
        mealRow2.createCell(1).setCellValue("Sałatka z kurczakiem");
        mealRow2.createCell(2).setCellValue("Wymieszać składniki");
        mealRow2.createCell(3).setCellValue("100g pierś z kurczaka, 50g sałata, 20g pomidor");
        mealRow2.createCell(4).setCellValue("250,30,10,5");

        // Pusty wiersz
        sheet.createRow(3);

        // Posiłek 3 (niepełny)
        Row mealRow3 = sheet.createRow(4);
        mealRow3.createCell(0).setCellValue(3);
        mealRow3.createCell(1).setCellValue("Koktajl proteinowy");

        // Zapisanie do strumienia bajtów
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "diet_template.xlsx",
                "diet_template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private MultipartFile createMockExcelFileWithExtraColumns() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("DietTemplate");

        // Wiersz nagłówkowy
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Lp.");
        headerRow.createCell(1).setCellValue("Nazwa posiłku");
        headerRow.createCell(2).setCellValue("Przygotowanie");
        headerRow.createCell(3).setCellValue("Składniki");
        headerRow.createCell(4).setCellValue("Wartości odżywcze (kcal,białko,tłuszcz,węglowodany)");

        // Dane posiłków
        // Posiłek 1
        Row mealRow1 = sheet.createRow(1);
        mealRow1.createCell(0).setCellValue(1);
        mealRow1.createCell(1).setCellValue("Owsianka z owocami");
        mealRow1.createCell(2).setCellValue("Ugotować płatki na mleku, dodać owoce");
        mealRow1.createCell(3).setCellValue("50g płatki owsiane, 200ml mleko 2%, 1 banan");
        mealRow1.createCell(4).setCellValue("350,15,7,60");

        // Posiłek 2
        Row mealRow2 = sheet.createRow(2);
        mealRow2.createCell(0).setCellValue(2);
        mealRow2.createCell(1).setCellValue("Sałatka z kurczakiem");
        mealRow2.createCell(2).setCellValue("Wymieszać składniki");
        mealRow2.createCell(3).setCellValue("100g pierś z kurczaka, 50g sałata, 20g pomidor");
        mealRow2.createCell(4).setCellValue("250,30,10,5");

        // Zapisanie do strumienia bajtów
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "diet_template.xlsx",
                "diet_template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    @Test
    @DisplayName("Powinien poprawnie pomijać określoną liczbę kolumn")
    void parseDietExcel_shouldSkipSpecifiedNumberOfColumns() throws IOException {
        // given
        MultipartFile file = createMockExcelFileWithExtraColumns();
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(1);
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(3);

        ExcelParserService.ParsedExcelResult result1 = excelParserService.parseDietExcel(file);

        ExcelParserService.ParsedExcelResult result2 = excelParserService.parseDietExcel(file, 2);

        ExcelParserService.ParsedExcelResult result3 = excelParserService.parseDietExcel(file, 0);

        // then
        // Dla domyślnej wartości skipColumnsCount (1)
        assertNotNull(result1);
        assertFalse(result1.meals().isEmpty());
        assertEquals("Owsianka z owocami", result1.meals().getFirst().getName());

        // Dla skipColumnsCount = 2
        assertNotNull(result2);
        assertFalse(result2.meals().isEmpty());
        // Powinien odczytać wartość z kolumny 2 (po przeskoczeniu 2 kolumn)
        assertEquals("Ugotować płatki na mleku, dodać owoce", result2.meals().getFirst().getName());

        // Dla skipColumnsCount = 0
        assertNotNull(result3);
        assertFalse(result3.meals().isEmpty());
        // Powinien odczytać wartość z kolumny 0
        assertEquals("1", result3.meals().getFirst().getName());
    }

    @Test
    @DisplayName("Powinien pomijać puste wiersze i wiersze bez nazwy posiłku")
    void parseDietExcel_shouldSkipEmptyRowsAndRowsWithoutMealName() throws IOException {
        // given
        MultipartFile file = createMockExcelFile();

        // when
        ExcelParserService.ParsedExcelResult result = excelParserService.parseDietExcel(file);

        // then
        assertEquals(3, result.totalMeals()); // Tylko 3 posiłki, jeden pusty wiersz został pominięty
    }

    @Test
    @DisplayName("Powinien poprawnie parsować wartości odżywcze")
    void parseNutritionalValues_shouldCorrectlyParseNutritionalValues() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ExcelParserService.class.getDeclaredMethod("parseNutritionalValues", String.class);
        method.setAccessible(true);

        // given, when
        NutritionalValues result1 = (NutritionalValues) method.invoke(excelParserService, "350,15,7,60");
        NutritionalValues result2 = (NutritionalValues) method.invoke(excelParserService, "250,30,10,5");
        NutritionalValues result3 = (NutritionalValues) method.invoke(excelParserService, (Object) null);
        NutritionalValues result4 = (NutritionalValues) method.invoke(excelParserService, "");
        NutritionalValues result5 = (NutritionalValues) method.invoke(excelParserService, "niepoprawne wartości");
        NutritionalValues result6 = (NutritionalValues) method.invoke(excelParserService, "100,200,300,1500"); // Za duża wartość

        // then
        assertNotNull(result1);
        assertEquals(350.0, result1.getCalories());
        assertEquals(15.0, result1.getProtein());
        assertEquals(7.0, result1.getFat());
        assertEquals(60.0, result1.getCarbs());

        assertNotNull(result2);
        assertEquals(250.0, result2.getCalories());
        assertEquals(30.0, result2.getProtein());
        assertEquals(10.0, result2.getFat());
        assertEquals(5.0, result2.getCarbs());

        assertNull(result3); // null wejściowy
        assertNull(result4); // pusty string
        assertNull(result5); // niepoprawny format
        assertNull(result6); // za duża wartość
    }

    @Test
    @DisplayName("Powinien poprawnie parsować pojedynczą wartość odżywczą")
    void parseNutritionalValue_shouldCorrectlyParseNutritionalValue() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ExcelParserService.class.getDeclaredMethod("parseNutritionalValue", String.class);
        method.setAccessible(true);

        // given, when, then
        assertEquals(350.0, (Double) method.invoke(excelParserService, "350"));
        assertEquals(15.5, (Double) method.invoke(excelParserService, "15,5"));
        assertEquals(7.25, (Double) method.invoke(excelParserService, "7.25"));
        assertEquals(0.0, (Double) method.invoke(excelParserService, "0"));
    }

    @Test
    @DisplayName("Powinien poprawnie walidować wartość odżywczą")
    void isValidNutritionalValue_shouldCorrectlyValidateNutritionalValue() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ExcelParserService.class.getDeclaredMethod("isValidNutritionalValue", double.class);
        method.setAccessible(true);

        // given, when, then
        assertTrue((Boolean) method.invoke(excelParserService, 0.0));
        assertTrue((Boolean) method.invoke(excelParserService, 100.0));
        assertTrue((Boolean) method.invoke(excelParserService, 500.0));
        assertTrue((Boolean) method.invoke(excelParserService, 1000.0));

        assertFalse((Boolean) method.invoke(excelParserService, -1.0));
        assertFalse((Boolean) method.invoke(excelParserService, 1001.0));
    }

    @Test
    @DisplayName("Powinien poprawnie parseProduct")
    void parseProduct_shouldCorrectlyParseProduct() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ExcelParserService.class.getDeclaredMethod("parseProduct", String.class);
        method.setAccessible(true);

        // given
        String ingredient = "50g mąka";
        ParsedProduct expectedProduct = ParsedProduct.builder()
                .name("mąka")
                .quantity(50.0)
                .unit("g")
                .original(ingredient)
                .hasCustomUnit(false)
                .build();

        when(productParsingService.parseProduct(ingredient)).thenReturn(new ParsingResult(expectedProduct));
        when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("pieczywo");

        // when
        ParsedProduct result = (ParsedProduct) method.invoke(excelParserService, ingredient);

        // then
        assertNotNull(result);
        assertEquals("mąka", result.getName());
        assertEquals(50.0, result.getQuantity());
        assertEquals("g", result.getUnit());
        assertEquals(ingredient, result.getOriginal());
        assertEquals("pieczywo", result.getCategoryId());

        verify(categorizationService).updateCategorization(any(ParsedProduct.class));
    }

    @Test
    @DisplayName("Powinien obsłużyć błędy w parseProduct")
    void parseProduct_shouldHandleErrorsGracefully() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ExcelParserService.class.getDeclaredMethod("parseProduct", String.class);
        method.setAccessible(true);

        // given
        String ingredient = "niepoprawny składnik";

        // Wymuszenie błędu
        when(productParsingService.parseProduct(ingredient)).thenThrow(new RuntimeException("Błąd parsowania"));

        // when
        ParsedProduct result = (ParsedProduct) method.invoke(excelParserService, ingredient);

        // then
        assertNotNull(result);
        assertEquals(ingredient, result.getName());
        assertEquals(1.0, result.getQuantity());
        assertEquals("szt", result.getUnit());
        assertEquals(ingredient, result.getOriginal());
        assertNull(result.getCategoryId());
    }

    @Test
    @DisplayName("Powinien poprawnie parsować składniki i wartości odżywcze z uwzględnieniem pomijanych kolumn")
    void parseDietExcel_shouldParseIngredientsAndNutritionalValuesWithSkippedColumns() throws IOException {
        // given
        MultipartFile file = createMockExcelFileWithExtraColumns();
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(1);

        when(productParsingService.parseProduct(anyString())).thenAnswer(invocation -> {
            String ingredient = invocation.getArgument(0);

            // Bardzo uproszczone parsowanie na potrzeby testu
            ParsedProduct product = ParsedProduct.builder()
                    .name(ingredient.replaceAll("\\d+[gml]+\\s*", ""))
                    .quantity(1.0)
                    .unit("g")
                    .original(ingredient)
                    .hasCustomUnit(false)
                    .build();

            return new ParsingResult(product);
        });

        when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("testowa-kategoria");

        // when
        ExcelParserService.ParsedExcelResult result = excelParserService.parseDietExcel(file, 1);

        // then
        assertNotNull(result);
        assertFalse(result.meals().isEmpty());

        // Sprawdzenie pierwszego posiłku
        ParsedMeal firstMeal = result.meals().getFirst();
        assertEquals("Owsianka z owocami", firstMeal.getName());
        assertEquals("Ugotować płatki na mleku, dodać owoce", firstMeal.getInstructions());

        // Sprawdzenie składników
        assertNotNull(firstMeal.getIngredients());
        assertFalse(firstMeal.getIngredients().isEmpty());

        // Sprawdzenie wartości odżywczych
        assertNotNull(firstMeal.getNutritionalValues());
        assertEquals(350.0, firstMeal.getNutritionalValues().getCalories());
        assertEquals(15.0, firstMeal.getNutritionalValues().getProtein());
        assertEquals(7.0, firstMeal.getNutritionalValues().getFat());
        assertEquals(60.0, firstMeal.getNutritionalValues().getCarbs());
    }

    @Test
    @DisplayName("Powinien obsłużyć niepoprawne wartości skipColumnsCount")
    void parseDietExcel_shouldHandleInvalidSkipColumnsCount() throws IOException {
        // given
        MultipartFile file = createMockExcelFileWithExtraColumns();
        int defaultSkipColumnsCount = 1;
        int maxSkipColumnsCount = 3;

        when(excelParserConfig.getSkipColumnsCount()).thenReturn(defaultSkipColumnsCount);
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(maxSkipColumnsCount);

        ExcelParserService.ParsedExcelResult result1 = excelParserService.parseDietExcel(file, -1);

        ExcelParserService.ParsedExcelResult result2 = excelParserService.parseDietExcel(file, maxSkipColumnsCount + 1);

        // then
        assertNotNull(result1);
        assertFalse(result1.meals().isEmpty());
        assertEquals("Owsianka z owocami", result1.meals().getFirst().getName());

        assertNotNull(result2);
        assertFalse(result2.meals().isEmpty());
        assertEquals("Owsianka z owocami", result2.meals().getFirst().getName());
    }
}