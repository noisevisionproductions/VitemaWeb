import { Timestamp } from 'firebase/firestore';
import { MealType } from './meal';

export interface ShoppingListV1 {
    id: string;
    dietId: string;
    userId: string;
    items: ShoppingListItem[];
    createdAt: Timestamp;
    startDate: Timestamp;
    endDate: Timestamp;
    version: 1;
}

export interface ShoppingListItem {
    name: string;
    recipes: ShoppingListRecipeReference[];
    contexts: ShoppingListProductContext[];
}

export interface ShoppingListRecipeReference {
    recipeId: string;
    recipeName: string;
    dayIndex: number;
    mealType: MealType;
    mealTime: string;
}

export interface ShoppingListProductContext {
    productId: string;
    name: string;
    recipeId: string;
    dayIndex: number;
    mealType: MealType;
}

export interface ShoppingListV2 {
    id: string;
    dietId: string;
    userId: string;
    items: string[];
    createdAt: Timestamp;
    startDate: Timestamp;
    endDate: Timestamp;
    version: 2;
}

export interface ShoppingListV3 {
    id: string;
    dietId: string;
    userId: string;
    items: Record<string, CategorizedShoppingListItem[]>;
    createdAt: Timestamp;
    startDate: Timestamp;
    endDate: Timestamp;
    version: 3;
}

export interface CategorizedShoppingListItem {
    name: string;
    quantity: number;
    unit: string;
    original: string;
    recipes?: ShoppingListRecipeReference[];
}

export type ShoppingList = ShoppingListV1 | ShoppingListV2 | ShoppingListV3;