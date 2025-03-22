import React, {useState} from 'react';
import Sidebar from '../../components/navigation/Sidebar';
import {MainNav} from "../../types/navigation";
import ExcelUpload from "../../components/diet/upload/ExcelUpload";
import UsersManagement from "../../components/users/UsersManagement";
import DietManagement from "../../components/diet/DietManagement";
import StatsPanel from "../../components/stats/StatsPanel";
import DietGuide from "../../components/guide/DietGuide";
import Changelog from "../../components/changelog/Changelog";
import RecipesPage from "../../components/recipes/RecipesPage";
import usePageTitle from "../../hooks/usePageTitle";

const MainPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<MainNav>('upload');

    const titleMap: Record<MainNav, string> = {
        upload: 'Upload Excel',
        diets: 'Diety',
        recipes: 'Przepisy',
        users: 'Użytkownicy',
        stats: 'Statystyki',
        guide: 'Przewodnik',
        changelog: 'Historia zmian',
        landing: 'Strona główna',
        dietitianDashboard: 'Panel Dietetyka'
    };

    usePageTitle(titleMap[activeTab], 'Panel Dietetyka');

    const renderContent = () => {
        switch (activeTab) {
            case 'upload':
                return (
                    <div>
                        <h2 className="text-2xl font-bold mb-4">Upload Plików Excel</h2>
                        <ExcelUpload onTabChange={setActiveTab}/>
                    </div>
                );
            case 'diets':
                return <DietManagement/>
            case "recipes":
                return <RecipesPage/>
            case 'users':
                return <UsersManagement/>
            case 'stats':
                return <StatsPanel/>
            case "guide":
                return (
                    <div>
                        <h2 className="text-2xl font-bold mb-4">Przewodnik</h2>
                        <DietGuide/>
                    </div>
                );
            case "changelog":
                return <Changelog/>
            case "landing":
            default:
                return null;
        }
    };

    return (
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </Sidebar>
    );
};

export default MainPanel;