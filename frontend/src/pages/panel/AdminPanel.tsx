import React, {useState} from 'react';
import {AdminNav} from '../../types/navigation';
import AdminDashboard from "../../components/admin/AdminDashboard";
import AdminNewsletterPanel from "../newsletter/AdminNewsletterPanel";
/*import SubscribersManagement from '../../components/panel/newsletter/SubscribersManagement';
import SurveysManagement from '../../components/panel/surveys/SurveysManagement';*/
import BulkEmailSender from '../../components/admin/newsletter/BulkEmailSender';
import ContactMessagesPanel from '../../components/admin/contact/ContactMessagesPanel';
import AdminSidebar from "../../components/navigation/AdminSidebar";
import usePageTitle from "../../hooks/usePageTitle";

const AdminPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<AdminNav>('adminDashboard');

    const titleMap: Record<AdminNav, string> = {
        adminDashboard: 'Pulpit',
        newsletter: 'Newsletter',
        subscribers: 'Subskrybenci',
        surveys: 'Ankiety',
        bulkEmail: 'Masowa wysyłka',
        contactMessages: 'Wiadomości kontaktowe',
    };

    usePageTitle(titleMap[activeTab], 'Panel Administratora');

    const renderContent = () => {
        switch (activeTab) {
            case 'adminDashboard':
                return <AdminDashboard/>;
            case 'newsletter':
                return <AdminNewsletterPanel/>;
            /*     case 'subscribers':
                     return <SubscribersManagement/>;
                 case 'surveys':
                     return <SurveysManagement/>;*/
            case 'bulkEmail':
                return <BulkEmailSender/>;
            case 'contactMessages':
                return <ContactMessagesPanel/>;
            default:
                return <AdminDashboard/>;
        }
    };

    return (
        <AdminSidebar activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </AdminSidebar>
    );
};

export default AdminPanel;