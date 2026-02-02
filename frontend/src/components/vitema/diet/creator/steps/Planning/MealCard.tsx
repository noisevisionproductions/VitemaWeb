import React from 'react';
import {Copy, Utensils, Clock, Camera} from 'lucide-react';
import {ParsedMeal} from '../../../../../../types';
import {ParsedProduct} from '../../../../../../types/product';
import {getMealTypeLabel} from '../../../../../../utils/diet/mealTypeUtils';
import MealEditor from '../../MealEditor';

interface MealCardProps {
    meal: ParsedMeal;
    mealIndex: number;
    dayIndex: number;
    onUpdateMeal: (mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (mealIndex: number, ingredientIndex: number) => void;
    onCopy?: () => void;
    enableTemplateFeatures?: boolean;
}

const MealCard: React.FC<MealCardProps> = ({
                                               meal,
                                               mealIndex,
                                               dayIndex,
                                               onUpdateMeal,
                                               onAddIngredient,
                                               onRemoveIngredient,
                                               onCopy,
                                               enableTemplateFeatures = true
                                           }) => {
    const hasContent = meal.name && meal.name.trim() !== '';
   /* const hasNutrition = meal.nutritionalValues && (
        meal.nutritionalValues.calories ||
        meal.nutritionalValues.protein ||
        meal.nutritionalValues.fat ||
        meal.nutritionalValues.carbs
    );*/
    const hasPhotos = meal.photos && meal.photos.length > 0;

    return (
        <div className={`bg-white rounded-lg border-2 transition-all duration-200 shadow-sm ${
            hasContent
                ? 'border-green-200 bg-green-50/30'
                : 'border-gray-200 hover:border-gray-300'
        }`}>
            {/* Header */}
            <div className="p-3 border-b border-gray-100 bg-gray-50/50">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-4">
                        <div className={`p-1.5 rounded-full ${
                            hasContent ? 'bg-green-100' : 'bg-gray-200'
                        }`}>
                            <Utensils className={`h-4 w-4 ${
                                hasContent ? 'text-green-600' : 'text-gray-500'
                            }`}/>
                        </div>
                        <div>
                            <div className="text-s font-medium text-blue-700 mb-1">
                                {getMealTypeLabel(meal.mealType)}
                            </div>
                            <div className="flex items-center gap-1 text-s text-gray-600">
                                <Clock className="h-4 w-4"/>
                                {meal.time}
                            </div>
                        </div>
                    </div>

                    <div className="flex items-center gap-1">
                        {/* Photo indicator */}
                        {hasPhotos && (
                            <div className="flex items-center gap-1 px-2 py-1 bg-blue-100 text-blue-700 rounded-full">
                                <Camera className="h-3 w-3"/>
                                <span className="text-xs font-medium">{meal.photos!.length}</span>
                            </div>
                        )}

                        {/* Copy button */}
                        {hasContent && onCopy && (
                            <button
                                onClick={onCopy}
                                className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
                                title="Skopiuj do innych dni"
                            >
                                <Copy className="h-4 w-4"/>
                            </button>
                        )}
                    </div>
                </div>

                {/* Quick preview of meal name */}
             {/*   {hasContent && (
                    <div className="mt-2">
                        <h5 className="font-medium text-gray-900 text-sm leading-tight">
                            {meal.name}
                        </h5>
                    </div>
                )}*/}

                {/* Nutrition preview */}
          {/*      {hasNutrition && (
                    <div className="mt-2">
                        <ColoredNutritionBadges
                            nutritionalValues={meal.nutritionalValues!}
                            size="sm"
                        />
                    </div>
                )}*/}

                {/* Additional info */}
             {/*   <div className="flex items-center gap-3 mt-2 text-xs text-gray-500">
                    {meal.ingredients && meal.ingredients.length > 0 && (
                        <span className="flex items-center gap-1">
                            <span className="w-1 h-1 bg-gray-400 rounded-full"></span>
                            {meal.ingredients.length} składników
                        </span>
                    )}
                    {meal.instructions && meal.instructions.trim() && (
                        <span className="flex items-center gap-1">
                            <span className="w-1 h-1 bg-blue-400 rounded-full"></span>
                            Ma instrukcje
                        </span>
                    )}
                </div>*/}
            </div>

            {/* Content - Enhanced editor */}
            <div className="p-4">
                <MealEditor
                    meal={meal}
                    dayIndex={dayIndex}
                    mealIndex={mealIndex}
                    onUpdateMeal={(_dayIdx, mealIdx, meal) => onUpdateMeal(mealIdx, meal)}
                    onAddIngredient={(_dayIdx, mealIdx, ingredient) => onAddIngredient(mealIdx, ingredient)}
                    onRemoveIngredient={(_dayIdx, mealIdx, ingredientIdx) => onRemoveIngredient(mealIdx, ingredientIdx)}
                    enableTemplateFeatures={enableTemplateFeatures}
                />
            </div>
        </div>
    );
};

export default MealCard;
