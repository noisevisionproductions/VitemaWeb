package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;

    private static final String SHOPPING_LIST_CACHE = "shoppingListCache";

    @Cacheable(value = SHOPPING_LIST_CACHE, key = "#dietId")
    public ShoppingList getShoppingListByDietId(String dietId) {
        return shoppingListRepository.findByDietId(dietId)
                .orElse(null);
    }

    public void saveShoppingList(ShoppingList shoppingList) {
        shoppingListRepository.save(shoppingList);
    }

    public ShoppingList updateShoppingListItems(String id, Map<String, List<CategorizedShoppingListItem>> items) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        if (items == null || items.isEmpty()) {
            log.warn("Received empty items map for shopping list update: {}", id);
            return shoppingList;
        }

        Map<String, List<CategorizedShoppingListItem>> updatedItems = new HashMap<>();
        items.forEach((key, value) -> {
            if (value != null) {
                updatedItems.put(key, new ArrayList<>(value));
            }
        });

        shoppingList.setItems(updatedItems);

        try {
            ShoppingList result = shoppingListRepository.save(shoppingList);
            refreshShoppingListCache();
            return result;
        } catch (Exception e) {
            log.error("Error updating shopping list items: {}", id, e);
            throw new RuntimeException("Failed to update shopping list items", e);
        }
    }

    public void removeItemFromCategory(String id, String categoryId, int itemIndex) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        Map<String, List<CategorizedShoppingListItem>> items = shoppingList.getItems();
        if (items.containsKey(categoryId)) {
            List<CategorizedShoppingListItem> categoryItems = items.get(categoryId);
            if (itemIndex >= 0 && itemIndex < categoryItems.size()) {
                categoryItems.remove(itemIndex);
                if (categoryItems.isEmpty()) {
                    items.remove(categoryId);
                }
                shoppingListRepository.save(shoppingList);
            }
        }
    }

    public ShoppingList addItemToCategory(String id, String categoryId, CategorizedShoppingListItem item) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        Map<String, List<CategorizedShoppingListItem>> items = shoppingList.getItems();
        items.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(item);

        shoppingListRepository.save(shoppingList);
        return shoppingList;
    }

    public ShoppingList updateDates(String id, Timestamp startDate, Timestamp endDate) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list not found: " + id));

        shoppingList.setStartDate(startDate);
        shoppingList.setEndDate(endDate);

        shoppingListRepository.save(shoppingList);
        return shoppingList;
    }

    @CacheEvict(value = SHOPPING_LIST_CACHE, allEntries = true)
    public void refreshShoppingListCache() {
        log.debug("Odświeżenie cache list zakupów");
    }
}