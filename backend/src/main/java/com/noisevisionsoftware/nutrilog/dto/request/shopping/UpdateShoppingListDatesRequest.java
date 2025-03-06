package com.noisevisionsoftware.nutrilog.dto.request.shopping;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingListDatesRequest {
    private Timestamp startDate;
    private Timestamp endDate;
}