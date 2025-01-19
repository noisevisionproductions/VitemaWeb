import {LucideIcon} from "lucide-react";
import React from "react";

export interface SidebarItemProps {
    icon: LucideIcon;
    text: string;
    to: string;
    isActive: boolean;
}

export interface LayoutProps {
    children: React.ReactNode;
}

export interface MenuItem{
    icon: LucideIcon;
    text: string;
    path: string;
}