import React, {useState, useEffect, useRef} from 'react';
import {DietExcelTemplate} from "../../../../types";
import ValidationMessage, {ValidationErrorType} from './ValidationMessage';
import {DietUploadService} from "../../../../services/diet/DietUploadService";
import LoadingSpinner from "../../../common/LoadingSpinner";
import {useDebounce} from '../../../../hooks/useDebounce';
import {AxiosError} from "axios";

interface ValidationSectionProps {
    file: File;
    template: DietExcelTemplate;
    totalMeals: number;
    userId?: string;
    skipColumnsCount: number;
    isCalorieValidationEnabled?: boolean;
    targetCalories?: number;
    onValidationChange: {
        onExcelStructureValidation: (valid: boolean) => void;
        onMealsPerDayValidation: (valid: boolean) => void;
        onDateValidation: (valid: boolean) => void;
        onMealsConfigValidation: (valid: boolean) => void;
        onCalorieValidation?: (valid: boolean) => void;
    };
    onNavigate?: (section: ValidationErrorType) => void;
    onCalorieValidationResult?: (result?: ValidationResult) => void;
}

interface ValidationResult {
    isValid: boolean;
    message: string;
    severity: 'error' | 'warning' | 'success';
}

const ValidationSection: React.FC<ValidationSectionProps> = ({
                                                                 file,
                                                                 template,
                                                                 totalMeals,
                                                                 userId,
                                                                 skipColumnsCount,
                                                                 isCalorieValidationEnabled = false,
                                                                 targetCalories = 2000,
                                                                 onValidationChange,
                                                                 onNavigate,
                                                                 onCalorieValidationResult
                                                             }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [validationResults, setValidationResults] = useState<ValidationResult[]>([]);

    const lastFileRef = useRef<string>('');
    const lastTemplateRef = useRef<string>('');
    const lastSkipColumnsCountRef = useRef<number>(skipColumnsCount);
    const lastCalorieSettingsRef = useRef<string>('');

    const templateSignature = JSON.stringify({
        mealsPerDay: template.mealsPerDay,
        duration: template.duration,
        startDate: template.startDate ? template.startDate.toDate().toISOString() : null,
        mealTimes: Object.entries(template.mealTimes).sort().toString(),
        mealTypes: template.mealTypes.toString(),
        skipColumnsCount
    });

    const calorieSettingsSignature = JSON.stringify({
        isCalorieValidationEnabled,
        targetCalories
    });

    const debouncedTemplateSignature = useDebounce(templateSignature, 500);
    const debouncedCalorieSettings = useDebounce(calorieSettingsSignature, 500);

    useEffect(() => {
        if (!file) {
            setValidationResults([]);
            Object.values(onValidationChange).forEach(callback => {
                if (typeof callback === 'function') callback(false);
            });
            return;
        }

        const fileSignature = `${file.name}-${file.size}-${file.lastModified}`;
        const shouldValidate =
            fileSignature !== lastFileRef.current ||
            debouncedTemplateSignature !== lastTemplateRef.current ||
            skipColumnsCount !== lastSkipColumnsCountRef.current ||
            debouncedCalorieSettings !== lastCalorieSettingsRef.current;

        if (!shouldValidate) return;

        lastFileRef.current = fileSignature;
        lastTemplateRef.current = debouncedTemplateSignature;
        lastSkipColumnsCountRef.current = skipColumnsCount;
        lastCalorieSettingsRef.current = debouncedCalorieSettings;

        const validateTemplate = async () => {
            setIsLoading(true);
            try {
                // Przygotowanie parametrów walidacji kalorii
                const extraParams: any = {};
                if (isCalorieValidationEnabled) {
                    extraParams.calorieValidationEnabled = true;
                    extraParams.targetCalories = targetCalories;
                    extraParams.calorieErrorMargin = 5;
                }

                const response = await DietUploadService.validateDietTemplateWithUser(
                    file,
                    template,
                    userId,
                    skipColumnsCount,
                    extraParams
                );

                // Safely handle the response
                const validationResults = response?.validationResults || [];
                setValidationResults(validationResults);

                // Helper function to check validation result
                const hasErrorOfType = (keywords: string[], results = validationResults) => {
                    return results.some(r =>
                        r.severity === 'error' &&
                        !r.isValid &&
                        keywords.some(keyword => r.message.toLowerCase().includes(keyword))
                    );
                };

                // Update validation states with safe defaults
                onValidationChange.onExcelStructureValidation(
                    !hasErrorOfType(['excel', 'struktura', 'pliku', 'wiersz', 'kolumn'])
                );

                onValidationChange.onMealsPerDayValidation(
                    !hasErrorOfType(['posiłków', 'posilkow', 'meals'])
                );

                onValidationChange.onDateValidation(
                    !hasErrorOfType(['daty', 'date', 'diet', 'okresie', 'konflikt'])
                );

                onValidationChange.onMealsConfigValidation(
                    !hasErrorOfType(['posiłku', 'posilku', 'godziny', 'time'])
                );

                // Sprawdzanie wyników walidacji kalorii
                if (isCalorieValidationEnabled && onValidationChange.onCalorieValidation) {
                    const calorieResult = validationResults.find(
                        result => result.message.toLowerCase().includes('kalori')
                    );

                    if (calorieResult) {
                        onValidationChange.onCalorieValidation(calorieResult.isValid);

                        // Przekazanie wyniku walidacji kalorii do rodzica
                        if (onCalorieValidationResult) {
                            onCalorieValidationResult(calorieResult);
                        }
                    } else {
                        onValidationChange.onCalorieValidation(true);
                        if (onCalorieValidationResult) {
                            onCalorieValidationResult(undefined);
                        }
                    }
                }

            } catch (error: unknown) {
                console.error("Błąd podczas walidacji:", error);

                const errorResult: ValidationResult = {
                    isValid: false,
                    severity: 'error',
                    message: error instanceof AxiosError
                        ? (error.response?.data?.message || 'Błąd podczas walidacji szablonu')
                        : (error instanceof Error ? error.message : 'Wystąpił nieoczekiwany błąd')
                };

                setValidationResults([errorResult]);

                // Reset all validations on error
                Object.values(onValidationChange).forEach(callback => {
                    if (typeof callback === 'function') callback(false);
                });
            } finally {
                setIsLoading(false);
            }
        };

        validateTemplate().catch(console.error);
    }, [
        file,
        debouncedTemplateSignature,
        debouncedCalorieSettings,
        onValidationChange,
        template,
        userId,
        skipColumnsCount,
        isCalorieValidationEnabled,
        targetCalories,
        onCalorieValidationResult
    ]);

    const getErrorType = (result: ValidationResult): ValidationErrorType => {
        const message = result.message.toLowerCase();

        if (message.includes('kalori')) {
            return 'calorie-validation';
        }

        if (message.includes('konflikt') || message.includes('już dietę') || message.includes('okresie') ||
            (message.includes('daty') && message.includes('rozpoczęcia'))) {
            return 'diet-overlap';
        }

        if (message.includes('wiersz') || message.includes('kolumn') || message.includes('pomijanych')) {
            return 'parser-settings';
        }

        if (message.includes('excel') || message.includes('pliku') || message.includes('struktura')) {
            return 'excel-structure';
        }

        if (message.includes('posiłków') || message.includes('posilkow') || message.includes('meals')) {
            return 'meals-per-day';
        }

        if (message.includes('daty') || message.includes('date') || message.includes('rozpoczęcia')) {
            return 'date';
        }

        if (message.includes('posiłku') || message.includes('posilku') || message.includes('godziny') ||
            message.includes('time') || message.includes('konfiguracji')) {
            return 'meals-config';
        }

        return 'unknown';
    };

    const handleNavigate = (errorType: ValidationErrorType) => {
        if (onNavigate) {
            onNavigate(errorType);
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center gap-2 p-4 bg-gray-50 rounded-lg">
                <LoadingSpinner/>
                <span>Walidacja w toku...</span>
            </div>
        );
    }

    return (
        <div className="space-y-4 bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-lg font-medium mb-4">Wyniki walidacji</h3>

            {validationResults.length > 0 ? (
                <div className="space-y-2">
                    {validationResults.map((result, index) => {
                        const errorType = !result.isValid ? getErrorType(result) : 'unknown';

                        return (
                            <ValidationMessage
                                key={index}
                                message={result.message}
                                severity={result.severity}
                                errorType={errorType}
                                onNavigate={!result.isValid ? handleNavigate : undefined}
                            />
                        );
                    })}
                </div>
            ) : (
                <div className="text-gray-500 p-4 bg-gray-50 rounded-lg">
                    Brak wyników walidacji
                </div>
            )}

            {totalMeals > 0 && (
                <div className="mt-4 p-3 bg-blue-50 rounded-lg">
                    <span className="font-medium">Liczba posiłków w pliku:</span> {totalMeals}
                </div>
            )}
        </div>
    );
};

export default ValidationSection;