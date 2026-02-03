import React from 'react';
import {Save} from 'lucide-react';
import {Button} from "../../../shared/ui/Button";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";

interface RecipeModalFooterProps {
    editMode: boolean;
    isCreateMode: boolean;
    saving: boolean;
    onSave: () => void;
    onCancel: () => void;
    onClose: () => void;
}

const RecipeModalFooter: React.FC<RecipeModalFooterProps> = ({
                                                                  editMode,
                                                                  isCreateMode,
                                                                  saving,
                                                                  onSave,
                                                                  onCancel,
                                                                  onClose
                                                              }) => {
    if (editMode || isCreateMode) {
        return (
            <div className="flex gap-2 w-full justify-end">
                <Button
                    variant="outline"
                    onClick={onCancel}
                    disabled={saving}
                >
                    Anuluj
                </Button>
                <Button
                    variant="default"
                    onClick={onSave}
                    disabled={saving}
                    className="flex items-center gap-2"
                >
                    {saving ? <LoadingSpinner size="sm"/> : <Save size={16}/>}
                    <span>{isCreateMode ? 'Utwórz przepis' : 'Zapisz zmiany'}</span>
                </Button>
            </div>
        );
    }

    return (
        <Button variant="outline" onClick={onClose}>
            Zamknij
        </Button>
    );
};

export default RecipeModalFooter;
