import React, {useEffect, useState} from 'react';
import {BarChart3, Calendar, Star, TrendingUp} from 'lucide-react';
import {DietTemplateService} from "../../../../services/diet/manual/DietTemplateService";
import LoadingSpinner from '../../../shared/common/LoadingSpinner';
import {DietTemplateStats} from "../../../../types/DietTemplate";

const DietTemplateStatsComponent: React.FC = () => {
    const [stats, setStats] = useState<DietTemplateStats | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadStats = async () => {
            try {
                const data = await DietTemplateService.getTemplateStats();
                setStats(data);
            } catch (error) {
                console.error('Error loading stats:', error);
            } finally {
                setLoading(false);
            }
        };

        loadStats().catch(console.error);
    }, []);

    if (loading) {
        return (
            <div className="flex justify-center py-8">
                <LoadingSpinner size="md"/>
            </div>
        );
    }

    if (!stats) {
        return (
            <div className="text-center py-8 text-gray-500">
                Nie udało się załadować statystyk
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-2">
                <BarChart3 className="h-5 w-5 text-primary"/>
                <h4 className="text-lg font-medium text-gray-900">Statystyki szablonów</h4>
            </div>

            {/* Ogólne statystyki */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-blue-50 p-4 rounded-lg">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                            <Calendar className="h-5 w-5 text-blue-600"/>
                        </div>
                        <div>
                            <p className="text-sm text-blue-600 font-medium">Łączna liczba szablonów</p>
                            <p className="text-2xl font-bold text-blue-900">{stats.totalTemplates}</p>
                        </div>
                    </div>
                </div>

                <div className="bg-green-50 p-4 rounded-lg">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                            <TrendingUp className="h-5 w-5 text-green-600"/>
                        </div>
                        <div>
                            <p className="text-sm text-green-600 font-medium">Łączne użycia</p>
                            <p className="text-2xl font-bold text-green-900">{stats.totalUsageCount}</p>
                        </div>
                    </div>
                </div>

                <div className="bg-yellow-50 p-4 rounded-lg">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center">
                            <Star className="h-5 w-5 text-yellow-600"/>
                        </div>
                        <div>
                            <p className="text-sm text-yellow-600 font-medium">Średnie użycia</p>
                            <p className="text-2xl font-bold text-yellow-900">
                                {stats.totalTemplates > 0
                                    ? (stats.totalUsageCount / stats.totalTemplates).toFixed(1)
                                    : '0'
                                }
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Kategorie */}
            <div>
                <h5 className="text-sm font-medium text-gray-900 mb-3">Szablony według kategorii</h5>
                <div className="space-y-2">
                    {Object.entries(stats.templatesByCategory).map(([category, count]) => (
                        <div key={category}
                             className="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg">
        <span className="text-sm text-gray-700">
            {DietTemplateService.getCategoryLabel(category as any)}
        </span>
                            <span className="text-sm font-medium text-gray-900">{String(count)}</span>
                        </div>
                    ))}
                </div>
            </div>

            {/* Najpopularniejszy szablon */}
            {stats.mostUsedTemplate && (
                <div>
                    <h5 className="text-sm font-medium text-gray-900 mb-3">Najpopularniejszy szablon</h5>
                    <div className="bg-primary-light/10 border border-primary-light/20 rounded-lg p-4">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="font-medium text-gray-900">{stats.mostUsedTemplate.name}</p>
                                <p className="text-sm text-gray-600">{stats.mostUsedTemplate.categoryLabel}</p>
                            </div>
                            <div className="text-right">
                                <p className="text-sm text-gray-600">Użycia</p>
                                <p className="text-lg font-bold text-primary">{stats.mostUsedTemplate.usageCount}</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DietTemplateStatsComponent;