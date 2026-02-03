import React from 'react';
import {Plus} from 'lucide-react';
import {Button} from "../../../shared/ui/Button";

interface RecipeListHeaderProps {
    count: number;
    onCreateNew?: () => void;
}

const RecipeListHeader: React.FC<RecipeListHeaderProps> = ({count, onCreateNew}) => {
    const getCountLabel = (count: number) => {
        if (count === 1) return 'przepis';
        if (count >= 2 && count <= 4) return 'przepisy';
        return 'przepisów';
    };

    return (
        <div className="mb-4 flex justify-between items-center">
            <div className="text-sm text-slate-600">
                {count} {getCountLabel(count)}
            </div>
            {onCreateNew && (
                <Button
                    onClick={onCreateNew}
                    variant="default"
                    size="lg"
                    className="flex items-center gap-2 shadow-md hover:shadow-lg transition-all hover:-translate-y-0.5"
                >
                    <Plus size={20}/>
                    <span className="font-semibold">Dodaj nowy przepis</span>
                </Button>
            )}
        </div>
    );
};

export default RecipeListHeader;
