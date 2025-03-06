import React from 'react';
import { Info } from 'lucide-react';

interface ChartCardProps {
    title: string;
    children: React.ReactNode;
    description?: string;
    footer?: React.ReactNode;
    isLoading?: boolean;
}

const ChartCard: React.FC<ChartCardProps> = ({
                                                 title,
                                                 children,
                                                 description,
                                                 footer,
                                                 isLoading = false
                                             }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
            <div className="p-5 border-b border-slate-100">
                <div className="flex justify-between items-center">
                    <h3 className="text-lg font-medium text-slate-800">{title}</h3>
                    {description && (
                        <div className="relative group">
                            <Info size={16} className="text-slate-400 cursor-help" />
                            <div className="absolute right-0 w-64 p-2 mt-2 text-xs bg-slate-800 text-white rounded shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-opacity z-10">
                                {description}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            <div className={`p-5 min-h-[300px] flex items-center justify-center ${isLoading ? 'animate-pulse' : ''}`}>
                {isLoading ? (
                    <div className="w-12 h-12 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin"></div>
                ) : (
                    <div className="w-full h-[300px]">
                        {children}
                    </div>
                )}
            </div>

            {footer && (
                <div className="p-4 bg-slate-50 border-t border-slate-100 text-sm text-slate-500">
                    {footer}
                </div>
            )}
        </div>
    );
};

export default ChartCard;