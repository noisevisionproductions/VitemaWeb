import {Timestamp} from 'firebase/firestore';

export interface Recipe {
    id: string;
    name: string;
    instructions: string;
    createdAt: Timestamp;
    photos: string[];
    nutritionalValues?: NutritionalValues;
    parentRecipeId: string | null;
}

export interface NutritionalValues {
    calories?: number;
    protein?: number;
    fat?: number;
    carbs?: number;
}