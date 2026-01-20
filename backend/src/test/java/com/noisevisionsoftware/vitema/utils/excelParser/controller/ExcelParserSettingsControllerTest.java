package com.noisevisionsoftware.vitema.utils.excelParser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.utils.excelParser.config.ExcelParserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ExcelParserSettingsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExcelParserConfig excelParserConfig;

    @InjectMocks
    private ExcelParserSettingsController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        // Default configuration for tests
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(3);
    }

    @Test
    public void getParserSettings_ShouldReturnCurrentSettings() throws Exception {
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(10);

        mockMvc.perform(get("/api/diets/parser-settings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.skipColumnsCount").value(3))
                .andExpect(jsonPath("$.maxSkipColumnsCount").value(10));

        verify(excelParserConfig).getSkipColumnsCount();
        verify(excelParserConfig).getMaxSkipColumnsCount();
    }

    @Test
    public void updateSkipColumnsCount_WithValidData_ShouldUpdateSettings() throws Exception {
        // Prepare for the return value after setting
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(5);

        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("skipColumnsCount", 5);
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(10);

        mockMvc.perform(put("/api/diets/parser-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skipColumnsCount").value(5))
                .andExpect(jsonPath("$.maxSkipColumnsCount").value(10));

        verify(excelParserConfig).setSkipColumnsCount(5);
    }

    @Test
    public void updateSkipColumnsCount_WithMissingData_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();

        mockMvc.perform(put("/api/diets/parser-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("skipColumnsCount is required"))
                .andExpect(jsonPath("$.currentValue").value(3));

        verify(excelParserConfig, never()).setSkipColumnsCount(anyInt());
    }

    @Test
    public void updateSkipColumnsCount_WithNegativeValue_ShouldSetToZero() throws Exception {
        // Prepare for the return value after setting
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(0);

        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("skipColumnsCount", -2);
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(10);

        mockMvc.perform(put("/api/diets/parser-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skipColumnsCount").value(0))
                .andExpect(jsonPath("$.maxSkipColumnsCount").value(10));

        verify(excelParserConfig).setSkipColumnsCount(0);
    }

    @Test
    public void updateSkipColumnsCount_WithValueAboveMax_ShouldSetToMax() throws Exception {
        // Prepare for the return value after setting
        when(excelParserConfig.getSkipColumnsCount()).thenReturn(10);
        when(excelParserConfig.getMaxSkipColumnsCount()).thenReturn(10);

        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("skipColumnsCount", 15); // Above max of 10

        mockMvc.perform(put("/api/diets/parser-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skipColumnsCount").value(10))
                .andExpect(jsonPath("$.maxSkipColumnsCount").value(10));

        verify(excelParserConfig).setSkipColumnsCount(10);
    }
}