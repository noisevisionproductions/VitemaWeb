import { ParsedProduct } from "../types/product";
import { MealIngredient } from "../types/mealSuggestions";

/**
 * Konwertuje ParsedProduct na MealIngredient
 */
export const convertParsedProductToMealIngredient = (product: ParsedProduct): MealIngredient => {
    return {
        id: product.id,
        name: product.name,
        quantity: product.quantity,
        unit: product.unit,
        original: product.original,
        categoryId: product.categoryId,
        hasCustomUnit: product.hasCustomUnit ?? false // Konwersja undefined na false
    };
};

/**
 * Konwertuje tablicę ParsedProduct na tablicę MealIngredient
 */
export const convertParsedProductsToMealIngredients = (products: ParsedProduct[]): MealIngredient[] => {
    return products.map(convertParsedProductToMealIngredient);
};

/**
 * Konwertuje MealIngredient na ParsedProduct
 */
export const convertMealIngredientToParsedProduct = (ingredient: MealIngredient): ParsedProduct => {
    return {
        id: ingredient.id,
        name: ingredient.name,
        quantity: ingredient.quantity,
        unit: ingredient.unit,
        original: ingredient.original,
        categoryId: ingredient.categoryId,
        hasCustomUnit: ingredient.hasCustomUnit
    };
};

/**
 * Konwertuje tablicę MealIngredient na tablicę ParsedProduct
 */
export const convertMealIngredientsToParsedProducts = (ingredients: MealIngredient[]): ParsedProduct[] => {
    return ingredients.map(convertMealIngredientToParsedProduct);
};