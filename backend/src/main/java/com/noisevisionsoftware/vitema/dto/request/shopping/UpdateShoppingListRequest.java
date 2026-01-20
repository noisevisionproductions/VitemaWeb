package com.noisevisionsoftware.vitema.dto.request.shopping;

import com.google.firebase.database.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingListRequest {
    @NotNull
    private Map<String, List<CategorizedShoppingListItemRequest>> items;
}