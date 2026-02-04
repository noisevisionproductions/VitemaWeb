import React from "react";
import {Calendar, ChevronDown, ChevronUp, Copy, CheckCircle2} from "lucide-react";
import {formatTimestamp} from "../../../../../../utils/dateFormatters";
import {DayData, ParsedMeal} from "../../../../../../types";
import {ParsedProduct} from "../../../../../../types/product";
import MealCard from "./MealCard";

interface DayPlanningCardProps {
    day: DayData;
    dayIndex: number;
    mealsPerDay: number;
    isExpanded: boolean;
    onToggleExpand: () => void;
    onUpdateMeal: (mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (mealIndex: number, ingredientIndex: number) => void;
    onUpdateIngredient: (mealIndex: number, ingredientIndex: number, ingredient: ParsedProduct) => void;
    onCopyMealToOtherDays?: (mealIndex: number) => void;
    onCopyDayClick?: () => void;
    trainerId?: string;
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
                                                             onUpdateIngredient,
                                                             onCopyMealToOtherDays,
                                                             onCopyDayClick,
                                                             trainerId
                                                         }) => {
    const completedMeals = day.meals.filter(meal => meal.name && meal.name.trim() !== '').length;
    const completionPercentage = (completedMeals / mealsPerDay) * 100;
    const isFullyCompleted = completionPercentage === 100;

    return (
        <div className={`
            rounded-xl border-2 transition-all duration-300 overflow-hidden
            ${isExpanded
            ? 'border-primary/20 shadow-md bg-white'
            : 'border-gray-200 bg-gray-50 hover:border-gray-300'
        }
        `}>
            <div
                className={`
                    p-4 cursor-pointer group select-none
                    flex flex-col sm:flex-row justify-between sm:items-center gap-4
                    ${isExpanded ? 'bg-white border-b border-gray-100' : 'bg-transparent'}
                `}
                onClick={onToggleExpand}
            >
                <div className="flex items-center gap-4">
                    <div className={`
                        shrink-0 rounded-xl p-3 transition-colors duration-300
                        ${isFullyCompleted
                        ? 'bg-green-100 text-green-600'
                        : 'bg-primary/10 text-primary group-hover:bg-primary group-hover:text-white'
                    }
                    `}>
                        {isFullyCompleted ? <CheckCircle2 className="h-5 w-5"/> : <Calendar className="h-5 w-5"/>}
                    </div>

                    <div>
                        <h4 className={`font-bold text-lg leading-tight transition-colors ${
                            isExpanded ? 'text-primary' : 'text-gray-700 group-hover:text-gray-900'
                        }`}>
                            Dzień {dayIndex + 1}
                        </h4>
                        <div className="flex items-center gap-2 mt-1">
                            <p className="text-sm text-gray-500 font-medium">
                                {formatTimestamp(day.date)}
                            </p>

                            {/* Desktop Progress Text */}
                            <span className="hidden sm:inline-block text-xs text-gray-400">•</span>
                            <span className={`text-xs font-medium ${
                                isFullyCompleted ? 'text-green-600' : 'text-gray-500'
                            }`}>
                                {completedMeals} / {mealsPerDay} posiłków
                            </span>
                        </div>
                    </div>
                </div>

                <div className="flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto">

                    <div className="flex-1 sm:flex-none sm:w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
                        <div
                            className={`h-full transition-all duration-500 ${
                                isFullyCompleted
                                    ? 'bg-green-500'
                                    : 'bg-primary'
                            }`}
                            style={{width: `${completionPercentage}%`}}
                        />
                    </div>

                    <div className="flex items-center gap-2 pl-2 border-l border-gray-200 ml-2">
                        {onCopyDayClick && (
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onCopyDayClick();
                                }}
                                className="flex items-center gap-2 px-3 py-2 text-gray-400 hover:text-primary hover:bg-primary/10 rounded-lg transition-colors group"
                            >
                                <Copy className="h-5 w-5"/>
                                <span className="text-sm font-medium">Skopiuj dzień</span>
                            </button>
                        )}

                        <div className={`
                            flex items-center gap-2 px-3 py-1.5 rounded-lg transition-all duration-200
                            ${isExpanded
                            ? 'bg-gray-100 text-gray-700'
                            : 'bg-white border border-gray-200 text-gray-600 shadow-sm group-hover:border-primary/50 group-hover:text-primary'
                        }
                        `}>
                            <span className="text-xs font-semibold uppercase tracking-wide hidden sm:inline-block">
                                {isExpanded ? 'Zwiń' : 'Rozwiń'}
                            </span>
                            {isExpanded ? (
                                <ChevronUp className="h-4 w-4"/>
                            ) : (
                                <ChevronDown className="h-4 w-4"/>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Content - meals */}
            {isExpanded && (
                <div className="p-4 sm:p-6 bg-white animate-in slide-in-from-top-2 duration-200">
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
                                onUpdateIngredient={onUpdateIngredient}
                                onCopy={onCopyMealToOtherDays ? () => onCopyMealToOtherDays(mealIndex) : undefined}
                                trainerId={trainerId}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DayPlanningCard;