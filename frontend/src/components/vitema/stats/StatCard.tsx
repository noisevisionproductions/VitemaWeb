import React from 'react';
import { ArrowDown, ArrowUp } from 'lucide-react';

interface StatCardProps {
    title: string;
    value: string | number;
    subtitle?: string;
    icon?: React.ReactNode;
    trend?: {
        value: number;
        isPositive: boolean;
        label: string;
    };
    color?: 'blue' | 'green' | 'amber' | 'purple' | 'rose';
}

const StatCard: React.FC<StatCardProps> = ({
                                               title,
                                               value,
                                               subtitle,
                                               icon,
                                               trend,
                                               color = 'blue'
                                           }) => {
    // Mapowanie kolorów dla różnych styli
    const colorStyles = {
        blue: "bg-blue-50 text-blue-700 border-blue-200",
        green: "bg-green-50 text-green-700 border-green-200",
        amber: "bg-amber-50 text-amber-700 border-amber-200",
        purple: "bg-purple-50 text-purple-700 border-purple-200",
        rose: "bg-rose-50 text-rose-700 border-rose-200"
    };

    const iconColor = {
        blue: "bg-blue-100 text-blue-600",
        green: "bg-green-100 text-green-600",
        amber: "bg-amber-100 text-amber-600",
        purple: "bg-purple-100 text-purple-600",
        rose: "bg-rose-100 text-rose-600"
    };

    const trendColor = trend?.isPositive
        ? "text-green-600 bg-green-50"
        : "text-red-600 bg-red-50";

    return (
        <div className={`rounded-lg shadow-sm p-6 border ${colorStyles[color]} bg-white bg-opacity-80`}>
            <div className="flex justify-between items-start mb-4">
                <div>
                    <h3 className="text-lg font-medium text-slate-800 mb-1">{title}</h3>
                    {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
                </div>
                {icon && (
                    <div className={`p-2 rounded-full ${iconColor[color]}`}>
                        {icon}
                    </div>
                )}
            </div>

            <div className="flex items-end justify-between">
                <p className="text-3xl font-bold text-slate-900">{value}</p>

                {trend && (
                    <div className={`flex items-center text-sm font-medium rounded-full px-2 py-1 ${trendColor}`}>
                        {trend.isPositive ? (
                            <ArrowUp className="h-3 w-3 mr-1" />
                        ) : (
                            <ArrowDown className="h-3 w-3 mr-1" />
                        )}
                        <span>{Math.abs(trend.value)}%</span>
                        <span className="ml-1 text-xs font-normal">{trend.label}</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StatCard;