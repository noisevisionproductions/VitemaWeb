import React from 'react';
import { Calendar, ChevronDown, ChevronUp } from 'lucide-react';
import { formatTimestamp } from '../../../../../../utils/dateFormatters';
import { DayData, ParsedMeal } from '../../../../../../types';
import { ParsedProduct } from '../../../../../../types/product';
import MealCard from './MealCard';

interface DayPlanningCardProps {
    day: DayData;
    dayIndex: number;
    mealsPerDay: number;
    isExpanded: boolean;
    onToggleExpand: () => void;
    onUpdateMeal: (mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (mealIndex: number, ingredientIndex: number) => void;
    onCopyMealToOtherDays?: (mealIndex: number) => void;
}

const DayPlanningCard: React.FC<DayPlanningCardProps> = ({
                                                             day,
                                                             dayIndex,
                                                             mealsPerDay,
                                                             isExpanded,
                                                             onToggleExpand,
                                                             onUpdateMeal,
                                                             onAddIngredient,
                                                             onRemoveIngredient,
                                                             onCopyMealToOtherDays
                                                         }) => {
    const completedMeals = day.meals.filter(meal => meal.name && meal.name.trim() !== '').length;
    const completionPercentage = (completedMeals / mealsPerDay) * 100;

    return (
        <div className="bg-white rounded-xl shadow-sm border-2 border-gray-100 overflow-hidden hover:shadow-md transition-all duration-300">
            {/* Header */}
            <div
                className="p-4 cursor-pointer hover:bg-gray-50 transition-colors border-b border-gray-100"
                onClick={onToggleExpand}
            >
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-3">
                            <div className={`rounded-full p-2 ${
                                completionPercentage === 100
                                    ? 'bg-green-500'
                                    : 'bg-primary'
                            }`}>
                                <Calendar className="h-4 w-4 text-white" />
                            </div>
                            <div>
                                <h4 className="font-semibold text-gray-900">
                                    Dzień {dayIndex + 1}
                                </h4>
                                <p className="text-sm text-gray-500">
                                    {formatTimestamp(day.date)}
                                </p>
                            </div>
                        </div>

                        {/* Progress bar */}
                        <div className="hidden sm:flex items-center gap-3">
                            <div className="w-32 bg-gray-200 rounded-full h-2.5">
                                <div
                                    className={`h-2.5 rounded-full transition-all duration-500 ${
                                        completionPercentage === 100
                                            ? 'bg-gradient-to-r from-green-500 to-emerald-500'
                                            : 'bg-gradient-to-r from-blue-400 to-blue-500'
                                    }`}
                                    style={{ width: `${completionPercentage}%` }}
                                />
                            </div>
                            <span className="text-sm font-medium text-gray-600 min-w-max">
                                {completedMeals}/{mealsPerDay}
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        {completionPercentage === 100 && (
                            <span className="px-3 py-1 bg-green-100 text-green-800 text-xs rounded-full font-medium flex items-center gap-1">
                                ✅ Ukończono
                            </span>
                        )}
                        {completionPercentage > 0 && completionPercentage < 100 && (
                            <span className="px-3 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-medium">
                                {Math.round(completionPercentage)}% gotowe
                            </span>
                        )}
                        {isExpanded ? (
                            <ChevronUp className="h-5 w-5 text-gray-400" />
                        ) : (
                            <ChevronDown className="h-5 w-5 text-gray-400" />
                        )}
                    </div>
                </div>

                {/* Mobile progress */}
                <div className="sm:hidden mt-3">
                    <div className="flex justify-between items-center mb-2">
                        <span className="text-xs text-gray-600 font-medium">Postęp dnia</span>
                        <span className="text-xs font-semibold text-gray-700">
                            {completedMeals}/{mealsPerDay} ({Math.round(completionPercentage)}%)
                        </span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2.5">
                        <div
                            className={`h-2.5 rounded-full transition-all duration-500 ${
                                completionPercentage === 100
                                    ? 'bg-gradient-to-r from-green-500 to-emerald-500'
                                    : 'bg-gradient-to-r from-blue-400 to-blue-500'
                            }`}
                            style={{ width: `${completionPercentage}%` }}
                        />
                    </div>
                </div>
            </div>

            {/* Content - meals */}
            {isExpanded && (
                <div className="p-6 bg-gray-50/30">
                    {/* Meals grid - responsywny layout */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
                        {day.meals.map((meal, mealIndex) => (
                            <MealCard
                                key={`${dayIndex}-${mealIndex}`}
                                meal={meal}
                                mealIndex={mealIndex}
                                dayIndex={dayIndex}
                                onUpdateMeal={onUpdateMeal}
                                onAddIngredient={onAddIngredient}
                                onRemoveIngredient={onRemoveIngredient}
                                onCopy={onCopyMealToOtherDays ? () => onCopyMealToOtherDays(mealIndex) : undefined}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DayPlanningCard;