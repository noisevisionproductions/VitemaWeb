import {ContactMessage, ContactMessageStatus} from "../../types/contact";
import api from "../../config/axios";

export class AdminContactService {

    static async getContactMessages(): Promise<ContactMessage[]> {
        const response = await api.get('/admin/contact/messages');
        return response.data;
    }

    static async getContactMessage(id: string): Promise<ContactMessage> {
        const response = await api.get(`/admin/contact/messages/${id}`);
        return response.data;
    }

    static async updateMessageStatus(id: string, status: ContactMessageStatus): Promise<void> {
        return api.put(`/admin/contact/messages/${id}/status`, {status});
    }
}