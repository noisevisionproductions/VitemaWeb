import {SavedEmailTemplate} from "../../../types/email";
import api from "../../../config/axios";
import {toast} from "../../../utils/toast";

/**
 * Usługa zarządzająca zapisanymi szablonami emaili
 */
export const SavedTemplateService = {
    /**
     * Pobiera wszystkie zapisane szablony
     */
    getAllTemplates: async (): Promise<SavedEmailTemplate[]> => {
        try {
            const response = await api.get<SavedEmailTemplate[]>('/admin/email/saved-templates');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas pobierania zapisanych szablonów:', error);
            toast.error('Nie udało się pobrać zapisanych szablonów. Spróbuj odświeżyć stronę.');
            return [];
        }
    },

    /**
     * Pobiera szablon po ID
     */
    getTemplateById: async (id: string): Promise<SavedEmailTemplate | null> => {
        try {
            const response = await api.get<SavedEmailTemplate>(`/admin/email/saved-templates/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Błąd podczas pobierania szablonu o ID ${id}:`, error);
            toast.error('Nie udało się pobrać szablonu.');
            return null;
        }
    },

    /**
     * Zapisuje nowy szablon lub aktualizuje istniejący
     */
    saveTemplate: async (template: {
        id?: string;
        name: string;
        subject: string;
        content: string;
        description?: string;
        useTemplate?: boolean;
        templateType?: string;
    }): Promise<SavedEmailTemplate | null> => {
        try {
            const response = await api.post<SavedEmailTemplate>('/admin/email/saved-templates', template);
            toast.success('Szablon został zapisany');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas zapisywania szablonu:', error);
            toast.error('Nie udało się zapisać szablonu');
            return null;
        }
    },

    /**
     * Usuwa szablon
     */
    deleteTemplate: async (id: string): Promise<boolean> => {
        try {
            await api.delete(`/admin/email/saved-templates/${id}`);
            toast.success('Szablon został usunięty');
            return true;
        } catch (error) {
            console.error('Błąd podczas usuwania szablonu:', error);
            toast.error('Nie udało się usunąć szablonu');
            return false;
        }
    },

    /**
     * Renderuje przechowywany szablon z niestandardową treścią
     */
    renderTemplate: async (templateId: string, customContent?: string): Promise<string> => {
        try {
            const response = await api.post('/admin/email/render-saved-template', {
                templateId,
                customContent
            });
            return response.data.content;
        } catch (error) {
            console.error('Błąd podczas renderowania szablonu:', error);
            return Promise.reject('Nie udało się wyrenderować szablonu');
        }
    }
};