package com.noisevisionsoftware.vitema.dto.request.category;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ParseProductsRequest {
    private List<String> products;

    public ParseProductsRequest() {
        this.products = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ParseProductsRequest{products=" + products + '}';
    }
}