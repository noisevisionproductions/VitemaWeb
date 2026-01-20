import React, {useState} from 'react';
import NewsletterStats from "../../components/vitema/admin/newsletter/NewsletterStats";
import BulkEmailSender from "../../components/vitema/admin/newsletter/email-sender/BulkEmailSender";
import SubscribersList from "../../components/vitema/admin/newsletter/SubscribersList";
import SurveyResults from "../../components/vitema/admin/newsletter/survey/SurveyResults";
import ExternalRecipientsManager from "../../components/vitema/admin/newsletter/external-recipients/ExternalRecipientsManager";
import SingleEmailSender from "../../components/vitema/admin/newsletter/email-sender/SingleEmailSender";

type NewsletterTabType = 'stats' | 'subscribers' | 'bulkEmail' | 'singleEmail' | 'externalRecipients' | 'surveys';

const AdminNewsletterPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<NewsletterTabType>('stats');

    const tabs = [
        {key: 'stats', label: 'Statystyki'},
        {key: 'subscribers', label: 'Subskrybenci'},
        {key: 'externalRecipients', label: 'Zewnętrzni odbiorcy'},
        {key: 'bulkEmail', label: 'Masowa wysyłka'},
        {key: 'singleEmail', label: 'Pojedynczy email'},
        {key: 'surveys', label: 'Ankiety'}
    ];

    const renderTabContent = () => {
        switch (activeTab) {
            case 'stats':
                return <NewsletterStats/>;
            case 'subscribers':
                return <SubscribersList/>;
            case 'externalRecipients':
                return <ExternalRecipientsManager/>;
            case 'bulkEmail':
                return <BulkEmailSender/>;
            case 'singleEmail':
                return <SingleEmailSender/>;
            case 'surveys':
                return <SurveyResults/>;
            default:
                return <NewsletterStats/>;
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6">Zarządzanie Newsletterem</h2>

            <div className="border-b border-gray-200 mb-6">
                <nav className="-mb-px flex space-x-6 overflow-x-auto pb-1">
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