import {MealType} from "../../types";

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