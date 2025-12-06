import {
    ArrowsPointingInIcon,
    ChartBarIcon,
    DevicePhoneMobileIcon,
    DocumentTextIcon,
    ListBulletIcon,
    UserGroupIcon
} from '@heroicons/react/24/outline';
import * as React from "react";

export interface Feature {
    id: number;
    icon: React.ElementType;
    status?: 'available' | 'coming_soon';
}

export const features: Feature[] = [
    {
        id: 1,
        icon: ArrowsPointingInIcon,
        status: 'available'
    },
    {
        id: 2,
        icon: ListBulletIcon,
        status: 'available'
    },
    {
        id: 3,
        icon: UserGroupIcon,
        status: 'available'
    },
    {
        id: 4,
        icon: DevicePhoneMobileIcon,
        status: 'available'
    },
    {
        id: 5,
        icon: ChartBarIcon,
        status: 'available'
    },
    {
        id: 6,
        icon: DocumentTextIcon,
        status: 'available'
    }
];