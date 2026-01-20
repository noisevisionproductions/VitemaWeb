import {
    MealIngredient,
    MealSavePreview,
    MealSuggestion,
    MealTemplate,
    SaveMealTemplateRequest
} from "../../../types/mealSuggestions";
import api from "../../../config/axios";
import {NutritionalValues} from "../../../types";

export class MealSuggestionService {
    private static readonly BASE_URL = '/diets/manual';

    static async searchMeals(query: string, limit: number = 10): Promise<MealSuggestion[]> {
        try {
            const response = await api.get(`${this.BASE_URL}/meals/search`, {
                params: {query, limit}
            });
            return response.data;
        } catch (error) {
            console.error('Błąd podczas wyszukiwania posiłków:', error);
            return [];
        }
    }

    static async getMealTemplate(id: string): Promise<MealTemplate> {
        const response = await api.get(`${this.BASE_URL}/meals/${id}`);
        return response.data;
    }

    static async previewMealSave(request: {
        name: string;
        instructions?: string;
        nutritionalValues?: NutritionalValues;
        photos?: string[];
        ingredients?: MealIngredient[];
    }): Promise<MealSavePreview> {
        const response = await api.post(`${this.BASE_URL}/meals/preview-save`, request);
        return response.data;
    }

    static async saveMealTemplate(request: SaveMealTemplateRequest): Promise<MealTemplate> {
        const response = await api.post(`${this.BASE_URL}/meals/save-template`, request);
        return response.data;
    }

    static async uploadMealImage(file: File, mealId?: string): Promise<string> {
        const formData = new FormData();
        formData.append('image', file);
        if (mealId) {
            formData.append('mealId', mealId);
        }

        const response = await api.post(`${this.BASE_URL}/meals/upload-image`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        });

        return response.data.imageUrl;
    }

    static async uploadBase64MealImage(base64Image: string, mealId?: string): Promise<string> {
        const response = await api.post(`${this.BASE_URL}/meals/upload-base64-image`, {
            imageData: base64Image,
            mealId
        });

        return response.data.imageUrl;
    }

    static async applyMealTemplate(templateId: string, targetMeal: any): Promise<any> {
        try {
            const template = await this.getMealTemplate(templateId);

            return {
                ...targetMeal,
                name: template.name,
                instructions: template.instructions || targetMeal.instructions,
                nutritionalValues: template.nutritionalValues || targetMeal.nutritionalValues,
                photos: template.photos || [],
                ingredients: template.ingredients || targetMeal.ingredients || []
            };
        } catch (error) {
            console.error('Błąd podczas aplikowania szablonu posiłku:', error);
            throw error;
        }
    }

    static async updateMealTemplate(templateId: string, request: SaveMealTemplateRequest): Promise<MealTemplate> {
        const response = await api.put(`${this.BASE_URL}/meals/templates/${templateId}`, request);
        return response.data;
    }

    /**
     * Inteligentne łączenie składników z różnych źródeł
     */
    static mergeMealIngredients(
        existingIngredients: MealIngredient[],
        newIngredients: MealIngredient[]
    ): MealIngredient[] {
        const merged = [...existingIngredients];

        for (const newIngredient of newIngredients) {
            const existingIndex = merged.findIndex(
                existing => existing.name.toLowerCase() === newIngredient.name.toLowerCase()
            );

            if (existingIndex >= 0) {
                // Jeśli składnik już istnieje, zaktualizuj ilość, jeśli jednostki są takie same
                const existing = merged[existingIndex];
                if (existing.unit === newIngredient.unit) {
                    existing.quantity += newIngredient.quantity;
                } else {
                    // Różne jednostki-dodaj jako nowy składnik
                    merged.push({
                        ...newIngredient,
                        id: `ingredient-${Date.now()}-${Math.random()}`
                    });
                }
            } else {
                // Nowy składnik
                merged.push({
                    ...newIngredient,
                    id: `ingredient-${Date.now()}-${Math.random()}`
                });
            }
        }

        return merged;
    }

    /**
     * Sprawdza, czy posiłek ma wystarczające dane do zapisania jako szablon
     */
    static validateMealForTemplate(meal: {
        name: string;
        instructions?: string;
        ingredients?: MealIngredient[];
    }): { isValid: boolean; errors: string[] } {
        const errors: string[] = [];

        if (!meal.name || meal.name.trim().length < 2) {
            errors.push('Nazwa posiłku musi mieć co najmniej 2 znaki');
        }

        if (!meal.instructions || meal.instructions.trim().length < 10) {
            errors.push('Instrukcje przygotowania są zbyt krótkie (minimum 10 znaków)');
        }

        if (!meal.ingredients || meal.ingredients.length === 0) {
            errors.push('Posiłek musi mieć co najmniej jeden składnik');
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }
}