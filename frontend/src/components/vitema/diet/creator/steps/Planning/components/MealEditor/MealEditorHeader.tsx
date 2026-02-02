import React from 'react';
import {MealType, ParsedMeal} from '../../../../../../../../types';
import {MealSuggestion} from '../../../../../../../../types/mealSuggestions';
import MealNameSearchField from '../../../../components/MealNameSearchField';
import {Save, Sparkles} from 'lucide-react';

interface MealEditorHeaderProps {
    meal: ParsedMeal;
    enableTemplateFeatures: boolean;
    saveAsTemplate: boolean;
    isSavingTemplate: boolean;
    hasUnsavedChanges: boolean;
    isApplyingTemplate: boolean;
    onMealNameChange: (name: string) => void;
    onMealSelect: (suggestion: MealSuggestion) => void;
    onManualSaveTemplate: () => void;
    onSavePreference: (save: boolean) => void;
    onTimeChange: (time: string) => void;
    onMealTypeChange: (type: MealType) => void;
}

const MEAL_TYPE_LABELS: Record<MealType, string> = {
    [MealType.BREAKFAST]: 'Śniadanie',
    [MealType.SECOND_BREAKFAST]: 'Drugie śniadanie',
    [MealType.LUNCH]: 'Obiad',
    [MealType.SNACK]: 'Przekąska',
    [MealType.DINNER]: 'Kolacja'
};

const MealEditorHeader: React.FC<MealEditorHeaderProps> = ({
                                                               meal,
                                                               enableTemplateFeatures,
                                                               saveAsTemplate,
                                                               isSavingTemplate,
                                                               hasUnsavedChanges,
                                                               isApplyingTemplate,
                                                               onMealNameChange,
                                                               onMealSelect,
                                                               onManualSaveTemplate,
                                                               onSavePreference,
                                                               onTimeChange,
                                                               onMealTypeChange
                                                           }) => {
    return (
        <div className="space-y-3">
            {/* Time and Type Section */}
            <div className="grid grid-cols-2 gap-3">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Godzina posiłku
                    </label>
                    <input
                        type="time"
                        value={meal.time}
                        onChange={(e) => onTimeChange(e.target.value)}
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Typ posiłku
                    </label>
                    <select
                        value={meal.mealType}
                        onChange={(e) => onMealTypeChange(e.target.value as MealType)}
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                    >
                        {Object.entries(MEAL_TYPE_LABELS).map(([value, label]) => (
                            <option key={value} value={value}>
                                {label}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Meal Name Section */}
            <div>
                <div className="flex items-center justify-between mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                        Nazwa posiłku
                    </label>
                    <div className="flex items-center gap-2">
                        {hasUnsavedChanges && (
                            <span className="text-xs text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full">
                                • Niezapisane zmiany
                            </span>
                        )}

                        {enableTemplateFeatures && meal.name && meal.name.trim().length > 2 && (
                            <div className="flex items-center gap-2">
                                {isSavingTemplate && (
                                    <span className="text-xs text-gray-500 flex items-center gap-1">
                                        <div
                                            className="w-3 h-3 border border-gray-300 border-t-primary rounded-full animate-spin"></div>
                                        Zapisywanie...
                                    </span>
                                )}
                                <button
                                    onClick={onManualSaveTemplate}
                                    disabled={isSavingTemplate}
                                    className="flex items-center gap-1 px-2 py-1 text-xs text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition-colors disabled:opacity-50"
                                    title="Zapisz jako szablon"
                                >
                                    <Save className="h-3 w-3"/>
                                    Zapisz szablon
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {enableTemplateFeatures ? (
                    <MealNameSearchField
                        value={meal.name}
                        onChange={onMealNameChange}
                        onMealSelect={onMealSelect}
                        placeholder="Wpisz nazwę posiłku, np. 'Owsianka z owocami'..."
                        onSavePreference={onSavePreference}
                    />
                ) : (
                    <input
                        type="text"
                        value={meal.name}
                        onChange={(e) => onMealNameChange(e.target.value)}
                        placeholder="np. Owsianka z owocami"
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                    />
                )}

                {isApplyingTemplate && (
                    <div className="mt-1 flex items-center gap-2 text-xs text-blue-600">
                        <Sparkles className="h-3 w-3 animate-pulse"/>
                        Aplikowanie szablonu...
                    </div>
                )}

                {enableTemplateFeatures && saveAsTemplate && (
                    <div className="mt-1 text-xs text-green-600 flex items-center gap-1">
                        <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                        Auto-zapisywanie szablonów włączone
                    </div>
                )}
            </div>
        </div>
    );
};

export default MealEditorHeader;
