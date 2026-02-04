import {Timestamp} from 'firebase/firestore';
import {DayMeal, ParsedMeal} from './meal';
import {MealType} from './meal';
import type {NutritionalValues} from './recipe';

/** Single item from GET /api/diets/manager/history (trainer's past diets). */
export interface DietHistorySummary {
    id: string;
    name: string;
    clientName: string;
    date: string;
}

/** Ingredient as returned in DietDraftDto from backend. */
export interface DietIngredientDto {
    name: string;
    quantity: number;
    unit: string;
    productId?: string | null;
    categoryId?: string | null;
}

/** Meal as returned in DietDraftDto from backend. */
export interface DietMealDto {
    originalRecipeId?: string | null;
    name: string;
    mealType: string;
    time: string;
    instructions?: string | null;
    ingredients: DietIngredientDto[];
    nutritionalValues?: NutritionalValues | null;
}

/** Day as returned in DietDraftDto from backend (date is ISO string). */
export interface DietDayDto {
    date: string;
    meals: DietMealDto[];
}

/** Full draft returned by GET /api/diets/manager/draft/{dietId}. Saving creates a NEW diet. */
export interface DietDraft {
    dietId: string | null;
    userId?: string | null;
    name: string;
    days: DietDayDto[];
}

export interface Diet {
    id: string;
    userId: string;
    createdAt: Timestamp;
    updatedAt: Timestamp;
    days: Day[];
    metadata: DietMetadata;
}

export interface ManualDietData {
    userId: string;
    mealsPerDay: number;
    startDate: string;
    duration: number;
    mealTimes: Record<string, string>;
    mealTypes: MealType[];
    days: DayData[];
}

export interface DayData {
    date: Timestamp;
    meals: ParsedMeal[];
}

export interface DietMetadata {
    totalDays: number;
    fileName: string;
    fileUrl: string;
}

export interface DietExcelTemplate {
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

export interface ParsedDay {
    date: Timestamp;
    meals: ParsedMeal[];
}

export interface CalorieAnalysis {
    averageCalories: number;
    dailyCalories: number[];
    hasDailyVariation: boolean;
    isWithinMargin?: boolean;
}

export interface ParsedDietData {
    days: ParsedDay[];
    categorizedProducts: Record<string, string[]>;
    shoppingList: string[];
    mealTimes: Record<string, string>;
    mealsPerDay: number;
    startDate: Timestamp;
    duration: number;
    mealTypes: MealType[];
    calorieAnalysis?: CalorieAnalysis;
}