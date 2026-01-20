import React from 'react';
import {FilterIcon} from 'lucide-react';

interface SubscriberFiltersProps {
    filters: {
        role: string;
        active: boolean;
        verified: boolean;
    };
    onChange: (filters: {
        role: string;
        active: boolean;
        verified: boolean;
    }) => void;
    isLoading: boolean;
}

const SubscriberFilters: React.FC<SubscriberFiltersProps> = ({filters, onChange, isLoading}) => {
    const handleRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onChange({
            ...filters,
            role: e.target.value
        });
    };

    const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onChange({
            ...filters,
            [e.target.name]: e.target.checked
        });
    };

    return (
        <div className="bg-gray-50 p-4 rounded-md border border-gray-200">
            <div className="flex items-start mb-4">
                <FilterIcon className="mr-2 h-5 w-5 text-gray-500 mt-0.5"/>
                <div>
                    <h3 className="font-medium text-gray-900">Filtry subskrybentów</h3>
                    <p className="text-sm text-gray-500">Wybierz kryteria dla subskrybentów newslettera</p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-1">
                        Rola
                    </label>
                    <select
                        id="role"
                        name="role"
                        value={filters.role}
                        onChange={handleRoleChange}
                        className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
                        disabled={isLoading}
                    >
                        <option value="all">Wszystkie role</option>
                        <option value="DIETITIAN">Dietetyk</option>
                        <option value="COMPANY">Firma</option>
                    </select>
                </div>

                <div className="space-y-2">
                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            id="active"
                            name="active"
                            checked={filters.active}
                            onChange={handleCheckboxChange}
                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                            disabled={isLoading}
                        />
                        <label htmlFor="active" className="ml-2 block text-sm text-gray-700">
                            Tylko aktywni
                        </label>
                    </div>

                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            id="verified"
                            name="verified"
                            checked={filters.verified}
                            onChange={handleCheckboxChange}
                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                            disabled={isLoading}
                        />
                        <label htmlFor="verified" className="ml-2 block text-sm text-gray-700">
                            Tylko zweryfikowani
                        </label>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SubscriberFilters;