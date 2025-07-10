import React from "react";
import {getMealTypeLabel} from "../../../../utils/diet/mealTypeUtils";
import {DietExcelTemplate, MealType} from "../../../../types";
import {stringToTimestamp, toISODate} from "../../../../utils/dateFormatters";
import {Calendar, Clock} from "react-feather";
import {ClipboardList} from "lucide-react";

interface DietTemplateConfigProps {
    template: DietExcelTemplate;
    onTemplateChange: (template: DietExcelTemplate) => void;
    refs?: {
        mealsPerDayRef?: React.RefObject<HTMLDivElement>;
        dateConfigRef?: React.RefObject<HTMLDivElement>;
        mealsConfigRef?: React.RefObject<HTMLDivElement>;
    };
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
                                                                   onTemplateChange,
                                                                   refs
                                                               }) => {
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
        <div className="bg-white p-6 rounded-lg shadow-md space-y-8">
            <div className="flex items-center gap-3 mb-4">
                <ClipboardList className="h-5 w-5 text-primary"/>
                <h3 className="font-medium text-lg">
                    Konfiguracja szablonu diety
                </h3>
            </div>

            {/* Meals per day selection */}
            <div ref={refs?.mealsPerDayRef} className="space-y-4">
                <label className="block text-sm font-medium text-gray-700">
                    Liczba posiłków dziennie
                </label>
                <div className="flex gap-4">
                    {[3, 4, 5].map((meals) => (
                        <button
                            key={meals}
                            onClick={() => handleMealsPerDayChange(meals)}
                            className={`
                                px-6 py-3 rounded-lg font-medium transition-all
                                ${template.mealsPerDay === meals
                                ? 'bg-blue-600 text-white shadow-md transform scale-105'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }
                            `}
                        >
                            {meals}
                        </button>
                    ))}
                </div>
            </div>

            {/* Date and Duration Configuration */}
            <div ref={refs?.dateConfigRef} className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-gradient-to-br from-green-50 to-blue-50 p-6 rounded-xl shadow-sm">
                    <div className="flex items-center gap-2 mb-4">
                        <Calendar className="w-5 h-5 text-green-600"/>
                        <label htmlFor="startDate" className="text-sm font-medium text-gray-700">
                            Data rozpoczęcia diety
                        </label>
                    </div>
                    <input
                        type="date"
                        id="startDate"
                        value={toISODate(template.startDate)}
                        onChange={(e) => onTemplateChange({
                            ...template,
                            startDate: stringToTimestamp(e.target.value)
                        })}
                        className="w-full px-4 py-2 rounded-lg border-2 border-green-100 focus:border-green-400 focus:ring-2 focus:ring-green-200 outline-none transition-all date-input"
                    />
                </div>

                <div className="bg-gradient-to-br from-yellow-50 to-orange-50 p-6 rounded-xl shadow-sm">
                    <div className="flex items-center gap-2 mb-4">
                        <Clock className="w-5 h-5 text-yellow-600"/>
                        <label htmlFor="duration" className="text-sm font-medium text-gray-700">
                            Długość diety (dni)
                        </label>
                    </div>
                    <input
                        type="number"
                        id="duration"
                        min={1}
                        max={90}
                        value={template.duration}
                        onChange={(e) => onTemplateChange({
                            ...template,
                            duration: Number(e.target.value)
                        })}
                        className="w-full px-4 py-2 rounded-lg border-2 border-yellow-100 focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all"
                        name="mealsPerDay"
                    />
                </div>
            </div>

            {/* Meal Configuration */}
            <div ref={refs?.mealsConfigRef} className="space-y-6">
                <h4 className="text-xl font-semibold text-gray-800">Konfiguracja posiłków</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {Array.from({length: template.mealsPerDay}).map((_, index) => (
                        <div key={index}
                             className="bg-gradient-to-br from-gray-50 to-blue-50 p-6 rounded-xl shadow-sm transform transition-all hover:scale-[1.02]">
                            <div className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Posiłek {index + 1}
                                    </label>
                                    <select
                                        value={template.mealTypes[index]}
                                        onChange={(e) => handleMealTypeChange(index, e.target.value as MealType)}
                                        className="w-full px-4 py-2 rounded-lg border-2 border-blue-100 focus:border-blue-400 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                                    >
                                        {Object.values(MealType).map((type) => (
                                            <option key={type} value={type}>
                                                {getMealTypeLabel(type)}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Godzina posiłku
                                    </label>
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
                                        className="w-full px-4 py-2 rounded-lg border-2 border-blue-100 focus:border-blue-400 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                                    />
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default DietTemplateConfig;