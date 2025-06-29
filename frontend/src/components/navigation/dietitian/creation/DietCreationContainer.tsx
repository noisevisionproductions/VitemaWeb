import React from 'react';
import {Route, Routes} from "react-router-dom";
import DietCreationMethod, {DietCreationMethodType} from './DietCreationMethod';
import ExcelUpload from "./excel/ExcelUpload";
import {MainNav} from "../../../../types/navigation";
import {ArrowLeft} from 'lucide-react';
import {useDietitianNavigation} from "../../../../hooks/useDietitianNavigation";
import ManualDietCreator from "./manual/ManualDietCreator";

interface DietCreationContainerProps {
    onTabChange: (tab: MainNav) => void;
}

const DietCreationContainer: React.FC<DietCreationContainerProps> = ({onTabChange}) => {
    const {navigateToSubPath} = useDietitianNavigation();

    const handleMethodSelect = (method: DietCreationMethodType) => {
        if (method === 'excel') {
            navigateToSubPath('diet-creation/excel');
        } else if (method === 'manual') {
            navigateToSubPath('diet-creation/manual');
        }
    };

    const handleBackToSelection = () => {
        navigateToSubPath('diet-creation');
    };

    const renderManualCreation = () => {
        return (
            <div className="space-y-6 pb-8">
                <div className="flex items-center space-x-4 mb-6">
                    <button
                        onClick={handleBackToSelection}
                        className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                    >
                        <ArrowLeft className="h-4 w-4 mr-2"/>
                        Powrót do wyboru metody
                    </button>
                </div>
                <ManualDietCreator onTabChange={onTabChange}/>
            </div>
        );
    };

    return (
        <Routes>

            {/* Główna strona wyboru metody */}
            <Route
                path=""
                element={<DietCreationMethod onMethodSelect={handleMethodSelect}/>}
            />

            {/* Excel upload */}
            <Route
                path="excel"
                element={
                    <div>
                        <div className="flex items-center space-x-4 mb-6">
                            <button
                                onClick={handleBackToSelection}
                                className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                            >
                                <ArrowLeft className="h-4 w-4 mr-2"/>
                                Powrót do wyboru metody
                            </button>
                        </div>
                        <ExcelUpload onTabChange={onTabChange}/>
                    </div>
                }
            />

            {/* Manual creation */}
            <Route
                path="manual"
                element={
                    <ManualDietCreator
                        onTabChange={onTabChange}
                        onBackToSelection={handleBackToSelection}
                    />
                }
            />

            {/* Fallback */}
            <Route
                path="*"
                element={<DietCreationMethod onMethodSelect={handleMethodSelect}/>}
            />
        </Routes>
    );
};

export default DietCreationContainer;