import React from "react";
import {DietTemplate, MealType} from "../../../types/diet";
import {getMealTypeLabel} from "../../../utils/mealTypeUtils";
import {Timestamp} from "firebase/firestore";

interface DietTemplateConfigProps {
    template: DietTemplate;
    onTemplateChange: (template: DietTemplate) => void;
}

interface MealTimeConfig {
    time: string;
    type: MealType;
}

const DEFAULT_MEAL_TIMES: MealTimeConfig[] = [
    {time: '08:00', type: MealType.BREAKFAST},
    {time: '11:00', type: MealType.SECOND_BREAKFAST},
    {time: '14:00', type: MealType.LUNCH},
    {time: '16:30', type: MealType.SNACK},
    {time: '19:00', type: MealType.DINNER},
];

const DietTemplateConfig: React.FC<DietTemplateConfigProps> = ({
                                                                   template,
                                                                   onTemplateChange
                                                               }) => {
    const dateToString = (timestamp: Timestamp) => {
        const date = timestamp.toDate();
        return date.toISOString().split('T')[0];
    };

    const stringToTimestamp = (dateString: string) => {
        const date = new Date(dateString);
        return Timestamp.fromDate(date);
    };

    const handleMealsPerDayChange = (value: number) => {
        const newTemplate = {...template, mealsPerDay: value};
        const mealTimes: { [key: string]: string } = {};
        const defaultConfigs = DEFAULT_MEAL_TIMES.slice(0, value);
        defaultConfigs.forEach((config, index) => {
            const key = `meal_${index}`;
            mealTimes[key] = config.time;
        });
        newTemplate.mealTimes = mealTimes;
        newTemplate.mealTypes = defaultConfigs.map(config => config.type);
        onTemplateChange(newTemplate);
    };

    const handleMealTypeChange = (index: number, newType: MealType) => {
        const newMealTypes = [...template.mealTypes];
        newMealTypes[index] = newType;
        onTemplateChange({
            ...template,
            mealTypes: newMealTypes
        });
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-sm space-y-8">
            <h3 className="text-xl font-semibold mb-4">Konfiguracja szablonu diety</h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-blue-50 p-4 rounded-lg">
                    <label htmlFor="mealsPerDay" className="block text-sm font-medium text-gray-700 mb-1">
                        Liczba posiłków dziennie
                    </label>
                    <select
                        id="mealsPerDay"
                        value={template.mealsPerDay}
                        onChange={(e) => handleMealsPerDayChange(Number(e.target.value))}
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                    >
                        <option value={3}>3 posiłki</option>
                        <option value={4}>4 posiłki</option>
                        <option value={5}>5 posiłków</option>
                    </select>
                </div>

                <div className="bg-green-50 p-4 rounded-lg">
                    <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
                        Data rozpoczęcia diety
                    </label>
                    <input
                        type="date"
                        id="startDate"
                        min={1}
                        max={90}
                        value={dateToString(template.startDate)}
                        onChange={(e) =>
                            onTemplateChange({
                                ...template,
                                startDate: stringToTimestamp(e.target.value)
                            })
                        }
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm"
                    />
                </div>

                <div className="bg-yellow-50 p-4 rounded-lg">
                    <label htmlFor="duration" className="block text-sm font-medium text-gray-700 mb-1">
                        Długość diety (dni)
                    </label>
                    <input
                        type="number"
                        id="duration"
                        min={1}
                        max={90}
                        value={template.duration}
                        onChange={(e) =>
                            onTemplateChange({
                                ...template,
                                duration: Number(e.target.value),
                            })
                        }
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-yellow-500 focus:ring-yellow-500 sm:text-sm"
                    />
                </div>
            </div>

            <div className="space-y-6">
                <h4 className="text-lg font-semibold">Konfiguracja posiłków</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {Array.from({ length: template.mealsPerDay }).map((_, index) => (
                        <div key={index} className="bg-gray-50 p-4 rounded-lg space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Typ posiłku {index + 1}
                                </label>
                                <select
                                    value={template.mealTypes[index]}
                                    onChange={(e) => handleMealTypeChange(index, e.target.value as MealType)}
                                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                >
                                    {Object.values(MealType).map((type) => (
                                        <option key={type} value={type}>
                                            {getMealTypeLabel(type)}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Godzina</label>
                                <input
                                    type="time"
                                    value={template.mealTimes[`meal_${index}`]}
                                    onChange={(e) => {
                                        const newMealTimes = {
                                            ...template.mealTimes,
                                            [`meal_${index}`]: e.target.value,
                                        };
                                        onTemplateChange({
                                            ...template,
                                            mealTimes: newMealTimes,
                                        });
                                    }}
                                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default DietTemplateConfig;