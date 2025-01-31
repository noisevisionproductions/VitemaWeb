import React, { useEffect, useCallback } from 'react';
import ValidationMessage from './ValidationMessage';
import { Timestamp } from 'firebase/firestore';

interface DateValidatorProps {
    startDate: Timestamp;
    mealsPerDay: number;
    totalMeals: number;
    duration: number;
    onValidationChange: (isValid: boolean) => void;
}

const DateValidator: React.FC<DateValidatorProps> = React.memo(({
                                                                    startDate,
                                                                    mealsPerDay,
                                                                    totalMeals,
                                                                    duration,
                                                                    onValidationChange
                                                                }) => {
    const validateDate = useCallback(() => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const selectedDate = startDate.toDate();
        selectedDate.setHours(0, 0, 0, 0);

        if (selectedDate < today) {
            return {
                isValid: false,
                message: 'Data rozpoczęcia diety nie może być w przeszłości',
                severity: 'error' as const
            };
        }

        const maxPossibleDays = Math.floor(totalMeals / mealsPerDay);
        if (duration > maxPossibleDays) {
            return {
                isValid: false,
                message: `Wybrana długość diety (${duration} dni) przekracza możliwą długość na podstawie liczby posiłków (${maxPossibleDays} dni)`,
                severity: 'error' as const
            };
        }

        if (duration > 30) {
            return {
                isValid: true,
                message: 'Uwaga: Wybrano dietę dłuższą niż 30 dni',
                severity: 'warning' as const
            };
        }

        return {
            isValid: true,
            message: `Plan diety na ${duration} dni jest poprawny`,
            severity: 'success' as const
        };
    }, [startDate, mealsPerDay, totalMeals, duration]);

    useEffect(() => {
        const validation = validateDate();
        onValidationChange(validation.isValid);
    }, [validateDate, onValidationChange]);

    const validation = validateDate();
    return <ValidationMessage message={validation.message} severity={validation.severity} />;
});

DateValidator.displayName = 'DateValidator';

export default DateValidator;