import api from "../../../config/axios";
import {ParsedProduct, Product} from "../../../types/product";
import {ProductService} from "../../product/ProductService";
import type {UnifiedSearchResult} from "../../../types";
import type {DietHistorySummary, DietDraft} from "../../../types";

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

/** Base path for diet manager API (upload, save, structure, search). */
const MANAGER_BASE = '/diets/manager';

export class DietCreatorService {
    private static readonly BASE_URL = '/diets/manual';

    /**
     * Unified search: returns both recipes and products in one response.
     */
    static async searchUnified(
        query: string,
        trainerId?: string
    ): Promise<UnifiedSearchResult[]> {
        const effectiveQuery = query && query.trim().length > 0 ? query.trim() : ' ';

        const response = await api.get<UnifiedSearchResult[]>(`${MANAGER_BASE}/search`, {
            params: {
                query: effectiveQuery,
                trainerId: trainerId || undefined
            },
        });
        return response.data ?? [];
    }

    /**
     * Fetch diet history for the given trainer (list of past diets).
     * GET /api/diets/manager/history?trainerId=...
     */
    static async getTrainerDietHistory(trainerId: string): Promise<DietHistorySummary[]> {
        const response = await api.get<DietHistorySummary[]>(`${MANAGER_BASE}/history`, {
            params: {trainerId},
        });
        return response.data ?? [];
    }

    /**
     * Load a diet as draft by id (to copy structure into a new diet).
     * GET /api/diets/manager/draft/{dietId}
     * Returns full diet structure; saving will create a NEW diet (dietId is not used on save).
     */
    static async loadDietDraft(dietId: string): Promise<DietDraft> {
        const response = await api.get<DietDraft>(`${MANAGER_BASE}/draft/${dietId}`);
        return response.data;
    }

    /**
     * Save manually created diet
     */
    static async saveManualDiet(request: ManualDietRequest): Promise<ManualDietResponse> {
        const response = await api.post(`${this.BASE_URL}/save`, request);
        return response.data;
    }

    /**
     * Search ingredients using new Product Management System
     * @param query - Search query
     * @param trainerId - Optional trainer ID to include custom products
     * @param limit - Maximum number of results
     */
    static async searchIngredients(
        query: string,
        trainerId?: string,
        limit: number = 10
    ): Promise<Product[]> {
        return ProductService.searchProducts({
            query,
            trainerId,
            limit
        });
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use searchIngredients instead which returns Product[]
     */
    static async searchIngredientsLegacy(query: string, limit: number = 10): Promise<ParsedProduct[]> {
        const response = await api.get(`${this.BASE_URL}/ingredients/search`, {
            params: {query, limit}
        });
        return response.data;
    }

    /**
     * Create ingredient (legacy)
     * @deprecated Use ProductService.createCustomProduct instead
     */
    static async createIngredient(ingredient: ParsedProduct): Promise<ParsedProduct> {
        const response = await api.post(`${this.BASE_URL}/ingredients`, ingredient);
        return response.data;
    }

    /**
     * Validate diet before saving
     */
    static async validateDiet(request: ManualDietRequest): Promise<ValidationResult> {
        const response = await api.post(`${this.BASE_URL}/validate`, request);
        return response.data;
    }

    /**
     * Convert Product to ParsedProduct for backward compatibility
     */
    static convertProductToParsedProduct(product: Product): ParsedProduct {
        return {
            id: product.id,
            name: product.name,
            quantity: 1.0,
            unit: product.defaultUnit,
            original: product.name,
            hasCustomUnit: false,
            categoryId: product.categoryId
        };
    }

    /**
     * Batch convert products to parsed products
     */
    static convertProductsToParsedProducts(products: Product[]): ParsedProduct[] {
        return products.map(this.convertProductToParsedProduct);
    }
}

// Export for backward compatibility
export const ManualDietService = DietCreatorService;
