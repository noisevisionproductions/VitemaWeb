import api from "../../config/axios";
import {Product} from "../../types/product";

export interface ProductSearchParams {
    query: string;
    trainerId?: string;
    limit?: number;
}

export interface CreateProductRequest {
    name: string;
    defaultUnit: string;
    nutritionalValues: {
        calories: number;
        protein: number;
        fat: number;
        carbs: number;
    };
    categoryId?: string;
}

export class ProductService {
    private static readonly BASE_URL = '/products';

    /**
     * Search products from the local Firestore database
     * @param params - Search parameters including query and optional trainerId
     * @returns List of products matching the search criteria
     */
    static async searchProducts(params: ProductSearchParams): Promise<Product[]> {
        try {
            const response = await api.get(`${this.BASE_URL}/search`, {
                params: {
                    query: params.query,
                    trainerId: params.trainerId,
                    limit: params.limit || 20
                }
            });
            return response.data;
        } catch (error) {
            console.error('Error searching products:', error);
            throw error;
        }
    }

    /**
     * Get a single product by ID
     * @param id - Product ID
     */
    static async getProductById(id: string): Promise<Product> {
        const response = await api.get(`${this.BASE_URL}/${id}`);
        return response.data;
    }

    /**
     * Create a custom product (for trainers)
     * @param product - Product data
     * @param trainerId - Trainer ID (will be set as author)
     */
    static async createCustomProduct(
        product: CreateProductRequest,
        trainerId: string
    ): Promise<Product> {
        const response = await api.post(`${this.BASE_URL}`, product, {
            params: {trainerId}
        });
        return response.data;
    }

    /**
     * Delete a custom product
     * @param id - Product ID
     * @param trainerId - Trainer ID (must be the author)
     */
    static async deleteProduct(id: string, trainerId: string): Promise<void> {
        await api.delete(`${this.BASE_URL}/${id}`, {
            params: {trainerId}
        });
    }

    /**
     * Seed initial products (admin/dev only)
     * Note: Products are automatically seeded on first application startup
     */
    static async seedBasicProducts(): Promise<string> {
        const response = await api.post(`${this.BASE_URL}/seed/basic`);
        return response.data;
    }
}
