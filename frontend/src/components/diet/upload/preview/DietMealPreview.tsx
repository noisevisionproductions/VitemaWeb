import React from "react";
import {ParsedMeal} from "../../../../types";
import {getMealTypeLabel} from "../../../../utils/mealTypeUtils";
import {Clock} from "lucide-react";

interface DietMealPreviewProps {
    meal: ParsedMeal;
    mealIndex: number;
}

const DietMealPreview: React.FC<DietMealPreviewProps> = ({ meal }) => {
    return (
        <div className="bg-gray-50 p-4 rounded-lg border border-gray-100">
            <div className="flex justify-between items-center">
                <div className="font-medium text-blue-700">
                    {getMealTypeLabel(meal.mealType)}
                </div>
                <div className="flex items-center gap-1 text-gray-600 bg-blue-50 px-2 py-1 rounded-md">
                    <Clock className="h-4 w-4" />
                    <span>{meal.time || "Brak godziny"}</span>
                </div>
            </div>
            <div className="mt-2">
                <div className="font-medium text-gray-900 text-lg">{meal.name}</div>
                {meal.instructions && (
                    <div className="text-sm text-gray-600 mt-2">{meal.instructions}</div>
                )}

                {meal.nutritionalValues && (
                    <div className="mt-3 grid grid-cols-4 gap-2 text-xs">
                        <div className="bg-green-50 p-2 rounded text-center">
                            <div className="font-bold text-green-700">{meal.nutritionalValues.calories}</div>
                            <div className="text-gray-600">kcal</div>
                        </div>
                        <div className="bg-blue-50 p-2 rounded text-center">
                            <div className="font-bold text-blue-700">{meal.nutritionalValues.protein}g</div>
                            <div className="text-gray-600">białko</div>
                        </div>
                        <div className="bg-red-50 p-2 rounded text-center">
                            <div className="font-bold text-red-700">{meal.nutritionalValues.fat}g</div>
                            <div className="text-gray-600">tłuszcz</div>
                        </div>
                        <div className="bg-yellow-50 p-2 rounded text-center">
                            <div className="font-bold text-yellow-700">{meal.nutritionalValues.carbs}g</div>
                            <div className="text-gray-600">węglowodany</div>
                        </div>
                    </div>
                )}
{/*
                {meal.ingredients && meal.ingredients.length > 0 && (
                    <div className="mt-3 bg-gray-100 p-2 rounded">
                        <div className="text-sm font-medium mb-1">Składniki:</div>
                        <div className="grid grid-cols-2 gap-x-2 text-sm">
                            {meal.ingredients.map((ingredient, idx) => (
                                <div key={idx} className="flex items-start">
                                    <span className="text-green-600 mr-1">•</span>
                                    <span>{(ingredient.name || ingredient.original || '')}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}*/}
            </div>
        </div>
    );
};

export default DietMealPreview;