import {DietTemplate, MealType} from '../types/diet';
import {Timestamp} from "firebase/firestore";

export const getMealTypeLabel = (mealType: MealType): string => {
    const labels: Record<MealType, string> = {
        [MealType.BREAKFAST]: 'Śniadanie',
        [MealType.SECOND_BREAKFAST]: 'Drugie śniadanie',
        [MealType.LUNCH]: 'Obiad',
        [MealType.SNACK]: 'Przekąska',
        [MealType.DINNER]: 'Kolacja'
    };
    return labels[mealType];
};

export const defaultTemplate: DietTemplate = {
    mealsPerDay: 5,
    startDate: Timestamp.fromDate(new Date()),
    duration: 7,
    mealTimes: {
        meal_0: '08:00',
        meal_1: '11:00',
        meal_2: '14:00',
        meal_3: '16:30',
        meal_4: '19:00',
    },
    mealTypes: [
        MealType.BREAKFAST,
        MealType.SECOND_BREAKFAST,
        MealType.LUNCH,
        MealType.SNACK,
        MealType.DINNER
    ]
};