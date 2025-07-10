import {useCallback, useEffect, useState} from "react";
import {DietTemplate, DietTemplateCategory} from "../../../types/DietTemplate";
import {DietTemplateService} from "../../../services/diet/manual/DietTemplateService";
import {toast} from "../../../utils/toast";

export const useDietTemplates = () => {
    const [templates, setTemplates] = useState<DietTemplate[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadTemplates = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await DietTemplateService.getAllTemplates();
            setTemplates(data);
        } catch (err) {
            setError('Nie udało się załadować szablonów');
            console.error('Error loading templates:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    const loadTemplatesByCategory = useCallback(async (category: DietTemplateCategory) => {
        setLoading(true);
        setError(null);
        try {
            const data = await DietTemplateService.getTemplatesByCategory(category);
            setTemplates(data);
        } catch (err) {
            setError('Nie udało się załadować szablonów');
            console.error('Error loading templates by category:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    const searchTemplates = useCallback(async (query: string) => {
        if (!query.trim()) {
            loadTemplates().catch(console.error);
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const data = await DietTemplateService.searchTemplates(query);
            setTemplates(data);
        } catch (err) {
            setError('Nie udało się wyszukać szablonów');
            console.error('Error searching templates:', err);
        } finally {
            setLoading(false);
        }
    }, [loadTemplates]);

    const deleteTemplate = useCallback(async (id: string) => {
        try {
            await DietTemplateService.deleteTemplate(id);
            setTemplates(prev => prev.filter(t => t.id !== id));
            toast.success('Szablon został usunięty');
        } catch (err) {
            toast.error('Nie udało się usunąć szablonu');
            console.error('Error deleting template:', err);
        }
    }, []);

    const incrementUsage = useCallback(async (id: string) => {
        try {
            await DietTemplateService.incrementUsage(id);
            setTemplates(prev => prev.map(t =>
                t.id === id ? {...t, usageCount: t.usageCount + 1} : t
            ));
        } catch (err) {
            console.error('Error incrementing usage:', err);
        }
    }, []);

    useEffect(() => {
        loadTemplates().catch(console.error);
    }, [loadTemplates]);

    return {
        templates,
        loading,
        error,
        loadTemplates,
        loadTemplatesByCategory,
        searchTemplates,
        deleteTemplate,
        incrementUsage
    };
};