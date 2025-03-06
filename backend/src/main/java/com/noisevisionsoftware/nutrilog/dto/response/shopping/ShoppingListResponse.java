package com.noisevisionsoftware.nutrilog.dto.response.shopping;

import com.google.cloud.Timestamp;
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
public class ShoppingListResponse {
    private String id;
    private String dietId;
    private String userId;
    private Map<String, List<CategorizedShoppingListItemResponse>> items;
    private Timestamp createdAt;
    private Timestamp startDate;
    private Timestamp endDate;
    private int version;
}
