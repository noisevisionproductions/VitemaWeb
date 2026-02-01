package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.noisevisionsoftware.vitema.utils.excelParser.config.ExcelReadConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExcelReaderServiceTest {

    private ExcelReaderService excelReaderService;

    @BeforeEach
    public void setUp() {
        // Initialize the service manually
        excelReaderService = new ExcelReaderService();
    }

    private MockMultipartFile createExcelFile() throws IOException {
        // Create a sample Excel file in memory
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();

            // Header row
            List<String> headerRow = Arrays.asList("Header1", "Header2", "Header3");
            List<List<String>> headerData = new ArrayList<>();
            headerData.add(headerRow);
            excelWriter.write(headerData, writeSheet);

            // Data rows
            List<List<String>> dataRows = new ArrayList<>();
            dataRows.add(Arrays.asList("Value1", "Value2", "Value3"));
            dataRows.add(Arrays.asList("Row2Col1", "Row2Col2", "Row2Col3"));
            dataRows.add(Arrays.asList(" TrimThis ", "", "  Spaces  "));
            excelWriter.write(dataRows, writeSheet);
        }

        return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );
    }

    @Test
    public void testReadExcelFile_Success() throws IOException {
        // Arrange
        MockMultipartFile excelFile = createExcelFile();

        // Act
        List<List<String>> result = excelReaderService.readExcelFile(excelFile);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size(), "Should read 4 rows (including header)");

        // Check header row
        assertEquals("Header1", result.getFirst().getFirst());
        assertEquals("Header2", result.get(0).get(1));
        assertEquals("Header3", result.get(0).get(2));

        // Check data rows
        assertEquals("Value1", result.get(1).get(0));
        assertEquals("Value2", result.get(1).get(1));
        assertEquals("Value3", result.get(1).get(2));

        assertEquals("Row2Col1", result.get(2).get(0));
        assertEquals("Row2Col2", result.get(2).get(1));
        assertEquals("Row2Col3", result.get(2).get(2));

        // Check trimming functionality
        assertEquals("TrimThis", result.get(3).get(0), "Should trim spaces from values");
        assertEquals("", result.get(3).get(1), "Should handle empty values");
        assertEquals("Spaces", result.get(3).get(2), "Should trim spaces from values");
    }

    @Test
    public void testReadExcelFile_EmptyFile() throws IOException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("EmptySheet").build();
            // Write an empty list to create a valid sheet
            excelWriter.write(new ArrayList<>(), writeSheet);
        }

        MockMultipartFile emptyExcelFile = new MockMultipartFile(
                "empty.xlsx",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );

        // Act
        List<List<String>> result = excelReaderService.readExcelFile(emptyExcelFile);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size(), "Should return empty list for empty excel file");
    }

    @Test
    public void testReadExcelFile_IOException() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);

        // Mock throwing IOException when getInputStream is called
        when(mockFile.getInputStream()).thenThrow(new IOException("Test exception"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            excelReaderService.readExcelFile(mockFile);
        }, "Should throw IOException when file cannot be read");
    }

    @Test
    public void testReadExcelFile_NullValues() throws IOException {
        // Create a special ByteArrayOutputStream to control the Excel content for this test
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();

            // Header row
            List<String> headerRow = Arrays.asList("Header1", "Header2", "Header3");
            List<List<String>> headerData = new ArrayList<>();
            headerData.add(headerRow);
            excelWriter.write(headerData, writeSheet);

            // Create a row with a cell that will be read as null
            List<List<String>> dataRows = new ArrayList<>();
            List<String> dataRow = new ArrayList<>();
            dataRow.add("Value1");
            dataRow.add(null); // This will create a null cell
            dataRow.add("Value3");
            dataRows.add(dataRow);

            excelWriter.write(dataRows, writeSheet);
        }

        MockMultipartFile excelFile = new MockMultipartFile(
                "nullValues.xlsx",
                "nullValues.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );

        // Act
        List<List<String>> result = excelReaderService.readExcelFile(excelFile);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Value1", result.get(1).get(0));
        assertEquals("", result.get(1).get(1), "Null cell should be converted to empty string");
        assertEquals("Value3", result.get(1).get(2));
    }

    @Test
    @DisplayName("Domyślna konfiguracja powinna mieć poprawne wartości")
    void defaultConfigShouldHaveCorrectValues() {
        ExcelReadConfig config = new ExcelReadConfig.Builder().build();

        assertEquals(0, config.getSheetNumber());
        assertEquals(0, config.getHeaderRowNumber());
        assertTrue(config.shouldSkipEmptyRows());
        assertTrue(config.shouldTrimCells());
    }

    @Test
    @DisplayName("Builder powinien prawidłowo ustawiać wartości")
    void builderShouldSetValuesCorrectly() {
        ExcelReadConfig config = new ExcelReadConfig.Builder()
                .sheetNumber(2)
                .headerRowNumber(3)
                .skipEmptyRows(false)
                .trimCells(false)
                .build();

        assertEquals(2, config.getSheetNumber());
        assertEquals(3, config.getHeaderRowNumber());
        assertFalse(config.shouldSkipEmptyRows());
        assertFalse(config.shouldTrimCells());
    }

    @Test
    @DisplayName("Builder powinien umożliwiać płynne konfigurowanie")
    void builderShouldAllowFluentConfiguration() {
        ExcelReadConfig config = new ExcelReadConfig.Builder()
                .sheetNumber(1)
                .headerRowNumber(2)
                .skipEmptyRows(true)
                .trimCells(false)
                .build();

        assertEquals(1, config.getSheetNumber());
        assertEquals(2, config.getHeaderRowNumber());
        assertTrue(config.shouldSkipEmptyRows());
        assertFalse(config.shouldTrimCells());
    }

    @Test
    @DisplayName("Częściowa konfiguracja powinna zachować pozostałe domyślne wartości")
    void partialConfigurationShouldPreserveDefaultValues() {
        ExcelReadConfig config = new ExcelReadConfig.Builder()
                .sheetNumber(1)
                .build();

        assertEquals(1, config.getSheetNumber());
        assertEquals(0, config.getHeaderRowNumber());
        assertTrue(config.shouldSkipEmptyRows());
        assertTrue(config.shouldTrimCells());
    }
}