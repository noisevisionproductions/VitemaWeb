import React, {useState} from 'react';
import AdminLayout from '../components/layout/AdminLayout';
import {TabName} from '../types/navigation';
import ExcelUpload from "../components/diet/upload/ExcelUpload";
import UsersManagement from "../components/users/UsersManagement";
import DietManagement from "../components/diet/DietManagement";
import StatsPanel from "../components/stats/StatsPanel";

const AdminPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<TabName>('upload');

    const renderContent = () => {
        switch (activeTab) {
            case 'upload':
                return (
                    <div>
                        <h2 className="text-2xl font-bold mb-4">Upload Plików Excel</h2>
                        <ExcelUpload/>
                    </div>
                );
            case 'data':
                return (
                    <div>
                        <h2 className="text-2xl font-bold mb-4">Zarządzanie Dietami</h2>
                        <DietManagement/>
                    </div>
                );
            case 'users':
                return <UsersManagement/>
            case 'stats':
                return <StatsPanel />;
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