import api from "../../config/axios";
import {ParsedProduct} from "../../types/product";

export class DietCategorizationService {
    private static readonly API_URL = '/diets/categorization';

    /**
     * Pobiera dostępne kategorie
     */
    static async getCategories(): Promise<any[]> {
        try {
            const response = await api.get(`${this.API_URL}/categories`);
            return response.data || [];
        } catch (error) {
            console.error('Błąd podczas pobierania kategorii:', error);
            throw error;
        }
    }


    static async suggestCategory(product: ParsedProduct): Promise<string> {
        try {
            const payload = {
                productName: product.original || product.name,
                name: product.name,
                original: product.original
            };

            const response = await api.post(`${this.API_URL}/suggest`, payload);
            return response.data?.categoryId || 'other';
        } catch (error) {
            console.error('Błąd podczas sugerowania kategorii:', error);
            return 'other';
        }
    }

    /**
     * Sugeruje kategorie dla wielu produktów
     */
    static async bulkSuggestCategories(products: ParsedProduct[]): Promise<Record<string, string>> {
        try {
            if (!products || products.length === 0) return {};

            const response = await api.post(`${this.API_URL}/suggest/bulk`, {products});
            return response.data || {};
        } catch (error) {
            console.error('Błąd podczas masowego sugerowania kategorii:', error);
            return {};
        }
    }

    /**
     * Aktualizuje kategorie produktów
     */
    static async updateCategories(categorizedProducts: Record<string, ParsedProduct[]>): Promise<void> {
        try {
            const simplifiedProducts: Record<string, any[]> = {};

            Object.entries(categorizedProducts).forEach(([categoryId, products]) => {
                simplifiedProducts[categoryId] = products.map(product => ({
                    name: product.original || product.name,
                    original: product.original || product.name,
                    quantity: product.quantity,
                    unit: product.unit,
                    categoryId: categoryId,
                    hasCustomUnit: product.hasCustomUnit || false
                }));
            });

            const payload = {categorizedProducts: simplifiedProducts};

            await api.post(`${this.API_URL}/update`, payload);
        } catch (error) {
            console.error('Błąd podczas aktualizacji kategorii:', error);
            throw error;
        }
    }
}