import React, {useEffect, useState} from "react";
import {ParserSettingsService} from "../../../../services/diet/ParserSettingsService";
import {Calculator, ChevronDown, ChevronUp, Settings, Save, FileSearch} from "lucide-react";

interface ExcelParserSettingsProps {
    // Excel parser settings
    skipColumnsCount: number;
    onSkipColumnsCountChange: (value: number) => void;

    // Calorie validation settings
    isCalorieValidationEnabled: boolean;
    onCalorieValidationEnabledChange: (enabled: boolean) => void;
    targetCalories: number;
    onTargetCaloriesChange: (calories: number) => void;

    // Validation results
    calorieValidationResult?: {
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    };

    // Callbacks
    onValidate?: () => void;
    sectionRef?: React.RefObject<HTMLDivElement>;
    className?: string;
}

const ExcelParserSettings: React.FC<ExcelParserSettingsProps> = ({
                                                                     skipColumnsCount,
                                                                     onSkipColumnsCountChange,
                                                                     isCalorieValidationEnabled,
                                                                     onCalorieValidationEnabledChange,
                                                                     targetCalories,
                                                                     onTargetCaloriesChange,
                                                                     calorieValidationResult,
                                                                     onValidate,
                                                                     sectionRef,
                                                                     className = ''
                                                                 }) => {
    const [isExpanded, setIsExpanded] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [maxSkipColumnsCount, setMaxSkipColumnsCount] = useState(3);
    const [showPredefinedValues, setShowPredefinedValues] = useState(false);

    // Predefiniowane wartości kaloryczne
    const predefinedCalorieValues = [1500, 1800, 2000, 2200, 2500, 2800, 3000];

    useEffect(() => {
        const loadSettings = async () => {
            setIsLoading(true);
            try {
                const settings = await ParserSettingsService.getAllSettings();
                setMaxSkipColumnsCount(settings.maxSkipColumnsCount);
            } catch (error) {
                console.error('Error loading parser settings:', error);
            } finally {
                setIsLoading(false);
            }
        };

        loadSettings().catch(console.error);
    }, []);

    // Obsługa zmiany wartości kalorii
    const handleCaloriesChange = (calories: number) => {
        onTargetCaloriesChange(calories);
        if (onValidate && isCalorieValidationEnabled) {
            setTimeout(onValidate, 300);
        }
    };

    // Obsługa zmiany włączenia walidacji kalorii
    const handleEnabledChange = (enabled: boolean) => {
        onCalorieValidationEnabledChange(enabled);
        if (onValidate) {
            setTimeout(onValidate, 300);
        }
    };

    const getStatusColor = (severity?: string) => {
        if (!severity) return '';
        const lowerSeverity = severity.toLowerCase();
        if (lowerSeverity === 'success') return 'text-green-600';
        if (lowerSeverity === 'warning') return 'text-yellow-600';
        return 'text-red-600';
    };

    return (
        <div ref={sectionRef} className={`bg-white rounded-lg shadow-md ${className}`}>
            {/* Nagłówek z ikoną rozwijania/zwijania */}
            <div
                className="flex items-center justify-between p-6 cursor-pointer border-b"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-3">
                    <FileSearch className="h-5 w-5 text-primary"/>
                    <h3 className="font-medium text-lg">Ustawienia parsowania diety</h3>
                </div>

                <button
                    type="button"
                    className="text-gray-500 hover:text-gray-700 focus:outline-none"
                >
                    {isExpanded ? (
                        <ChevronUp className="h-5 w-5"/>
                    ) : (
                        <ChevronDown className="h-5 w-5"/>
                    )}
                </button>
            </div>

            {isExpanded && (
                <div className="p-6">
                    {/* Dzielimy na dwie kolumny na większych ekranach */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Sekcja ustawień parsera Excel */}
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <h4 className="font-medium text-base text-gray-700 mb-3 flex items-center gap-2">
                                <Settings className="h-4 w-4 text-blue-600"/>
                                Konfiguracja parsera Excel
                            </h4>

                            <div className="flex items-center gap-2 mb-3">
                                <label htmlFor="skipColumnsCount" className="text-sm text-gray-600">
                                    Liczba pomijanych kolumn:
                                </label>
                                <input
                                    id="skipColumnsCount"
                                    type="number"
                                    min="0"
                                    max={maxSkipColumnsCount}
                                    value={skipColumnsCount}
                                    onChange={(e) => onSkipColumnsCountChange(parseInt(e.target.value, 10) || 0)}
                                    className="w-16 px-2 py-1 border border-gray-300 rounded text-center"
                                    disabled={isLoading}
                                />
                            </div>

                            <div className="text-xs text-gray-500">
                                Określa, ile początkowych kolumn ma zostać pominiętych podczas przetwarzania pliku
                                Excel.
                                Domyślnie pomijana jest 1 kolumna (indeksowa).
                                <span className="block mt-1">Maksymalna wartość: {maxSkipColumnsCount}</span>
                            </div>
                        </div>

                        {/* Sekcja walidacji kalorii */}
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <h4 className="font-medium text-base text-gray-700 mb-3 flex items-center gap-2">
                                <Calculator className="h-4 w-4 text-amber-600"/>
                                Walidacja kalorii
                            </h4>

                            <div className="flex items-center gap-2 mb-3">
                                <input
                                    type="checkbox"
                                    id="enableCalorieValidation"
                                    checked={isCalorieValidationEnabled}
                                    onChange={(e) => handleEnabledChange(e.target.checked)}
                                    className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                                />
                                <label htmlFor="enableCalorieValidation" className="text-sm">
                                    Sprawdź zgodność kalorii z pliku Excel z założonym celem
                                </label>
                            </div>

                            {isCalorieValidationEnabled && (
                                <>
                                    <div className="mb-3">
                                        <label htmlFor="targetCalories"
                                               className="block text-sm font-medium text-gray-700 mb-1">
                                            Docelowa ilość kalorii dziennie:
                                        </label>
                                        <div className="flex items-center gap-2">
                                            <input
                                                type="number"
                                                id="targetCalories"
                                                value={targetCalories}
                                                onChange={(e) => handleCaloriesChange(Number(e.target.value))}
                                                min="500"
                                                max="10000"
                                                step="50"
                                                className="w-24 p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                            />
                                            <span className="text-gray-500">kcal</span>

                                            <button
                                                type="button"
                                                onClick={() => setShowPredefinedValues(!showPredefinedValues)}
                                                className="ml-2 text-sm text-blue-600 hover:text-blue-800 flex items-center"
                                            >
                                                {showPredefinedValues ? 'Ukryj' : 'Wybierz wartość'}
                                                {showPredefinedValues ? (
                                                    <ChevronUp className="h-4 w-4 ml-1"/>
                                                ) : (
                                                    <ChevronDown className="h-4 w-4 ml-1"/>
                                                )}
                                            </button>
                                        </div>

                                        {showPredefinedValues && (
                                            <div className="mt-2 flex flex-wrap gap-2">
                                                {predefinedCalorieValues.map((calories) => (
                                                    <button
                                                        key={calories}
                                                        type="button"
                                                        onClick={() => {
                                                            handleCaloriesChange(calories);
                                                            setShowPredefinedValues(false);
                                                        }}
                                                        className={`px-3 py-1 text-sm rounded-full border 
                                                            ${targetCalories === calories
                                                            ? 'bg-blue-100 border-blue-300 text-blue-800'
                                                            : 'bg-gray-50 border-gray-200 text-gray-700 hover:bg-gray-100'}`}
                                                    >
                                                        {calories} kcal
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    {calorieValidationResult && (
                                        <div className={`p-3 rounded-md mb-3 ${
                                            calorieValidationResult.severity?.toLowerCase() === 'success' ? 'bg-green-50' :
                                                calorieValidationResult.severity?.toLowerCase() === 'warning' ? 'bg-yellow-50' : 'bg-red-50'
                                        }`}>
                                            <span
                                                className={`text-sm ${getStatusColor(calorieValidationResult.severity)}`}>
                                                {calorieValidationResult.message}
                                            </span>
                                        </div>
                                    )}

                                    <div className="text-xs text-gray-500">
                                        System sprawdzi, czy średnia wartość kalorii w diecie z pliku Excel jest zgodna
                                        z
                                        podaną wartością docelową. Dopuszczalny jest margines błędu ±5%.
                                    </div>
                                </>
                            )}
                        </div>
                    </div>

                    {/* Informacja o automatycznym zapisywaniu */}
                    <div className="mt-6 flex items-center p-3 bg-blue-50 rounded-lg text-blue-700 text-xs">
                        <Save className="h-4 w-4 mr-2 flex-shrink-0"/>
                        <p>
                            Wszystkie zmiany ustawień parsowania diety są zapisywane automatycznie.
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ExcelParserSettings;