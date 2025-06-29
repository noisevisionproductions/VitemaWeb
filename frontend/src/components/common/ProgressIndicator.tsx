import React from 'react';
import { CheckCircle, Clock, AlertTriangle } from 'lucide-react';

export type ProgressVariant = 'diet-planning' | 'categorization' | 'upload';

interface ProgressIndicatorProps {
    variant: ProgressVariant;
    current: number;
    total: number;
    label?: string;
    className?: string;
}

const ProgressIndicator: React.FC<ProgressIndicatorProps> = ({
                                                                 variant,
                                                                 current,
                                                                 total,
                                                                 label,
                                                                 className = ''
                                                             }) => {
    const percentage = total > 0 ? (current / total) * 100 : 0;
    const isComplete = current === total;

    const getVariantStyles = () => {
        switch (variant) {
            case 'diet-planning':
                return {
                    container: 'bg-gradient-to-r from-green-50 to-emerald-50 border-green-200',
                    progress: isComplete
                        ? 'bg-gradient-to-r from-green-500 to-emerald-500'
                        : 'bg-gradient-to-r from-green-400 to-emerald-400',
                    text: isComplete ? 'text-green-800' : 'text-green-700',
                    icon: isComplete ? CheckCircle : Clock
                };
            case 'categorization':
                return {
                    container: 'bg-gradient-to-r from-blue-50 to-indigo-50 border-blue-200',
                    progress: isComplete
                        ? 'bg-gradient-to-r from-blue-500 to-indigo-500'
                        : 'bg-gradient-to-r from-blue-400 to-indigo-400',
                    text: isComplete ? 'text-blue-800' : 'text-blue-700',
                    icon: isComplete ? CheckCircle : Clock
                };
            case 'upload':
                return {
                    container: 'bg-gradient-to-r from-purple-50 to-violet-50 border-purple-200',
                    progress: isComplete
                        ? 'bg-gradient-to-r from-purple-500 to-violet-500'
                        : 'bg-gradient-to-r from-purple-400 to-violet-400',
                    text: isComplete ? 'text-purple-800' : 'text-purple-700',
                    icon: isComplete ? CheckCircle : AlertTriangle
                };
            default:
                return {
                    container: 'bg-gray-50 border-gray-200',
                    progress: 'bg-primary',
                    text: 'text-gray-700',
                    icon: Clock
                };
        }
    };

    const styles = getVariantStyles();
    const Icon = styles.icon;

    return (
        <div className={`rounded-xl border-2 p-6 ${styles.container} ${className}`}>
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-full ${isComplete ? 'bg-white/50' : 'bg-white/30'}`}>
                        <Icon className="h-5 w-5" />
                    </div>
                    <span className={`font-semibold ${styles.text}`}>
                        {label || 'Postęp'}
                    </span>
                </div>
                <span className={`text-sm font-medium ${styles.text}`}>
                    {current}/{total}
                </span>
            </div>

            <div className="relative">
                <div className="w-full bg-white/40 rounded-full h-3 shadow-inner">
                    <div
                        className={`h-3 rounded-full transition-all duration-500 ease-out shadow-sm ${styles.progress}`}
                        style={{ width: `${percentage}%` }}
                    />
                </div>
                <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-xs font-bold text-white drop-shadow-sm">
                        {Math.round(percentage)}%
                    </span>
                </div>
            </div>

            {isComplete && (
                <div className={`text-xs mt-2 ${styles.text} opacity-80`}>
                    ✨ Gotowe!
                </div>
            )}
        </div>
    );
};

export default ProgressIndicator;