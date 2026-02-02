import React, {useState} from "react";
import {AdminNav} from "../../../types/navigation";
import {
    LayoutDashboard,
    ChevronLeft,
    ChevronRight,
    Home,
    LogOut,
    Newspaper,
    Utensils,
    Shield
} from "lucide-react";
import {cn} from "../../../utils/cs";
import {useAuth} from "../../../contexts/AuthContext";
import {useNavigate} from "react-router-dom";
import {toast} from "../../../utils/toast";

interface AdminSidebarProps {
    activeTab: AdminNav;
    onTabChange: (tab: AdminNav) => void;
    children: React.ReactNode;
}

interface AdminNavButtonProps {
    icon: React.ElementType;
    label: string;
    isActive: boolean;
    onClick: () => void;
    isCollapsed: boolean;
    className?: string;
    showNotification?: boolean;
}

const AdminNavButton: React.FC<AdminNavButtonProps> = ({
                                                           icon: Icon,
                                                           label,
                                                           isActive,
                                                           onClick,
                                                           isCollapsed,
                                                           className,
                                                           showNotification = false
                                                       }) => {
    return (
        <button
            onClick={onClick}
            className={cn(
                "w-full transition-colors relative",
                isCollapsed
                    ? "flex justify-center items-center h-14"
                    : "flex items-center py-3 px-4",
                isActive
                    ? "bg-secondary text-white font-medium"
                    : "text-gray-700 hover:bg-secondary/10",
                className
            )}
        >
            <Icon className={cn("h-5 w-5", isActive ? "text-white" : "text-secondary")}/>

            {!isCollapsed && (
                <span className="ml-3 truncate">{label}</span>
            )}

            {showNotification && (
                <span className={cn(
                    "w-2 h-2 bg-red-500 rounded-full absolute",
                    isCollapsed ? "top-3 right-3" : "top-2 right-2"
                )}/>
            )}
        </button>
    );
};

const AdminSidebar: React.FC<AdminSidebarProps> = ({activeTab, onTabChange, children}) => {
    const [isCollapsed, setIsCollapsed] = useState(true);
    const {logout} = useAuth();
    const navigate = useNavigate();

    const navigationItems = [
        {id: 'adminDashboard', label: 'Pulpit', icon: LayoutDashboard},
        {id: 'newsletter', label: 'Newsletter', icon: Newspaper},
    ] as const;

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/');
            toast.success('Wylogowano pomyślnie');
        } catch (error) {
            toast.error('Wystąpił błąd podczas wylogowywania');
        }
    };

    const handleGoToDietitianPanel = () => {
        navigate('/dashboard');
    };

    const handleGoToHomepage = () => {
        navigate('/');
    };

    return (
        <div className="flex h-screen bg-surface">
            <div className={cn(
                "bg-white shadow-lg h-screen flex flex-col transition-all duration-300",
                isCollapsed ? "w-20" : "w-64"
            )}>
                {/* Header */}
                <div className={cn(
                    "relative border-b border-gray-200 bg-secondary text-white",
                    isCollapsed ? "py-4 flex justify-center" : "p-4 flex items-center"
                )}>
                    {!isCollapsed && (
                        <div className="flex items-center space-x-2">
                            <Shield className="h-6 w-6"/>
                            <h1 className="text-xl font-bold">Panel Administratora</h1>
                        </div>
                    )}

                    {isCollapsed && (
                        <Shield className="h-8 w-8"/>
                    )}

                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        className={cn(
                            "absolute p-1 rounded-full bg-white text-secondary shadow-md hover:bg-gray-50",
                            "transition-all duration-200",
                            isCollapsed ? "-right-3" : "-right-4"
                        )}
                    >
                        {isCollapsed ? (
                            <ChevronRight className="w-4 h-4"/>
                        ) : (
                            <ChevronLeft className="h-4 w-4"/>
                        )}
                    </button>
                </div>

                {/* Navigation */}
                <nav className="flex-1 mt-4">
                    {navigationItems.map((item) => (
                        <AdminNavButton
                            key={item.id}
                            icon={item.icon}
                            label={item.label}
                            isActive={activeTab === item.id}
                            onClick={() => onTabChange(item.id as AdminNav)}
                            isCollapsed={isCollapsed}
                        />
                    ))}
                </nav>

                {/* Przyciski w dolnej części */}
                <div className="p-4 border-t border-gray-200 space-y-2">
                    {/* Przycisk do panelu dietetyka */}
                    <AdminNavButton
                        icon={Utensils}
                        label="Panel Trenera"
                        isActive={false}
                        onClick={handleGoToDietitianPanel}
                        isCollapsed={isCollapsed}
                        className="text-gray-600 hover:text-secondary hover:bg-secondary/10"
                    />

                    {/* Przycisk do strony głównej */}
                    <AdminNavButton
                        icon={Home}
                        label="Strona główna"
                        isActive={false}
                        onClick={handleGoToHomepage}
                        isCollapsed={isCollapsed}
                        className="text-gray-600 hover:text-primary hover:bg-primary-light/10"
                    />

                    {/* Przycisk wylogowania */}
                    <AdminNavButton
                        icon={LogOut}
                        label="Wyloguj"
                        isActive={false}
                        onClick={handleLogout}
                        isCollapsed={isCollapsed}
                        className="text-gray-600 hover:text-red-600 hover:bg-red-50"
                    />
                </div>
            </div>

            <main className="flex-1 p-8 overflow-auto">
                {children}
            </main>
        </div>
    );
};

export default AdminSidebar;