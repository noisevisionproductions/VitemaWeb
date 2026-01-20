import {LucideIcon} from "lucide-react";
import React from "react";
import {cn} from "../../../utils/cs";

interface NavButtonProps {
    icon: LucideIcon;
    label: string;
    isActive: boolean;
    onClick: () => void;
    className?: string;
    isCollapsed?: boolean;
    showNotification?: boolean;
}

const NavButton: React.FC<NavButtonProps> = ({
                                                 icon: Icon,
                                                 label,
                                                 isActive,
                                                 onClick,
                                                 className = '',
                                                 isCollapsed = false,
                                                 showNotification
                                             }) => {
    return (
        <button
            onClick={onClick}
            className={cn(
                "w-full flex items-center px-4 py-3 transition-all duration-200",
                "hover:bg-gray-50",
                isActive && "bg-blue-50 text-blue-600",
                isCollapsed ? "justify-center" : "justify-start",
                className
            )}
        >
            <div className="relative">
                <Icon className={cn(
                    "w-5 h-5",
                    isCollapsed ? "mr-0" : "mr-3"
                )}/>
                {showNotification && (
                    <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"/>
                )}
            </div>
            {!isCollapsed && (
                <span className="transition-opacity duration-200">
                    {label}
                </span>
            )}
        </button>
    );
};

export default NavButton;