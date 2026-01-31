package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.config.JacksonConfig;
import com.noisevisionsoftware.vitema.controller.diet.DietController;
import com.noisevisionsoftware.vitema.dto.request.diet.DayMealRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DayRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietMetadataRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.DietResponse;
import com.noisevisionsoftware.vitema.mapper.diet.DietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DietController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
class DietControllerIntegrationTest {

    private static final String BASE_URL = "/api/diets";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_DIET_ID = "diet-456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DietService dietService;

    @MockBean
    private DietMapper dietMapper;

    private Diet createTestDiet() {
        return Diet.builder()
                .id(TEST_DIET_ID)
                .userId(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(Collections.emptyList())
                .build();
    }

    private DietRequest createValidDietRequest() {
        DayMealRequest meal = DayMealRequest.builder()
                .recipeId("recipe-1")
                .mealType(MealType.BREAKFAST)
                .time("08:00")
                .build();

        DayRequest day = DayRequest.builder()
                .date(Timestamp.ofTimeSecondsAndNanos(1704067200, 0))
                .meals(List.of(meal))
                .build();

        DietMetadataRequest metadata = DietMetadataRequest.builder()
                .totalDays(1)
                .fileName("test-diet")
                .fileUrl("http://example.com/diet")
                .build();

        return DietRequest.builder()
                .userId(TEST_USER_ID)
                .days(List.of(day))
                .metadata(metadata)
                .build();
    }

    @Nested
    @DisplayName("createDiet")
    class CreateDietTests {

        @Test
        @DisplayName("Should return 201 Created and call service when request is valid")
        void givenValidRequest_When_CreateDiet_Then_Return201AndCallService() throws Exception {
            // Given
            DietRequest request = createValidDietRequest();
            Diet savedDiet = createTestDiet();
            DietResponse expectedResponse = DietResponse.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .build();

            when(dietMapper.toDomain(any(DietRequest.class))).thenReturn(savedDiet);
            when(dietService.createDiet(any(Diet.class))).thenReturn(savedDiet);
            when(dietMapper.toResponse(any(Diet.class))).thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(TEST_DIET_ID))
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID));

            verify(dietService).createDiet(any(Diet.class));
        }
    }

    @Nested
    @DisplayName("getAllDiets")
    class GetAllDietsTests {

        @Test
        @DisplayName("Should return list of diets for user when userId param provided")
        void givenUserIdParam_When_GetAllDiets_Then_ReturnUserDiets() throws Exception {
            // Given
            Diet diet = createTestDiet();
            DietResponse dietResponse = DietResponse.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .build();
            when(dietService.getDietsByUserId(TEST_USER_ID)).thenReturn(List.of(diet));
            when(dietMapper.toResponse(any(Diet.class))).thenReturn(dietResponse);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("userId", TEST_USER_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(TEST_DIET_ID))
                    .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID));

            verify(dietService).getDietsByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return all diets when no userId param")
        void givenNoUserIdParam_When_GetAllDiets_Then_ReturnAllDiets() throws Exception {
            // Given
            Diet diet = createTestDiet();
            DietResponse dietResponse = DietResponse.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .build();
            when(dietService.getAllDiets()).thenReturn(List.of(diet));
            when(dietMapper.toResponse(any(Diet.class))).thenReturn(dietResponse);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(TEST_DIET_ID));

            verify(dietService).getAllDiets();
        }
    }

    @Nested
    @DisplayName("deleteDiet")
    class DeleteDietTests {

        @Test
        @DisplayName("Should return 204 No Content when diet deleted successfully")
        void givenExistingDietId_When_DeleteDiet_Then_Return204NoContent() throws Exception {
            // Given - no setup needed, controller just delegates to service

            // When & Then
            mockMvc.perform(delete(BASE_URL + "/{id}", TEST_DIET_ID))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(dietService).deleteDiet(TEST_DIET_ID);
        }
    }
}
