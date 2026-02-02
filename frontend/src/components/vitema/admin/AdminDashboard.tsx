import React from 'react';
import {Link} from 'react-router-dom';
import {
    ChartBarIcon,
    UserGroupIcon
} from '@heroicons/react/24/outline';
import {useAuth} from "../../../contexts/AuthContext";

const AdminDashboard: React.FC = () => {
    const {currentUser} = useAuth();

    const adminModules = [
        {
            name: 'Statystyki newslettera',
            description: 'Przegląd statystyk newslettera, aktywnych subskrybentów i wskaźników.',
            icon: ChartBarIcon,
            tabName: 'newsletter'
        },
        {
            name: 'Zarządzanie subskrybentami',
            description: 'Przeglądaj, weryfikuj i zarządzaj subskrybentami newslettera.',
            icon: UserGroupIcon,
            tabName: 'subscribers'
        }
    ];

    const handleModuleClick = (tabName: string) => {
        window.dispatchEvent(new CustomEvent('panel-tab-change', {detail: tabName}));
    };

    return (
        <div className="space-y-6 pb-8">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold mb-4">
                        Panel Administratora
                    </h1>
                    <p className="text-slate-500 text-sm mt-1">
                        Witaj, {currentUser?.displayName || currentUser?.email}! Zarządzaj ustawieniami systemu,
                        newsletterem i wiadomościami kontaktowymi.
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {adminModules.map((module) => {
                    const Icon = module.icon;

                    return (
                        <div
                            key={module.name}
                            className="relative bg-white p-6 border border-gray-200 rounded-lg overflow-hidden hover:shadow-md transition duration-200 cursor-pointer hover:border-primary-light group"
                            onClick={() => handleModuleClick(module.tabName)}
                        >
                            <div className="flex items-center mb-4">
                                <div
                                    className="p-2 rounded-md bg-primary-light/10 group-hover:bg-primary-light/20 transition-colors">
                                    <Icon className="h-6 w-6 text-primary" aria-hidden="true"/>
                                </div>
                                <h3 className="ml-3 text-lg font-medium text-gray-900 group-hover:text-primary">
                                    {module.name}
                                </h3>
                            </div>
                            <p className="text-sm text-gray-500 mb-4">
                                {module.description}
                            </p>
                            <span
                                className="text-sm font-medium text-primary hover:text-primary-dark group-hover:underline">
                                Przejdź &rarr;
                            </span>
                        </div>
                    );
                })}
            </div>

            <div className="mt-8 bg-gray-50 p-6 rounded-lg border border-gray-200">
                <h3 className="text-lg font-medium text-gray-900 mb-3">
                    Skróty do panelu trenera
                </h3>
                <div className="flex space-x-4">
                    <Link
                        to="/dashboard"
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary hover:bg-primary-dark"
                    >
                        Panel Trenera
                    </Link>
                    <Link
                        to="/frontend/public"
                        className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                    >
                        Strona główna
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;