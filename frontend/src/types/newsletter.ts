import {Timestamp} from "firebase/firestore";

export interface NewsletterFormData {
    email: string;
    role: 'dietetyk' | 'firma';
}

export interface NewsletterSubscriber {
    id: string;
    email: string;
    role: 'DIETITIAN' | 'COMPANY';
    createdAt: Timestamp | any;
    verified: boolean;
    verifiedAt: Timestamp | any | null;
    active: boolean;
    lastEmailSent?: Timestamp | any | null;
    metadata?: {
        surveyCompleted?: string;
        surveyAnswers?: string;
        [key: string]: string | undefined;
    };}

export interface NewsletterStatsData {
    total: number;
    verified: number;
    active: number;
    activeVerified: number;
    roleDistribution: Record<string, number>;
}

export interface BulkEmailRequest {
    subject: string;
    content: string;
}