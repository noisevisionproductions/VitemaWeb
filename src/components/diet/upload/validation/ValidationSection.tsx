import React from 'react';
import { DietTemplate } from "../../../../types/diet";
import ExcelStructureValidator from "./ExcelStructureValidator";
import MealsPerDayValidator from "./MealsPerDayValidator";
import DateValidator from "./DateValidator";
import MealsConfigValidator from "./MealsConfigValidator";

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

const ValidationSection: React.FC<ValidationSectionProps> = ({
                                                                 file,
                                                                 template,
                                                                 totalMeals,
                                                                 onValidationChange
                                                             }) => {
    return (
        <div className="space-y-4">
            <ExcelStructureValidator
                file={file}
                onValidationChange={onValidationChange.onExcelStructureValidation}
            />
            {totalMeals > 0 && (
                <>
                    <MealsPerDayValidator
                        totalMeals={totalMeals}
                        mealsPerDay={template.mealsPerDay}
                        onValidationChange={onValidationChange.onMealsPerDayValidation}
                    />
                    <DateValidator
                        startDate={template.startDate}
                        mealsPerDay={template.mealsPerDay}
                        totalMeals={totalMeals}
                        duration={template.duration}
                        onValidationChange={onValidationChange.onDateValidation}
                    />
                    <MealsConfigValidator
                        mealTimes={template.mealTimes}
                        mealTypes={template.mealTypes}
                        onValidationChange={onValidationChange.onMealsConfigValidation}
                    />
                </>
            )}
        </div>
    );
};

export default ValidationSection;