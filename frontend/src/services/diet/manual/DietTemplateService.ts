import api from "../../../config/axios";
import {
    CreateDietTemplateRequest,
    DietTemplate,
    DietTemplateCategory,
    DietTemplateStats
} from "../../../types/DietTemplate";

export class DietTemplateService {
    private static readonly BASE_URL = '/diet-templates';

    static async getAllTemplates(): Promise<DietTemplate[]> {
        const response = await api.get(this.BASE_URL);
        return response.data;
    }

    static async getTemplate(id: string): Promise<DietTemplate> {
        const response = await api.get(`${this.BASE_URL}/${id}`);
        return response.data;
    }

    static async getTemplatesByCategory(category: DietTemplateCategory): Promise<DietTemplate[]> {
        const response = await api.get(`${this.BASE_URL}/category/${category}`);
        return response.data;
    }

    static async getPopularTemplates(limit: number = 10): Promise<DietTemplate[]> {
        const response = await api.get(`${this.BASE_URL}/popular`, {
            params: {limit}
        });
        return response.data;
    }

    static async searchTemplates(query: string, limit: number = 20): Promise<DietTemplate[]> {
        const response = await api.get(`${this.BASE_URL}/search`, {
            params: {query, limit}
        });
        return response.data;
    }

    static async createTemplate(request: CreateDietTemplateRequest): Promise<DietTemplate> {
        const response = await api.post(this.BASE_URL, request);
        return response.data;
    }

    static async createTemplateFromDiet(request: CreateDietTemplateRequest): Promise<DietTemplate> {
        const response = await api.post(`${this.BASE_URL}/from-diet`, request);
        return response.data;
    }

    static async updateTemplate(id: string, request: CreateDietTemplateRequest): Promise<DietTemplate> {
        const response = await api.put(`${this.BASE_URL}/${id}`, request);
        return response.data;
    }

    static async deleteTemplate(id: string): Promise<void> {
        await api.delete(`${this.BASE_URL}/${id}`);
    }

    static async incrementUsage(id: string): Promise<void> {
        await api.post(`${this.BASE_URL}/${id}/use`);
    }

    static async getTemplateStats(): Promise<DietTemplateStats> {
        const response = await api.get(`${this.BASE_URL}/stats`);
        return response.data;
    }

    // Utility methods
    static getCategoryLabel(category: DietTemplateCategory): string {
        const labels: Record<DietTemplateCategory, string> = {
            [DietTemplateCategory.WEIGHT_LOSS]: 'Odchudzanie',
            [DietTemplateCategory.WEIGHT_GAIN]: 'Nabieranie masy',
            [DietTemplateCategory.MAINTENANCE]: 'Utrzymanie wagi',
            [DietTemplateCategory.SPORT]: 'Sportowa',
            [DietTemplateCategory.MEDICAL]: 'Medyczna',
            [DietTemplateCategory.VEGETARIAN]: 'Wegetariańska',
            [DietTemplateCategory.VEGAN]: 'Wegańska',
            [DietTemplateCategory.CUSTOM]: 'Niestandardowa'
        };
        return labels[category];
    }

    static getAllCategories(): { value: DietTemplateCategory; label: string }[] {
        return Object.values(DietTemplateCategory).map(category => ({
            value: category,
            label: this.getCategoryLabel(category)
        }));
    }
}