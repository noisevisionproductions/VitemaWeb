import React, {useState} from 'react';
import {AdminNav} from '../../types/navigation';
import AdminDashboard from "../../components/vitema/admin/AdminDashboard";
import AdminNewsletterPanel from "../newsletter/AdminNewsletterPanel";
import BulkEmailSender from '../../components/vitema/admin/newsletter/email-sender/BulkEmailSender';
import ContactMessagesPanel from '../../components/vitema/admin/contact/ContactMessagesPanel';
import AdminSidebar from "../../components/vitema/navigation/AdminSidebar";
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