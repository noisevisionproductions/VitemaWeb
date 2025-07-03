import React from "react";

export type EmailTemplateType = 'basic' | 'promotional' | 'survey' | 'announcement' | string;

export interface SingleEmailRequest {
    recipientEmail: string;
    recipientName?: string;
    externalRecipientId?: string;
    subject: string;
    content: string;
    useTemplate?: boolean;
    templateType?: EmailTemplateType;
    savedTemplateId?: string;
    updateLastContactDate?: boolean;
    categories?: string[];
}

export interface TargetedEmailParams {
    subject: string;
    content: string;
    recipientType: 'subscribers' | 'external' | 'mixed';
    subscriberFilters?: {
        role?: string;
        active?: boolean;
        verified?: boolean;
    };
    externalFilters?: {
        category?: string;
        status?: string;
        tags?: string[];
    };
    externalRecipientIds?: string[];
    useTemplate?: boolean;
    templateType?: EmailTemplateType;
    savedTemplateId?: string;
    categories?: string[];
    updateStatus?: boolean;
    newStatus?: string;
}

export interface SendgridEmailParams {
    subject: string;
    content: string;
    useTemplate?: boolean;
    templateType?: EmailTemplateType;
    recipients?: string[];
    categories?: string[];
}

export interface EmailTemplate {
    id: EmailTemplateType;
    name: string;
    description: string;
    icon?: React.ReactNode;
}

export interface SavedEmailTemplate {
    id: string;
    name: string;
    subject: string;
    content: string;
    description?: string;
    useTemplate: boolean;
    templateType: EmailTemplateType;
    createdAt: any;
    updatedAt: any;
}

export interface ExternalRecipient {
    id: string;
    email: string;
    name?: string;
    category: string;
    tags?: string[];
    status: 'new' | 'contacted' | 'responded' | 'subscribed' | 'rejected';
    lastContactDate?: any;
    notes?: string;
    createdAt: any;
}

export interface ExternalRecipientFormData {
    email: string;
    name?: string;
    category: string;
    tags?: string[];
    notes?: string;
}