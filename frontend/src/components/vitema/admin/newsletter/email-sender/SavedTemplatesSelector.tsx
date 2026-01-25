import React, {useState} from 'react';
import {SavedEmailTemplate} from '../../../../../types/email';
import {RefreshCw, FileText, Clock, Eye, Trash} from 'lucide-react';
import LoadingSpinner from '../../../../shared/common/LoadingSpinner';
import {toast} from '../../../../../utils/toast';
import ConfirmationDialog from "../../../../shared/common/ConfirmationDialog";
import {SavedTemplateService} from "../../../../../services/newsletter/temlates/SavedTemplateService";
import {formatPostgresTimestamp} from "../../../../../utils/dateFormatters";

interface SavedTemplatesSelectorProps {
    templates: SavedEmailTemplate[];
    selectedId: string | null;
    onSelect: (id: string | null) => void;
    isLoading: boolean;
    onRefresh: () => void;
    compact?: boolean;
}

const SavedTemplatesSelector: React.FC<SavedTemplatesSelectorProps> = ({
                                                                           templates,
                                                                           selectedId,
                                                                           onSelect,
                                                                           isLoading,
                                                                           onRefresh,
                                                                           compact = false
                                                                       }) => {
    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
    const [templateToDelete, setTemplateToDelete] = useState<string | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);
    const [previewTemplate, setPreviewTemplate] = useState<SavedEmailTemplate | null>(null);
    const [isPreviewOpen, setIsPreviewOpen] = useState(false);

    const handleSelectTemplate = (id: string) => {
        onSelect(id === selectedId ? null : id);
    };

    const handleDeleteClick = (e: React.MouseEvent, id: string) => {
        e.stopPropagation();
        setTemplateToDelete(id);
        setIsDeleteDialogOpen(true);
    };

    const handleDeleteTemplate = async () => {
        if (!templateToDelete) return;

        try {
            setIsDeleting(true);
            const success = await SavedTemplateService.deleteTemplate(templateToDelete);

            if (success) {
                if (selectedId === templateToDelete) {
                    onSelect(null);
                }

                toast.success('Szablon został usunięty');
                onRefresh();
            }
        } catch (error) {
            console.error('Error deleting template:', error);
            toast.error('Wystąpił błąd podczas usuwania szablonu');
        } finally {
            setIsDeleting(false);
            setIsDeleteDialogOpen(false);
            setTemplateToDelete(null);
        }
    };

    const handlePreviewClick = (e: React.MouseEvent, template: SavedEmailTemplate) => {
        e.stopPropagation();
        setPreviewTemplate(template);
        setIsPreviewOpen(true);
    };

    const handleClosePreview = () => {
        setIsPreviewOpen(false);
        setPreviewTemplate(null);
    };

    return (
        <div className="space-y-4">
            <div className="flex justify-between items-center">
                <h4 className="font-medium text-gray-900">Zapisane szablony</h4>
                <button
                    type="button"
                    onClick={onRefresh}
                    className="flex items-center text-sm text-primary hover:text-primary-dark"
                    disabled={isLoading}
                >
                    {isLoading ? (
                        <LoadingSpinner size="sm"/>
                    ) : (
                        <RefreshCw className="h-4 w-4 mr-1"/>
                    )}
                    Odśwież
                </button>
            </div>

            {isLoading ? (
                <div className="flex justify-center items-center py-8">
                    <LoadingSpinner/>
                </div>
            ) : templates.length === 0 ? (
                <div className="bg-gray-50 rounded-md p-8 text-center">
                    <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4"/>
                    <h4 className="text-gray-900 font-medium mb-1">Brak zapisanych szablonów</h4>
                    <p className="text-sm text-gray-500 max-w-md mx-auto">
                        Zapisuj swoje szablony emaili, aby móc szybko ich użyć w przyszłości bez potrzeby tworzenia
                        wiadomości od nowa.
                    </p>
                </div>
            ) : (
                <div className={compact
                    ? "space-y-2" // Kompaktowy widok (lista pionowa)
                    : "grid grid-cols-1 md:grid-cols-2 gap-4" // Oryginalny widok (siatka)
                }>
                    {templates.map(template => (
                        <div
                            key={template.id}
                            onClick={() => handleSelectTemplate(template.id)}
                            className={`
                                border rounded-md ${compact ? 'p-2' : 'p-4'} cursor-pointer hover:bg-gray-50 transition-colors relative
                                ${selectedId === template.id ? 'border-primary bg-primary-light/10 bg-opacity-10' : 'border-gray-200'}
                            `}
                        >
                            <div className="flex justify-between items-start">
                                <div>
                                    <h4 className={`font-medium text-gray-900 ${compact ? 'text-sm' : ''}`}>{template.name}</h4>
                                    <p className="text-sm text-gray-500 line-clamp-1 mt-1">
                                        {template.subject}
                                    </p>
                                </div>
                                <div className="flex space-x-1">
                                    <button
                                        type="button"
                                        onClick={(e) => handlePreviewClick(e, template)}
                                        className="p-1 text-gray-500 hover:text-primary rounded hover:bg-gray-100"
                                        title="Podgląd szablonu"
                                    >
                                        <Eye className={`${compact ? 'h-3 w-3' : 'h-4 w-4'}`}/>
                                    </button>
                                    <button
                                        type="button"
                                        onClick={(e) => handleDeleteClick(e, template.id)}
                                        className="p-1 text-gray-500 hover:text-red-600 rounded hover:bg-gray-100"
                                        title="Usuń szablon"
                                    >
                                        <Trash className={`${compact ? 'h-3 w-3' : 'h-4 w-4'}`}/>
                                    </button>
                                </div>
                            </div>

                            {!compact && (
                                <div className="flex items-center mt-3 text-xs text-gray-500">
                                    <Clock className="h-3 w-3 mr-1 inline"/>
                                    Utworzono: {formatPostgresTimestamp(template.createdAt)}
                                </div>
                            )}

                            {selectedId === template.id && (
                                <span className="absolute inset-y-0 left-0 w-1 bg-primary rounded-l-md"></span>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {/* Dialog potwierdzenia usunięcia */}
            <ConfirmationDialog
                isOpen={isDeleteDialogOpen}
                onClose={() => {
                    setIsDeleteDialogOpen(false);
                    setTemplateToDelete(null);
                }}
                onConfirm={handleDeleteTemplate}
                title="Potwierdź usunięcie"
                description="Czy na pewno chcesz usunąć ten szablon? Tej operacji nie można cofnąć."
                confirmLabel={isDeleting ? "Usuwanie..." : "Usuń"}
                variant="destructive"
                isLoading={isDeleting}
            />

            {/* Dialog podglądu szablonu */}
            {previewTemplate && (
                <TemplatePreviewDialog
                    isOpen={isPreviewOpen}
                    onClose={handleClosePreview}
                    template={previewTemplate}
                />
            )}
        </div>
    );
};

const TemplatePreviewDialog: React.FC<{
    isOpen: boolean;
    onClose: () => void;
    template: SavedEmailTemplate;
}> = ({isOpen, onClose, template}) => {
    return (
        <div className={`fixed inset-0 z-50 flex items-center justify-center ${isOpen ? 'block' : 'hidden'}`}>
            <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose}></div>
            <div
                className="relative bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
                <div className="flex justify-between items-center p-4 border-b">
                    <h3 className="text-lg font-medium">
                        Podgląd szablonu: {template.name}
                    </h3>
                    <button
                        type="button"
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-500 focus:outline-none"
                    >
                        <span className="sr-only">Zamknij</span>
                        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
                <div className="p-6 overflow-y-auto flex-1">
                    <div className="mb-4">
                        <h4 className="text-sm font-medium text-gray-500">Temat:</h4>
                        <p className="text-lg font-medium mt-1">{template.subject}</p>
                    </div>

                    <div className="mb-4">
                        <h4 className="text-sm font-medium text-gray-500 mb-2">Treść:</h4>
                        <div className="border rounded-md p-4 bg-gray-50 overflow-auto max-h-96">
                            <div dangerouslySetInnerHTML={{__html: template.content}}/>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <h4 className="text-sm font-medium text-gray-500">Szablon systemowy:</h4>
                            <p>{template.useTemplate ? template.templateType : 'Nie używa'}</p>
                        </div>
                        <div>
                            <h4 className="text-sm font-medium text-gray-500">Data utworzenia:</h4>
                            <p>{formatPostgresTimestamp(template.createdAt)}</p>
                        </div>
                    </div>
                </div>
                <div className="border-t px-4 py-3 flex justify-end">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                    >
                        Zamknij
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SavedTemplatesSelector;