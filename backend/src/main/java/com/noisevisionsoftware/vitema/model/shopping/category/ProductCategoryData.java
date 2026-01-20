package com.noisevisionsoftware.vitema.model.shopping.category;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryData {
    private String productName;
    private String categoryId;
    private int usageCount;

    @Builder.Default
    private List<String> variations = new ArrayList<>();

    private Timestamp lastUsed;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}