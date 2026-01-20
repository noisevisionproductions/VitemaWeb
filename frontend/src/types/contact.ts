import {Timestamp} from "firebase/firestore";

export interface ContactMessage {
    id: string;
    name: string;
    email: string;
    phone?: string;
    message: string;
    createdAt: Timestamp;
    status: ContactMessageStatus;
}

export interface ContactFormData {
    name: string;
    email: string;
    phone?: string;
    message: string;
}

export type ContactMessageStatus = 'NEW' | 'PROCESSING' | 'COMPLETED';