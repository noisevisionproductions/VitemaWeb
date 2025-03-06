import api from "../config/axios";
import {Diet} from "../types";
import axios from "axios";

export interface DietInfo {
    hasDiet: boolean;
    startDate: string | null;
    endDate: string | null;
}

export interface UserDietInfo {
    [userId: string]: DietInfo;
}

export class DietService {
    private static readonly BASE_URL = '/diets';

    static async getDiets(params?: {
        userId?: string,
        page?: number,
        size?: number
    }): Promise<Diet[]> {
        const response = await api.get(this.BASE_URL, {params});
        return response.data;
    }

    static async getDietById(id: string): Promise<Diet> {
        try {
            const response = await api.get(`${this.BASE_URL}/${id}`);
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                console.log(`Dieta o ID ${id} nie istnieje`);
                throw new Error(`Dieta o ID ${id} nie istnieje`);
            }
            throw error;
        }
    }

    static async getDietsInfoForUsers(userIds: string[]): Promise<UserDietInfo> {
        const response = await api.get(`${this.BASE_URL}/info`, {
            params: {userIds: userIds.join(',')}
        });
        return response.data;
    }

    static async createDiet(diet: Omit<Diet, 'id'>): Promise<Diet> {
        const response = await api.post(this.BASE_URL, diet);
        return response.data;
    }

    static async updateDiet(id: string, diet: Partial<Diet>): Promise<Diet> {
        const response = await api.put(`${this.BASE_URL}/${id}`, diet);
        return response.data;
    }

    static async deleteDiet(id: string): Promise<void> {
        try {
            const response = await api.delete(`/diets/${id}`);
            return response.data;
        } catch (error) {
            console.error('DietService: Delete request failed:', error);
            throw error;
        }
    }
}