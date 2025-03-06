import React, { useState, useEffect, useRef } from 'react';
import { DietTemplate } from "../../../../types";
import ValidationMessage from './ValidationMessage';
import { DietUploadService } from "../../../../services/DietUploadService";
import LoadingSpinner from "../../../common/LoadingSpinner";
import { useDebounce } from '../../../../hooks/useDebounce';
import {AxiosError} from "axios";

interface ValidationSectionProps {
    file: File;
    template: DietTemplate;
    totalMeals: number;
    onValidationChange: {
        onExcelStructureValidation: (valid: boolean) => void;
        onMealsPerDayValidation: (valid: boolean) => void;
        onDateValidation: (valid: boolean) => void;
        onMealsConfigValidation: (valid: boolean) => void;
    };
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
                                                                   onValidationChange
                                                               }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [validationResults, setValidationResults] = useState<Array<{
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    }>>([]);

    const lastFileRef = useRef<string>('');
    const lastTemplateRef = useRef<string>('');

    const templateSignature = JSON.stringify({
        mealsPerDay: template.mealsPerDay,
        duration: template.duration,
        startDate: template.startDate ? template.startDate.toDate().toISOString() : null,
        mealTimes: Object.entries(template.mealTimes).sort().toString(),
        mealTypes: template.mealTypes.toString()
    });

    const debouncedTemplateSignature = useDebounce(templateSignature, 500);

    useEffect(() => {
        if (!file) {
            setValidationResults([]);
            Object.values(onValidationChange).forEach(callback => callback(false));
            return;
        }

        const fileSignature = `${file.name}-${file.size}-${file.lastModified}`;
        const shouldValidate =
            fileSignature !== lastFileRef.current ||
            debouncedTemplateSignature !== lastTemplateRef.current;

        if (!shouldValidate) return;

        lastFileRef.current = fileSignature;
        lastTemplateRef.current = debouncedTemplateSignature;

        const validateTemplate = async () => {
            setIsLoading(true);
            try {
                const response = await DietUploadService.validateDietTemplate(file, template);

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
                    !hasErrorOfType(['excel', 'struktura'])
                );

                onValidationChange.onMealsPerDayValidation(
                    !hasErrorOfType(['posiłków', 'posilkow', 'meals'])
                );

                onValidationChange.onDateValidation(
                    !hasErrorOfType(['daty', 'date', 'diet'])
                );

                onValidationChange.onMealsConfigValidation(
                    !hasErrorOfType(['posiłku', 'posilku', 'godziny', 'time'])
                );

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
                Object.values(onValidationChange).forEach(callback => callback(false));
            } finally {
                setIsLoading(false);
            }
        };

        validateTemplate().catch(console.error);
    }, [file, debouncedTemplateSignature, onValidationChange, template]);

    if (isLoading) {
        return (
            <div className="flex items-center gap-2 p-4 bg-gray-50 rounded-lg">
                <LoadingSpinner />
                <span>Walidacja w toku...</span>
            </div>
        );
    }

    return (
        <div className="space-y-4 bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-lg font-medium mb-4">Wyniki walidacji</h3>

            {validationResults.length > 0 ? (
                <div className="space-y-2">
                    {validationResults.map((result, index) => (
                        <ValidationMessage
                            key={index}
                            message={result.message}
                            severity={result.severity}
                        />
                    ))}
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