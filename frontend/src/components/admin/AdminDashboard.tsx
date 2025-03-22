import React from 'react';
import { Link } from 'react-router-dom';
import {
    ChartBarIcon,
    UserGroupIcon,
    ClipboardDocumentCheckIcon,
    EnvelopeOpenIcon,
    ChatBubbleLeftRightIcon
} from '@heroicons/react/24/outline';
import { useAuth} from "../../contexts/AuthContext";

const AdminDashboard: React.FC = () => {
    const { currentUser } = useAuth();

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
        },
        {
            name: 'Masowa wysyłka wiadomości',
            description: 'Wysyłaj wiadomości email do wszystkich aktywnych subskrybentów.',
            icon: EnvelopeOpenIcon,
            tabName: 'bulkEmail'
        },
        {
            name: 'Ankiety i badania',
            description: 'Zarządzaj ankietami i przeglądaj wyniki badań.',
            icon: ClipboardDocumentCheckIcon,
            tabName: 'surveys'
        },
        {
            name: 'Wiadomości kontaktowe',
            description: 'Przeglądaj i odpowiadaj na wiadomości z formularza kontaktowego.',
            icon: ChatBubbleLeftRightIcon,
            tabName: 'contactMessages'
        }
    ];

    return (
        <div className="space-y-8">
            <div className="bg-white">
                <div className="pb-5 border-b border-gray-200">
                    <h2 className="text-2xl font-bold leading-6 text-gray-900">
                        Panel Administratora
                    </h2>
                    <p className="mt-2 max-w-4xl text-sm text-gray-500">
                        Witaj, {currentUser?.displayName || currentUser?.email}! Zarządzaj ustawieniami systemu, newsletterem i wiadomościami kontaktowymi.
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {adminModules.map((module) => {
                    const Icon = module.icon;

                    return (
                        <div
                            key={module.name}
                            className="relative bg-white p-6 border border-gray-200 rounded-lg overflow-hidden hover:shadow-md transition duration-200"
                        >
                            <div className="flex items-center mb-4">
                                <div className="p-2 rounded-md bg-primary-light/10">
                                    <Icon className="h-6 w-6 text-primary" aria-hidden="true" />
                                </div>
                                <h3 className="ml-3 text-lg font-medium text-gray-900">
                                    {module.name}
                                </h3>
                            </div>
                            <p className="text-sm text-gray-500 mb-4">
                                {module.description}
                            </p>
                            <button
                                onClick={() => window.dispatchEvent(new CustomEvent('panel-tab-change', { detail: module.tabName }))}
                                className="text-sm font-medium text-primary hover:text-primary-dark"
                            >
                                Przejdź &rarr;
                            </button>
                        </div>
                    );
                })}
            </div>

            <div className="mt-8 bg-gray-50 p-6 rounded-lg border border-gray-200">
                <h3 className="text-lg font-medium text-gray-900 mb-3">
                    Skróty do panelu dietetyka
                </h3>
                <div className="flex space-x-4">
                    <Link
                        to="/dashboard"
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary hover:bg-primary-dark"
                    >
                        Panel dietetyka
                    </Link>
                    <Link
                        to="/"
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