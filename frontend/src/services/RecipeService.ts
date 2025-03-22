import api from "../config/axios";
import {Recipe, NutritionalValues} from "../types";

interface RecipeUpdateData {
    name?: string;
    instructions?: string;
    nutritionalValues?: NutritionalValues;
    photos?: string[];
    parentRecipeId?: string | null;
}

interface RecipesPageResponse {
    content: Recipe[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export class RecipeService {
    private static readonly BASE_URL = '/recipes';

    static async getRecipeById(id: string): Promise<Recipe> {
        const response = await api.get(`${this.BASE_URL}/${id}`);
        return response.data;
    }

    static async getRecipesByIds(ids: string[]): Promise<Recipe[]> {
        if (!ids || ids.length === 0) return [];

        const response = await api.get(`${this.BASE_URL}/batch`, {
            params: {ids: ids.join(',')}
        });
        return response.data;
    }

    static async getRecipesPage(
        page = 0,
        size = 50,
        sortBy = 'createdAt',
        sortDir = 'desc'
    ): Promise<RecipesPageResponse> {
        const response = await api.get<RecipesPageResponse>(`${this.BASE_URL}`, {
            params: {
                page,
                size,
                sortBy,
                sortDir
            }
        });
        return response.data;
    }

    static async getAllRecipes(
        page = 0,
        size = 1000,
        sortBy = 'createdAt',
        sortDir = 'desc'
    ): Promise<Recipe[]> {
        const response = await api.get<RecipesPageResponse>(`${this.BASE_URL}`, {
            params: {
                page,
                size,
                sortBy,
                sortDir
            }
        });
        return response.data.content || [];
    }

    static async updateRecipe(id: string, data: RecipeUpdateData): Promise<Recipe> {
        const response = await api.put(`${this.BASE_URL}/${id}`, data);
        return response.data;
    }

    static async uploadRecipeImage(recipeId: string, file: File): Promise<string> {
        const formData = new FormData();
        formData.append('image', file);

        const response = await api.post(`${this.BASE_URL}/${recipeId}/image`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        });

        return response.data.imageUrl;
    }

    static async deleteRecipeImage(recipeId: string, imageUrl: string): Promise<void> {
        await api.delete(`${this.BASE_URL}/${recipeId}/image`, {
            data: {imageUrl}
        });
    }

    static async searchRecipes(query: string): Promise<Recipe[]> {
        const response = await api.get(`${this.BASE_URL}/search`, {
            params: {query}
        });
        return response.data;
    }
}