import React, {useState} from 'react';
import NewsletterStats from "../../components/admin/newsletter/NewsletterStats";
import BulkEmailSender from "../../components/admin/newsletter/BulkEmailSender";
import SubscribersList from "../../components/admin/newsletter/SubscribersList";
import SurveyResults from "../../components/admin/newsletter/survey/SurveyResults";

type NewsletterTabType = 'stats' | 'subscribers' | 'bulkEmail' | 'surveys';

const AdminNewsletterPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<NewsletterTabType>('stats');

    const tabs = [
        {key: 'stats', label: 'Statystyki'},
        {key: 'subscribers', label: 'Subskrybenci'},
        {key: 'bulkEmail', label: 'Masowa wysyłka'},
        {key: 'surveys', label: 'Ankiety'}
    ];

    const renderTabContent = () => {
        switch (activeTab) {
            case 'subscribers':
                return <SubscribersList/>;
            case 'stats':
                return <NewsletterStats/>;
            case 'bulkEmail':
                return <BulkEmailSender/>;
            case 'surveys':
                return <SurveyResults/>;
            default:
                return <SubscribersList/>;
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6">Zarządzanie Newsletterem</h2>

            <div className="border-b border-gray-200 mb-6">
                <nav className="-mb-px flex space-x-6">
                    {tabs.map(tab => (
                        <button
                            key={tab.key}
                            onClick={() => setActiveTab(tab.key as NewsletterTabType)}
                            className={`${
                                activeTab === tab.key
                                    ? 'border-primary text-primary'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </nav>
            </div>

            {renderTabContent()}
        </div>
    );
};

export default AdminNewsletterPanel;