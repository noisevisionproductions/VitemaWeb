import {
    ClipboardDocumentCheckIcon, // Pasuje do: Szablony / Żywienie bez bycia dietetykiem
    ShoppingCartIcon,           // Pasuje do: Listy Zakupów
    UsersIcon,                  // Pasuje do: Centrum dowodzenia (klienci)
    DevicePhoneMobileIcon,      // Pasuje do: Aplikacja mobilna (ZAMIAST Excela)
    ChartBarIcon,               // Pasuje do: Wizualizacja postępów
    ArrowTrendingUpIcon         // Pasuje do: Skalowanie (nowa funkcja)
} from '@heroicons/react/24/outline';
import React from "react";

export interface Feature {
    id: number;
    icon: React.ElementType;
    status?: 'available' | 'coming_soon';
}

export const features: Feature[] = [
    {
        id: 1,
        icon: ClipboardDocumentCheckIcon,
        status: 'available'
    },
    {
        id: 2,
        icon: ShoppingCartIcon,
        status: 'available'
    },
    {
        id: 3,
        icon: UsersIcon,
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
        status: 'coming_soon'
    },
    {
        id: 6,
        icon: ArrowTrendingUpIcon,
        status: 'coming_soon'
    }
];