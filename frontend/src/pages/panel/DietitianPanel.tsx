import React, {useEffect, useState} from 'react';
import {MainNav} from "../../types/navigation";
import ExcelUpload from "../../components/navigation/dietitian/creation/excel/ExcelUpload";
import UsersManagement from "../../components/navigation/dietitian/creation/excel/UsersManagement";
import DietManagement from "../../components/diet/DietManagement";
import StatsPanel from "../../components/stats/StatsPanel";
import DietGuide from "../../components/navigation/dietitian/creation/excel/DietGuide";
import Changelog from "../../components/navigation/dietitian/creation/excel/Changelog";
import RecipesPage from "../../components/navigation/dietitian/creation/excel/RecipesPage";
import usePageTitle from "../../hooks/usePageTitle";
import DietitianDashboard from "../../components/navigation/dietitian/creation/excel/DietitianDashboard";
import DietitianSidebar from "../../components/navigation/DietitianSidebar";
import DietCreationContainer from "../../components/navigation/dietitian/creation/DietCreationContainer";



const DietitianPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<MainNav>('dietitianDashboard');

    useEffect(() => {
        const handleTabChange = (event: CustomEvent) => {
            setActiveTab(event.detail as MainNav);
        };

        window.addEventListener('panel-tab-change', handleTabChange as EventListener);

        return () => {
            window.removeEventListener('panel-tab-change', handleTabChange as EventListener);
        };
    }, []);

    const titleMap: Record<MainNav, string> = {
        dietitianDashboard: 'Pulpit',
        dietCreation: 'Tworzenie diety',
        upload: 'Import Excel',
        diets: 'Zarządzanie dietami',
        users: 'Klienci',
        stats: 'Statystyki',
        guide: 'Przewodnik',
        changelog: 'Historia zmian',
        landing: 'Strona główna',
        recipes: 'Przepisy'
    };

    usePageTitle(titleMap[activeTab], 'Panel Dietetyka');

    const renderContent = () => {
        switch (activeTab) {
            case 'dietitianDashboard':
                return <DietitianDashboard/>;
            case 'dietCreation':
                return <DietCreationContainer onTabChange={setActiveTab}/>;
            case 'upload':
                return <ExcelUpload onTabChange={setActiveTab}/>;
            case 'diets':
                return <DietManagement/>;
            case 'users':
                return <UsersManagement/>;
            case 'stats':
                return <StatsPanel/>;
            case 'guide':
                return <DietGuide/>;
            case 'changelog':
                return <Changelog/>;
            case 'recipes':
                return <RecipesPage/>;
            default:
                return <DietitianDashboard/>;
        }
    };

    return (
        <DietitianSidebar activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </DietitianSidebar>
    );
};

export default DietitianPanel;