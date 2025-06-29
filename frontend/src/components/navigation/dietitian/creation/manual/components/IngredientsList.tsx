import React from "react";
import { ParsedProduct } from "../../../../../../types/product";
import { Edit2, Trash2, Package } from "lucide-react";

interface IngredientsListProps {
    ingredients: ParsedProduct[];
    onRemove: (index: number) => void;
    onEdit?: (index: number, ingredient: ParsedProduct) => void;
}

const IngredientsList: React.FC<IngredientsListProps> = ({
                                                             ingredients,
                                                             onRemove,
                                                             onEdit
                                                         }) => {
    if (!ingredients || ingredients.length === 0) {
        return (
            <div className="text-center py-6 text-gray-500 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50/30">
                <div className="flex flex-col items-center gap-2">
                    <Package className="h-8 w-8 text-gray-400" />
                    <p className="font-medium">Brak składników</p>
                    <p className="text-sm">Dodaj składniki używając pola wyszukiwania powyżej</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-1.5">
            {ingredients.map((ingredient, index) => (
                <div
                    key={ingredient.id || `ingredient-${index}`}
                    className="flex items-center justify-between p-2.5 bg-white rounded-md border border-gray-200 hover:border-gray-300 transition-colors"
                >
                    <div className="flex-1 min-w-0">
                        <div className="text-sm font-medium text-gray-900 truncate">
                            {ingredient.name}
                        </div>
                        <div className="text-xs text-gray-600">
                            <span className="font-semibold text-primary">{ingredient.quantity}</span> {ingredient.unit}
                        </div>
                    </div>

                    <div className="flex items-center gap-0.5">
                        {onEdit && (
                            <button
                                onClick={() => onEdit(index, ingredient)}
                                className="p-1.5 text-gray-400 hover:text-blue-600 rounded transition-colors"
                                title="Edytuj"
                            >
                                <Edit2 className="h-3.5 w-3.5" />
                            </button>
                        )}
                        <button
                            onClick={() => onRemove(index)}
                            className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors"
                            title="Usuń"
                        >
                            <Trash2 className="h-3.5 w-3.5" />
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default IngredientsList;