export interface ParsedProduct {
    name: string;
    quantity: number;
    unit: string;
    original: string;
    hasCustomUnit?: boolean;
    categoryId?: string;
    id?: string;
}

export interface ProductUnit {
    value: string;
    label: string;
    type: 'weight' | 'volume' | 'piece' | 'kitchen' | 'custom';
    baseUnit?: string;
    conversionFactor?: number;
}