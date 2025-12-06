import {
    UserIcon,
    BuildingOfficeIcon,
    AcademicCapIcon,
    UserGroupIcon
} from '@heroicons/react/24/outline';
import React from "react";

export interface UserType {
    id: number;
    icon: React.ElementType;
    primary?: boolean;
}

export const userTypes: UserType[] = [
    {
        id: 1,
        icon: UserIcon,
        primary: true
    },
    {
        id: 2,
        icon: BuildingOfficeIcon,
        primary: true
    },
    {
        id: 3,
        icon: AcademicCapIcon,
        primary: true
    },
    {
        id: 4,
        icon: UserGroupIcon
    }
];