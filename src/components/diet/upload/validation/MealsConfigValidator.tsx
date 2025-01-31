import React, {useEffect, useState} from 'react';
import ValidationMessage from './ValidationMessage';
import {MealType} from "../../../../types/diet";
import {getMealTypeLabel} from "../../../../utils/mealTypeUtils";

interface MealsConfigValidatorProps {
    mealTimes: { [key: string]: string };
    mealTypes: MealType[];
    onValidationChange: (isValid: boolean) => void;
}

interface TimeValidation {
    isValid: boolean;
    message: string;
    severity: 'error' | 'warning' | 'success';
}

const MealsConfigValidator: React.FC<MealsConfigValidatorProps> = ({
                                                                       mealTimes,
                                                                       mealTypes,
                                                                       onValidationChange
                                                                   }) => {
    const [validations, setValidations] = useState<TimeValidation[]>([]);

    const getTimeInMinutes = (time: string): number => {
        const [hours, minutes] = time.split(':').map(Number);
        return hours * 60 + minutes;
    };

    const validateMealTimes = () => {
        const newValidations: TimeValidation[] = [];
        let isValid = true;

        for (let i = 0; i < mealTypes.length - 1; i++) {
            const currentTime = getTimeInMinutes(mealTimes[`meal_${i}`]);
            const nextTime = getTimeInMinutes(mealTimes[`meal_${i + 1}`]);
            const timeDiff = nextTime - currentTime;

            if (timeDiff <= 0) {
                newValidations.push({
                    isValid: false,
                    message: `${getMealTypeLabel(mealTypes[i + 1])} nie może być wcześniej niż ${getMealTypeLabel(mealTypes[i])}`,
                    severity: 'error'
                });
                isValid = false;
            } else if (timeDiff < 120) {
                newValidations.push({
                    isValid: true,
                    message: `Odstęp między ${getMealTypeLabel(mealTypes[i])} a ${getMealTypeLabel(mealTypes[i + 1])} jest mniejszy niż 2 godziny`,
                    severity: 'warning'
                });
            }
        }

        mealTypes.forEach((type, index) => {
            const time = getTimeInMinutes(mealTimes[`meal_${index}`]);

            switch (type) {
                case MealType.BREAKFAST:
                    if (time < 240  || time > 600) {
                        newValidations.push({
                            isValid: true,
                            message: 'Nietypowa pora na śniadanie',
                            severity: 'warning'
                        });
                    }
                    break;
                case MealType.DINNER:
                    if (time > 1440) {
                        newValidations.push({
                            isValid: true,
                            message: 'Bardzo późna pora na kolację',
                            severity: 'warning'
                        });
                    }
                    break;
            }
        });

        const typeCounts = mealTypes.reduce((acc, type) => {
            acc[type] = (acc[type] || 0) + 1;
            return acc;
        }, {} as Record<string, number>);

        Object.entries(typeCounts).forEach(([type, count]) => {
            if (count > 1) {
                newValidations.push({
                    isValid: false,
                    message: `${getMealTypeLabel(type as MealType)} występuje ${count} razy`,
                    severity: 'error'
                });
                isValid = false;
            }
        });

        setValidations(newValidations);
        onValidationChange(isValid);
    };

    useEffect(() => {
        validateMealTimes();
    }, [mealTimes, mealTypes]);

    return (
        <div className="space-y-2">
            {validations.map((validation, index) => (
                <ValidationMessage
                    key={index}
                    message={validation.message}
                    severity={validation.severity}
                />
            ))}
        </div>
    );
};

export default MealsConfigValidator;