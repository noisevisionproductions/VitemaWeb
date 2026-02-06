# DietCreator Refactoring Summary

## Overview

The `DietCreator.tsx` component has been refactored to align with the new backend architecture where the **Shopping List is generated automatically on the server side** based on product IDs. The manual "Categorization" step is no longer required on the frontend.

## New Flow

```
Configuration → Template Selection → Planning → Preview → Save
     25%             50%              75%        100%
```

**Previous Flow (removed):**
```
Configuration → Template Selection → Planning → Categorization → Preview → Save
     20%             40%              60%           80%           100%
```

---

## Changes Made

### 1. Removed Imports

The following imports were removed as they are no longer needed:

```typescript
// Removed:
- useMemo, useRef (from React)
- useCategorization (hook)
- DietCategorizationService (service)
- CategorySection (component)
```

### 2. Updated Step Type

```typescript
// Before:
type Step = 'templateSelection' | 'configuration' | 'planning' | 'categorization' | 'preview';

// After:
type Step = 'templateSelection' | 'configuration' | 'planning' | 'preview';
```

### 3. Removed State and Logic

The following state variables and logic were removed:

| Removed Item | Description |
|--------------|-------------|
| `shoppingListRef` | Ref used to track shopping list items |
| `currentShoppingListItems` | Memoized calculation of shopping list strings |
| `categorizationShoppingList` | Conditional shopping list for categorization step |
| `useCategorization` hook | All categorization state and handlers |
| `handleCategorizationComplete` | Function to handle categorization completion |

### 4. Updated Navigation Functions

#### `handleNext`
- **Before**: When in 'planning' step, checked if there were shopping list items and redirected to 'categorization' step
- **After**: Proceeds directly from 'planning' to 'preview' step

#### `handlePreviewCancel`
- **Before**: Checked if shopping list had items to decide whether to go back to 'categorization' or 'planning'
- **After**: Always goes back to 'planning' step

### 5. Updated `convertToPreviewData`

```typescript
// Before: Built categorizedProducts from categorization state
const convertToPreviewData = useCallback((): ParsedDietData => {
    const simplifiedCategorizedProducts: Record<string, string[]> = {};
    Object.entries(categorizedProducts).forEach(([categoryId, products]) => {
        simplifiedCategorizedProducts[categoryId] = products.map(product =>
            product.original || `${product.name} ${product.quantity} ${product.unit}`
        );
    });
    return {
        days: dietData.days,
        categorizedProducts: simplifiedCategorizedProducts,
        shoppingList: shoppingListRef.current,
        // ...
    };
}, [dietData, categorizedProducts]);

// After: Empty categorizedProducts and shoppingList (backend handles this)
const convertToPreviewData = useCallback((): ParsedDietData => {
    // Shopping list is now generated automatically on the backend based on product IDs
    return {
        days: dietData.days,
        categorizedProducts: {},
        shoppingList: [],
        // ...
    };
}, [dietData]);
```

### 6. Updated Progress Bar

```typescript
// Before: 5 steps (20% increments)
width: currentStep === 'configuration' ? '20%' :
       currentStep === 'templateSelection' ? '40%' :
       currentStep === 'planning' ? '60%' :
       currentStep === 'categorization' ? '80%' : '100%'

// After: 4 steps (25% increments)
width: currentStep === 'configuration' ? '25%' :
       currentStep === 'templateSelection' ? '50%' :
       currentStep === 'planning' ? '75%' : '100%'
```

### 7. Removed UI Elements

- Removed `CategorySection` component rendering
- Removed 'categorization' cases from `getStepTitle()` and `getStepDescription()`
- Simplified navigation button visibility condition

---

## How Shopping List Categorization Now Works

### Before (Frontend-driven)
1. User adds ingredients to meals in the Planning step
2. Frontend collects all ingredient strings (e.g., "Chicken breast 200 g")
3. User manually categorizes each ingredient in the Categorization step
4. Frontend sends categorized products to the backend
5. Backend stores the pre-categorized shopping list

### After (Backend-driven)
1. User adds ingredients to meals in the Planning step
2. Each ingredient is linked to a **product ID** from the product database
3. Frontend sends meal data with **product IDs** to the backend
4. Backend automatically:
   - Looks up each product by ID
   - Retrieves the product's category from the database
   - Generates the categorized shopping list
   - Aggregates quantities for duplicate products

### Product ID Preservation

The `draftIngredientToParsedProduct` function correctly preserves the `productId`:

```typescript
function draftIngredientToParsedProduct(i: DietIngredientDto): ParsedProduct {
    return {
        name: i.name,
        quantity: i.quantity,
        unit: i.unit,
        original: i.name,
        categoryId: i.categoryId ?? undefined,
        id: i.productId ?? undefined,  // Product ID is preserved here
    };
}
```

---

## Benefits

| Benefit | Description |
|---------|-------------|
| **Simplified UX** | Users no longer need to manually categorize ingredients |
| **Faster workflow** | One less step in the diet creation process |
| **Consistent categorization** | Products are always categorized the same way (from database) |
| **Reduced frontend complexity** | ~100 lines of code removed |
| **Better data integrity** | Categorization is driven by the single source of truth (product database) |

---

## Files Affected

- `frontend/src/components/vitema/diet/creator/DietCreator.tsx` - Main component refactored

## Potentially Unused Files (Consider for Cleanup)

After this refactoring, the following files may no longer be needed:

- `frontend/src/hooks/shopping/useCategorization.ts`
- `frontend/src/services/diet/DietCategorizationService.ts`
- `frontend/src/components/vitema/diet/creator/steps/Categorization/CategorySection.tsx`

Verify these are not used elsewhere before removing.
