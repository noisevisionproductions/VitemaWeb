package com.noisevisionsoftware.vitema.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealSavePreviewResponse {

    private boolean willCreateNew;
    private boolean foundSimilar;
    private List<MealSuggestionResponse> similarMeals;
    private String recommendedAction; // "CREATE_NEW", "USE_EXISTING", "UPDATE_EXISTING"
    private String message;
}
