import React, {useState} from 'react';
import {MealType, Recipe} from "../../../../../../types";
import {ChevronDown, ChevronUp, Clock, Edit2, Save, X} from 'lucide-react';
import {toast} from "../../../../../../utils/toast";

interface MealItemProps {
    recipe: Recipe;
    time: string;
    mealType: MealType;
    mealTypeLabel: string;
    onTimeChange: (newTime: string) => Promise<void>;
    onRecipeUpdate: (updatedRecipe: Partial<Recipe>) => Promise<void>;
    onRecipeUpdated?: (recipeId: string) => void;
}

const MealItem: React.FC<MealItemProps> = ({
                                               recipe,
                                               time,
                                               mealTypeLabel,
                                               onTimeChange,
                                               onRecipeUpdate,
                                               onRecipeUpdated,
                                           }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [isExpanded, setIsExpanded] = useState(false);
    const [editedRecipe, setEditedRecipe] = useState(recipe);

    const handleSave = async () => {
        try {
            await onRecipeUpdate(editedRecipe);
            setIsEditing(false);

            onRecipeUpdated?.(recipe.id);
            toast.success('Zapisano zmiany');
        } catch (error) {
            toast.error('Błąd podczas zapisywania zmian');
            console.error(error);
        }
    };

    const handleCancel = () => {
        setEditedRecipe(recipe);
        setIsEditing(false);
    };

    return (
        <div className="bg-white rounded-lg shadow-sm">
            <div className="p-4">
                <div className="flex items-start justify-between">
                    {/* Lewa strona-czas i kontrolki */}
                    <div className="flex items-center gap-2">
                        <Clock className="h-4 w-4 text-gray-400"/>
                        <input
                            type="time"
                            value={time}
                            onChange={(e) => onTimeChange(e.target.value)}
                            className="border rounded px-2 py-1"
                        />
                        <span className="text-sm text-gray-500">
                            {mealTypeLabel}
                        </span>
                    </div>

                    {/* Prawa strona-przyciski akcji */}
                    <div className="flex items-center gap-2">
                        {isEditing ? (
                            <>
                                <button
                                    onClick={handleSave}
                                    className="p-1 text-green-600 hover:text-green-700"
                                >
                                    <Save className="h-4 w-4"/>
                                </button>
                                <button
                                    onClick={handleCancel}
                                    className="p-1 text-red-600 hover:text-red-700"
                                >
                                    <X className="h-4 w-4"/>
                                </button>
                            </>
                        ) : (
                            <button
                                onClick={() => setIsEditing(true)}
                                className="p-1 text-blue-600 hover:text-blue-700"
                            >
                                <Edit2 className="h-4 w-4"/>
                            </button>
                        )}
                        <button
                            onClick={() => setIsExpanded(!isExpanded)}
                            className="p-1 text-gray-400 hover:text-gray-500"
                        >
                            {isExpanded ? <ChevronUp className="h-4 w-4"/> : <ChevronDown className="h-4 w-4"/>}
                        </button>
                    </div>
                </div>

                {/* Nazwa posiłku */}
                <div className="mt-3">
                    {isEditing ? (
                        <input
                            type="text"
                            value={editedRecipe.name}
                            onChange={(e) => setEditedRecipe({...editedRecipe, name: e.target.value})}
                            className="w-full border rounded-md px-3 py-2"
                            placeholder="Nazwa posiłku"
                        />
                    ) : (
                        <h3 className="font-medium">{recipe.name}</h3>
                    )}
                </div>

                {/* Wartości odżywcze */}
                <div className="mt-2">
                    {isEditing ? (
                        <div className="grid grid-cols-4 gap-2">
                            <div>
                                <label className="text-xs text-gray-500">Kalorie</label>
                                <input
                                    type="number"
                                    value={editedRecipe.nutritionalValues?.calories || 0}
                                    onChange={(e) => setEditedRecipe({
                                        ...editedRecipe,
                                        nutritionalValues: {
                                            ...editedRecipe.nutritionalValues,
                                            calories: parseFloat(e.target.value)
                                        }
                                    })}
                                    className="w-full border rounded-md px-2 py-1"
                                />
                            </div>
                            <div>
                                <label className="text-xs text-gray-500">Białko</label>
                                <input
                                    type="number"
                                    value={editedRecipe.nutritionalValues?.protein || 0}
                                    onChange={(e) => setEditedRecipe({
                                        ...editedRecipe,
                                        nutritionalValues: {
                                            ...editedRecipe.nutritionalValues,
                                            protein: parseFloat(e.target.value)
                                        }
                                    })}
                                    className="w-full border rounded-md px-2 py-1"
                                />
                            </div>
                            <div>
                                <label className="text-xs text-gray-500">Tłuszcze</label>
                                <input
                                    type="number"
                                    value={editedRecipe.nutritionalValues?.fat || 0}
                                    onChange={(e) => setEditedRecipe({
                                        ...editedRecipe,
                                        nutritionalValues: {
                                            ...editedRecipe.nutritionalValues,
                                            fat: parseFloat(e.target.value)
                                        }
                                    })}
                                    className="w-full border rounded-md px-2 py-1"
                                />
                            </div>
                            <div>
                                <label className="text-xs text-gray-500">Węglowodany</label>
                                <input
                                    type="number"
                                    value={editedRecipe.nutritionalValues?.carbs || 0}
                                    onChange={(e) => setEditedRecipe({
                                        ...editedRecipe,
                                        nutritionalValues: {
                                            ...editedRecipe.nutritionalValues,
                                            carbs: parseFloat(e.target.value)
                                        }
                                    })}
                                    className="w-full border rounded-md px-2 py-1"
                                />
                            </div>
                        </div>
                    ) : (
                        <div className="text-sm text-gray-500">
                            {recipe.nutritionalValues?.calories || 0} kcal |
                            B: {recipe.nutritionalValues?.protein || 0}g |
                            T: {recipe.nutritionalValues?.fat || 0}g |
                            W: {recipe.nutritionalValues?.carbs || 0}g
                        </div>
                    )}
                </div>
            </div>

            {/* Sposób przygotowania */}
            {isExpanded && (
                <div className="px-4 pb-4 border-t mt-4 pt-4">
                    {isEditing ? (
                        <div>
                            <label className="text-sm font-medium text-gray-700 block mb-2">
                                Sposób przygotowania
                            </label>
                            <textarea
                                value={editedRecipe.instructions}
                                onChange={(e) => setEditedRecipe({...editedRecipe, instructions: e.target.value})}
                                className="w-full border rounded-md px-3 py-2 min-h-[100px]"
                                placeholder="Wpisz sposób przygotowania..."
                            />
                        </div>
                    ) : (
                        <div className="text-gray-600 whitespace-pre-wrap">
                            {recipe.instructions}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default MealItem;