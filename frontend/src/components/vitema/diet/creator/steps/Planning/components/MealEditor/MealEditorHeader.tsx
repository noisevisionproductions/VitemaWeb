import React, {useState} from 'react';
import {MealType, ParsedMeal} from '../../../../../../../../types';
import MealNameSearchField from '../../../../components/MealNameSearchField';
import type {UnifiedSearchResult} from "../../../../../../../../types";
import {Sparkles, BookOpen, Loader2, BookmarkPlus, Check} from 'lucide-react';
import RecipeBrowserModal from './RecipeBrowserModal';

interface MealEditorHeaderProps {
    meal: ParsedMeal;
    enableTemplateFeatures: boolean;
    isSavingTemplate: boolean;
    isApplyingTemplate: boolean;
    onMealNameChange: (name: string) => void;
    onUnifiedResultSelect?: (result: UnifiedSearchResult) => void;
    onManualSaveTemplate: () => void;
    onTimeChange: (time: string) => void;
    onMealTypeChange: (type: MealType) => void;
    trainerId?: string;
    hasSaved?: boolean;
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
                                                               isSavingTemplate,
                                                               isApplyingTemplate,
                                                               onMealNameChange,
                                                               onUnifiedResultSelect,
                                                               onManualSaveTemplate,
                                                               onTimeChange,
                                                               onMealTypeChange,
                                                               trainerId,
                                                               hasSaved = false
                                                           }) => {
    const [isBrowserOpen, setIsBrowserOpen] = useState(false);

    const handleBrowserSelect = (result: UnifiedSearchResult) => {
        if (onUnifiedResultSelect) {
            onUnifiedResultSelect(result);
        } else {
            onMealNameChange(result.name);
        }
        setIsBrowserOpen(false);
    };

    return (
        <div className="space-y-3">
            {/* Time and Type Section */}
            <div className="grid grid-cols-2 gap-3">
                {/* ... (sekcja czasu bez zmian) ... */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Godzina
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
                        Rodzaj
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
                        Co planujesz?
                    </label>

                    {enableTemplateFeatures && meal.name && meal.name.trim().length > 2 && !hasSaved && (
                        <button
                            onClick={onManualSaveTemplate}
                            disabled={isSavingTemplate}
                            className="flex items-center gap-1.5 px-3 py-1 text-xs font-medium text-emerald-700 bg-emerald-50 hover:bg-emerald-100 hover:text-emerald-800 border border-emerald-200 rounded-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                            title="Zapisz ten posiłek w moich szablonach do późniejszego użycia"
                        >
                            {isSavingTemplate ? (
                                <Loader2 className="h-3.5 w-3.5 animate-spin"/>
                            ) : (
                                <BookmarkPlus className="h-3.5 w-3.5"/>
                            )}
                            {isSavingTemplate ? 'Zapisywanie...' : 'Zapisz jako ulubiony'}
                        </button>
                    )}
                    {hasSaved && (
                        <div
                            className="flex items-center gap-1.5 px-3 py-1 text-xs font-medium text-gray-500 border border-gray-200 rounded-lg bg-gray-50">
                            <Check className="h-3.5 w-3.5 text-green-500"/>
                            Zapisano
                        </div>
                    )}
                </div>

                <div className="relative flex gap-2">
                    <div className="flex-1">
                        {enableTemplateFeatures ? (
                            <MealNameSearchField
                                value={meal.name}
                                onChange={onMealNameChange}
                                onUnifiedResultSelect={onUnifiedResultSelect}
                                placeholder="Szukaj przepisu lub wpisz nazwę..."
                                trainerId={trainerId}
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
                    </div>

                    {enableTemplateFeatures && (
                        <button
                            onClick={() => setIsBrowserOpen(true)}
                            className="shrink-0 px-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-primary/50 hover:text-primary transition-all flex items-center justify-center shadow-sm"
                            title="Przeglądaj wszystkie przepisy"
                        >
                            <BookOpen className="h-5 w-5"/>
                        </button>
                    )}
                </div>

                {isApplyingTemplate && (
                    <div
                        className="mt-2 flex items-center gap-2 text-xs text-blue-600 bg-blue-50 p-2 rounded-lg border border-blue-100 animate-in fade-in">
                        <Sparkles className="h-3 w-3 animate-pulse"/>
                        Wczytywanie składników z przepisu...
                    </div>
                )}
            </div>

            {/* Modal Render */}
            <RecipeBrowserModal
                isOpen={isBrowserOpen}
                onClose={() => setIsBrowserOpen(false)}
                onSelect={handleBrowserSelect}
                trainerId={trainerId}
            />
        </div>
    );
};

export default MealEditorHeader;