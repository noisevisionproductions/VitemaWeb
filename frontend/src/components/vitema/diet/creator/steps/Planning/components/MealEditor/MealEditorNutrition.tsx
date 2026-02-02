import React from 'react';
import {NutritionalValues} from '../../../../../../../../types';

interface MealEditorNutritionProps {
    nutritionalValues?: NutritionalValues;
    onUpdate: (field: keyof NutritionalValues, value: string) => void;
}

const MealEditorNutrition: React.FC<MealEditorNutritionProps> = ({
                                                                      nutritionalValues,
                                                                      onUpdate
                                                                  }) => {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
                Wartości odżywcze (opcjonalnie)
            </label>

            <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
                <div>
                    <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                        <span className="w-2 h-2 bg-nutrition-calories rounded-full"></span>
                        Kalorie
                    </label>
                    <input
                        type="number"
                        step="1"
                        min="0"
                        value={nutritionalValues?.calories || ''}
                        onChange={(e) => onUpdate('calories', e.target.value)}
                        placeholder="kcal"
                        className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-calories focus:border-nutrition-calories"
                    />
                </div>
                <div>
                    <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                        <span className="w-2 h-2 bg-nutrition-protein rounded-full"></span>
                        Białko
                    </label>
                    <input
                        type="number"
                        step="1"
                        min="0"
                        value={nutritionalValues?.protein || ''}
                        onChange={(e) => onUpdate('protein', e.target.value)}
                        placeholder="g"
                        className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-protein focus:border-nutrition-protein"
                    />
                </div>
                <div>
                    <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                        <span className="w-2 h-2 bg-nutrition-fats rounded-full"></span>
                        Tłuszcze
                    </label>
                    <input
                        type="number"
                        step="1"
                        min="0"
                        value={nutritionalValues?.fat || ''}
                        onChange={(e) => onUpdate('fat', e.target.value)}
                        placeholder="g"
                        className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-fats focus:border-nutrition-fats"
                    />
                </div>
                <div>
                    <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                        <span className="w-2 h-2 bg-nutrition-carbs rounded-full"></span>
                        Węglowodany
                    </label>
                    <input
                        type="number"
                        step="1"
                        min="0"
                        value={nutritionalValues?.carbs || ''}
                        onChange={(e) => onUpdate('carbs', e.target.value)}
                        placeholder="g"
                        className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-carbs focus:border-nutrition-carbs"
                    />
                </div>
            </div>
            
            <p className="mt-2 text-xs text-gray-500">
                Wartości odżywcze są opcjonalne. Możesz je wypełnić ręcznie lub pozostawić puste.
            </p>
        </div>
    );
};

export default MealEditorNutrition;
