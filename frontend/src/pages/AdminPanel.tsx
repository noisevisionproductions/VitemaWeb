import React, {useState} from 'react';
import AdminLayout from '../components/layout/AdminLayout';
import {TabName} from '../types/navigation';
import ExcelUpload from "../components/diet/upload/ExcelUpload";
import UsersManagement from "../components/users/UsersManagement";
import DietManagement from "../components/diet/DietManagement";
import StatsPanel from "../components/stats/StatsPanel";
import DietGuide from "../components/guide/DietGuide";
import Changelog from "../components/changelog/Changelog";

const AdminPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<TabName>('upload');

    const renderContent = () => {
        switch (activeTab) {
            case 'upload':
                return (
                    <div>
                        <h2 className="text-2xl font-bold mb-4">Upload Plików Excel</h2>
                        <ExcelUpload onTabChange={setActiveTab}/>
                    </div>
                );
            case 'data':
                return <DietManagement/>
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
            default:
                return null;
        }
    };

    return (
        <AdminLayout activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </AdminLayout>
    );
};

export default AdminPanel;