import React, {useMemo, useState} from "react";
import {ManualDietData, ParsedMeal} from "../../../../../../types";
import {ParsedProduct} from "../../../../../../types/product";
import {toast} from "../../../../../../utils/toast";
import {
    ChevronDown,
    ChevronUp,
    User as UserIcon,
    CheckCircle,
    Ruler,
    Scale,
    Activity,
    Calendar,
    Copy,
    X,
} from "lucide-react";
import DayPlanningCard from "./DayPlanningCard";
import {DietTemplate} from "../../../../../../types/DietTemplate";
import TemplateInfoBanner from "../../components/TemplateInfoBanner";
import {User} from "../../../../../../types/user";
import {useMeasurements} from "../../../../../../hooks/useMeasurements";
import {BodyMeasurements} from "../../../../../../types/measurements";
import {calculateAge, formatTimestamp, timestampToDate} from "../../../../../../utils/dateFormatters";

interface MealPlanningStepsProps {
    dietData: ManualDietData;
    selectedTemplate?: DietTemplate | null;
    selectedUser: User | null;
    onRemoveTemplate?: () => void;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
    onUpdateIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number, ingredient: ParsedProduct) => void;
    trainerId?: string;
}

const MealPlanningStep: React.FC<MealPlanningStepsProps> = ({
                                                                dietData,
                                                                selectedTemplate,
                                                                selectedUser,
                                                                onRemoveTemplate,
                                                                onUpdateMeal,
                                                                onAddIngredient,
                                                                onRemoveIngredient,
                                                                onUpdateIngredient,
                                                                trainerId
                                                            }) => {
    const [expandedDays, setExpandedDays] = useState<number[]>([0]);
    const [copyDaySource, setCopyDaySource] = useState<number | null>(null);
    const [copyDayTargets, setCopyDayTargets] = useState<number[]>([]);

    const {measurements} = useMeasurements(selectedUser?.id || "");

    const sortedMeasurements = useMemo(() => {
        if (!measurements || measurements.length === 0) return [];
        return [...measurements].sort((a, b) => {
            const dateA = timestampToDate(a.date)?.getTime() || 0;
            const dateB = timestampToDate(b.date)?.getTime() || 0;
            return dateB - dateA;
        });
    }, [measurements]);

    const getLatestMetric = (key: keyof BodyMeasurements) => {
        const found = sortedMeasurements.find(m => {
            const val = m[key];
            return val !== null && val !== undefined && val !== 0 && typeof val === 'number';
        });
        return found ? found[key] : null;
    };

    const latestRecordDate = sortedMeasurements[0]?.date;

    const userAge = useMemo(() => {
        return calculateAge(selectedUser?.birthDate);
    }, [selectedUser]);

    const measurementFields = [
        {key: 'weight', label: 'Waga', unit: 'kg', icon: Scale},
        {key: 'height', label: 'Wzrost', unit: 'cm', icon: Ruler},
        {key: 'neck', label: 'Szyja', unit: 'cm', icon: Activity},
        {key: 'chest', label: 'Klatka', unit: 'cm', icon: Activity},
        {key: 'biceps', label: 'Biceps', unit: 'cm', icon: Activity},
        {key: 'waist', label: 'Talia', unit: 'cm', icon: Activity},
        {key: 'hips', label: 'Biodra', unit: 'cm', icon: Activity},
        {key: 'thigh', label: 'Udo', unit: 'cm', icon: Activity},
        {key: 'calf', label: 'Łydka', unit: 'cm', icon: Activity},
    ] as const;

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

        const sourceNameNormalized = sourceMeal.name.trim().toLowerCase();
        const emptyDaysIndices: number[] = [];
        const conflictDaysIndices: number[] = [];

        dietData.days.forEach((day, dayIndex) => {
            if (dayIndex === sourceDayIndex) return;

            const targetMeal = day.meals[sourceMealIndex];
            const targetName = targetMeal.name?.trim() || '';

            if (targetName === '') {
                emptyDaysIndices.push(dayIndex);
            } else if (targetName.toLowerCase() !== sourceNameNormalized) {
                conflictDaysIndices.push(dayIndex);
            }
        });

        if (emptyDaysIndices.length === 0 && conflictDaysIndices.length === 0) {
            toast.info('Ten posiłek jest już zaplanowany we wszystkich dniach o tej porze');
            return;
        }

        const performUpdate = (indices: number[]) => {
            indices.forEach(dayIndex => {
                onUpdateMeal(dayIndex, sourceMealIndex, {
                    ...sourceMeal,
                    photos: [...(sourceMeal.photos || [])]
                });
            });
            toast.success(`Zaktualizowano posiłek w ${indices.length} dniach`);
        };

        if (conflictDaysIndices.length > 0) {
            const totalToUpdate = [...emptyDaysIndices, ...conflictDaysIndices];
            const confirmMessage = emptyDaysIndices.length > 0
                ? `Wykryto inne posiłki w ${conflictDaysIndices.length} dniach. Czy chcesz nadpisać istniejące posiłki oraz uzupełnić puste pola (łącznie ${totalToUpdate.length} dni)?`
                : `Wykryto inne posiłki w ${conflictDaysIndices.length} dniach. Czy na pewno chcesz je nadpisać?`;

            if (window.confirm(confirmMessage)) {
                performUpdate(totalToUpdate);
            } else if (emptyDaysIndices.length > 0) {
                if (window.confirm(`Czy chcesz w takim razie uzupełnić tylko puste pola (${emptyDaysIndices.length} dni)?`)) {
                    performUpdate(emptyDaysIndices);
                }
            }
        } else {
            performUpdate(emptyDaysIndices);
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

    const openCopyDayModal = (sourceDayIndex: number) => {
        setCopyDaySource(sourceDayIndex);
        setCopyDayTargets([]);
    };

    const toggleCopyDayTarget = (dayIndex: number) => {
        setCopyDayTargets((prev) =>
            prev.includes(dayIndex) ? prev.filter((i) => i !== dayIndex) : [...prev, dayIndex]
        );
    };

    const confirmCopyDay = () => {
        if (copyDaySource === null || copyDayTargets.length === 0) {
            if (copyDayTargets.length === 0) toast.error("Wybierz co najmniej jeden dzień");
            return;
        }
        const sourceMeals = dietData.days[copyDaySource].meals;
        copyDayTargets.forEach((dayIndex) => {
            sourceMeals.forEach((meal, mealIndex) => {
                const cloned: ParsedMeal = {
                    ...meal,
                    ingredients: (meal.ingredients ?? []).map((ing) => ({...ing})),
                    photos: meal.photos ? [...meal.photos] : undefined,
                };
                onUpdateMeal(dayIndex, mealIndex, cloned);
            });
        });
        toast.success(`Skopiowano plan dnia do ${copyDayTargets.length} dni`);
        setCopyDaySource(null);
        setCopyDayTargets([]);
    };

    const totalMeals = dietData.days.length * dietData.mealsPerDay;
    const completedMeals = dietData.days.reduce((sum, day) =>
        sum + day.meals.filter(meal => meal.name && meal.name.trim() !== '').length, 0
    );

    const progressPercentage = Math.round((completedMeals / totalMeals) * 100);
    const allExpanded = expandedDays.length === dietData.days.length;
    const noneExpanded = expandedDays.length === 0;

    return (
        <div className="space-y-6">
            {selectedTemplate && onRemoveTemplate && (
                <TemplateInfoBanner
                    template={selectedTemplate}
                    onRemoveTemplate={onRemoveTemplate}
                />
            )}

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                <div className="p-5">
                    <div className="flex flex-col xl:flex-row gap-6">
                        {/* Left Side: Client Data & Measurements */}
                        <div className="flex-1">
                            {/* User Header */}
                            <div className="flex items-start justify-between mb-5">
                                <div className="flex items-center gap-3">
                                    <div className="bg-primary/10 p-2.5 rounded-full">
                                        <UserIcon className="h-6 w-6 text-primary"/>
                                    </div>
                                    <div>
                                        <h3 className="text-lg font-bold text-gray-900 leading-tight">
                                            {selectedUser?.nickname || selectedUser?.email || 'Nieznany klient'}
                                        </h3>
                                        <div
                                            className="text-sm text-gray-500 flex flex-wrap items-center gap-x-3 gap-y-1 mt-1">
                                            <span>
                                                {userAge > 0 ? `${userAge} lat(a)` : 'Wiek nieznany'}
                                            </span>
                                            {latestRecordDate && (
                                                <span
                                                    className="flex items-center gap-1 bg-gray-100 px-2 py-0.5 rounded text-xs">
                                                    <Calendar className="h-3 w-3"/>
                                                    Ostatni pomiar: {formatTimestamp(latestRecordDate)}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Compact Measurements Grid */}
                            <div className="grid grid-cols-3 sm:grid-cols-5 md:grid-cols-6 lg:grid-cols-9 gap-2">
                                {measurementFields.map((field) => {
                                    const value = getLatestMetric(field.key as keyof BodyMeasurements);
                                    // Don't display empty tiles unless it's weight/height
                                    if (!value && field.key !== 'weight' && field.key !== 'height') return null;

                                    return (
                                        <div key={field.key}
                                             className="bg-gray-50 p-2 rounded border border-gray-100 flex flex-col items-center justify-center min-h-[60px]">
            <span
                className="text-[10px] uppercase font-semibold text-gray-500 mb-0.5 text-center">
                {field.label}
            </span>
                                            <span
                                                className={`font-bold text-gray-900 ${!value ? 'text-gray-300' : ''}`}>
                                                {value ? String(value) : '-'}
            </span>
                                            <span className="text-[10px] text-gray-400">
                {field.unit}
            </span>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Divider for large screens */}
                        <div className="hidden xl:block w-px bg-gray-200 self-stretch"></div>

                        {/* Right Side: Diet Progress (Compact) */}
                        <div
                            className="flex flex-col justify-between min-w-[220px] bg-gray-50/50 rounded-lg p-4 xl:bg-transparent xl:p-0">
                            <div>
                                <h4 className="text-sm font-semibold text-gray-700 mb-3">Postęp planowania</h4>
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="relative w-14 h-14 shrink-0">
                                        <svg className="w-full h-full" viewBox="0 0 36 36">
                                            <path
                                                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                                fill="none"
                                                stroke="#E5E7EB"
                                                strokeWidth="3"
                                            />
                                            <path
                                                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                                fill="none"
                                                stroke={progressPercentage === 100 ? "#10B981" : "#3B82F6"}
                                                strokeWidth="3"
                                                strokeDasharray={`${progressPercentage}, 100`}
                                                className="transition-all duration-500 ease-out"
                                            />
                                        </svg>
                                        <div
                                            className="absolute inset-0 flex items-center justify-center text-xs font-bold text-gray-700">
                                            {progressPercentage}%
                                        </div>
                                    </div>
                                    <div>
                                        <div className="text-sm font-medium text-gray-900">
                                            {completedMeals} z {totalMeals}
                                        </div>
                                        <div className="text-xs text-gray-500">zaplanowanych posiłków</div>
                                    </div>
                                </div>
                            </div>

                            <button
                                onClick={toggleAllDays}
                                className={`flex items-center justify-center gap-2 px-3 py-2 text-xs font-medium rounded-lg transition-colors w-full ${
                                    allExpanded
                                        ? 'bg-primary text-white hover:bg-primary-dark shadow-sm'
                                        : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                }`}
                            >
                                {allExpanded ? (
                                    <>
                                        <ChevronUp className="h-3 w-3"/>
                                        Zwiń wszystkie dni
                                    </>
                                ) : (
                                    <>
                                        <ChevronDown className="h-3 w-3"/>
                                        {noneExpanded ? 'Rozwiń wszystkie dni' : 'Rozwiń pozostałe'}
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Bottom Bar: Quick Validation */}
                {/* {progressPercentage < 100 && (
                    <div className="bg-amber-50 px-5 py-2 border-t border-amber-100 flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full bg-amber-400 animate-pulse"></div>
                        <span className="text-xs text-amber-700 font-medium">
                            Planowanie w toku. Uzupełnij brakujące posiłki ({totalMeals - completedMeals}).
                        </span>
                    </div>
                )}*/}
                {progressPercentage === 100 && (
                    <div className="bg-green-50 px-5 py-2 border-t border-green-100 flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-600"/>
                        <span className="text-xs text-green-700 font-medium">
                            Plan gotowy do kategoryzacji.
                        </span>
                    </div>
                )}
            </div>

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
                        onAddIngredient={(mealIndex, ingredient) =>
                            onAddIngredient(dayIndex, mealIndex, ingredient)
                        }
                        onRemoveIngredient={(mealIndex, ingredientIndex) =>
                            onRemoveIngredient(dayIndex, mealIndex, ingredientIndex)
                        }
                        onUpdateIngredient={(mealIndex, ingredientIndex, ingredient) =>
                            onUpdateIngredient(dayIndex, mealIndex, ingredientIndex, ingredient)
                        }
                        onCopyMealToOtherDays={(mealIndex) => copyMealToOtherDays(dayIndex, mealIndex)}
                        onCopyDayClick={() => openCopyDayModal(dayIndex)}
                        trainerId={trainerId}
                    />
                ))}
            </div>

            {/* Copy Day modal */}
            {copyDaySource !== null && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
                    <div className="w-full max-w-sm rounded-xl bg-white p-5 shadow-xl">
                        <div className="mb-4 flex items-center justify-between">
                            <h3 className="text-lg font-semibold text-gray-900">
                                Skopiuj dzień {copyDaySource + 1} do
                            </h3>
                            <button
                                type="button"
                                onClick={() => setCopyDaySource(null)}
                                className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                                aria-label="Zamknij"
                            >
                                <X className="h-5 w-5"/>
                            </button>
                        </div>
                        <p className="mb-3 text-sm text-gray-600">
                            Zaznacz dni, do których chcesz skopiować plan dnia {copyDaySource + 1}.
                        </p>
                        <div className="max-h-48 space-y-2 overflow-y-auto">
                            {dietData.days.map((_, dayIndex) => {
                                if (dayIndex === copyDaySource) return null;
                                return (
                                    <label
                                        key={dayIndex}
                                        className="flex cursor-pointer items-center gap-3 rounded-lg border border-gray-100 p-2 hover:bg-gray-50"
                                    >
                                        <input
                                            type="checkbox"
                                            checked={copyDayTargets.includes(dayIndex)}
                                            onChange={() => toggleCopyDayTarget(dayIndex)}
                                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                        <span className="text-sm font-medium text-gray-800">
                                            Dzień {dayIndex + 1}
                                        </span>
                                    </label>
                                );
                            })}
                        </div>
                        <div className="mt-4 flex gap-2">
                            <button
                                type="button"
                                onClick={() => setCopyDaySource(null)}
                                className="flex-1 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                            >
                                Anuluj
                            </button>
                            <button
                                type="button"
                                onClick={confirmCopyDay}
                                disabled={copyDayTargets.length === 0}
                                className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-primary px-3 py-2 text-sm font-medium text-white hover:bg-primary-dark disabled:opacity-50"
                            >
                                <Copy className="h-4 w-4"/>
                                Skopiuj ({copyDayTargets.length})
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MealPlanningStep;