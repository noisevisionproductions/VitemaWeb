import React from "react";
import {ParsedDietData} from "../../../types/diet";
import {getMealTypeLabel} from "../../../utils/mealTypeUtils";
import {formatDate} from "../../../utils/dateFormatters";

interface DietPreviewProps {
    parsedData: ParsedDietData;
    onConfirm: () => Promise<void>;
    onCancel: () => void;
    selectedUserEmail: string;
}

const DietPreview: React.FC<DietPreviewProps> = ({
                                                     parsedData,
                                                     onConfirm,
                                                     onCancel,
                                                     selectedUserEmail
                                                 }) => {
    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">Podgląd diety przed zapisem</h2>
                <div className="text-gray-600">
                    Użytkownik: {selectedUserEmail}
                </div>
            </div>

            {/* Przegląd dni i posiłków */}
            <div className="space-y-6">
                {parsedData.days.map((day, dayIndex) => (
                    <div key={dayIndex} className="border rounded-lg p-4">
                        <h3 className="text-lg font-semibold mb-4">
                            Dzień {dayIndex + 1} - {formatDate(day.date)}
                        </h3>
                        <div className="space-y-4">
                            {day.meals.map((meal, mealIndex) => (
                                <div key={mealIndex} className="bg-gray-50 p-4 rounded-lg">
                                    <div className="font-medium">
                                        {getMealTypeLabel(meal.mealType)} - {meal.time}
                                    </div>
                                    <div className="mt-2">
                                        <div className="font-medium text-gray-900">{meal.name}</div>
                                        <div className="text-sm text-gray-600 mt-1">{meal.instructions}</div>
                                        <div className="mt-2 space-y-1">
                                            <div className="text-sm font-medium">Składniki:</div>
                                            <div className="text-sm text-gray-600">
                                                {meal.ingredients.join(', ')}
                                            </div>
                                        </div>
                                        {meal.nutritionalValues && (
                                            <div className="mt-2 text-sm text-gray-600">
                                                Wartości odżywcze: {meal.nutritionalValues.calories} kcal,{' '}
                                                B: {meal.nutritionalValues.protein}g,{' '}
                                                T: {meal.nutritionalValues.fat}g,{' '}
                                                W: {meal.nutritionalValues.carbs}g
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {/* Lista zakupów */}
            <div className="border rounded-lg p-4">
                <h3 className="text-lg font-semibold mb-4">Lista zakupów</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {parsedData.shoppingList.map((item, index) => (
                        <div key={index} className="text-gray-600">
                            • {item}
                        </div>
                    ))}
                </div>
            </div>

            {/* Przyciski akcji */}
            <div className="flex justify-end space-x-4 pt-4">
                <button
                    onClick={onCancel}
                    className="px-4 py-2 border rounded-lg hover:bg-gray-50"
                >
                    Wróć do edycji
                </button>
                <button
                    onClick={onConfirm}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                    Zapisz dietę
                </button>
            </div>
        </div>
    );
};

export default DietPreview;