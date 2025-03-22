import React, {useEffect, useState} from "react";
import {NewsletterSubscriber} from "../../../../types/newsletter";
import {AdminNewsletterService} from "../../../../services/newsletter";
import SurveyResultsChart from "./SurveyResultsChart";
import LoadingSpinner from "../../../common/LoadingSpinner";
import {formatTimestamp} from "../../../../utils/dateFormatters";
import SurveyDetailsModal from "./SurveyDetailsModal";

const SurveyResults: React.FC = () => {
    const [subscribers, setSubscribers] = useState<NewsletterSubscriber[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedSubscriber, setSelectedSubscriber] = useState<NewsletterSubscriber | null>(null);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [surveyStats, setSurveyStats] = useState<any>(null);

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

    const processSurveyStatistics = (dietitians: NewsletterSubscriber[]) => {
        if (!dietitians.length) return;

        try {
            const stats = {
                total: dietitians.length,
                dietGoals: countResponses(dietitians, 'dietGoals'),
                clientsPerMonth: countResponses(dietitians, 'clientsPerMonth'),
                toolsUsed: countResponses(dietitians, 'toolsUsed'),
                desiredFeatures: countResponses(dietitians, 'desiredFeatures')
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

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-semibold">Wyniki Ankiet Dietetyków</h2>
                {!loading && subscribers.length > 0 && (
                    <p className="text-text-secondary">
                        Łączna liczba wypełnionych ankiet: <span className="font-semibold">{subscribers.length}</span>
                    </p>
                )}
            </div>

            {loading ? (
                <div className="flex justify-center py-8">
                    <LoadingSpinner/>
                </div>
            ) : subscribers.length === 0 ? (
                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 text-center">
                    <p className="text-yellow-700">Brak wypełnionych ankiet przez dietetyków.</p>
                </div>
            ) : (
                <>
                    {/* Sekcja z wykresami */}
                    {surveyStats && (
                        <div className="grid md:grid-cols-2 gap-6 mb-8">
                            <SurveyResultsChart
                                title="Cele żywieniowe klientów"
                                data={surveyStats.dietGoals}
                                total={surveyStats.total}
                            />

                            <SurveyResultsChart
                                title="Liczba klientów miesięcznie"
                                data={surveyStats.clientsPerMonth}
                                total={surveyStats.total}
                                type="pie"
                            />

                            <SurveyResultsChart
                                title="Obecnie używane narzędzia"
                                data={surveyStats.toolsUsed}
                                total={surveyStats.total}
                            />

                            <SurveyResultsChart
                                title="Pożądane funkcje w aplikacji"
                                data={surveyStats.desiredFeatures}
                                total={surveyStats.total}
                            />
                        </div>
                    )}

                    {/* Lista ankiet */}
                    <div className="bg-white rounded-lg border border-gray-200">
                        <h3 className="text-lg font-medium p-4 border-b border-gray-200">
                            Szczegółowe wyniki ankiet
                        </h3>

                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Email
                                    </th>
                                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Data wypełnienia
                                    </th>
                                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Liczba klientów
                                    </th>
                                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Akcje
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {subscribers.map(subscriber => {
                                    let clientsCount = '';
                                    try {
                                        if (subscriber.metadata?.surveyAnswers) {
                                            const answers = JSON.parse(subscriber.metadata.surveyAnswers);
                                            clientsCount = answers.clientsPerMonth || 'Nie podano';
                                        }
                                    } catch (e) {
                                        clientsCount = 'Błąd odczytu';
                                    }

                                    return (
                                        <tr key={subscriber.id}>
                                            <td className="px-3 py-4 text-sm whitespace-nowrap">
                                                {subscriber.email}
                                            </td>
                                            <td className="px-3 py-4 text-sm text-gray-500 whitespace-nowrap">
                                                {formatTimestamp(subscriber.verifiedAt)}
                                            </td>
                                            <td className="px-3 py-4 text-sm whitespace-nowrap">
                                                {clientsCount}
                                            </td>
                                            <td className="px-3 py-4 text-sm whitespace-nowrap">
                                                <button
                                                    onClick={() => handleViewDetails(subscriber)}
                                                    className="text-primary hover:text-primary-dark"
                                                >
                                                    Zobacz szczegóły
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })}
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