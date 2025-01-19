import React from "react";
import {LayoutProps} from "../../types";
import Sidebar from "./Sidebar";

const Layout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />
            <main className="flex-1 p-8">
                {children}
            </main>
        </div>
    );
};

export default Layout;