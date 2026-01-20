package com.noisevisionsoftware.vitema.mapper.shopping;

import com.noisevisionsoftware.vitema.dto.request.shopping.CategorizedShoppingListItemRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.ShoppingListItemRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.ShoppingListRecipeReferenceRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.UpdateShoppingListRequest;
import com.noisevisionsoftware.vitema.dto.response.shopping.CategorizedShoppingListItemResponse;
import com.noisevisionsoftware.vitema.dto.response.shopping.ShoppingListRecipeReferenceResponse;
import com.noisevisionsoftware.vitema.dto.response.shopping.ShoppingListResponse;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingListRecipeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShoppingListMapper {

    public Map<String, List<CategorizedShoppingListItem>> toModel(UpdateShoppingListRequest request) {
        if (request == null || request.getItems() == null) {
            return new HashMap<>();
        }

        return request.getItems().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(this::toCategorizedShoppingListItem)
                                .collect(Collectors.toList())
                ));
    }

    public CategorizedShoppingListItem toModel(ShoppingListItemRequest request) {
        return CategorizedShoppingListItem.builder()
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .original(request.getOriginal())
                .recipes(request.getRecipes() != null ?
                        request.getRecipes().stream()
                                .map(this::toShoppingListRecipeReference)
                                .collect(Collectors.toList()) :
                        null)
                .build();
    }

    public CategorizedShoppingListItem toCategorizedShoppingListItem(CategorizedShoppingListItemRequest request) {
        return CategorizedShoppingListItem.builder()
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .original(request.getOriginal())
                .recipes(request.getRecipes() != null ?
                        request.getRecipes().stream()
                                .map(this::toShoppingListRecipeReference)
                                .collect(Collectors.toList()) :
                        null)
                .build();
    }

    private ShoppingListRecipeReference toShoppingListRecipeReference(ShoppingListRecipeReferenceRequest request) {
        return ShoppingListRecipeReference.builder()
                .recipeId(request.getRecipeId())
                .recipeName(request.getRecipeName())
                .dayIndex(request.getDayIndex())
                .mealType(request.getMealType())
                .mealTime(request.getMealTime())
                .build();
    }

    public ShoppingListResponse toResponse(ShoppingList shoppingList) {
        if (shoppingList == null) return null;

        return ShoppingListResponse.builder()
                .id(shoppingList.getId())
                .dietId(shoppingList.getDietId())
                .userId(shoppingList.getUserId())
                .items(mapItemsToResponse(shoppingList.getItems()))
                .createdAt(shoppingList.getCreatedAt())
                .startDate(shoppingList.getStartDate())
                .endDate(shoppingList.getEndDate())
                .version(shoppingList.getVersion())
                .build();
    }

    private Map<String, List<CategorizedShoppingListItemResponse>> mapItemsToResponse(
            Map<String, List<CategorizedShoppingListItem>> items) {
        if (items == null) return new HashMap<>();

        return items.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::toCategorizedShoppingListItemResponse)
                                .collect(Collectors.toList())
                ));
    }

    private CategorizedShoppingListItemResponse toCategorizedShoppingListItemResponse(
            CategorizedShoppingListItem item) {
        return CategorizedShoppingListItemResponse.builder()
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .original(item.getOriginal())
                .recipes(item.getRecipes() != null ?
                        item.getRecipes().stream()
                                .map(this::toShoppingListRecipeReferenceResponse)
                                .collect(Collectors.toList()) :
                        null)
                .build();
    }

    private ShoppingListRecipeReferenceResponse toShoppingListRecipeReferenceResponse(
            ShoppingListRecipeReference reference) {
        return ShoppingListRecipeReferenceResponse.builder()
                .recipeId(reference.getRecipeId())
                .recipeName(reference.getRecipeName())
                .dayIndex(reference.getDayIndex())
                .mealType(reference.getMealType().name())
                .mealTime(reference.getMealTime())
                .build();
    }
}