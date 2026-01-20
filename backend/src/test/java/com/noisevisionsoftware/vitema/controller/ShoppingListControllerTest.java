package com.noisevisionsoftware.vitema.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.dto.request.shopping.ShoppingListItemRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.UpdateShoppingListRequest;
import com.noisevisionsoftware.vitema.dto.response.shopping.ShoppingListResponse;
import com.noisevisionsoftware.vitema.mapper.shopping.ShoppingListMapper;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.service.ShoppingListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private ShoppingListMapper shoppingListMapper;

    @InjectMocks
    private ShoppingListController shoppingListController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "/api/shopping-lists";
    private ShoppingList sampleShoppingList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(shoppingListController)
                .build();

        sampleShoppingList = ShoppingList.builder()
                .id("test-id")
                .dietId("test-diet-id")
                .userId("test-user-id")
                .items(new HashMap<>())
                .createdAt(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .startDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .endDate(com.google.cloud.Timestamp.of(new Timestamp(Instant.now().toEpochMilli())))
                .version(1)
                .build();
    }

    @Test
    void getShoppingListByDietId_WhenExists_ShouldReturnOk() throws Exception {
        // given
        String dietId = "test-diet-id";
        ShoppingListResponse response = new ShoppingListResponse();

        when(shoppingListService.getShoppingListByDietId(dietId)).thenReturn(sampleShoppingList);
        when(shoppingListMapper.toResponse(sampleShoppingList)).thenReturn(response);

        // when/then
        mockMvc.perform(get(BASE_URL + "/diet/{dietId}", dietId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getShoppingListByDietId_WhenNotExists_ShouldReturnNoContent() throws Exception {
        // given
        String dietId = "non-existing-diet-id";
        when(shoppingListService.getShoppingListByDietId(dietId)).thenReturn(null);

        // when/then
        mockMvc.perform(get(BASE_URL + "/diet/{dietId}", dietId))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateItems_ShouldReturnUpdatedList() throws Exception {
        // given
        String id = "test-id";
        Map<String, List<CategorizedShoppingListItem>> modelItems = new HashMap<>();
        ShoppingListResponse response = new ShoppingListResponse();

        UpdateShoppingListRequest request = new UpdateShoppingListRequest(new HashMap<>());

        when(shoppingListMapper.toModel(any(UpdateShoppingListRequest.class))).thenReturn(modelItems);
        when(shoppingListService.updateShoppingListItems(eq(id), any())).thenReturn(sampleShoppingList);
        when(shoppingListMapper.toResponse(sampleShoppingList)).thenReturn(response);

        // when/then
        mockMvc.perform(put(BASE_URL + "/{id}/items", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_ShouldReturnUpdatedList() throws Exception {
        // given
        String id = "test-id";
        String categoryId = "category-1";
        ShoppingListResponse response = new ShoppingListResponse(); // Załóżmy, że mamy odpowiedni konstruktor

        ShoppingListItemRequest request = ShoppingListItemRequest.builder()
                .name("Test Item")
                .quantity(1.0)
                .unit("kg")
                .original("1 kg")
                .recipes(new ArrayList<>())
                .build();

        CategorizedShoppingListItem item = CategorizedShoppingListItem.builder()
                .name("Test Item")
                .quantity(1.0)
                .unit("kg")
                .original("1 kg")
                .recipes(new ArrayList<>())
                .build();

        when(shoppingListMapper.toModel(any(ShoppingListItemRequest.class))).thenReturn(item);
        when(shoppingListService.addItemToCategory(eq(id), eq(categoryId), any())).thenReturn(sampleShoppingList);
        when(shoppingListMapper.toResponse(sampleShoppingList)).thenReturn(response);

        // when/then
        mockMvc.perform(post(BASE_URL + "/{id}/categories/{categoryId}/items", id, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // given
        String id = "test-id";
        String categoryId = "category-1";

        ShoppingListItemRequest request = ShoppingListItemRequest.builder()
                .name("")  // Invalid - blank
                .quantity(-1.0)  // Invalid - negative
                .unit("")  // Invalid - blank
                .original("")  // Invalid - blank
                .build();

        // when/then
        mockMvc.perform(post(BASE_URL + "/{id}/categories/{categoryId}/items", id, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeItem_ShouldReturnNoContent() throws Exception {
        // given
        String id = "test-id";
        String categoryId = "category-1";
        int itemIndex = 0;

        // when/then
        mockMvc.perform(delete(BASE_URL + "/{id}/categories/{categoryId}/items/{itemIndex}",
                        id, categoryId, itemIndex))
                .andExpect(status().isNoContent());
    }
}