package com.noisevisionsoftware.vitema.mapper.shopping;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingListRecipeReference;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FirestoreShoppingMapper {

    public Map<String, Object> toFirestoreMap(ShoppingList shoppingList) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", shoppingList.getId());
        data.put("dietId", shoppingList.getDietId());
        data.put("userId", shoppingList.getUserId());
        data.put("items", mapItemsToFirestore(shoppingList.getItems()));
        data.put("createdAt", shoppingList.getCreatedAt());
        data.put("startDate", shoppingList.getStartDate());
        data.put("endDate", shoppingList.getEndDate());
        data.put("version", shoppingList.getVersion());
        return data;
    }

    private Map<String, List<Map<String, Object>>> mapItemsToFirestore(
            Map<String, List<CategorizedShoppingListItem>> items) {
        if (items == null) return new HashMap<>();

        return items.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::itemToFirestore)
                                .collect(Collectors.toList())
                ));
    }

    private Map<String, Object> itemToFirestore(CategorizedShoppingListItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", item.getName());
        data.put("quantity", item.getQuantity());
        data.put("unit", item.getUnit());
        data.put("original", item.getOriginal());
        if (item.getRecipes() != null) {
            data.put("recipes", item.getRecipes().stream()
                    .map(this::recipeReferenceToFirestore)
                    .collect(Collectors.toList()));
        }
        return data;
    }

    private Map<String, Object> recipeReferenceToFirestore(ShoppingListRecipeReference reference) {
        Map<String, Object> data = new HashMap<>();
        data.put("recipeId", reference.getRecipeId());
        data.put("recipeName", reference.getRecipeName());
        data.put("dayIndex", reference.getDayIndex());
        data.put("mealType", reference.getMealType().name());
        data.put("mealTime", reference.getMealTime());
        return data;
    }

    @SuppressWarnings("unchecked")
    public ShoppingList toShoppingList(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return ShoppingList.builder()
                .id(document.getId())
                .dietId((String) data.get("dietId"))
                .userId((String) data.get("userId"))
                .items(parseItems((Map<String, List<Map<String, Object>>>) data.get("items")))
                .createdAt((Timestamp) data.get("createdAt"))
                .startDate((Timestamp) data.get("startDate"))
                .endDate((Timestamp) data.get("endDate"))
                .version(((Long) data.get("version")).intValue())
                .build();
    }

    private Map<String, List<CategorizedShoppingListItem>> parseItems(
            Map<String, List<Map<String, Object>>> items) {
        if (items == null) return new HashMap<>();

        return items.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::parseCategorizedItem)
                                .collect(Collectors.toList())
                ));
    }

    @SuppressWarnings("unchecked")
    private CategorizedShoppingListItem parseCategorizedItem(Map<String, Object> data) {
        return CategorizedShoppingListItem.builder()
                .name((String) data.get("name"))
                .quantity(((Number) data.get("quantity")).doubleValue())
                .unit((String) data.get("unit"))
                .original((String) data.get("original"))
                .recipes(data.get("recipes") != null ?
                        ((List<Map<String, Object>>) data.get("recipes")).stream()
                                .map(this::parseRecipeReference)
                                .collect(Collectors.toList()) :
                        null)
                .build();
    }

    private ShoppingListRecipeReference parseRecipeReference(Map<String, Object> data) {
        return ShoppingListRecipeReference.builder()
                .recipeId((String) data.get("recipeId"))
                .recipeName((String) data.get("recipeName"))
                .dayIndex(((Long) data.get("dayIndex")).intValue())
                .mealType(MealType.valueOf((String) data.get("mealType")))
                .mealTime((String) data.get("mealTime"))
                .build();
    }
}