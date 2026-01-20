import React, {useState, useEffect} from 'react';
import {Calculator, Check, ChevronDown, ChevronUp, HelpCircle} from 'lucide-react';
import {CalorieValidationService} from "../../../../services/vitema/diet/CalorieValidationService";

interface CalorieValidationUploadProps {
    isEnabled: boolean;
    onEnabledChange: (enabled: boolean) => void;
    targetCalories: number;
    onTargetCaloriesChange: (calories: number) => void;
    validationResult?: {
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    };
    onValidate?: () => void;
}

/**
 * Komponent do konfiguracji walidacji kalorii podczas uploadu pliku Excel
 */
const CalorieValidationUpload: React.FC<CalorieValidationUploadProps> = ({
                                                                             isEnabled,
                                                                             onEnabledChange,
                                                                             targetCalories,
                                                                             onTargetCaloriesChange,
                                                                             validationResult,
                                                                             onValidate
                                                                         }) => {
    const [isExpanded, setIsExpanded] = useState(true);
    const [showPredefinedValues, setShowPredefinedValues] = useState(false);

    // Lista typowych wartości kalorycznych do szybkiego wyboru
    const predefinedCalorieValues = [1500, 1800, 2000, 2200, 2500, 2800, 3000];

    useEffect(() => {
        CalorieValidationService.saveCalorieValidationPreferences(isEnabled, targetCalories);
    }, [isEnabled, targetCalories]);

    const handleCaloriesChange = (calories: number) => {
        onTargetCaloriesChange(calories);
        if (onValidate && isEnabled) {
            setTimeout(onValidate, 300);
        }
    };

    const handleEnabledChange = (enabled: boolean) => {
        onEnabledChange(enabled);
        if (onValidate) {
            setTimeout(onValidate, 300);
        }
    };

    const getStatusColor = () => {
        if (!validationResult) return '';
        if (validationResult.severity === 'success') return 'text-green-600';
        if (validationResult.severity === 'warning') return 'text-yellow-600';
        return 'text-red-600';
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-sm">
            <div
                className="flex items-center justify-between cursor-pointer"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-2">
                    <Calculator className="h-5 w-5 text-amber-600"/>
                    <h3 className="font-medium text-lg">Walidacja kalorii</h3>
                </div>

                {isExpanded ? (
                    <ChevronUp className="h-5 w-5 text-gray-400"/>
                ) : (
                    <ChevronDown className="h-5 w-5 text-gray-400"/>
                )}
            </div>

            {isExpanded && (
                <div className="mt-4 space-y-4">
                    <div className="flex items-center gap-2">
                        <input
                            type="checkbox"
                            id="enableCalorieValidation"
                            checked={isEnabled}
                            onChange={(e) => handleEnabledChange(e.target.checked)}
                            className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                        />
                        <label htmlFor="enableCalorieValidation" className="text-sm">
                            Sprawdź zgodność kalorii z pliku Excel z założonym celem
                        </label>
                    </div>

                    {isEnabled && (
                        <>
                            <div>
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

                            {validationResult && (
                                <div className={`p-3 rounded-md ${
                                    validationResult.severity === 'success' ? 'bg-green-50' :
                                        validationResult.severity === 'warning' ? 'bg-yellow-50' : 'bg-red-50'
                                }`}>
                                    <div className="flex items-start gap-2">
                                        {validationResult.severity === 'success' && (
                                            <Check className="h-5 w-5 text-green-500 flex-shrink-0"/>
                                        )}
                                        <span className={`text-sm ${getStatusColor()}`}>
                                            {validationResult.message}
                                        </span>
                                    </div>
                                </div>
                            )}

                            <div className="flex items-start gap-2 p-3 bg-blue-50 rounded-md text-xs text-blue-700">
                                <HelpCircle className="h-4 w-4 flex-shrink-0 mt-0.5"/>
                                <div>
                                    System sprawdzi, czy średnia wartość kalorii w diecie z pliku Excel jest zgodna z
                                    podaną
                                    wartością docelową. Dopuszczalny jest margines błędu ±5%.
                                </div>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default CalorieValidationUpload;