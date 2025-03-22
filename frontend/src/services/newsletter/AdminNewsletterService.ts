import { BulkEmailRequest, NewsletterStatsData, NewsletterSubscriber } from "../../types/newsletter";
import api from "../../config/axios";

interface ApiResponse {
    message: string;
}

export class AdminNewsletterService {
    /**
     * Pobiera wszystkich subskrybentów
     */
    static async getAllSubscribers(): Promise<NewsletterSubscriber[]> {
        const response = await api.get('/admin/newsletter/subscribers');
        return response.data;
    }

    /**
     * Aktywuje subskrybenta
     */
    static async activateSubscriber(id: string): Promise<void> {
        await api.post(`/admin/newsletter/subscribers/${id}/activate`);
    }

    /**
     * Dezaktywuje subskrybenta
     */
    static async deactivateSubscriber(id: string): Promise<void> {
        await api.post(`/admin/newsletter/subscribers/${id}/deactivate`);
    }

    /**
     * Ręcznie weryfikuje subskrybenta
     */
    static async verifySubscriberManually(id: string): Promise<void> {
        await api.post(`/admin/newsletter/subscribers/${id}/verify`);
    }

    /**
     * Usuwa subskrybenta
     */
    static async deleteSubscriber(id: string): Promise<void> {
        await api.delete(`/admin/newsletter/subscribers/${id}`);
    }

    /**
     * Wysyła masowy email do wszystkich aktywnych subskrybentów
     */
    static async sendBulkEmail(data: BulkEmailRequest): Promise<void> {
        await api.post('/admin/newsletter/send-bulk-email', data);
    }

    /**
     * Pobiera statystyki newslettera
     */
    static async getNewsletterStats(): Promise<NewsletterStatsData> {
        const response = await api.get('/admin/newsletter/stats');
        return response.data;
    }

    /**
     * Pobiera dane ankiet
     */
    static async getSubscriberSurveyData(): Promise<any> {
        const response = await api.get('/admin/newsletter/survey-data');
        return response.data;
    }

    /**
     * Aktualizuje metadane subskrybenta (panel admina)
     */
    static async updateSubscriberMetadata(subscriberId: string, metadata: Record<string, string>): Promise<ApiResponse> {
        const response = await api.post(`/admin/newsletter/subscribers/${subscriberId}/metadata`, {metadata});
        return response.data;
    }
}