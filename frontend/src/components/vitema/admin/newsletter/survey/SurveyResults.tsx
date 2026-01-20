import React, {useEffect, useState} from "react";
import {NewsletterSubscriber} from "../../../../../types/newsletter";
import {AdminNewsletterService} from "../../../../../services/newsletter";
import SurveyResultsChart from "./SurveyResultsChart";
import LoadingSpinner from "../../../../shared/common/LoadingSpinner";
import {formatPostgresTimestamp} from "../../../../../utils/dateFormatters";
import SurveyDetailsModal from "./SurveyDetailsModal";
import {Download, RefreshCw, Search} from "lucide-react";

const SurveyResults: React.FC = () => {
    const [subscribers, setSubscribers] = useState<NewsletterSubscriber[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [selectedSubscriber, setSelectedSubscriber] = useState<NewsletterSubscriber | null>(null);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [surveyStats, setSurveyStats] = useState<any>(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchSurveyData().catch(console.error);
    }, []);

    const fetchSurveyData = async () => {
        try {
            setLoading(true);
            const data = await AdminNewsletterService.getAllSubscribers();

            const respondents = data.filter((s: NewsletterSubscriber) =>
                s.metadata?.surveyCompleted === 'true' &&
                s.metadata?.surveyAnswers
            );

            setSubscribers(respondents);
            processSurveyStatistics(respondents);
        } catch (error) {
            console.error('Error fetching survey data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRefresh = async () => {
        try {
            setRefreshing(true);
            await fetchSurveyData();
        } finally {
            setRefreshing(false);
        }
    };

    const processSurveyStatistics = (dietitians: NewsletterSubscriber[]) => {
        if (!dietitians.length) return;

        try {
            const stats = {
                total: dietitians.length,
                dietSoftwareExperience: countResponses(dietitians, 'dietSoftwareExperience'),
                clientsPerMonth: countResponses(dietitians, 'clientsPerMonth'),
                currentTools: countResponses(dietitians, 'currentTools'),
                softwareKeyFeatures: countResponses(dietitians, 'softwareKeyFeatures')
            };

            setSurveyStats(stats);
        } catch (error) {
            console.error('Error processing survey data:', error);
        }
    };

    const countResponses = (dietitians: NewsletterSubscriber[], questionId: string) => {
        const counts: Record<string, number> = {};

        dietitians.forEach(dietitian => {
            try {
                if (dietitian.metadata?.surveyAnswers) {
                    const answers = JSON.parse(dietitian.metadata.surveyAnswers);
                    const response = answers[questionId];

                    if (Array.isArray(response)) {
                        response.forEach(item => {
                            counts[item] = (counts[item] || 0) + 1;
                        });
                    } else if (response) {
                        counts[response] = (counts[response] || 0) + 1;
                    }
                }
            } catch (e) {
                console.error('Error parsing survey answers:', e);
            }
        });

        return counts;
    };

    const handleViewDetails = (subscriber: NewsletterSubscriber) => {
        setSelectedSubscriber(subscriber);
        setIsDetailsModalOpen(true);
    };

    const exportToCSV = () => {
        if (subscribers.length === 0) return;

        try {
            const headers = ["Email", "Data wypełnienia", "Korzysta z oprogramowania", "Liczba klientów miesięcznie", "Obecne narzędzia", "Kluczowe funkcje", "Dodatkowe informacje"];
            const rows = subscribers.map(subscriber => {
                let surveyData = {
                    dietSoftwareExperience: 'Nie podano',
                    clientsPerMonth: 'Nie podano',
                    currentTools: [],
                    softwareKeyFeatures: [],
                    additionalInfo: ''
                };

                try {
                    if (subscriber.metadata?.surveyAnswers) {
                        const answers = JSON.parse(subscriber.metadata.surveyAnswers);
                        surveyData = {
                            dietSoftwareExperience: answers.dietSoftwareExperience || 'Nie podano',
                            clientsPerMonth: answers.clientsPerMonth || 'Nie podano',
                            currentTools: Array.isArray(answers.currentTools) ? answers.currentTools : [],
                            softwareKeyFeatures: Array.isArray(answers.softwareKeyFeatures) ? answers.softwareKeyFeatures : [],
                            additionalInfo: answers.additionalInfo || ''
                        };
                    }
                } catch (e) {
                    console.error('Error parsing survey data for CSV export:', e);
                }

                return [
                    subscriber.email,
                    formatPostgresTimestamp(subscriber.verifiedAt),
                    surveyData.dietSoftwareExperience,
                    surveyData.clientsPerMonth,
                    Array.isArray(surveyData.currentTools) ? surveyData.currentTools.join(', ') : surveyData.currentTools,
                    Array.isArray(surveyData.softwareKeyFeatures) ? surveyData.softwareKeyFeatures.join(', ') : surveyData.softwareKeyFeatures,
                    surveyData.additionalInfo
                ];
            });

            const csvContent = [
                headers.join(','),
                ...rows.map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
            ].join('\n');

            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.setAttribute('href', url);
            link.setAttribute('download', `ankiety-dietetykow-${new Date().toISOString().slice(0, 10)}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (error) {
            console.error('Error exporting to CSV:', error);
        }
    };

    // Filtrowanie subskrybentów według wyszukiwanego terminu
    const filteredSubscribers = subscribers.filter(subscriber => {
        if (!searchTerm) return true;
        return subscriber.email.toLowerCase().includes(searchTerm.toLowerCase());
    });

    return (
        <div className="space-y-8">
            <div className="flex flex-wrap justify-between items-center gap-4">
                <h2 className="text-xl font-semibold">Wyniki Ankiet Dietetyków</h2>
                <div className="flex items-center space-x-2">
                    {!loading && subscribers.length > 0 && (
                        <span className="text-text-secondary text-sm px-3 py-1 bg-gray-100 rounded-full">
                            Ankiety: <span className="font-semibold">{subscribers.length}</span>
                        </span>
                    )}

                    <button
                        onClick={handleRefresh}
                        disabled={loading || refreshing}
                        className="inline-flex items-center px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors disabled:opacity-50"
                    >
                        <RefreshCw size={16} className={`mr-1 ${refreshing ? 'animate-spin' : ''}`} />
                        Odśwież
                    </button>

                    {!loading && subscribers.length > 0 && (
                        <button
                            onClick={exportToCSV}
                            className="inline-flex items-center px-3 py-1.5 text-sm bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                        >
                            <Download size={16} className="mr-1" />
                            Eksportuj CSV
                        </button>
                    )}
                </div>
            </div>

            {loading ? (
                <div className="flex justify-center py-16">
                    <LoadingSpinner size="lg" />
                </div>
            ) : subscribers.length === 0 ? (
                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-8 text-center">
                    <p className="text-yellow-700 text-lg">Brak wypełnionych ankiet przez dietetyków.</p>
                    <p className="text-yellow-600 mt-2">
                        Ankiety są wypełniane przez dietetyków po zapisaniu się do newslettera.
                        Sprawdź ponownie później.
                    </p>
                </div>
            ) : (
                <>
                    {/* Sekcja z wykresami */}
                    {surveyStats && (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                            <SurveyResultsChart
                                title="Doświadczenie z oprogramowaniem dietetycznym"
                                data={surveyStats.dietSoftwareExperience}
                                total={surveyStats.total}
                                type="pie"
                            />

                            <SurveyResultsChart
                                title="Liczba klientów miesięcznie"
                                data={surveyStats.clientsPerMonth}
                                total={surveyStats.total}
                                type="pie"
                            />

                            <SurveyResultsChart
                                title="Obecnie używane narzędzia"
                                data={surveyStats.currentTools}
                                total={surveyStats.total}
                            />

                            <SurveyResultsChart
                                title="Najważniejsze funkcje w aplikacji"
                                data={surveyStats.softwareKeyFeatures}
                                total={surveyStats.total}
                            />
                        </div>
                    )}

                    {/* Lista ankiet */}
                    <div className="bg-white rounded-lg border border-gray-200 shadow-sm">
                        <div className="p-4 border-b border-gray-200 flex flex-wrap justify-between items-center gap-3">
                            <h3 className="text-lg font-medium text-gray-900">
                                Szczegółowe wyniki ankiet
                            </h3>

                            <div className="relative">
                                <Search size={16} className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                                <input
                                    type="text"
                                    placeholder="Szukaj po email..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-9 pr-3 py-1.5 border border-gray-300 rounded-md text-sm focus:ring-primary focus:border-primary w-full sm:w-64"
                                />
                            </div>
                        </div>

                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Email
                                    </th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Data wypełnienia
                                    </th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Korzysta z oprogramowania
                                    </th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Liczba klientów
                                    </th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Akcje
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {filteredSubscribers.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                                            Brak wyników dla wyszukiwanego terminu
                                        </td>
                                    </tr>
                                ) : (
                                    filteredSubscribers.map(subscriber => {
                                        let clientsCount = '';
                                        let softwareUse = '';
                                        try {
                                            if (subscriber.metadata?.surveyAnswers) {
                                                const answers = JSON.parse(subscriber.metadata.surveyAnswers);
                                                clientsCount = answers.clientsPerMonth || 'Nie podano';
                                                softwareUse = answers.dietSoftwareExperience || 'Nie podano';
                                            }
                                        } catch (e) {
                                            clientsCount = 'Błąd odczytu';
                                            softwareUse = 'Błąd odczytu';
                                        }

                                        return (
                                            <tr key={subscriber.id} className="hover:bg-gray-50 transition-colors">
                                                <td className="px-4 py-4 text-sm whitespace-nowrap font-medium text-gray-900">
                                                    {subscriber.email}
                                                </td>
                                                <td className="px-4 py-4 text-sm text-gray-500 whitespace-nowrap">
                                                    {formatPostgresTimestamp(subscriber.verifiedAt)}
                                                </td>
                                                <td className="px-4 py-4 text-sm whitespace-nowrap">
                                                    <span className={`px-2 py-1 rounded-full text-xs ${
                                                        softwareUse === 'Tak'
                                                            ? 'bg-green-100 text-green-800'
                                                            : softwareUse === 'Nie'
                                                                ? 'bg-red-100 text-red-800'
                                                                : 'bg-gray-100 text-gray-800'
                                                    }`}>
                                                        {softwareUse}
                                                    </span>
                                                </td>
                                                <td className="px-4 py-4 text-sm whitespace-nowrap">
                                                    {clientsCount}
                                                </td>
                                                <td className="px-4 py-4 text-sm whitespace-nowrap">
                                                    <button
                                                        onClick={() => handleViewDetails(subscriber)}
                                                        className="text-primary hover:text-primary-dark transition-colors inline-flex items-center"
                                                    >
                                                        <Search size={14} className="mr-1" />
                                                        Zobacz szczegóły
                                                    </button>
                                                </td>
                                            </tr>
                                        );
                                    })
                                )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </>
            )}

            {/* Modal szczegółów ankiety */}
            {selectedSubscriber && (
                <SurveyDetailsModal
                    subscriber={selectedSubscriber}
                    isOpen={isDetailsModalOpen}
                    onClose={() => setIsDetailsModalOpen(false)}
                />
            )}
        </div>
    );
};

export default SurveyResults;