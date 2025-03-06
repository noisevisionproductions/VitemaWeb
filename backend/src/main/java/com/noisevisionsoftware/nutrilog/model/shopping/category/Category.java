package com.noisevisionsoftware.nutrilog.model.shopping.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private String id;
    private String name;
    private String color;
    private String icon;
    private int order;
}