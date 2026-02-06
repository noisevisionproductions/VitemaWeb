package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.shopping.ShoppingListItemRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.UpdateShoppingListDatesRequest;
import com.noisevisionsoftware.vitema.dto.request.shopping.UpdateShoppingListRequest;
import com.noisevisionsoftware.vitema.dto.response.shopping.ShoppingListResponse;
import com.noisevisionsoftware.vitema.mapper.shopping.ShoppingListMapper;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.service.shoppingList.ShoppingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
@Validated
public class ShoppingListController {
    private final ShoppingListService shoppingListService;
    private final ShoppingListMapper shoppingListMapper;

    @GetMapping("/diet/{dietId}")
    public ResponseEntity<ShoppingListResponse> getShoppingListByDietId(
            @PathVariable String dietId) {
        ShoppingList shoppingList = shoppingListService.getShoppingListByDietId(dietId);
        if (shoppingList == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(shoppingListMapper.toResponse(shoppingList));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<ShoppingListResponse> updateItems(
            @PathVariable String id,
            @Valid @RequestBody UpdateShoppingListRequest request) {
        ShoppingList updated = shoppingListService.updateShoppingListItems(
                id,
                shoppingListMapper.toModel(request)
        );
        return ResponseEntity.ok(shoppingListMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}/categories/{categoryId}/items/{itemIndex}")
    public ResponseEntity<Void> removeItem(
            @PathVariable String id,
            @PathVariable String categoryId,
            @PathVariable int itemIndex) {
        shoppingListService.removeItemFromCategory(id, categoryId, itemIndex);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/categories/{categoryId}/items")
    public ResponseEntity<ShoppingListResponse> addItem(
            @PathVariable String id,
            @PathVariable String categoryId,
            @Valid @RequestBody ShoppingListItemRequest request) {
        ShoppingList updated = shoppingListService.addItemToCategory(
                id,
                categoryId,
                shoppingListMapper.toModel(request)
        );
        return ResponseEntity.ok(shoppingListMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/dates")
    public ResponseEntity<ShoppingListResponse> updateDates(
            @PathVariable String id,
            @RequestBody UpdateShoppingListDatesRequest request) {
        ShoppingList updated = shoppingListService.updateDates(
                id,
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(shoppingListMapper.toResponse(updated));
    }
}
