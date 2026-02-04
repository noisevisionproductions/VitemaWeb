import {useState, useCallback} from 'react';
import {DietTemplateService} from "../../../services/diet/manual/DietTemplateService";
import {ManualDietData, MealType, ParsedMeal} from "../../../types";
import {ParsedProduct} from "../../../types/product";
import {Timestamp} from 'firebase/firestore';
import {toast} from "../../../utils/toast";
import {DietTemplate} from "../../../types/DietTemplate";

export const useTemplateLoader = () => {
    const [loading, setLoading] = useState(false);

    const loadTemplateIntoDiet = useCallback(async (
        template: DietTemplate,
        userId: string,
        startDate: string
    ): Promise<ManualDietData> => {
        setLoading(true);
        try {
            await DietTemplateService.incrementUsage(template.id);

            const days = template.days.map((templateDay, dayIndex) => {
                const currentDate = new Date(startDate);
                currentDate.setDate(currentDate.getDate() + dayIndex);

                const meals: ParsedMeal[] = templateDay.meals.map((templateMeal) => ({
                    name: templateMeal.name,
                    instructions: templateMeal.instructions || '',
                    ingredients: templateMeal.ingredients.map((ing): ParsedProduct => ({
                        id: `ingredient-${Date.now()}-${Math.random()}`,
                        name: ing.name,
                        quantity: ing.quantity,
                        unit: ing.unit,
                        original: ing.original,
                        categoryId: ing.categoryId,
                        hasCustomUnit: ing.hasCustomUnit
                    })),
                    mealType: templateMeal.mealType as MealType,
                    time: templateMeal.time,
                    photos: [...(templateMeal.photos || [])],
                    nutritionalValues: templateMeal.nutritionalValues ? {
                        calories: templateMeal.nutritionalValues.calories,
                        protein: templateMeal.nutritionalValues.protein,
                        fat: templateMeal.nutritionalValues.fat,
                        carbs: templateMeal.nutritionalValues.carbs
                    } : undefined
                }));

                return {
                    date: Timestamp.fromDate(currentDate),
                    meals
                };
            });

            const dietData: ManualDietData = {
                userId,
                days,
                mealsPerDay: template.mealsPerDay,
                startDate,
                duration: template.duration,
                mealTimes: {...template.mealTimes},
                mealTypes: template.mealTypes.map(type => type as MealType)
            };

            toast.success(`Załadowano szablon "${template.name}"`);
            return dietData;

        } catch (error) {
            console.error('Error loading template:', error);
            toast.error('Nie udało się załadować szablonu');
            throw error;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        loadTemplateIntoDiet,
        loading
    };
};