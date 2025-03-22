import api from "../../config/axios";

export interface ContactFormData {
    name: string;
    email: string;
    phone?: string;
    message: string;
}

export class ContactService {

    static async sendContactForm(data: ContactFormData): Promise<void> {
        return api.post('/contact', data);
    }
}