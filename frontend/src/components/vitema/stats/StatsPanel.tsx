import React, { useState } from 'react';
import {
    XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, PieChart, Pie, Cell, Legend, AreaChart, Area
} from 'recharts';
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import { useStatsData } from "../../../hooks/shopping/useStatsData";
import StatCard from './StatCard';
import ChartCard from './ChartCard';
import { Users, TrendingUp, Utensils, Calendar, Clock, UserCheck } from 'lucide-react';
import SectionHeader from "../../shared/common/SectionHeader";

// Kolory do wykresów
const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

const StatsPanel: React.FC = () => {
    const { loading, error, stats } = useStatsData();
    const [showCumulative, setShowCumulative] = useState<boolean>(false);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner />
            </div>
        );
    }

    if (error || !stats) {
        return (
            <div className="text-red-500 text-center p-4 bg-white rounded-lg shadow-sm border border-red-100 my-4">
                <h3 className="font-medium">Wystąpił błąd podczas ładowania statystyk</h3>
                <p className="text-sm mt-2">Spróbuj odświeżyć stronę lub skontaktuj się z administratorem.</p>
            </div>
        );
    }

    const formatTooltipValue = (value: number) => {
        return `${value} ${value === 1 ? 'dieta' : value < 5 ? 'diety' : 'diet'}`;
    };

    return (
        <div className="space-y-6 pb-8">
            <SectionHeader
                title="Panel Statystyk"
                description="Przegląd kluczowych metryk i wskaźników związanych z dietami i użytkownikami."
            />

            {/* Karty statystyk */}
            <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-4">
                <StatCard
                    title="Użytkownicy"
                    value={stats.totalUsers}
                    subtitle="Wszyscy zarejestrowani"
                    icon={<Users size={20} />}
                    trend={{
                        value: stats.userGrowthRate,
                        isPositive: stats.userGrowthRate > 0,
                        label: "mies."
                    }}
                    color="blue"
                />

                <StatCard
                    title="Aktywni Użytkownicy"
                    value={stats.activeUsers}
                    subtitle={`${Math.round((stats.activeUsers / stats.totalUsers) * 100)}% wszystkich użytkowników`}
                    icon={<UserCheck size={20} />}
                    color="green"
                />

                <StatCard
                    title="Wszystkie Diety"
                    value={stats.totalDiets}
                    subtitle="Liczba wszystkich diet"
                    icon={<Utensils size={20} />}
                    trend={{
                        value: stats.dietGrowthRate,
                        isPositive: stats.dietGrowthRate > 0,
                        label: "mies."
                    }}
                    color="amber"
                />

                <StatCard
                    title="Średnia Długość"
                    value={`${stats.averageDietDays} dni`}
                    subtitle="Średnia długość diety"
                    icon={<Calendar size={20} />}
                    color="purple"
                />

                <StatCard
                    title="Diet na Użytkownika"
                    value={stats.dietPerUserRate}
                    subtitle="Średnia liczba diet"
                    icon={<TrendingUp size={20} />}
                    color="rose"
                />

                <StatCard
                    title="Wskaźnik Ukończenia"
                    value={`${stats.completionRate}%`}
                    subtitle="Diet ukończonych"
                    icon={<Clock size={20} />}
                    color="green"
                />
            </div>

            {/* Wykresy */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
                <ChartCard
                    title={`${showCumulative ? 'Skumulowany wzrost' : 'Miesięczny przyrost'} diet`}
                    description="Liczba nowych diet tworzonych w każdym miesiącu. Przełącz między widokiem miesięcznym a skumulowanym."
                    footer={
                        <div className="flex justify-between items-center">
                            <span className="text-xs text-slate-500">Źródło: dane z systemu Vitema</span>
                            <button
                                onClick={() => setShowCumulative(!showCumulative)}
                                className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                            >
                                Pokaż {showCumulative ? 'miesięczne' : 'skumulowane'}
                            </button>
                        </div>
                    }
                >
                    <ResponsiveContainer width="100%" height={300}>
                        {showCumulative ? (
                            <AreaChart data={stats.monthlyData}>
                                <defs>
                                    <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8} />
                                        <stop offset="95%" stopColor="#8884d8" stopOpacity={0.1} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                <XAxis
                                    dataKey="month"
                                    tickFormatter={(value) => value === "NaN/NaN" ? "Brak daty" : value}
                                />
                                <YAxis />
                                <Tooltip
                                    formatter={(value: number) => [`${value} diet`, 'Suma']}
                                    labelFormatter={(label) => label === "NaN/NaN" ? "Brak daty" : label}
                                />
                                <Area
                                    type="monotone"
                                    dataKey="cumulativeCount"
                                    stroke="#8884d8"
                                    fillOpacity={1}
                                    fill="url(#colorCount)"
                                    name="Suma"
                                />
                            </AreaChart>
                        ) : (
                            <BarChart data={stats.monthlyData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                <XAxis
                                    dataKey="month"
                                    tickFormatter={(value) => value === "NaN/NaN" ? "Brak daty" : value}
                                />
                                <YAxis />
                                <Tooltip
                                    formatter={(value: number) => [formatTooltipValue(value), 'Liczba diet']}
                                    labelFormatter={(label) => label === "NaN/NaN" ? "Brak daty" : label}
                                />
                                <Bar dataKey="count" fill="#3b82f6" name="Liczba diet" />
                            </BarChart>
                        )}
                    </ResponsiveContainer>
                </ChartCard>

                <ChartCard
                    title="Użytkownicy Systemu"
                    description="Porównanie liczby wszystkich użytkowników systemu do tych, którzy aktywnie korzystają z dietami."
                >
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={stats.usersData} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
                            <XAxis type="number" />
                            <YAxis type="category" dataKey="name" width={150} />
                            <Tooltip />
                            <Bar dataKey="value" fill="#3b82f6" radius={[0, 4, 4, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </ChartCard>

                <ChartCard
                    title="Popularność Typów Posiłków"
                    description="Rozkład typów posiłków używanych w dietach."
                >
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={stats.mealsTypeData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                outerRadius={100}
                                fill="#8884d8"
                                dataKey="value"
                                nameKey="name"
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                                {stats.mealsTypeData.map((_entry: any, index: number) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Legend />
                            <Tooltip formatter={(value: number) => [`${value} posiłków`, '']} />
                        </PieChart>
                    </ResponsiveContainer>
                </ChartCard>

                <ChartCard
                    title="Rozkład Długości Diet"
                    description="Liczba diet w zależności od ich długości (w dniach)."
                >
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={stats.dietLengthDistribution}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                            <XAxis dataKey="range" />
                            <YAxis />
                            <Tooltip formatter={(value: number) => [formatTooltipValue(value), 'Liczba diet']} />
                            <Bar dataKey="count" name="Liczba diet">
                                {stats.dietLengthDistribution.map((_entry: any, index: number) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </ChartCard>
            </div>
        </div>
    );
};

export default StatsPanel;