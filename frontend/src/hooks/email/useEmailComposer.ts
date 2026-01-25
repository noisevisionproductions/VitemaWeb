import {useState, useEffect, useCallback} from 'react';
import {EmailTemplate, EmailTemplateType, SavedEmailTemplate} from '../../types/email';
import {EmailTemplateService} from '../../services/newsletter/temlates/EmailTemplateService';
import {SavedTemplateService} from '../../services/newsletter/temlates/SavedTemplateService';
import {toast} from '../../utils/toast';
import debounce from 'lodash/debounce';

interface UseEmailComposerProps {
    initialSubject?: string;
    initialContent?: string;
    initialUseTemplate?: boolean;
    initialTemplateType?: EmailTemplateType;
}

export function useEmailComposer({
                                     initialSubject = '',
                                     initialContent = '',
                                     initialUseTemplate = true,
                                     initialTemplateType = 'basic'
                                 }: UseEmailComposerProps = {}) {
    // Stan formularza
    const [subject, setSubject] = useState(initialSubject);
    const [content, setContent] = useState(initialContent);
    const [useTemplate, setUseTemplate] = useState(initialUseTemplate);
    const [templateType, setTemplateType] = useState<EmailTemplateType>(initialTemplateType);
    const [selectedSavedTemplateId, setSelectedSavedTemplateId] = useState<string | null>(null);
    const [previewContent, setPreviewContent] = useState('');

    // Stan ładowania
    const [isLoadingTemplates, setIsLoadingTemplates] = useState(false);
    const [isLoadingSavedTemplates, setIsLoadingSavedTemplates] = useState(false);
    const [isLoadingPreview, setIsLoadingPreview] = useState(false);

    // Dane
    const [availableTemplates, setAvailableTemplates] = useState<EmailTemplate[]>([]);
    const [savedTemplates, setSavedTemplates] = useState<SavedEmailTemplate[]>([]);

    // Pobierz szablony przy inicjalizacji
    useEffect(() => {
        fetchTemplates();
        fetchSavedTemplates();
    }, []);

    // Generuj podgląd przy zmianie treści lub szablonu
    const debouncedPreview = useCallback(
        debounce(() => {
            if (content.trim()) {
                handlePreview()
                    .catch(error => console.error('Błąd podczas generowania podglądu:', error));
            }
            return Promise.resolve();
        }, 500),
        [content, useTemplate, templateType, selectedSavedTemplateId]
    );

    useEffect(() => {
        let isMounted = true;

        if (content.trim()) {
            setIsLoadingPreview(true);

            const previewPromise = debouncedPreview();
            if (previewPromise) {
                previewPromise.catch(error => {
                    if (isMounted) {
                        console.error('Błąd podczas obsługi podglądu:', error);
                    }
                });
            }
        }

        return () => {
            isMounted = false;
            debouncedPreview.cancel();
        };
    }, [content, useTemplate, templateType, selectedSavedTemplateId, debouncedPreview]);

    // Pobieranie listy dostępnych szablonów
    const fetchTemplates = async () => {
        try {
            setIsLoadingTemplates(true);
            const templates = await EmailTemplateService.getTemplates();
            setAvailableTemplates(templates);
        } catch (error) {
            console.error('Błąd podczas pobierania szablonów:', error);
        } finally {
            setIsLoadingTemplates(false);
        }
    };

    // Pobieranie zapisanych szablonów
    const fetchSavedTemplates = async () => {
        try {
            setIsLoadingSavedTemplates(true);
            const response = await SavedTemplateService.getAllTemplates();
            setSavedTemplates(response);
        } catch (error) {
            console.error('Błąd podczas pobierania zapisanych szablonów:', error);
        } finally {
            setIsLoadingSavedTemplates(false);
        }
    };

    // Obsługa wyboru zapisanego szablonu
    const handleSelectSavedTemplate = async (templateId: string | null) => {
        setSelectedSavedTemplateId(templateId);

        if (templateId) {
            try {
                const template = await SavedTemplateService.getTemplateById(templateId);

                if (template) {
                    setSubject(template.subject);
                    setContent(template.content);
                    setUseTemplate(template.useTemplate);
                    setTemplateType(template.templateType as EmailTemplateType);
                } else {
                    toast.error('Nie znaleziono szablonu');
                }
            } catch (error) {
                console.error('Błąd podczas pobierania szablonu:', error);
                toast.error('Nie udało się pobrać szablonu');
            }
        }
    };

    // Generowanie podglądu
    const handlePreview = async () => {
        try {
            if (selectedSavedTemplateId) {
                try {
                    const template = await SavedTemplateService.getTemplateById(selectedSavedTemplateId);

                    if (template) {
                        let updatedContent = template.content;
                        if (updatedContent.includes('{{content}}')) {
                            updatedContent = updatedContent.replace('{{content}}', content);
                        } else {
                            updatedContent = content;
                        }
                        setPreviewContent(updatedContent);
                    } else {
                        toast.error('Nie znaleziono szablonu');
                    }
                } catch (error) {
                    console.error('Błąd podczas pobierania zapisanego szablonu:', error);
                }
            } else if (useTemplate) {
                try {
                    const preview = await EmailTemplateService.previewTemplate(content, templateType);
                    setPreviewContent(preview);
                } catch (error) {
                    console.error('Błąd podczas pobierania podglądu:', error);
                }
            } else {
                setPreviewContent(content);
            }
        } catch (error) {
            console.error('Error generating preview:', error);
        } finally {
            setIsLoadingPreview(false);
        }
    };

    // Obsługa zapisywania szablonu
    const handleSaveTemplate = async () => {
        if (!subject.trim()) {
            toast.error('Podaj temat wiadomości przed zapisaniem szablonu');
            return false;
        }

        if (!content.trim()) {
            toast.error('Wpisz treść wiadomości przed zapisaniem szablonu');
            return false;
        }

        const templateName = prompt('Podaj nazwę dla zapisywanego szablonu:');
        if (!templateName) return false;

        try {
            await SavedTemplateService.saveTemplate({
                name: templateName,
                subject,
                content,
                description: 'Szablon zapisany ' + new Date().toLocaleDateString(),
                useTemplate,
                templateType: useTemplate ? templateType : 'basic'
            });

            toast.success('Szablon został zapisany');
            fetchSavedTemplates();
            return true;
        } catch (error) {
            console.error('Error saving template:', error);
            toast.error('Wystąpił błąd podczas zapisywania szablonu');
            return false;
        }
    };

    const resetForm = () => {
        setSubject('');
        setContent('');
        setPreviewContent('');
        setSelectedSavedTemplateId(null);
    };

    return {
        // Stan
        subject,
        setSubject,
        content,
        setContent,
        useTemplate,
        setUseTemplate,
        templateType,
        setTemplateType,
        selectedSavedTemplateId,
        previewContent,

        // Szablony
        availableTemplates,
        savedTemplates,

        // Metody
        handleSelectSavedTemplate,
        handleSaveTemplate,
        resetForm,
        fetchSavedTemplates,

        // Stan ładowania
        isLoadingTemplates,
        isLoadingSavedTemplates,
        isLoadingPreview
    };
}