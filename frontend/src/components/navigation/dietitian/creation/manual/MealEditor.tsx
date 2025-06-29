import React, {useCallback} from "react";
import {NutritionalValues, ParsedMeal} from "../../../../../types";
import {ParsedProduct} from "../../../../../types/product";
import IngredientsList from "./components/IngredientsList";
import InlineIngredientSearch from "./steps/InlineIngredientSearch";
import ColoredNutritionBadges from "./steps/ColoredNutritionBadges";

interface MealEditorProps {
    meal: ParsedMeal;
    dayIndex: number;
    mealIndex: number;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
}

const MealEditor: React.FC<MealEditorProps> = ({
                                                   meal,
                                                   dayIndex,
                                                   mealIndex,
                                                   onUpdateMeal,
                                                   onAddIngredient,
                                                   onRemoveIngredient
                                               }) => {
    const handleMealUpdate = (updates: Partial<ParsedMeal>) => {
        onUpdateMeal(dayIndex, mealIndex, {...meal, ...updates});
    };

    const handleAddIngredient = useCallback((ingredient: ParsedProduct) => {
        const ingredientWithId = {
            ...ingredient,
            id: ingredient.id || `ingredient-${Date.now()}-${Math.random()}`
        };

        onAddIngredient(dayIndex, mealIndex, ingredientWithId);
    }, [dayIndex, mealIndex, onAddIngredient]);

    const updateNutritionalValue = (field: keyof NutritionalValues, value: string) => {
        const numValue = parseFloat(value) || undefined;
        const currentValues = meal.nutritionalValues || {};

        handleMealUpdate({
            nutritionalValues: {
                ...currentValues,
                [field]: numValue
            }
        });
    };

    return (
        <div className="space-y-4">

            {/* Meal name */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    Nazwa posiłku *
                </label>
                <input
                    type="text"
                    value={meal.name}
                    onChange={(e) => handleMealUpdate({name: e.target.value})}
                    placeholder="np. Owsianka z owocami"
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                />
            </div>

            {/* Instructions */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    Instrukcje przygotowania
                </label>
                <textarea
                    value={meal.instructions}
                    onChange={(e) => handleMealUpdate({instructions: e.target.value})}
                    placeholder="Opisz jak przygotować posiłek..."
                    rows={2} // Zmniejszone z 3 na 2
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary resize-none"
                />
            </div>

            {/* Ingredients section */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    Składniki
                </label>

                {/* Always visible search */}
                <div className="mb-3">
                    <InlineIngredientSearch
                        onSelect={handleAddIngredient}
                        placeholder="Dodaj składnik, np. 'mleko 200ml'..."
                    />
                </div>

                {/* Ingredients list */}
                <IngredientsList
                    ingredients={meal.ingredients || []}
                    onRemove={(index) => onRemoveIngredient(dayIndex, mealIndex, index)}
                />
            </div>

            {/* Nutritional values */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    Wartości odżywcze (opcjonalnie)
                </label>

                {/* Preview of current values */}
                {meal.nutritionalValues && (
                    <div className="mb-2">
                        <ColoredNutritionBadges
                            nutritionalValues={meal.nutritionalValues}
                            size="sm"
                            layout="horizontal"
                        />
                    </div>
                )}

                <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
                    <div>
                        <label className="block text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-calories rounded-full"></span>
                            Kalorie
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.calories || ''}
                            onChange={(e) => updateNutritionalValue('calories', e.target.value)}
                            placeholder="kcal"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-calories focus:border-nutrition-calories"
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-protein rounded-full"></span>
                            Białko
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.protein || ''}
                            onChange={(e) => updateNutritionalValue('protein', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-protein focus:border-nutrition-protein"
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-fats rounded-full"></span>
                            Tłuszcze
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.fat || ''}
                            onChange={(e) => updateNutritionalValue('fat', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-fats focus:border-nutrition-fats"
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-carbs rounded-full"></span>
                            Węglowodany
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.carbs || ''}
                            onChange={(e) => updateNutritionalValue('carbs', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-carbs focus:border-nutrition-carbs"
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MealEditor;