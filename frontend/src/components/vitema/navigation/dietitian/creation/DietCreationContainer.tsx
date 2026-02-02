import React from 'react';
import {MainNav} from "../../../../../types/navigation";
import DietCreator from "../../../diet/creator/DietCreator";
import {useDietitianNavigation} from "../../../../../hooks/useDietitianNavigation";

interface DietCreationContainerProps {
    onTabChange: (tab: MainNav) => void;
}

const DietCreationContainer: React.FC<DietCreationContainerProps> = ({onTabChange}) => {
    const {navigateToTab} = useDietitianNavigation();

    const handleBackToSelection = () => {
        navigateToTab('diets');
    };

    return (
        <DietCreator
            onTabChange={onTabChange}
            onBackToSelection={handleBackToSelection}
        />
    );
};

export default DietCreationContainer;