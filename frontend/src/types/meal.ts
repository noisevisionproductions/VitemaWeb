import {NutritionalValues} from "./recipe";
import {ParsedProduct} from "./product";

export enum MealType {
    BREAKFAST = 'BREAKFAST',
    SECOND_BREAKFAST = 'SECOND_BREAKFAST',
    LUNCH = 'LUNCH',
    SNACK = 'SNACK',
    DINNER = 'DINNER'
}

export interface DayMeal {
    recipeId: string;
    mealType: MealType;
    time: string;
}

export interface ParsedMeal {
    name: string;
    instructions: string;
    ingredients: ParsedProduct[];
    nutritionalValues?: NutritionalValues;
    mealType: MealType;
    time: string;
    photos?: string[];
    /** @deprecated Prefer originalRecipeId for embedded meal structure */
    recipeId?: string;
    /** Reference to source recipe when meal was "exploded" from a recipe; meal holds its own copy of data. */
    originalRecipeId?: string;
}
