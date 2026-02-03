import React from 'react';
import {Package, X} from 'lucide-react';
import {RecipeIngredient} from "../../../../types";
import {Input} from "../../../shared/ui/Input";
import {Label} from "../../../shared/ui/Label";
import InlineIngredientSearch from "../../diet/creator/components/InlineIngredientSearch";
import {ParsedProduct} from "../../../../types/product";

interface RecipeIngredientsListProps {
    ingredients: RecipeIngredient[];
    editMode: boolean;
    onAdd: (product: ParsedProduct) => void;
    onRemove: (index: number) => void;
    onUpdate: (index: number, field: keyof RecipeIngredient, value: any) => void;
}

const UNITS = ['g', 'ml', 'szt', 'opakowanie', 'łyżka', 'łyżeczka', 'szklanka', 'plaster', 'porcja'];

const RecipeIngredientsList: React.FC<RecipeIngredientsListProps> = ({
                                                                          ingredients,
                                                                          editMode,
                                                                          onAdd,
                                                                          onRemove,
                                                                          onUpdate
                                                                      }) => {
    if (editMode) {
        return (
            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-semibold mb-3">Składniki</h3>

                <div className="space-y-3">
                    {/* Ingredient list in edit mode */}
                    {ingredients.length > 0 && (
                        <div className="space-y-2 mb-4">
                            {ingredients.map((ingredient, index) => (
                                <div
                                    key={index}
                                    className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg border border-gray-200"
                                >
                                    <Package className="h-4 w-4 text-gray-400 flex-shrink-0"/>
                                    <div className="flex-1 min-w-0">
                                        <div className="font-medium text-sm text-gray-900 truncate">
                                            {ingredient.name}
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Input
                                            type="number"
                                            min="1"
                                            step="1"
                                            value={ingredient.quantity}
                                            onChange={(e) => onUpdate(index, 'quantity', parseInt(e.target.value) || 0)}
                                            className="w-20 text-sm"
                                        />
                                        <select
                                            value={ingredient.unit}
                                            onChange={(e) => onUpdate(index, 'unit', e.target.value)}
                                            className="text-sm border border-gray-300 rounded px-2 py-1.5"
                                        >
                                            {UNITS.map(unit => (
                                                <option key={unit} value={unit}>{unit}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <button
                                        onClick={() => onRemove(index)}
                                        className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded transition-all"
                                        title="Usuń składnik"
                                    >
                                        <X className="h-4 w-4"/>
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Ingredient search */}
                    <div>
                        <Label className="mb-2">Dodaj składnik</Label>
                        <InlineIngredientSearch
                            onSelect={onAdd}
                            placeholder="Wpisz składnik (np. kurczak, ryż)..."
                        />
                    </div>
                </div>
            </div>
        );
    }

    // View mode
    return (
        <div className="bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-lg font-semibold mb-3">Składniki</h3>
            <div className="space-y-2">
                {ingredients.length > 0 ? (
                    ingredients.map((ingredient, index) => (
                        <div
                            key={index}
                            className="flex items-center gap-2 p-2 bg-gray-50 rounded"
                        >
                            <Package className="h-4 w-4 text-gray-400"/>
                            <span className="flex-1 text-sm text-gray-900">
                                {ingredient.name}
                            </span>
                            <span className="text-sm font-medium text-gray-700">
                                {ingredient.quantity} {ingredient.unit}
                            </span>
                        </div>
                    ))
                ) : (
                    <p className="text-sm text-gray-500 italic">Brak składników</p>
                )}
            </div>
        </div>
    );
};

export default RecipeIngredientsList;
