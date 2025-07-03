import {SendgridEmailParams, SingleEmailRequest, TargetedEmailParams} from "../../types/sendGrid";
import api from "../../config/axios";

/**
 * Usługa obsługująca wysyłkę emaili przez SendGrid
 */
export const SendGridService = {

    /*
    * Wysyła pojedynczy email do konkretnego odbiorcy
    * */
    sendSingleEmail: async (params: SingleEmailRequest) => {
        return api.post('/admin/email/single', params);
    },

    /**
     * Wysyła masowy email do wszystkich subskrybentów newslettera
     */
    sendBulkEmail: async (params: SendgridEmailParams) => {
        return api.post('/admin/email/bulk', params);
    },

    /**
     * Wysyła masowy email do wybranych odbiorców (subskrybenci, zewnętrzni, mieszani)
     */
    sendTargetedBulkEmail: async (params: TargetedEmailParams) => {
        return api.post('/admin/email/bulk-targeted', params);
    },

    /**
     * Wysyła pojedynczy email do konkretnego odbiorcy
     */
    sendEmail: async (email: string, params: SendgridEmailParams) => {
        return api.post(`/admin/email/send/${email}`, params);
    },

    /**
     * Pobiera dostępne systemowe szablony emaili
     */
    getEmailTemplates: async () => {
        return api.get('/admin/email/templates');
    },

    /**
     * Generuje podgląd emaila z wybranym szablonem systemowym
     */
    previewTemplate: async (params: SendgridEmailParams) => {
        return api.post('/admin/email/preview', params);
    }
};