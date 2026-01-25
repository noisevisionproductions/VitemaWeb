export interface NewsletterFormData {
    email: string;
    role: 'dietetyk' | 'firma';
}

export interface NewsletterSubscriber {
    id: string;
    email: string;
    role: 'DIETITIAN' | 'COMPANY';
    createdAt: string;
    verified: boolean;
    verifiedAt: string | null;
    active: boolean;
    lastEmailSent?: string | null;
    metadata?: {
        surveyCompleted?: string;
        surveyAnswers?: string;
        [key: string]: string | undefined;
    };
}

export interface EmailStats {
    total: number;
    delivered: number;
    opens: number;
    clicks: number;
    bounces: number;
    unsubscribes: number;
    spamReports: number;
    uniqueOpens: number;
    uniqueClicks: number;
    dailyStats: {
        date: string;
        delivered: number;
        opens: number;
        clicks: number;
    }[];
}

export interface NewsletterStatsData {
    total: number;
    verified: number;
    active: number;
    activeVerified: number;
    roleDistribution: Record<string, number>;
    emailStats?: EmailStats;
}