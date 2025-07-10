import {DietExcelTemplate, MealType} from "../../../../../types";
import {getMealTypeLabel} from "../../../../../utils/diet/mealTypeUtils";
import React from "react";

interface MealConfigSectionProps {
    mealConfig: DietExcelTemplate;
    onMealTypeChange: (index: number, type: MealType) => void;
    onMealTimeChange: (index: number, time: string) => void;
    onApplyConfig: () => void;
}

const MealConfigSection: React.FC<MealConfigSectionProps> = ({
                                                                 mealConfig,
                                                                 onMealTypeChange,
                                                                 onMealTimeChange,
                                                                 onApplyConfig
                                                             }) => (
    <div className="bg-white p-4 rounded-lg">
        <h3 className="text-lg font-medium mb-4">Konfiguracja posiłków</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Array.from({length: mealConfig.mealsPerDay}).map((_, index) => (
                <div key={index} className="bg-gray-50 p-4 rounded-lg space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Typ posiłku {index + 1}
                        </label>
                        <select
                            value={mealConfig.mealTypes[index]}
                            onChange={(e) => onMealTypeChange(index, e.target.value as MealType)}
                            className="mt-1 w-full rounded-md border-gray-300"
                        >
                            {Object.values(MealType).map((type) => (
                                <option key={type} value={type}>
                                    {getMealTypeLabel(type)}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Godzina
                        </label>
                        <input
                            type="time"
                            value={mealConfig.mealTimes[`meal_${index}`]}
                            onChange={(e) => onMealTimeChange(index, e.target.value)}
                            className="mt-1 w-full rounded-md border-gray-300"
                        />
                    </div>
                </div>
            ))}
        </div>
        <button
            onClick={onApplyConfig}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
            Zastosuj konfigurację do wszystkich dni
        </button>
    </div>
);

export default MealConfigSection;