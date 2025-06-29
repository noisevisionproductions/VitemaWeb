import React, {useState} from "react";
import {ManualDietData, ParsedMeal} from "../../../../../../types";
import {ParsedProduct} from "../../../../../../types/product";
import {toast} from "../../../../../../utils/toast";
import {Expand, Minimize} from "lucide-react";
import ProgressIndicator from "../../../../../common/ProgressIndicator";
import DayPlanningCard from "./DayPlanningCard";

interface MealPlanningStepsProps {
    dietData: ManualDietData;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
}

const MealPlanningStep: React.FC<MealPlanningStepsProps> = ({
                                                                dietData,
                                                                onUpdateMeal,
                                                                onAddIngredient,
                                                                onRemoveIngredient
                                                            }) => {
    const [expandedDays, setExpandedDays] = useState<number[]>([0]);

    const toggleDayExpansion = (dayIndex: number) => {
        setExpandedDays(prev =>
            prev.includes(dayIndex)
                ? prev.filter(i => i !== dayIndex)
                : [...prev, dayIndex]
        );
    };

    const copyMealToOtherDays = (sourceDayIndex: number, sourceMealIndex: number) => {
        const sourceMeal = dietData.days[sourceDayIndex].meals[sourceMealIndex];

        if (!sourceMeal.name || sourceMeal.name.trim() === '') {
            toast.error('Nie można skopiować pustego posiłku');
            return;
        }

        let copiedCount = 0;
        dietData.days.forEach((day, dayIndex) => {
            if (dayIndex !== sourceDayIndex) {
                const targetMeal = day.meals[sourceDayIndex];
                if (!targetMeal.name || targetMeal.name.trim() === '') {
                    onUpdateMeal(dayIndex, sourceMealIndex, {
                        ...sourceMeal,
                        photos: [...(sourceMeal.photos || [])]
                    });
                    copiedCount++;
                }
            }
        });

        if (copiedCount > 0) {
            toast.success(`Posiłek skopiowany do ${copiedCount} dni`);
        } else {
            toast.info('Nie znaleziono pustych miejsc do skopiowania posiłku');
        }
    };

    const expandAllDays = () => {
        setExpandedDays(dietData.days.map((_, index) => index));
    };

    const collapseAllDays = () => {
        setExpandedDays([]);
    };

    // Calculate overall progress
    const totalMeals = dietData.days.length * dietData.mealsPerDay;
    const completedMeals = dietData.days.reduce((sum, day) =>
        sum + day.meals.filter(meal => meal.name && meal.name.trim() !== '').length, 0
    );

    return (
        <div className="space-y-6">

            {/* Progress indicator */}
            <ProgressIndicator
                variant="diet-planning"
                current={completedMeals}
                total={totalMeals}
                label="Postęp planowania posiłków"
            />

            {/* Controls */}
            <div className="bg-white p-4 rounded-xl shadow-sm border flex justify-between items-center">
                <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                        Planowanie posiłków
                    </h3>
                    <p className="text-sm text-gray-600 mt-1">
                        Zaplanuj wszystkie posiłki dla każdego dnia diety • {completedMeals}/{totalMeals} ukończone
                    </p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={expandAllDays}
                        className="flex items-center gap-2 px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        <Expand className="h-4 w-4" />
                        Rozwiń wszystkie
                    </button>
                    <button
                        onClick={collapseAllDays}
                        className="flex items-center gap-2 px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        <Minimize className="h-4 w-4" />
                        Zwiń wszystkie
                    </button>
                </div>
            </div>

            {/* Days list */}
            <div className="space-y-4">
                {dietData.days.map((day, dayIndex) => (
                    <DayPlanningCard
                        key={dayIndex}
                        day={day}
                        dayIndex={dayIndex}
                        mealsPerDay={dietData.mealsPerDay}
                        isExpanded={expandedDays.includes(dayIndex)}
                        onToggleExpand={() => toggleDayExpansion(dayIndex)}
                        onUpdateMeal={(mealIndex, meal) => onUpdateMeal(dayIndex, mealIndex, meal)}
                        onAddIngredient={(mealIndex, ingredient) => onAddIngredient(dayIndex, mealIndex, ingredient)}
                        onRemoveIngredient={(mealIndex, ingredientIndex) => onRemoveIngredient(dayIndex, mealIndex, ingredientIndex)}
                        onCopyMealToOtherDays={(mealIndex) => copyMealToOtherDays(dayIndex, mealIndex)}
                    />
                ))}
            </div>
        </div>
    );
};

export default MealPlanningStep;