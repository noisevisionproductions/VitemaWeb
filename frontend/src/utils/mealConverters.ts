import { ParsedProduct } from "../types/product";
import { MealIngredient } from "../types/mealSuggestions";
import type { RecipeIngredient} from "../types";

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
        hasCustomUnit: product.hasCustomUnit ?? false
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
 * Converts RecipeIngredient to ParsedProduct (e.g. when "exploding" a recipe into a meal).
 */
export const convertRecipeIngredientToParsedProduct = (ingredient: RecipeIngredient): ParsedProduct => {
    return {
        id: (ingredient.productId as string) || ingredient.id,
        name: ingredient.name,
        quantity: ingredient.quantity,
        unit: ingredient.unit,
        original: ingredient.original ?? ingredient.name,
        categoryId: ingredient.categoryId,
        hasCustomUnit: ingredient.hasCustomUnit ?? false,
    };
};

/**
 * Converts Recipe.ingredients to ParsedProduct[].
 */
export const convertRecipeIngredientsToParsedProducts = (ingredients: RecipeIngredient[] = []): ParsedProduct[] => {
    return ingredients.map(convertRecipeIngredientToParsedProduct);
};