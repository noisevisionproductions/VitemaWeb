package com.noisevisionsoftware.vitema.model.shopping;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ShoppingList {
    private String id;
    private String dietId;
    private String userId;
    private Map<String, List<CategorizedShoppingListItem>> items;
    private Timestamp createdAt;
    private Timestamp startDate;
    private Timestamp endDate;
    private int version;
}