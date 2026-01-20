import React, {useEffect, useState} from "react";
import {NewsletterStatsData} from "../../../../types/newsletter";
import {AdminNewsletterService} from "../../../../services/newsletter";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";

const NewsletterStats: React.FC = () => {
    const [stats, setStats] = useState<NewsletterStatsData | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats().catch(console.error);
    }, []);

    const fetchStats = async () => {
        try {
            setLoading(true);
            const data = await AdminNewsletterService.getNewsletterStats();
            setStats(data);
        } catch (error) {
            console.error('Error fetching newsletter stats:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center py-8">
                <LoadingSpinner/>
            </div>
        );
    }

    if (!stats) {
        return <div className="text-center py-8 text-gray-500">Nie udało się załadować statystyk</div>;
    }

    return (
        <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-900">Wszyscy subskrybenci</h3>
                    <p className="mt-2 text-3xl font-bold text-primary">{stats.total}</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-900">Aktywni</h3>
                    <p className="mt-2 text-3xl font-bold text-primary">{stats.active}</p>
                    <p className="text-sm text-gray-500 mt-1">
                        ({Math.round((stats.active / stats.total) * 100) || 0}% wszystkich)
                    </p>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-900">Zweryfikowani</h3>
                    <p className="mt-2 text-3xl font-bold text-primary">{stats.verified}</p>
                    <p className="text-sm text-gray-500 mt-1">
                        ({Math.round((stats.verified / stats.total) * 100) || 0}% wszystkich)
                    </p>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-900">Aktywni i zweryfikowani</h3>
                    <p className="mt-2 text-3xl font-bold text-primary">{stats.activeVerified}</p>
                    <p className="text-sm text-gray-500 mt-1">
                        ({Math.round((stats.activeVerified / stats.total) * 100) || 0}% wszystkich)
                    </p>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Rozkład ról</h3>

                <div className="space-y-4">
                    <div>
                        <div className="flex justify-between items-center mb-1">
                            <span className="text-sm font-medium">Dietetycy</span>
                            <span className="text-sm font-medium">
                {stats.roleDistribution?.DIETITIAN || 0} ({Math.round(((stats.roleDistribution?.DIETITIAN || 0) / stats.total) * 100) || 0}%)
              </span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2.5">
                            <div
                                className="bg-primary h-2.5 rounded-full"
                                style={{width: `${Math.round(((stats.roleDistribution?.DIETITIAN || 0) / stats.total) * 100) || 0}%`}}
                            ></div>
                        </div>
                    </div>

                    <div>
                        <div className="flex justify-between items-center mb-1">
                            <span className="text-sm font-medium">Firmy</span>
                            <span className="text-sm font-medium">
                {stats.roleDistribution?.COMPANY || 0} ({Math.round(((stats.roleDistribution?.COMPANY || 0) / stats.total) * 100) || 0}%)
              </span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2.5">
                            <div
                                className="bg-secondary h-2.5 rounded-full"
                                style={{width: `${Math.round(((stats.roleDistribution?.COMPANY || 0) / stats.total) * 100) || 0}%`}}
                            ></div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex justify-center">
                <button
                    onClick={fetchStats}
                    className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark"
                >
                    Odśwież statystyki
                </button>
            </div>
        </div>
    );
};

export default NewsletterStats;