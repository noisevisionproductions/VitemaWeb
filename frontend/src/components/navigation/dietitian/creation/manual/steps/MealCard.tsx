import React from 'react';
import { Copy, Utensils, Clock } from 'lucide-react';
import { ParsedMeal } from '../../../../../../types';
import { ParsedProduct } from '../../../../../../types/product';
import { getMealTypeLabel } from '../../../../../../utils/diet/mealTypeUtils';
import MealEditor from '../MealEditor';
import ColoredNutritionBadges from './ColoredNutritionBadges';

interface MealCardProps {
    meal: ParsedMeal;
    mealIndex: number;
    dayIndex: number;
    onUpdateMeal: (mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (mealIndex: number, ingredientIndex: number) => void;
    onCopy?: () => void;
}

const MealCard: React.FC<MealCardProps> = ({
                                               meal,
                                               mealIndex,
                                               dayIndex,
                                               onUpdateMeal,
                                               onAddIngredient,
                                               onRemoveIngredient,
                                               onCopy
                                           }) => {
    const hasContent = meal.name && meal.name.trim() !== '';
    const hasNutrition = meal.nutritionalValues && (
        meal.nutritionalValues.calories ||
        meal.nutritionalValues.protein ||
        meal.nutritionalValues.fat ||
        meal.nutritionalValues.carbs
    );

    return (
        <div className={`bg-white rounded-lg border-2 transition-all duration-200 shadow-sm ${
            hasContent
                ? 'border-green-200 bg-green-50/30'
                : 'border-gray-200 hover:border-gray-300'
        }`}>
            {/* Header */}
            <div className="p-3 border-b border-gray-100 bg-gray-50/50">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-2">
                        <div className={`p-1.5 rounded-full ${
                            hasContent ? 'bg-green-100' : 'bg-gray-200'
                        }`}>
                            <Utensils className={`h-3 w-3 ${
                                hasContent ? 'text-green-600' : 'text-gray-500'
                            }`} />
                        </div>
                        <div>
                            <div className="text-xs font-medium text-blue-700 mb-1">
                                {getMealTypeLabel(meal.mealType)}
                            </div>
                            <div className="flex items-center gap-1 text-xs text-gray-600">
                                <Clock className="h-3 w-3" />
                                {meal.time}
                            </div>
                        </div>
                    </div>

                    {/* Copy button */}
                    {hasContent && onCopy && (
                        <button
                            onClick={onCopy}
                            className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
                            title="Skopiuj do innych dni"
                        >
                            <Copy className="h-3 w-3" />
                        </button>
                    )}
                </div>

                {/* Quick preview of meal name */}
                {hasContent && (
                    <div className="mt-2">
                        <h5 className="font-medium text-gray-900 text-sm leading-tight">
                            {meal.name}
                        </h5>
                    </div>
                )}

                {/* Nutrition preview */}
                {hasNutrition && (
                    <div className="mt-2">
                        <ColoredNutritionBadges
                            nutritionalValues={meal.nutritionalValues!}
                            size="sm"
                        />
                    </div>
                )}

                {/* Ingredients count */}
                {meal.ingredients && meal.ingredients.length > 0 && (
                    <div className="text-xs text-gray-500 mt-2 flex items-center gap-1">
                        <span className="w-1 h-1 bg-gray-400 rounded-full"></span>
                        {meal.ingredients.length} składników
                    </div>
                )}
            </div>

            {/* Content */}
            <div className="p-4">
                <MealEditor
                    meal={meal}
                    dayIndex={dayIndex}
                    mealIndex={mealIndex}
                    onUpdateMeal={(_dayIdx, mealIdx, meal) => onUpdateMeal(mealIdx, meal)}
                    onAddIngredient={(_dayIdx, mealIdx, ingredient) => onAddIngredient(mealIdx, ingredient)}
                    onRemoveIngredient={(_dayIdx, mealIdx, ingredientIdx) => onRemoveIngredient(mealIdx, ingredientIdx)}
                />
            </div>
        </div>
    );
};

export default MealCard;