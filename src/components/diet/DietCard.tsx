import React from 'react';
import {Diet} from '../../types/diet';
import {formatTimestamp} from "../../utils/dateFormatters";

interface DietCardProps {
    diet: Diet & { userEmail?: string };
    onViewClick: () => void;
    onEditClick: () => void;
}

const DietCard: React.FC<DietCardProps> = ({diet, onViewClick, onEditClick}) => {
    const getDietPeriod = (days: Diet['days']) => {
        if (!days || days.length === 0) return 'Brak dni';

        const sortedDays = [...days].sort((a, b) => {
            return a.date.seconds - b.date.seconds;
        });

        const firstDay = formatTimestamp(sortedDays[0].date);
        const lastDay = formatTimestamp(sortedDays[sortedDays.length - 1].date);

        return `${firstDay} - ${lastDay}`;
    };

    return (
        <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-all">
            <div className="text-lg font-medium text-gray-900 mb-2">
                Dieta #{diet.id.slice(0, 8)}
            </div>
            <div className="text-sm text-gray-600">
                <div className="mb-1">
                    <span className="font-medium">Email użytkownika:</span> {diet.userEmail}
                </div>
                {diet.metadata && (
                    <div className="mb-1">
                        <span className="font-medium">Liczba dni:</span> {diet.metadata.totalDays || diet.days.length}
                    </div>
                )}
                <div className="mb-1">
                    <span className="font-medium">Okres diety:</span> {getDietPeriod(diet.days)}
                </div>
                {diet.createdAt && (
                    <div className="mb-4">
                        <span className="font-medium">Data utworzenia:</span>{' '}
                        {formatTimestamp(diet.createdAt)}
                    </div>
                )}
                <div className="flex space-x-3">
                    <button
                        onClick={onViewClick}
                        className="text-blue-600 hover:text-blue-800 font-medium"
                    >
                        Podgląd
                    </button>
                    <button
                        onClick={onEditClick}
                        className="text-green-600 hover:text-green-800 font-medium"
                    >
                        Edytuj
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DietCard;