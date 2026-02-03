import {Timestamp} from 'firebase/firestore';

export interface Recipe {
    id: string;
    name: string;
    instructions: string;
    createdAt: Timestamp;
    photos: string[];
    ingredients?: RecipeIngredient[];
    nutritionalValues?: NutritionalValues;
    parentRecipeId: string | null;
    authorId?: string;
    isPublic?: boolean;
}

export interface RecipeIngredient {
    id?: string;
    name: string;
    quantity: number;
    unit: string;
    original?: string;
    categoryId?: string;
    hasCustomUnit?: boolean;
}

export interface NutritionalValues {
    calories?: number;
    protein?: number;
    fat?: number;
    carbs?: number;
}