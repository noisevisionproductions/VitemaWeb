package com.noisevisionsoftware.vitema.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpenFoodFactsProduct {
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_name_pl")
    private String productNamePl;

    @JsonProperty("brands")
    private String brands;

    @JsonProperty("quantity")
    private String quantity;
}
