import {Timestamp} from "firebase/firestore";

export interface ProductCategorization {
    id: string;
    productName: string;
    categoryId: string;
    usageCount: number;
    lastUsed: Timestamp;
    variations: string[];
    createdAt: Timestamp;
    updatedAt: Timestamp;
}

export interface UncategorizedProduct {
    id: string;
    name: string;
    dietId: string;
    suggestedCategory?: string;
    createdAt: Timestamp;
}

export interface Category {
    id: string;
    name: string;
    icon?: string;
    color?: string;
    order: number;
}