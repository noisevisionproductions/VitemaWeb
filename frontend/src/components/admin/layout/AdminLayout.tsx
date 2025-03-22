import React, {ReactNode, useEffect} from 'react';
import {AdminNav} from "../../../types/navigation";
import AdminNavigation from './AdminNavigation';
import Container from "../../ui/landing/Container";

interface AdminLayoutProps {
    children: ReactNode;
    activeTab: AdminNav;
    onTabChange: (tab: AdminNav) => void;
}

const AdminLayout: React.FC<AdminLayoutProps> = ({children, activeTab, onTabChange}) => {
    useEffect(() => {
        const handleTabChange = (event: CustomEvent) => {
            const tabName = event.detail as AdminNav;
            if (tabName) {
                onTabChange(tabName);
            }
        };

        window.addEventListener('panel-tab-change', handleTabChange as EventListener);

        return () => {
            window.removeEventListener('panel-tab-change', handleTabChange as EventListener);
        };
    }, [onTabChange]);

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex">
                            <div className="flex-shrink-0 flex items-center">
                                <span className="text-xl font-bold text-primary">
                                    Panel Administratora
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="py-10">
                <Container>
                    <div className="lg:grid lg:grid-cols-12 lg:gap-8">
                        <AdminNavigation activeTab={activeTab} onTabChange={onTabChange}/>

                        <main className="lg:col-span-9 xl:col-span-10">
                            <div className="bg-white shadow sm:rounded-lg p-6">
                                {children}
                            </div>
                        </main>
                    </div>
                </Container>
            </div>
        </div>
    );
};

export default AdminLayout;