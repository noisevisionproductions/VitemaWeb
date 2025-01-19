import React from "react";
import {useLocation, Link} from "react-router-dom";
import {Database, Upload, Users} from "lucide-react";
import {MenuItem, SidebarItemProps} from "../../types";

const SidebarItem: React.FC<SidebarItemProps> = ({icon: Icon, text, to, isActive}) => (
    <Link to={to}
          className={`flex items-center gap-3 px-4 py-3 transition-colors
            ${isActive
              ? 'bg-blue-100 text-blue 600'
              : 'hover:bg-gray-100'
          }`}
    >
        <Icon className="w-5 h-5"/>
        <span>{text}</span>
    </Link>
);

const Sidebar: React.FC = () => {
    const location = useLocation();

    const menuItems: MenuItem[] = [
        {icon: Upload, text: 'Dodaj Excel', path: '/upload'},
        {icon: Database, text: 'Zarządzanie Plikami', path: '/data'},
        {icon: Users, text: 'Użytkownicy', path: '/users'}
    ];

    return (
        <div className="w-64 h-screen bg-white border-r">
            <div className="p-4 border-b">
                <h1 className="text-xl font-bold">Panel Admina</h1>
            </div>
            <nav className="mt-4">
                {menuItems.map((item) => (
                    <SidebarItem
                        key={item.path}
                        icon={item.icon}
                        text={item.text}
                        to={item.path}
                        isActive={location.pathname === item.path}
                    />
                ))}
            </nav>
        </div>
    );
};

export default Sidebar;