import {ExternalRecipient, ExternalRecipientFormData} from "../../types/email";
import api from "../../config/axios";
import {toast} from "../../utils/toast";

/**
 * Usługa zarządzająca zewnętrznymi odbiorcami
 */
export const ExternalRecipientService = {
    /**
     * Pobiera wszystkich zewnętrznych odbiorców
     */
    getAllRecipients: async (): Promise<ExternalRecipient[]> => {
        try {
            const response = await api.get<ExternalRecipient[]>('/admin/email/external-recipients');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas pobierania odbiorców:', error);
            toast.error('Nie udało się pobrać listy odbiorców');
            return [];
        }
    },

    /**
     * Dodaje nowego odbiorcę
     */
    addRecipient: async (data: ExternalRecipientFormData): Promise<ExternalRecipient | null> => {
        try {
            const response = await api.post<ExternalRecipient>('/admin/email/external-recipients', data);
            toast.success('Dodano nowego odbiorcę');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas dodawania odbiorcy:', error);
            toast.error('Nie udało się dodać odbiorcy');
            return null;
        }
    },

    /**
     * Masowe dodawanie odbiorców
     */
    bulkAddRecipients: async (data: ExternalRecipientFormData[]): Promise<{ added: number, errors: any[] }> => {
        try {
            const response = await api.post<{ added: number, errors: any[] }>('/admin/email/external-recipients/bulk', {
                recipients: data
            });
            toast.success(`Dodano ${response.data.added} odbiorców`);
            return response.data;
        } catch (error) {
            console.error('Błąd podczas masowego dodawania odbiorców:', error);
            toast.error('Wystąpił błąd podczas dodawania odbiorców');
            return {added: 0, errors: [{message: 'Błąd podczas dodawania odbiorców'}]};
        }
    },

    /**
     * Aktualizuje dane odbiorcy
     */
    updateRecipient: async (id: string, data: Partial<ExternalRecipientFormData>): Promise<ExternalRecipient | null> => {
        try {
            const response = await api.put<ExternalRecipient>(`/admin/email/external-recipients/${id}`, data);
            toast.success('Zaktualizowano dane odbiorcy');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas aktualizacji odbiorcy:', error);
            toast.error('Nie udało się zaktualizować danych odbiorcy');
            return null;
        }
    },

    /**
     * Aktualizuje status odbiorcy
     */
    updateStatus: async (id: string, status: string): Promise<ExternalRecipient | null> => {
        try {
            const response = await api.patch<ExternalRecipient>(
                `/admin/email/external-recipients/${id}/status`,
                {status}
            );
            toast.success('Zaktualizowano status odbiorcy');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas aktualizacji statusu odbiorcy:', error);
            toast.error('Nie udało się zaktualizować statusu');
            return null;
        }
    },

    /**
     * Usuwa odbiorcę
     */
    deleteRecipient: async (id: string): Promise<boolean> => {
        try {
            await api.delete(`/admin/email/external-recipients/${id}`);
            toast.success('Usunięto odbiorcę');
            return true;
        } catch (error) {
            console.error('Błąd podczas usuwania odbiorcy:', error);
            toast.error('Nie udało się usunąć odbiorcy');
            return false;
        }
    },

    /**
     * Importuje odbiorców z pliku CSV
     */
    importFromCsv: async (file: File, defaultCategory?: string): Promise<{ added: number, errors: any[] }> => {
        try {
            const formData = new FormData();
            formData.append('file', file);
            if (defaultCategory) {
                formData.append('defaultCategory', defaultCategory);
            }

            const response = await api.post<{
                added: number,
                errors: any[]
            }>('/admin/email/external-recipients/import', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            toast.success(`Zaimportowano ${response.data.added} odbiorców`);
            return response.data;
        } catch (error) {
            console.error('Błąd podczas importu odbiorców:', error);
            toast.error('Wystąpił błąd podczas importu');
            return {added: 0, errors: [{message: 'Błąd podczas importu'}]};
        }
    },

    /**
     * Pobiera dostępne kategorie
     */
    getCategories: async (): Promise<{ categories: string[] }> => {
        try {
            const response = await api.get<{ categories: string[] }>('/admin/email/external-recipients/categories');
            return response.data;
        } catch (error) {
            console.error('Błąd podczas pobierania kategorii:', error);
            return {categories: []};
        }
    }
};