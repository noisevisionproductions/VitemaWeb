import {MainNav} from "../../../types/navigation";
import React from "react";
import TrainerSidebar from "./TrainerSidebar";

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
            <TrainerSidebar
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