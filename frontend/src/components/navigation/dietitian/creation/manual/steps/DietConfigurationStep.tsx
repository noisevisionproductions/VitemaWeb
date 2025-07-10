import React from "react";
import {DietExcelTemplate, ManualDietData} from "../../../../../../types";
import {User} from "../../../../../../types/user";
import UserSelectionSection from "../../../../../diet/upload/sections/user/UserSelectionSection";
import {Timestamp} from "firebase/firestore";
import DietTemplateConfig from "../../../../../diet/upload/sections/DietTemplateConfig";

interface DietConfigurationStepProps {
    dietData: ManualDietData;
    onUpdate: (updates: Partial<ManualDietData>) => void;
    selectedUser: User | null;
    onUserSelect: (user: User | null) => void;
}

const DietConfigurationStep: React.FC<DietConfigurationStepProps> = ({
                                                                         dietData,
                                                                         onUpdate,
                                                                         selectedUser,
                                                                         onUserSelect
                                                                     }) => {
    const template: DietExcelTemplate = {
        mealsPerDay: dietData.mealsPerDay,
        startDate: Timestamp.fromDate(new Date(dietData.startDate)),
        duration: dietData.duration,
        mealTimes: dietData.mealTimes,
        mealTypes: dietData.mealTypes
    };

    const handleUserSelect = (user: User | null) => {
        onUserSelect(user);
        onUpdate({userId: user?.id || ''});
    };

    const handleTemplateChange = (newTemplate: DietExcelTemplate) => {
        onUpdate({
            mealsPerDay: newTemplate.mealsPerDay,
            startDate: newTemplate.startDate.toDate().toISOString().split('T')[0],
            duration: newTemplate.duration,
            mealTimes: newTemplate.mealTimes,
            mealTypes: newTemplate.mealTypes
        });
    };

    return (
        <div className="space-y-8">
            {/* User selection */}
            <UserSelectionSection
                selectedUser={selectedUser}
                onUserSelect={handleUserSelect}
            />

            {/* Diet template configuration */}
            <DietTemplateConfig
                template={template}
                onTemplateChange={handleTemplateChange}
            />
        </div>
    );
};

export default DietConfigurationStep;