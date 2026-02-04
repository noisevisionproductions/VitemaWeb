// Legacy ParsedProduct (from Excel parser - still used for backward compatibility)
export interface ParsedProduct {
    name: string;
    quantity: number;
    unit: string;
    original: string;
    hasCustomUnit?: boolean;
    categoryId?: string;
    id?: string;
}

// New Product interface (matches backend DTO from Firestore)
export interface Product {
    id: string;
    name: string;
    defaultUnit: string;
    type: 'GLOBAL' | 'CUSTOM';
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    categoryId?: string;
}

/** Product from PostgreSQL product database (recipe ingredients). */
export interface ProductDb {
    id: number | string;
    name: string;
    category: string | null;
    unit: string | null;
    kcal: number;
    protein: number;
    fat: number;
    carbs: number;
    isVerified?: boolean;
}

export interface ProductUnit {
    value: string;
    label: string;
    type: 'weight' | 'volume' | 'piece' | 'kitchen' | 'custom';
    baseUnit?: string;
    conversionFactor?: number;
}