import React, {useState} from "react";
import {MainNav} from "../../types/navigation";
import {
    LayoutDashboard,
    ChevronLeft,
    ChevronRight,
    ClipboardList,
    FileSpreadsheet,
    HelpCircle,
    Home,
    LogOut,
    Upload,
    Users,
    Shield,
    BookOpen,
    UtensilsCrossed  // Zmieniona ikona
} from "lucide-react";
import {cn} from "../../utils/cs";
import {useAuth} from "../../contexts/AuthContext";
import {useNavigate} from "react-router-dom";
import {toast} from "../../utils/toast";
import {useChangeLog} from "../../hooks/useChangeLog";

interface DietitianNavButtonProps {
    icon: React.ElementType;
    label: string;
    isActive: boolean;
    onClick: () => void;
    isCollapsed: boolean;
    className?: string;
    showNotification?: boolean;
}

// Własny komponent przycisku nawigacyjnego dla panelu dietetyka
const DietitianNavButton: React.FC<DietitianNavButtonProps> = ({
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
                    ? "bg-primary-light text-white font-medium"
                    : "text-gray-700 hover:bg-primary-light/10",
                className
            )}
        >
            <Icon className={cn("h-5 w-5", isActive ? "text-white" : "text-primary")} />

            {!isCollapsed && (
                <span className="ml-3 truncate">{label}</span>
            )}

            {showNotification && (
                <span className={cn(
                    "w-2 h-2 bg-red-500 rounded-full absolute",
                    isCollapsed ? "top-3 right-3" : "top-2 right-2"
                )} />
            )}
        </button>
    );
};

interface SidebarProps {
    activeTab: MainNav;
    onTabChange: (tab: MainNav) => void;
}

const navigationItems = [
    {id: 'upload', label: 'Upload Excel', icon: Upload},
    {id: 'diets', label: 'Diety', icon: FileSpreadsheet},
    {id: 'recipes', label: 'Przepisy', icon: BookOpen},
    {id: 'users', label: 'Użytkownicy', icon: Users},
    {id: 'stats', label: 'Statystyki', icon: LayoutDashboard},
    {id: 'guide', label: 'Przewodnik', icon: HelpCircle},
    {id: 'changelog', label: 'Historia zmian', icon: ClipboardList},
] as const;

const DietitianSidebar: React.FC<SidebarProps> = ({activeTab, onTabChange}) => {
    const [isCollapsed, setIsCollapsed] = useState(true);
    const {logout, isAdmin} = useAuth();
    const {hasUnread} = useChangeLog();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/');
            toast.success('Wylogowano pomyślnie');
        } catch (error) {
            toast.error('Wystąpił błąd podczas wylogowywania');
        }
    };

    const handleGoToHomepage = () => {
        navigate('/');
    };

    const filteredNavigationItems = navigationItems.filter(() =>
        isAdmin
    );

    return (
        <div className="flex h-screen bg-surface">
            <div className={cn(
                "bg-white shadow-lg h-screen flex flex-col transition-all duration-300",
                isCollapsed ? "w-20" : "w-64"
            )}>
                {/* Header */}
                <div className={cn(
                    "relative border-b border-gray-200 bg-primary text-white",
                    isCollapsed ? "py-4 flex justify-center" : "p-4 flex items-center"
                )}>
                    {!isCollapsed && (
                        <div className="flex items-center space-x-2">
                            <UtensilsCrossed className="h-6 w-6" />
                            <h1 className="text-xl font-bold">Panel Dietetyka</h1>
                        </div>
                    )}

                    {isCollapsed && (
                        <UtensilsCrossed className="h-8 w-8" />
                    )}

                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        className={cn(
                            "absolute p-1 rounded-full bg-white text-primary shadow-md hover:bg-gray-50",
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
                    {filteredNavigationItems.map((item) => (
                        <DietitianNavButton
                            key={item.id}
                            icon={item.icon}
                            label={item.label}
                            isActive={activeTab === item.id}
                            onClick={() => onTabChange(item.id as MainNav)}
                            isCollapsed={isCollapsed}
                            showNotification={item.id === 'changelog' && hasUnread}
                        />
                    ))}
                </nav>

                {/* Przyciski w dolnej części */}
                <div className="p-4 border-t border-gray-200 space-y-2">
                    {isAdmin() && (
                        <DietitianNavButton
                            icon={Shield}
                            label="Panel administratora"
                            isActive={false}
                            onClick={() => navigate('/admin')}
                            isCollapsed={isCollapsed}
                            className="text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50"
                        />
                    )}

                    {/* Przycisk do strony głównej */}
                    <DietitianNavButton
                        icon={Home}
                        label="Strona główna"
                        isActive={false}
                        onClick={handleGoToHomepage}
                        isCollapsed={isCollapsed}
                        className="text-gray-600 hover:text-primary hover:bg-primary-light/10"
                    />

                    {/* Przycisk wylogowania */}
                    <DietitianNavButton
                        icon={LogOut}
                        label="Wyloguj"
                        isActive={false}
                        onClick={handleLogout}
                        isCollapsed={isCollapsed}
                        className="text-gray-600 hover:text-red-600 hover:bg-red-50"
                    />
                </div>
            </div>
        </div>
    );
};

export default DietitianSidebar;