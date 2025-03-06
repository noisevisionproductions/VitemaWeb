import React, {useEffect, useState} from 'react';
import DietView from './view/DietView';
import DietEditModal from "./edit/DietEditModal";
import LoadingSpinner from "../common/LoadingSpinner";
import DietCard from "./DietCard";
import useUsers from "../../hooks/useUsers";
import DietFilter, {SortOption} from "./DietFilter";
import {Diet} from "../../types";
import {useDiets} from "../../hooks/useDiets";
import {toast} from "sonner";
import {getTimestamp} from "../../utils/dateUtils";
import {AlertTriangle, FileText, Filter, RefreshCw} from 'lucide-react';
import {getDaysRemainingToDietEnd, getDietWarningStatus, isDietEnded} from "../../utils/dietWarningUtils";

interface DietWithUser extends Diet {
    userEmail?: string;
}

const DietManagement: React.FC = () => {
    const {users, loading: usersLoading} = useUsers();
    const {
        diets,
        loading: dietsLoading,
        deleteDiet,
        updateDiet,
        refreshDiets
    } = useDiets(users, usersLoading);

    const [selectedDiet, setSelectedDiet] = useState<DietWithUser | null>(null);
    const [editingDiet, setEditingDiet] = useState<DietWithUser | null>(null);
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState<SortOption>('newest');
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [filterExpanded, setFilterExpanded] = useState(false);
    const [showOnlyWarnings, setShowOnlyWarnings] = useState(false);

    useEffect(() => {
        const interval = setInterval(() => {
            refreshDiets().catch(console.error);
        }, 5 * 60 * 1000);
        return () => clearInterval(interval);
    }, [refreshDiets]);

    const filteredAndSortedDiets = React.useMemo(() => {
        let result = diets.filter(diet => {
            const matchesUser = selectedUserId ? diet.userId === selectedUserId : true;
            const matchesSearch = diet.userEmail?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                diet.metadata?.fileName?.toLowerCase().includes(searchQuery.toLowerCase());
            const matchesWarning = showOnlyWarnings ?
                (getDietWarningStatus(diet) !== 'normal' && !isDietEnded(diet)) :
                true;
            return matchesUser && matchesSearch && matchesWarning;
        });

        if (showOnlyWarnings) {
            return result.sort((a, b) => {
                const daysRemainingA = getDaysRemainingToDietEnd(a);
                const daysRemainingB = getDaysRemainingToDietEnd(b);
                return daysRemainingA - daysRemainingB;
            });
        }

        return result.sort((a, b) => {
            switch (sortBy) {
                case "newest":
                    return getTimestamp(b.createdAt) - getTimestamp(a.createdAt);
                case "oldest":
                    return getTimestamp(a.createdAt) - getTimestamp(b.createdAt);
                case "name":
                    return (a.metadata?.fileName || '').localeCompare(b.metadata?.fileName || '');
                default:
                    return 0;
            }
        });
    }, [diets, selectedUserId, searchQuery, sortBy, showOnlyWarnings]);

    const warningCount = React.useMemo(() => {
        return diets.filter(diet => {
            const status = getDietWarningStatus(diet);
            return (status === 'critical' || status === 'warning') && !isDietEnded(diet);
        }).length;
    }, [diets]);

    const handleResetFilters = () => {
        setSelectedUserId(null);
        setSearchQuery('');
        setSortBy('newest');
        setShowOnlyWarnings(false);
    };

    const handleRefreshDiets = async () => {
        setIsRefreshing(true);
        try {
            await refreshDiets();
        } catch (error) {
            toast.error('Nie udało się odświeżyć diet');
        } finally {
            setIsRefreshing(false);
        }
    };

    const handleDietUpdate = async (diet: Diet) => {
        try {
            await updateDiet(diet.id, diet);
            setEditingDiet(null);
            await refreshDiets();
            toast.success('Dieta została zaktualizowana');
        } catch (error) {
            toast.error('Błąd podczas aktualizacji diety');
        }
    };

    const handleDietDelete = async (dietId: string) => {
        try {
            setSelectedDiet(null);
            setEditingDiet(null);
            await deleteDiet(dietId);
        } catch (error) {
            console.error('Diet deletion failed:', error);
            toast.error('Błąd podczas usuwania diety');
        }
    };

    const activeUsers = React.useMemo(() => {
        const uniqueUserIds = new Set(diets.map(diet => diet.userId));
        return users.filter(user => uniqueUserIds.has(user.id));
    }, [diets, users]);

    if (dietsLoading || usersLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    return (
        <div className="space-y-6 pb-8">
            {/* Nagłówek */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold mb-4">Zarządzanie Dietami</h1>
                    <p className="text-slate-500 text-sm mt-1">Zarządzaj dietami wszystkich użytkowników</p>
                </div>
                <div className="flex gap-2">
                    {warningCount > 0 && (
                        <button
                            onClick={() => setShowOnlyWarnings(!showOnlyWarnings)}
                            className={`px-3 py-2 rounded-md border shadow-sm flex items-center text-sm font-medium
                               ${showOnlyWarnings
                                ? 'bg-amber-50 text-amber-700 border-amber-200'
                                : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'}
                            transition-colors`}
                        >
                            <AlertTriangle
                                className={`h-4 w-4 mr-2 ${showOnlyWarnings ? 'text-amber-500' : 'text-slate-500'}`}/>
                            {showOnlyWarnings ? 'Wszystkie diety' : `Ostrzeżenia (${warningCount})`}
                        </button>
                    )}
                    <button
                        onClick={() => setFilterExpanded(!filterExpanded)}
                        className="px-3 py-2 rounded-md bg-white text-slate-700 border border-slate-200 shadow-sm hover:bg-slate-50 transition-colors flex items-center text-sm font-medium"
                    >
                        <Filter className="h-4 w-4 mr-2"/>
                        Filtry {filterExpanded ? '↑' : '↓'}
                    </button>
                    <button
                        onClick={handleRefreshDiets}
                        disabled={isRefreshing}
                        className="px-3 py-2 rounded-md bg-white text-slate-700 border border-slate-200 shadow-sm hover:bg-slate-50 transition-colors flex items-center text-sm font-medium disabled:opacity-50"
                    >
                        <RefreshCw className={`h-4 w-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`}/>
                        Odśwież
                    </button>
                </div>
            </div>

            {/* Baner ostrzeżeń */}
            {warningCount > 0 && !showOnlyWarnings && (
                <div
                    className="flex items-center p-4 bg-amber-50 border border-amber-200 rounded-lg text-amber-700 text-sm">
                    <AlertTriangle className="h-5 w-5 mr-2 text-amber-500"/>
                    <div className="flex-1">
                        Masz <strong>{warningCount}</strong> {warningCount === 1 ? 'dietę' : (warningCount < 5 ? 'diety' : 'diet')} wymagających
                        uwagi.
                    </div>
                    <button
                        className="px-3 py-1 bg-amber-100 hover:bg-amber-200 text-amber-700 rounded-md text-xs font-medium transition"
                        onClick={() => setShowOnlyWarnings(true)}
                    >
                        Pokaż tylko ostrzeżenia
                    </button>
                </div>
            )}

            {/* Filtry */}
            <div
                className={`transition-all duration-300 overflow-hidden ${filterExpanded ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'}`}>
                {filterExpanded && (
                    <div className="bg-white rounded-lg shadow-sm p-4 border border-slate-200">
                        <DietFilter
                            activeUsers={activeUsers}
                            selectedUserId={selectedUserId}
                            onUserSelect={setSelectedUserId}
                            searchQuery={searchQuery}
                            onSearchChange={setSearchQuery}
                            onReset={handleResetFilters}
                            sortBy={sortBy}
                            onSortChange={setSortBy}
                        />
                    </div>
                )}
            </div>

            {/* Lista diet */}
            {filteredAndSortedDiets.length === 0 ? (
                <div className="text-center py-16 bg-white rounded-lg shadow-sm">
                    <FileText className="h-12 w-12 text-slate-300 mx-auto mb-4"/>
                    <h3 className="text-lg font-medium text-slate-700">Brak diet do wyświetlenia</h3>
                    <p className="text-slate-500 max-w-md mx-auto mt-2">
                        {showOnlyWarnings
                            ? 'Nie ma żadnych diet wymagających uwagi. Wszystkie diety mają odpowiedni zapas czasu.'
                            : 'Nie znaleziono diet spełniających kryteria wyszukiwania. Spróbuj zmienić filtry lub odświeżyć listę.'}
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {filteredAndSortedDiets.map(diet => (
                        <DietCard
                            key={diet.id}
                            diet={diet}
                            onViewClick={() => setSelectedDiet(diet)}
                            onEditClick={() => setEditingDiet(diet)}
                            onDeleteClick={handleDietDelete}
                        />
                    ))}
                </div>
            )}

            {selectedDiet && (
                <DietView
                    diet={selectedDiet}
                    onClose={() => setSelectedDiet(null)}
                />
            )}

            {editingDiet && (
                <DietEditModal
                    diet={editingDiet}
                    onClose={() => setEditingDiet(null)}
                    onUpdate={async (updatedDiet) => await handleDietUpdate(updatedDiet)}
                    onDelete={handleDietDelete}
                />
            )}
        </div>
    );
};

export default DietManagement;