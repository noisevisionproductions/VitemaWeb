import {Timestamp} from 'firebase/firestore';
import {DayMeal, ParsedMeal} from './meal';
import {MealType} from './meal';

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