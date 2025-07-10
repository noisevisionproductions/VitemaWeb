import React, {useEffect} from 'react';
import {Route, Routes} from "react-router-dom";
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
import {useDietitianNavigation} from "../../hooks/useDietitianNavigation";
import {MainNav} from "../../types/navigation";
import DietTemplatesManager from "../../components/diet/templates/DietTemplatesManager";

const DietitianPanel: React.FC = () => {
    const {currentTab, navigateToTab} = useDietitianNavigation();

    const titleMap: Record<MainNav, string> = {
        dietitianDashboard: 'Pulpit',
        dietCreation: 'Tworzenie diety',
        dietTemplates: 'Gotowe szablony diet',
        upload: 'Import Excel',
        diets: 'Zarządzanie dietami',
        users: 'Klienci',
        stats: 'Statystyki',
        guide: 'Przewodnik',
        changelog: 'Historia zmian',
        landing: 'Strona główna',
        recipes: 'Przepisy'
    };

    usePageTitle(titleMap[currentTab], 'Panel Dietetyka');

    useEffect(() => {
        const handleTabChangeEvent = (event: CustomEvent) => {
            const tab = event.detail as MainNav;
            navigateToTab(tab);
        };

        window.addEventListener('panel-tab-change', handleTabChangeEvent as EventListener);

        return () => {
            window.removeEventListener('panel-tab-change', handleTabChangeEvent as EventListener);
        };
    }, [navigateToTab]);

    return (
        <DietitianSidebar activeTab={currentTab} onTabChange={navigateToTab}>
            <Routes>
                <Route path="" element={<DietitianDashboard/>}/>

                {/* Zagnieżdżone rout dla tworzenia diety */}
                <Route
                    path="diet-creation/*"
                    element={<DietCreationContainer onTabChange={navigateToTab}/>}
                />

                {/* Stara ścieżka upload-dla kompatybilności */}
                <Route path="upload" element={<ExcelUpload onTabChange={navigateToTab}/>}/>

                <Route path="diets" element={<DietManagement/>}/>
                <Route path="diet-templates" element={<DietTemplatesManager/>}/>
                <Route path="users" element={<UsersManagement/>}/>
                <Route path="stats" element={<StatsPanel/>}/>
                <Route path="guide" element={<DietGuide/>}/>
                <Route path="changelog" element={<Changelog/>}/>
                <Route path="recipes" element={<RecipesPage/>}/>

                {/* Fallback */}
                <Route path="*" element={<DietitianDashboard/>}/>
            </Routes>
        </DietitianSidebar>
    );
};

export default DietitianPanel;