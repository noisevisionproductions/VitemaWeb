import React from 'react';
import {Mail, Users, UserPlus} from 'lucide-react';
import LoadingSpinner from '../../../../shared/common/LoadingSpinner';

interface RecipientTypeSelectorProps {
    selected: 'subscribers' | 'external' | 'mixed';
    onChange: (type: 'subscribers' | 'external' | 'mixed') => void;
    counts: {
        subscribers: number;
        external: number;
        total: number;
    };
    isLoading: boolean;
}

const RecipientTypeSelector: React.FC<RecipientTypeSelectorProps> = ({
                                                                         selected,
                                                                         onChange,
                                                                         counts,
                                                                         isLoading
                                                                     }) => {
    const options = [
        {
            id: 'subscribers' as const,
            label: 'Subskrybenci newslettera',
            description: 'Wysyłka do wszystkich aktywnych i zweryfikowanych subskrybentów newslettera',
            icon: Users,
            count: counts.subscribers
        },
        {
            id: 'external' as const,
            label: 'Zewnętrzni odbiorcy',
            description: 'Wysyłka do listy zewnętrznych odbiorców (potencjalni klienci, partnerzy itp.)',
            icon: UserPlus,
            count: counts.external
        },
        {
            id: 'mixed' as const,
            label: 'Wszyscy odbiorcy',
            description: 'Wysyłka zarówno do subskrybentów jak i zewnętrznych odbiorców',
            icon: Mail,
            count: counts.total
        }
    ];

    return (
        <div className="space-y-3">
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-medium text-gray-900">Wybierz odbiorców</h3>
                {isLoading && (
                    <div className="flex items-center text-sm text-gray-500">
                        <LoadingSpinner size="sm"/>
                        Analizowanie odbiorców...
                    </div>
                )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {options.map((option) => {
                    const isActive = selected === option.id;

                    return (
                        <div
                            key={option.id}
                            onClick={() => onChange(option.id)}
                            className={`
                                border rounded-lg p-4 relative cursor-pointer transition duration-200
                                ${isActive
                                ? 'border-primary bg-primary-light bg-opacity-10 shadow-sm'
                                : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'}
                            `}
                        >
                            <div className="flex items-start space-x-3">
                                <div className={`
                                    p-2 rounded-full flex-shrink-0
                                    ${isActive ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600'}
                                `}>
                                    <option.icon size={20}/>
                                </div>

                                <div className="flex-1">
                                    <div className="flex justify-between items-start">
                                        <h4 className="font-medium text-gray-900">{option.label}</h4>
                                        <span className={`
                                            font-medium text-sm rounded-full px-2 py-1
                                            ${isActive ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}
                                        `}>
                                            {option.count}
                                        </span>
                                    </div>
                                    <p className="mt-1 text-sm text-gray-500">{option.description}</p>
                                </div>
                            </div>

                            {isActive && (
                                <div className="absolute top-2 right-2 w-3 h-3 bg-primary rounded-full"></div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default RecipientTypeSelector;