import React, { useEffect, useCallback } from 'react';
import ValidationMessage from './ValidationMessage';

interface MealsPerDayValidatorProps {
    totalMeals: number;
    mealsPerDay: number;
    onValidationChange: (isValid: boolean) => void;
}

const MealsPerDayValidator: React.FC<MealsPerDayValidatorProps> = React.memo(({
                                                                                  totalMeals,
                                                                                  mealsPerDay,
                                                                                  onValidationChange
                                                                              }) => {
    const validateMealsCount = useCallback(() => {
        if (totalMeals === 0) {
            return {
                isValid: false,
                message: 'Nie wczytano żadnych posiłków z pliku Excel.',
                severity: 'error' as const
            };
        }

        const remainder = totalMeals % mealsPerDay;
        const numberOfDays = Math.floor(totalMeals / mealsPerDay);

        if (remainder === 0) {
            return {
                isValid: true,
                message: `Plik zawiera ${numberOfDays} pełnych dni po ${mealsPerDay} posiłków.`,
                severity: 'success' as const
            };
        }

        return {
            isValid: false,
            message: `Liczba posiłków (${totalMeals}) nie jest podzielna przez ${mealsPerDay}. Brakuje ${mealsPerDay - remainder} posiłków do pełnego dnia.`,
            severity: 'error' as const
        };
    }, [totalMeals, mealsPerDay]);

    useEffect(() => {
        const validation = validateMealsCount();
        onValidationChange(validation.isValid);
    }, [validateMealsCount, onValidationChange]);

    const validation = validateMealsCount();
    return <ValidationMessage message={validation.message} severity={validation.severity} />;
});

MealsPerDayValidator.displayName = 'MealsPerDayValidator';

export default MealsPerDayValidator;