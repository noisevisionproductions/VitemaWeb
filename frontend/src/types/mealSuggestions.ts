import {NutritionalValues} from "./recipe";

export interface MealSuggestion {
    id: string;
    name: string;
    instructions?: string;
    nutritionalValues?: any;
    photos?: string[];
    ingredients?: any[];
    similarity: number;
    isExact: boolean;
    source: 'RECIPE' | 'TEMPLATE';
    usageCount: number;
    lastUsed?: string;
}

export interface MealIngredient {
    id?: string;
    name: string;
    quantity: number;
    unit: string;
    original: string;
    categoryId?: string;
    hasCustomUnit: boolean;
}

export interface MealTemplate {
    id: string;
    name: string;
    instructions?: string;
    nutritionalValues?: NutritionalValues;
    photos?: string[];
    ingredients?: MealIngredient[];
    mealType?: string;
    category?: string;
    isPublic: boolean;
    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;
    usageCount: number;
}

export interface SaveMealTemplateRequest {
    name: string;
    instructions?: string;
    nutritionalValues?: NutritionalValues;
    photos?: string[];
    ingredients?: MealIngredient[];
    mealType?: string;
    category?: string;
    isPublic: boolean;
    shouldSave: boolean;
}

export interface MealSavePreview {
    willCreateNew: boolean;
    foundSimilar: boolean;
    similarMeals: MealSuggestion[];
    recommendedAction: 'CREATE_NEW' | 'USE_EXISTING' | 'UPDATE_EXISTING';
    message: string;
}

export interface TemplateChange {
    field: string;
    oldValue: any;
    newValue: any;
    timestamp: Date;
}

export interface TemplateUpdateSummary {
    templateId: string;
    templateName: string;
    source: 'RECIPE' | 'TEMPLATE';
    changes: TemplateChange[];
    hasSignificantChanges: boolean;
}