export interface DietTemplate {
    id: string;
    name: string;
    description?: string;
    category: DietTemplateCategory;
    categoryLabel: string;
    createdBy: string;
    createdAt: string;
    updatedAt: string;
    version: number;
    duration: number;
    mealsPerDay: number;
    mealTimes: Record<string, string>;
    mealTypes: string[];
    days: DietTemplateDay[];
    targetNutrition?: DietTemplateNutrition;
    usageCount: number;
    lastUsed?: string;
    notes?: string;
    totalMeals: number;
    totalIngredients: number;
    hasPhotos: boolean;
}

export interface DietTemplateDay {
    dayNumber: number;
    dayName: string;
    notes?: string;
    meals: DietTemplateMeal[];
}

export interface DietTemplateMeal {
    name: string;
    mealType: string;
    time: string;
    instructions?: string;
    ingredients: DietTemplateIngredient[];
    nutritionalValues?: any;
    photos: string[];
    mealTemplateId?: string;
}

export interface DietTemplateIngredient {
    name: string;
    quantity: number;
    unit: string;
    original: string;
    categoryId?: string;
    hasCustomUnit: boolean;
}

export interface DietTemplateNutrition {
    targetCalories?: number;
    targetProtein?: number;
    targetFat?: number;
    targetCarbs?: number;
    calculationMethod?: string;
}

export enum DietTemplateCategory {
    WEIGHT_LOSS = 'WEIGHT_LOSS',
    WEIGHT_GAIN = 'WEIGHT_GAIN',
    MAINTENANCE = 'MAINTENANCE',
    SPORT = 'SPORT',
    MEDICAL = 'MEDICAL',
    VEGETARIAN = 'VEGETARIAN',
    VEGAN = 'VEGAN',
    CUSTOM = 'CUSTOM'
}

export interface CreateDietTemplateRequest {
    name: string;
    description?: string;
    category: string;
    duration: number;
    mealsPerDay: number;
    mealTimes: Record<string, string>;
    mealTypes: string[];
    days?: DietTemplateDay[];
    targetNutrition?: DietTemplateNutrition;
    notes?: string;
    dietData?: any;
}

export interface DietTemplateStats {
    totalTemplates: number;
    templatesByCategory: Record<string, number>;
    mostUsedTemplate?: DietTemplate;
    newestTemplate?: DietTemplate;
    totalUsageCount: number;
}