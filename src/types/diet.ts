import {Timestamp} from 'firebase/firestore';

export interface Recipe {
    id: string;
    name: string;
    instructions: string;
    createdAt: Timestamp;
    photos: string[];
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    parentRecipeId: string | null;
}

export interface RecipeReference {
    recipeId: string;
    dietId: string;
    userId: string;
    mealType: MealType;
    addedAt: Timestamp;
}

export interface Diet {
    id: string;
    userId: string;
    createdAt: Timestamp;
    updatedAt: Timestamp;
    days: Day[];
    metadata: {
        totalDays: number;
        fileName: string;
        fileUrl: string;
    }
}

export interface DietInfo {
    hasDiet: boolean;
    startDate: Timestamp | null;
    endDate: Timestamp | null;
}

export interface DietTemplate {
    mealsPerDay: number;
    startDate: Timestamp;
    duration: number;
    mealTimes: {
        [key: string]: string;
    };
    mealTypes: MealType[];
}

export interface Day {
    date: Timestamp;
    meals: DayMeal[];
}

export interface DayMeal {
    recipeId: string;
    mealType: MealType;
    time: string;
}

export interface ParsedMeal {
    name: string;
    instructions: string;
    ingredients: string[];
    nutritionalValues?: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    mealType: MealType;
    time: string;
}

export interface ParsedDay {
    date: Timestamp;
    meals: ParsedMeal[];
}

export interface ParsedDietData {
    days: ParsedDay[];
    shoppingList: string[];
}

export interface ShoppingList {
    id: string;
    dietId: string;
    userId: string;
    items: ShoppingListItem[];
    createdAt: Timestamp;
    startDate: string;
    endDate: string;
}

export interface ShoppingListItem {
    name: string;
    recipes: {
        recipeId: string;
        recipeName: string;
        dayIndex: number;
    }[];
}

export enum MealType {
    BREAKFAST = 'BREAKFAST',
    SECOND_BREAKFAST = 'SECOND_BREAKFAST',
    LUNCH = 'LUNCH',
    SNACK = 'SNACK',
    DINNER = 'DINNER'
}