import React, {useState} from "react";
import {TemplateUpdateSummary} from "../../../../../types/mealSuggestions";
import {AlertTriangle, Check, RefreshCw, X} from "lucide-react";

interface TemplateChangeManagerProps {
    updateSummary: TemplateUpdateSummary;
    onSaveChanges: () => Promise<void>;
    onDiscardChanges: () => void;
    onKeepLocal: () => void;
    isVisible: boolean;
}

const TemplateChangeManager: React.FC<TemplateChangeManagerProps> = ({
                                                                         updateSummary,
                                                                         onSaveChanges,
                                                                         onDiscardChanges,
                                                                         onKeepLocal,
                                                                         isVisible
                                                                     }) => {
    const [isSaving, setIsSaving] = useState(false);

    if (!isVisible) return null;

    const handleSaveChanges = async () => {
        setIsSaving(true);
        try {
            await onSaveChanges();
        } finally {
            setIsSaving(false);
        }
    };

    const getPolishFieldName = (field: string) => {
        switch (field) {
            case 'name':
                return '📝 nazwa';
            case 'instructions':
                return '📋 instrukcje';
            case 'ingredients':
                return '🥕 składniki';
            case 'nutritionalValues':
                return '🔢 wartości odżywcze';
            case 'photos':
                return '📷 zdjęcia';
            default:
                return field;
        }
    };

    return (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 mb-4">
            <div className="flex items-start gap-3">
                <AlertTriangle className="h-5 w-5 text-amber-600 mt-0.5 flex-shrink-0"/>
                <div className="flex-1">
                    <h4 className="font-medium text-amber-900 mb-2">
                        Wykryto zmiany w szablonie "{updateSummary.templateName}"
                    </h4>

                    <div className="space-y-2 mb-4">
                        {updateSummary.changes.map((change, index) => (
                            <div key={index} className="text-sm text-amber-800 flex items-center gap-2">
                                <span>{getPolishFieldName(change.field)}</span>
                                <span className="text-amber-600">→</span>
                                <span className="font-medium">zaktualizowano</span>
                            </div>
                        ))}
                    </div>

                    <div className="flex flex-wrap gap-2">
                        <button
                            onClick={handleSaveChanges}
                            disabled={isSaving}
                            className="flex items-center gap-2 px-3 py-1.5 bg-green-600 text-white text-sm rounded-md hover:bg-green-700 disabled:opacity-50"
                        >
                            {isSaving ? (
                                <RefreshCw className="h-4 w-4 animate-spin"/>
                            ) : (
                                <Check className="h-4 w-4"/>
                            )}
                            Zapisz zmiany w szablonie
                        </button>

                        <button
                            onClick={onKeepLocal}
                            className="flex items-center gap-2 px-3 py-1.5 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700"
                        >
                            <X className="h-4 w-4"/>
                            Zachowaj tylko lokalnie
                        </button>

                        <button
                            onClick={onDiscardChanges}
                            className="flex items-center gap-2 px-3 py-1.5 bg-gray-600 text-white text-sm rounded-md hover:bg-gray-700"
                        >
                            <RefreshCw className="h-4 w-4"/>
                            Przywróć szablon
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TemplateChangeManager;
