export interface ParsedProduct {
    name: string;
    quantity: number;
    unit: string;
    original: string;
    hasCustomUnit: boolean;
    similarity?: number;
}

export interface ParsingResult {
    success: boolean;
    product?: ParsedProduct;
    error?: string;
}

export interface ProductUnit {
    value: string;
    label: string;
    type: 'weight' | 'volume' | 'piece' | 'kitchen' | 'custom';
    baseUnit?: string;
    conversionFactor?: number;
}