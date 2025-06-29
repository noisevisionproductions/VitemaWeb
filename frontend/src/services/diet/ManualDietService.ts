import api from "../../config/axios";
import {ParsedProduct} from "../../types/product";

export interface ManualDietRequest {
    userId: string;
    days: any[];
    mealsPerDay: number;
    startDate: string;
    duration: number;
    mealTimes: Record<string, string>;
    mealTypes: string[];
}

export interface ManualDietResponse {
    dietId: string;
    message: string;
}

export interface ValidationResult {
    isValid: boolean;
    errors: string[];
    warnings: string[];
}

export class ManualDietService {
    private static readonly BASE_URL = '/diets/manual';

    static async saveManualDiet(request: ManualDietRequest): Promise<ManualDietResponse> {
        const response = await api.post(`${this.BASE_URL}/save`, request);
        return response.data;
    }

    static async searchIngredients(query: string, limit: number = 10): Promise<ParsedProduct[]> {
        const response = await api.get(`${this.BASE_URL}/ingredients/search`, {
            params: {query, limit}
        });
        return response.data;
    }

    static async createIngredient(ingredient: ParsedProduct): Promise<ParsedProduct> {
        const response = await api.post(`${this.BASE_URL}/ingredients`, ingredient);
        return response.data;
    }

    static async validateDiet(request: ManualDietRequest): Promise<ValidationResult> {
        const response = await api.post(`${this.BASE_URL}/validate`, request);
        return response.data;
    }
}