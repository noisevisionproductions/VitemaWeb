import React, {useState} from 'react';
import {ParsedProduct} from "../../../../../../../../types/product";
import InlineIngredientSearch from '../../../../components/InlineIngredientSearch';
import {Edit2, Package, Trash2} from 'lucide-react';

interface MealEditorIngredientsProps {
    ingredients: ParsedProduct[];
    onAddIngredient: (ingredient: ParsedProduct) => void;
    onRemoveIngredient: (index: number) => void;
    onUpdateIngredient?: (index: number, ingredient: ParsedProduct) => void;
    trainerId?: string;
}

const MealEditorIngredients: React.FC<MealEditorIngredientsProps> = ({
                                                                         ingredients,
                                                                         onAddIngredient,
                                                                         onRemoveIngredient,
                                                                         onUpdateIngredient,
                                                                         trainerId
                                                                     }) => {
    const [editingIndex, setEditingIndex] = useState<number | null>(null);

    const handleUpdateConfirm = (ingredient: ParsedProduct) => {
        if (editingIndex !== null && onUpdateIngredient) {
            onUpdateIngredient(editingIndex, ingredient);
            setEditingIndex(null);
        }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-semibold text-gray-700">
                    Składniki
                </label>
                {ingredients.length > 0 && (
                    <span className="text-xs text-gray-400 font-medium">
                        {ingredients.length} dodano
                    </span>
                )}
            </div>

            {editingIndex === null && (
                <div className="mb-4">
                    <InlineIngredientSearch
                        onSelect={onAddIngredient}
                        placeholder="Dodaj składnik (np. 'banan')..."
                        trainerId={trainerId}
                    />
                </div>
            )}

            <div className="space-y-1.5">
                {ingredients.length === 0 ? (
                    <div
                        className="text-center py-6 text-gray-500 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50/30">
                        <div className="flex flex-col items-center gap-2">
                            <Package className="h-8 w-8 text-gray-400"/>
                            <p className="font-medium">Brak składników</p>
                        </div>
                    </div>
                ) : (
                    ingredients.map((ingredient, index) => {
                        if (editingIndex === index) {
                            return (
                                <div key={ingredient.id || index}
                                     className="animate-in fade-in zoom-in-95 duration-200">
                                    <InlineIngredientSearch
                                        onSelect={handleUpdateConfirm}
                                        placeholder="Edytuj składnik..."
                                        trainerId={trainerId}
                                        initialIngredient={ingredient}
                                        onCancelEdit={() => setEditingIndex(null)}
                                    />
                                </div>
                            );
                        }

                        return (
                            <div
                                key={ingredient.id || index}
                                className="flex items-center justify-between p-2.5 bg-white rounded-md border border-gray-200 hover:border-gray-300 transition-colors group"
                            >
                                <div className="flex-1 min-w-0">
                                    <div className="text-sm font-medium text-gray-900 truncate">
                                        {ingredient.name}
                                    </div>
                                    <div className="text-xs text-gray-600">
                                        <span
                                            className="font-semibold text-primary">{ingredient.quantity}</span> {ingredient.unit}
                                    </div>
                                </div>

                                <div
                                    className="flex items-center gap-0.5 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={() => setEditingIndex(index)}
                                        className="p-1.5 text-gray-400 hover:text-blue-600 rounded transition-colors"
                                        title="Edytuj"
                                    >
                                        <Edit2 className="h-3.5 w-3.5"/>
                                    </button>
                                    <button
                                        onClick={() => onRemoveIngredient(index)}
                                        className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors"
                                        title="Usuń"
                                    >
                                        <Trash2 className="h-3.5 w-3.5"/>
                                    </button>
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
};

export default MealEditorIngredients;