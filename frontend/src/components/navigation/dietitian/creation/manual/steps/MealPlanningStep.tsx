import React, {useState} from "react";
import {ManualDietData, ParsedMeal} from "../../../../../../types";
import {ParsedProduct} from "../../../../../../types/product";
import {toast} from "../../../../../../utils/toast";
import {ChevronDown, ChevronUp} from "lucide-react";
import DayPlanningCard from "./DayPlanningCard";
import {DietTemplate} from "../../../../../../types/DietTemplate";
import TemplateInfoBanner from "../templates/TemplateInfoBanner";

interface MealPlanningStepsProps {
    dietData: ManualDietData;
    selectedTemplate?: DietTemplate | null;
    onRemoveTemplate?: () => void;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
}


const MealPlanningStep: React.FC<MealPlanningStepsProps> = ({
                                                                dietData,
                                                                selectedTemplate,
                                                                onRemoveTemplate,
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
                const targetMeal = day.meals[sourceMealIndex];
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

    const toggleAllDays = () => {
        const allExpanded = expandedDays.length === dietData.days.length;
        if (allExpanded) {
            setExpandedDays([]);
        } else {
            setExpandedDays(dietData.days.map((_, index) => index));
        }
    };

    // Calculate overall progress
    const totalMeals = dietData.days.length * dietData.mealsPerDay;
    const completedMeals = dietData.days.reduce((sum, day) =>
        sum + day.meals.filter(meal => meal.name && meal.name.trim() !== '').length, 0
    );

    const allExpanded = expandedDays.length === dietData.days.length;
    const noneExpanded = expandedDays.length === 0;

    return (
        <div className="space-y-6">
            {/* Banner szablonu */}
            {selectedTemplate && onRemoveTemplate && (
                <TemplateInfoBanner
                    template={selectedTemplate}
                    onRemoveTemplate={onRemoveTemplate}
                />
            )}

            {/* Główny nagłówek z postępem */}
            <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
                {/* Sekcja postępu i kontrolek */}
                <div className="p-6">
                    <div className="flex items-center justify-between">
                        {/* Lewa strona-postęp */}
                        <div className="flex items-center gap-4">
                            <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                                completedMeals === totalMeals
                                    ? 'bg-green-500'
                                    : 'bg-primary'
                            }`}>
                                <span className="text-white font-bold">
                                    {Math.round((completedMeals / totalMeals) * 100)}%
                                </span>
                            </div>
                            <div>
                                <h3 className="text-xl font-semibold text-gray-900">
                                    Postęp planowania posiłków
                                </h3>
                                <p className="text-gray-600">
                                    {completedMeals}/{totalMeals} posiłków ukończone
                                </p>
                            </div>
                        </div>

                        {/* Prawa strona-kontrolki */}
                        <div className="flex items-center gap-2">
                            {completedMeals === totalMeals && (
                                <div
                                    className="flex items-center gap-2 px-4 py-2 bg-green-100 text-green-800 rounded-full mr-4">
                                    <span className="text-lg">✅</span>
                                    <span className="font-medium text-sm">Ukończono!</span>
                                </div>
                            )}

                            {/* Przycisk rozwiń/zwiń wszystkie */}
                            <button
                                onClick={toggleAllDays}
                                className={`flex items-center gap-2 px-4 py-2 text-sm rounded-lg transition-colors ${
                                    allExpanded
                                        ? 'bg-primary text-white hover:bg-primary-dark'
                                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                }`}
                            >
                                {allExpanded ? (
                                    <>
                                        <ChevronUp className="h-4 w-4"/>
                                        <span className="hidden sm:inline">Zwiń wszystkie dni</span>
                                    </>
                                ) : (
                                    <>
                                        <ChevronDown className="h-4 w-4"/>
                                        <span className="hidden sm:inline">
                                            {noneExpanded ? 'Rozwiń wszystkie dni' : `Rozwiń pozostałe dni (${dietData.days.length - expandedDays.length})`}
                                        </span>
                                    </>
                                )}
                            </button>
                        </div>
                    </div>

                    {/* Progress bar */}
                    <div className="w-full bg-gray-200 rounded-full h-2 mt-4">
                        <div
                            className={`h-2 rounded-full transition-all duration-500 ${
                                completedMeals === totalMeals
                                    ? 'bg-gradient-to-r from-green-500 to-emerald-500'
                                    : 'bg-gradient-to-r from-primary to-primary-light'
                            }`}
                            style={{width: `${(completedMeals / totalMeals) * 100}%`}}
                        />
                    </div>

                    {/* Statystyki-tylko, jeśli są większe dane */}
                    {dietData.days.length > 3 && (
                        <div className="grid grid-cols-4 gap-4 mt-4 pt-4 border-t border-gray-100">
                            <div className="text-center">
                                <div className="text-lg font-bold text-gray-900">{dietData.days.length}</div>
                                <div className="text-xs text-gray-600 uppercase tracking-wide">Dni</div>
                            </div>
                            <div className="text-center">
                                <div className="text-lg font-bold text-gray-900">{totalMeals}</div>
                                <div className="text-xs text-gray-600 uppercase tracking-wide">Posiłków</div>
                            </div>
                            <div className="text-center">
                                <div className="text-lg font-bold text-primary">{completedMeals}</div>
                                <div className="text-xs text-gray-600 uppercase tracking-wide">Gotowe</div>
                            </div>
                            <div className="text-center">
                                <div className="text-lg font-bold text-gray-400">{totalMeals - completedMeals}</div>
                                <div className="text-xs text-gray-600 uppercase tracking-wide">Pozostało</div>
                            </div>
                        </div>
                    )}
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