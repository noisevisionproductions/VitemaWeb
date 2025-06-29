import {MealType} from "./meal";

export interface MealTimeConfig {
    time: string;
    type: MealType;
}

export interface DietDefaults {
    mealTimes: Record<string, string>;
    mealTypes: MealType[];
}

export const DEFAULT_MEAL_CONFIGURATIONS: Record<number, MealTimeConfig[]> = {
    3: [
        {time: '08:00', type: MealType.BREAKFAST},
        {time: '14:00', type: MealType.LUNCH},
        {time: '19:00', type: MealType.DINNER},
    ],
    4: [
        {time: '08:00', type: MealType.BREAKFAST},
        {time: '11:00', type: MealType.SECOND_BREAKFAST},
        {time: '14:00', type: MealType.LUNCH},
        {time: '19:00', type: MealType.DINNER},
    ],
    5: [
        {time: '08:00', type: MealType.BREAKFAST},
        {time: '11:00', type: MealType.SECOND_BREAKFAST},
        {time: '14:00', type: MealType.LUNCH},
        {time: '16:30', type: MealType.SNACK},
        {time: '19:00', type: MealType.DINNER},
    ],
    6: [
        {time: '08:00', type: MealType.BREAKFAST},
        {time: '10:30', type: MealType.SECOND_BREAKFAST},
        {time: '13:00', type: MealType.LUNCH},
        {time: '16:00', type: MealType.SNACK},
        {time: '19:00', type: MealType.DINNER},
        {time: '21:00', type: MealType.SNACK},
    ]
};

export const generateDietDefaults = (mealsPerDay: number): DietDefaults => {
    const configs = DEFAULT_MEAL_CONFIGURATIONS[mealsPerDay] || DEFAULT_MEAL_CONFIGURATIONS[5];

    const mealTimes: Record<string, string> = {};
    const mealTypes: MealType[] = [];

    configs.forEach((config, index) => {
        mealTimes[`meal_${index}`] = config.time;
        mealTypes.push(config.type);
    });

    return {mealTimes, mealTypes};
};

export const DEFAULT_DIET_CONFIG = {
    mealsPerDay: 5,
    duration: 7,
    startDate: new Date().toISOString().split('T')[0],
    ...generateDietDefaults(5)
};