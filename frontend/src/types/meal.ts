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
    recipeId?: string;
}
