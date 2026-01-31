import api from "../config/axios";
import { User } from "../types/user";

interface UserUpdateData {
    nickname: string;
    gender: string | null;
    birthDate: number | null;
    note: string;
}

export class UserService {
    private static readonly BASE_URL = '/users';

    static async getAllUsers(): Promise<User[]> {
        const response = await api.get(this.BASE_URL);
        return response.data;
    }

    static async getMyClients(): Promise<User[]> {
        const response = await api.get(`${this.BASE_URL}/my-clients`);
        return response.data;
    }

    static async getUserById(id: string): Promise<User> {
        const response = await api.get(`${this.BASE_URL}/${id}`);
        return response.data;
    }

    static async updateUser(id: string, data: UserUpdateData): Promise<User> {
        const response = await api.put(`${this.BASE_URL}/${id}`, data);
        return response.data;
    }

    static async updateUserNote(id: string, note: string): Promise<User> {
        const response = await api.patch(`${this.BASE_URL}/${id}/note`, { note });
        return response.data;
    }
}