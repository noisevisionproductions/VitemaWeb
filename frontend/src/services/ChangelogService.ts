import api from "../config/axios";
import { ChangelogEntry } from "../types/changeLog";

interface CreateChangelogEntryRequest {
    title: string;
    description: string;
    type: 'feature' | 'fix' | 'improvement';
}

export class ChangelogService {
    private static readonly BASE_URL = '/changelog';

    static async getAllEntries(): Promise<ChangelogEntry[]> {
        const response = await api.get(this.BASE_URL);
        return response.data;
    }

    static async createEntry(data: CreateChangelogEntryRequest): Promise<ChangelogEntry> {
        const response = await api.post(this.BASE_URL, data);
        return response.data;
    }

    static async markAsRead(): Promise<void> {
        await api.post(`${this.BASE_URL}/mark-read`);
    }

    static async hasUnreadEntries(): Promise<boolean> {
        const response = await api.get(`${this.BASE_URL}/has-unread`);
        return response.data;
    }
}