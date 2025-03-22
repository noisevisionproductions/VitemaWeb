import { useCallback } from 'react';
import { MealType, DietTemplate } from '../../types';

interface MealTimeConfig {
    time: string;
    type: MealType;
}

export const DEFAULT_MEAL_TIMES: MealTimeConfig[] = [
    {time: '08:00', type: MealType.BREAKFAST},
    {time: '11:00', type: MealType.SECOND_BREAKFAST},
    {time: '14:00', type: MealType.LUNCH},
    {time: '16:30', type: MealType.SNACK},
    {time: '19:00', type: MealType.DINNER},
];

interface UseMealConfigurationReturn {
    handleMealsPerDayChange: (value: number) => void;
    handleMealTypeChange: (index: number, newType: MealType) => void;
    handleMealTimeChange: (index: number, newTime: string) => void;
    applyMealConfiguration: (days: any[]) => any[];
}

export const useMealConfiguration = (
    config: DietTemplate,
    onConfigChange: (newConfig: DietTemplate) => void
): UseMealConfigurationReturn => {
    const handleMealsPerDayChange = useCallback((value: number) => {
        const mealTimes: { [key: string]: string } = {};
        const defaultConfigs = DEFAULT_MEAL_TIMES.slice(0, value);
        defaultConfigs.forEach((config, index) => {
            const key = `meal_${index}`;
            mealTimes[key] = config.time;
        });

        onConfigChange({
            ...config,
            mealsPerDay: value,
            mealTimes,
            mealTypes: defaultConfigs.map(config => config.type)
        });
    }, [config, onConfigChange]);

    const handleMealTypeChange = useCallback((index: number, newType: MealType) => {
        const newMealTypes = [...config.mealTypes];
        newMealTypes[index] = newType;
        onConfigChange({
            ...config,
            mealTypes: newMealTypes
        });
    }, [config, onConfigChange]);

    const handleMealTimeChange = useCallback((index: number, newTime: string) => {
        const newMealTimes = {
            ...config.mealTimes,
            [`meal_${index}`]: newTime,
        };
        onConfigChange({
            ...config,
            mealTimes: newMealTimes,
        });
    }, [config, onConfigChange]);

    // Funkcja do aplikowania konfiguracji do wszystkich dni
    const applyMealConfiguration = useCallback((days: { meals: any[]; }[]) => {
        return days.map((day: { meals: any[]; }) => ({
            ...day,
            meals: day.meals.map((meal: any, index: number) => ({
                ...meal,
                time: config.mealTimes[`meal_${index}`],
                mealType: config.mealTypes[index]
            }))
        }));
    }, [config]);

    return {
        handleMealsPerDayChange,
        handleMealTypeChange,
        handleMealTimeChange,
        applyMealConfiguration
    };
};