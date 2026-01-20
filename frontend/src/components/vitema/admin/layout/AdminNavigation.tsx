import React from 'react';
import { AdminNav} from "../../../../types/navigation";
import {
    ChartBarIcon,
    EnvelopeIcon,
    UserGroupIcon,
    ClipboardDocumentCheckIcon,
    EnvelopeOpenIcon,
    ChatBubbleLeftRightIcon
} from '@heroicons/react/24/outline';

interface AdminNavigationProps {
    activeTab: AdminNav;
    onTabChange: (tab: AdminNav) => void;
}

const AdminNavigation: React.FC<AdminNavigationProps> = ({ activeTab, onTabChange }) => {
    const tabs = [
        { name: 'dashboard', label: 'Pulpit', icon: ChartBarIcon },
        { name: 'newsletter', label: 'Newsletter', icon: EnvelopeIcon },
        { name: 'subscribers', label: 'Subskrybenci', icon: UserGroupIcon },
        { name: 'surveys', label: 'Ankiety', icon: ClipboardDocumentCheckIcon },
        { name: 'bulkEmail', label: 'Masowa wysyłka', icon: EnvelopeOpenIcon },
        { name: 'contactMessages', label: 'Wiadomości kontaktowe', icon: ChatBubbleLeftRightIcon },
    ];

    return (
        <aside className="py-6 px-2 sm:px-6 lg:col-span-3 xl:col-span-2">
            <nav className="space-y-1">
                {tabs.map((tab) => {
                    const Icon = tab.icon;
                    const isActive = activeTab === tab.name;

                    return (
                        <button
                            key={tab.name}
                            onClick={() => onTabChange(tab.name as AdminNav)}
                            className={`${
                                isActive
                                    ? 'bg-primary-light text-white'
                                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                            } group flex items-center px-3 py-2 text-sm font-medium rounded-md w-full`}
                        >
                            <Icon
                                className={`${
                                    isActive
                                        ? 'text-white'
                                        : 'text-gray-400 group-hover:text-gray-500'
                                } flex-shrink-0 -ml-1 mr-3 h-6 w-6`}
                            />
                            <span className="truncate">{tab.label}</span>
                        </button>
                    );
                })}
            </nav>
        </aside>
    );
};

export default AdminNavigation;