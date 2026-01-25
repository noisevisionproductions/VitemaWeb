import {AdminEmailService} from '../AdminEmailService';
import {EmailTemplate, EmailTemplateType} from "../../../types/email";
import {toast} from "../../../utils/toast";

/**
 * Usługa zarządzająca szablonami emaili
 */
export const EmailTemplateService = {

    /**
     * Pobiera dostępne szablony z API
     */
    getTemplates: async (): Promise<EmailTemplate[]> => {
        try {
            const response = await AdminEmailService.getSystemTemplates();

            if (response.data && response.data.templates) {
                return response.data.templates.map((template: any) => ({
                    id: template.id,
                    name: template.name,
                    description: template.description
                }));
            }

            console.error('Nieprawidłowa struktura odpowiedzi z API');
            toast.error('Nie udało się pobrać szablonów emaili.');
            return [];
        } catch (error) {
            console.error('Błąd podczas pobierania szablonów:', error);
            toast.error('Nie udało się pobrać szablonów emaili.');
            return [];
        }
    },

    /**
     * Generuje podgląd emaila z użyciem wybranego szablonu
     */
    previewTemplate: async (content: string, templateType: EmailTemplateType): Promise<string> => {
        const response = await AdminEmailService.previewEmail({
            subject: 'Podgląd',
            content,
            useTemplate: true,
            templateType
        }).catch(error => {
            console.error('Błąd podczas generowania podglądu:', error);
            return Promise.reject('Nie udało się wygenerować podglądu email.');
        });

        if (!response?.data?.preview) {
            console.error('Brak podglądu w odpowiedzi API');
            return Promise.reject('Brak podglądu w odpowiedzi serwera');
        }

        return response.data.preview;
    },

    /**
     * Pobiera informacje o pojedynczym szablonie po jego ID
     */
    getTemplateById: async (templateId: EmailTemplateType): Promise<EmailTemplate | undefined> => {
        try {
            const templates = await EmailTemplateService.getTemplates();
            return templates.find(template => template.id === templateId);
        } catch (error) {
            console.error(`Błąd podczas pobierania szablonu o ID ${templateId}:`, error);
            return undefined;
        }
    }
};