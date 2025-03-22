import {MainNav} from "../../types/navigation";
import React from "react";
import DietitianSidebar from "./DietitianSidebar";

interface AdminLayoutProps {
    children: React.ReactNode;
    activeTab: MainNav;
    onTabChange: (tab: MainNav) => void;
}

const Sidebar: React.FC<AdminLayoutProps> = ({
                                                          children,
                                                          activeTab,
                                                          onTabChange
                                                      }) => {
    return (
        <div className="flex h-screen bg-gray-100">
            <DietitianSidebar
                activeTab={activeTab}
                onTabChange={onTabChange}
            />
            <main className="flex-1 p-8 overflow-auto">
                {children}
            </main>
        </div>
    );
};

export default Sidebar;