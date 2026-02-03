import React from 'react';
import {Plus} from 'lucide-react';
import {Button} from "../../../shared/ui/Button";

interface RecipeEmptyStateProps {
    onCreateNew?: () => void;
}

const RecipeEmptyState: React.FC<RecipeEmptyStateProps> = ({onCreateNew}) => {
    return (
        <div className="text-center py-16 bg-white rounded-lg shadow-sm border border-slate-200">
            <h3 className="text-lg font-medium text-slate-700">
                Brak przepisów do wyświetlenia
            </h3>
            <p className="text-slate-500 max-w-md mx-auto mt-2">
                Nie znaleziono żadnych przepisów spełniających kryteria. 
                Spróbuj zmienić filtry lub odśwież listę.
            </p>
            {onCreateNew && (
                <Button
                    onClick={onCreateNew}
                    variant="default"
                    size="default"
                    className="mt-4 flex items-center gap-2 mx-auto"
                >
                    <Plus size={18}/>
                    <span>Utwórz pierwszy przepis</span>
                </Button>
            )}
        </div>
    );
};

export default RecipeEmptyState;
