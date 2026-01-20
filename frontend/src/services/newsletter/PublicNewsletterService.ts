import { NewsletterFormData } from "../../types/newsletter";
import api from "../../config/axios";

interface ApiResponse {
    message: string;
}

export interface VerifyEmailResponse extends ApiResponse {
    subscriberId?: string;
    subscriberRole?: string;
    email?: string;
    verifiedAt?: any;
}

export class PublicNewsletterService {
    /**
     * Zapisuje użytkownika do newslettera
     */
    static async subscribe(data: NewsletterFormData): Promise<ApiResponse> {
        const response = await api.post('/newsletter/subscribe', data);
        return response.data;
    }

    /**
     * Weryfikuje adres email na podstawie tokenu
     */
    static async verifyEmail(token: string): Promise<VerifyEmailResponse> {
        const response = await api.get(`/newsletter/verify?token=${token}`);
        return response.data;
    }

    /**
     * Wypisuje użytkownika z newslettera
     */
    static async unsubscribe(email: string): Promise<ApiResponse> {
        const response = await api.post(`/newsletter/unsubscribe?email=${encodeURIComponent(email)}`);
        return response.data;
    }

    /**
     * Zapisuje metadane ankiety subskrybenta
     */
    static async saveSubscriberMetadata(subscriberId: string, metadata: Record<string, string>): Promise<ApiResponse> {
        const response = await api.post(`/newsletter/subscribers/${subscriberId}/metadata`, {metadata});
        return response.data;
    }
}