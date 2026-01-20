package com.noisevisionsoftware.vitema.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenFoodFactsResponse {

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("page_count")
    private Integer pageCount;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("products")
    private List<OpenFoodFactsProduct> products;
}
