import React from 'react';
import SearchInput from '../../shared/common/SearchInput';
import {User} from '../../../types/user';
import {XCircle, Clock, AlertTriangle} from 'lucide-react';

export type SortOption = 'newest' | 'oldest' | 'name';

interface DietFilterProps {
    activeUsers: User[];
    selectedUserId: string | null;
    onUserSelect: (userId: string | null) => void;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onReset: () => void;
    sortBy: SortOption;
    onSortChange: (sort: SortOption) => void;
    showOnlyWarnings: boolean;
    onWarningsChange: (show: boolean) => void;
    showOnlyGaps: boolean;
    onGapsChange: (show: boolean) => void;
    warningCount: number;
    gapsCount: number;
}

const DietFilter: React.FC<DietFilterProps> = ({
                                                   activeUsers,
                                                   selectedUserId,
                                                   onUserSelect,
                                                   searchQuery,
                                                   onSearchChange,
                                                   onReset,
                                                   sortBy,
                                                   onSortChange,
                                                   showOnlyWarnings,
                                                   onWarningsChange,
                                                   showOnlyGaps,
                                                   onGapsChange,
                                                   warningCount,
                                                   gapsCount
                                               }) => {
    const hasActiveFilters = selectedUserId !== null ||
        searchQuery !== '' ||
        sortBy !== 'newest' ||
        showOnlyWarnings ||
        showOnlyGaps;

    return (
        <div className="space-y-4">
            {/* Główne filtry */}
            <div className="flex flex-wrap items-center gap-4">
                <div className="flex-1 min-w-[250px]">
                    <SearchInput
                        value={searchQuery}
                        onChange={onSearchChange}
                        placeholder="Szukaj diet po nazwie lub emailu..."
                    />
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    {hasActiveFilters && (
                        <button
                            onClick={onReset}
                            className="flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors border border-red-200"
                        >
                            <XCircle className="w-4 h-4"/>
                            Wyczyść filtry
                        </button>
                    )}

                    <select
                        value={sortBy}
                        onChange={(e) => onSortChange(e.target.value as SortOption)}
                        className="px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-blue-500 pr-8"
                    >
                        <option value="newest">Najnowsze</option>
                        <option value="oldest">Najstarsze</option>
                        <option value="name">Nazwa pliku</option>
                    </select>

                    {warningCount > 0 && (
                        <button
                            onClick={() => onWarningsChange(!showOnlyWarnings)}
                            className={`px-3 py-2 rounded-md flex items-center text-sm 
                                ${showOnlyWarnings
                                ? 'bg-amber-100 text-amber-700 border border-amber-200'
                                : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'}`}
                        >
                            <AlertTriangle className="w-4 h-4 mr-2"/>
                            {showOnlyWarnings ? 'Wszystkie diety' : `Tylko ostrzeżenia (${warningCount})`}
                        </button>
                    )}

                    {gapsCount > 0 && (
                        <button
                            onClick={() => onGapsChange(!showOnlyGaps)}
                            className={`px-3 py-2 rounded-md flex items-center text-sm 
                                ${showOnlyGaps
                                ? 'bg-blue-100 text-blue-700 border border-blue-200'
                                : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'}`}
                        >
                            <Clock className="w-4 h-4 mr-2"/>
                            {showOnlyGaps ? 'Wszystkie diety' : `Bez kontynuacji (${gapsCount})`}
                        </button>
                    )}
                </div>
            </div>

            {/* Filtry użytkowników */}
            <div className="flex flex-wrap gap-2">
                <button
                    onClick={() => onUserSelect(null)}
                    className={`px-3 py-1 rounded-full text-sm ${
                        selectedUserId === null
                            ? 'bg-blue-100 text-blue-800 font-medium'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                >
                    Wszyscy
                </button>
                {activeUsers.map((user) => (
                    <button
                        key={user.id}
                        onClick={() => onUserSelect(user.id)}
                        className={`px-3 py-1 rounded-full text-sm ${
                            selectedUserId === user.id
                                ? 'bg-blue-100 text-blue-800 font-medium'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                        }`}
                    >
                        {user.email}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default DietFilter;