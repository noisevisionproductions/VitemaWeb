# Frontend Diet Planning Refactor – Summary

Short summary of frontend changes that align with the backend’s flexible diet planning system.

## 1. Service layer & types

- **`frontend/src/types/search.ts`**  
  New type `UnifiedSearchResult` (and `UnifiedSearchResultType`: `'RECIPE' | 'PRODUCT'`) matching backend `UnifiedSearchDto`: `id`, `name`, `type`, `nutritionalValues`, `unit`, `photos`, `authorId`.
- **`frontend/src/types/meal.ts`**  
  `ParsedMeal` extended with optional `originalRecipeId`; `recipeId` kept but deprecated.
- **`frontend/src/services/diet/creator/DietCreatorService.ts`**  
  - `searchUnified(query, trainerId?)` – calls `GET /diets/manager/search`.  
  - `getTrainerDietHistory()` – calls `GET /diets/manager/history`.  
  - `loadDietDraft(oldDietId)` – calls `GET /diets/manager/draft/:id`.

## 2. Backend: unified search endpoint

- **`DietManagerController`**  
  New `GET /api/diets/manager/search?query=...&trainerId=...` that uses `UnifiedSearchService` and returns a single list of recipes and products.

## 3. Unified search in meal editor

- **`MealNameSearchField.tsx`**  
  Replaced recipe/meal-suggestion search with `DietCreatorService.searchUnified()`. Shows both recipes and products with type badges (Przepis / Produkt). New callback: `onUnifiedResultSelect(result: UnifiedSearchResult)`.
- **`MealEditorHeader.tsx`**  
  Uses `onUnifiedResultSelect` and `trainerId` instead of `onMealSelect`; passes them into `MealNameSearchField`.
- **`MealEditor.tsx`**  
  New `handleUnifiedResultSelect`:  
  - **RECIPE:** loads full recipe via `RecipeService.getRecipeById`, then sets meal `name`, `instructions`, `ingredients`, `photos`, `nutritionalValues`, and `originalRecipeId`.  
  - **PRODUCT:** adds one ingredient to the current meal (does not overwrite meal name if already set).
- **`mealConverters.ts`**  
  Added `convertRecipeIngredientToParsedProduct` and `convertRecipeIngredientsToParsedProducts` for “exploding” a recipe into a meal.

## 4. Meal editor behaviour

- Ingredients and instructions remain freely editable regardless of `originalRecipeId`; no “Save as new Recipe” prompt when only ingredients change. The commented-out Template Change Manager is unchanged; backend accepts embedded meal data (including modified ingredients) in the diet structure.

## 5. Copy day

- **`MealPlanningStep.tsx`**  
  - State: `copyDaySource` (day index or null), `copyDayTargets` (selected day indices).  
  - `openCopyDayModal(sourceDayIndex)`, `toggleCopyDayTarget(dayIndex)`, `confirmCopyDay()` – duplicate source day’s `meals` (cloned) into selected target days via existing `onUpdateMeal`.  
  - Modal: “Skopiuj dzień X do” with checkboxes for other days and Confirm/Cancel.
- **`DayPlanningCard.tsx`**  
  New “Skopiuj dzień” button in the day header and prop `onCopyDayClick`; clicking opens the copy-day modal for that day.

## Notes

- Existing styles and UI patterns (e.g. day cards, modals) were kept.
- `getTrainerDietHistory` and `loadDietDraft` are implemented on the frontend; backend endpoints (`/history`, `/draft/:id`) need to be added if not present.
- Diet save/update payloads already support embedded meals and `originalRecipeId` via the existing backend DTOs (`DietMealDto`, `UpdateDietRequest`).
