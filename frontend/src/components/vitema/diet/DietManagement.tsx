import React, {useEffect, useState} from 'react';
import DietView from './view/DietView';
import DietEditModal from "./edit/DietEditModal";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import DietCard from "./DietCard";
import useUsers from "../../../hooks/useUsers";
import DietFilter, {SortOption} from "./DietFilter";
import {Diet} from "../../../types";
import {useDiets} from "../../../hooks/diet/useDiets";
import {toast} from "../../../utils/toast";
import {getTimestamp} from "../../../utils/dateUtils";
import {FileText, Filter, RefreshCw} from 'lucide-react';
import {getDaysRemainingToDietEnd, getDietWarningStatus, isDietEnded} from "../../../utils/diet/dietWarningUtils";
import {hasDietGap} from "../../../utils/diet/dietContinuityUtils";
import SectionHeader from "../../shared/common/SectionHeader";

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

    // Stany filtrów i sortowania
    const [selectedDiet, setSelectedDiet] = useState<DietWithUser | null>(null);
    const [editingDiet, setEditingDiet] = useState<DietWithUser | null>(null);
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState<SortOption>('newest');
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [filterExpanded, setFilterExpanded] = useState(true);
    const [showOnlyWarnings, setShowOnlyWarnings] = useState(false);
    const [showOnlyGaps, setShowOnlyGaps] = useState(false);

    // Odświeżanie diet co 5 minut
    useEffect(() => {
        const interval = setInterval(() => {
            refreshDiets().catch(console.error);
        }, 5 * 60 * 1000); // 5 minut
        return () => clearInterval(interval);
    }, [refreshDiets]);

    // Filtrowane i posortowane diety
    const filteredAndSortedDiets = React.useMemo(() => {
        let result = diets.filter(diet => {
            const matchesUser = selectedUserId ? diet.userId === selectedUserId : true;
            const matchesSearch = diet.userEmail?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                diet.metadata?.fileName?.toLowerCase().includes(searchQuery.toLowerCase());

            const warningStatus = getDietWarningStatus(diet);
            const matchesWarning = showOnlyWarnings ?
                (warningStatus !== 'normal' && !isDietEnded(diet)) :
                true;

            const hasGap = showOnlyGaps ?
                hasDietGap(diet, diets) && !isDietEnded(diet) :
                true;

            return matchesUser && matchesSearch && matchesWarning && hasGap;
        });

        if (showOnlyWarnings) {
            // Jeśli pokazujemy tylko ostrzeżenia, sortujemy po dniach pozostałych do końca diety
            return result.sort((a, b) => {
                const daysRemainingA = getDaysRemainingToDietEnd(a);
                const daysRemainingB = getDaysRemainingToDietEnd(b);
                return daysRemainingA - daysRemainingB;
            });
        }

        return result.sort((a, b) => {
            const timestampA = getTimestamp(a.createdAt);
            const timestampB = getTimestamp(b.createdAt);

            switch (sortBy) {
                case "newest":
                    return timestampB - timestampA;
                case "oldest":
                    return timestampA - timestampB;
                case "name":
                    return (a.metadata?.fileName || '').localeCompare(b.metadata?.fileName || '');
                default:
                    return 0;
            }
        });
    }, [diets, selectedUserId, searchQuery, sortBy, showOnlyWarnings, showOnlyGaps]);

    // Liczniki ostrzeżeń i przerw
    const gapsCount = React.useMemo(() => {
        return diets.filter(diet =>
            !isDietEnded(diet) && hasDietGap(diet, diets)
        ).length;
    }, [diets]);

    const warningCount = React.useMemo(() => {
        return diets.filter(diet => {
            const status = getDietWarningStatus(diet);
            return (status === 'critical' || status === 'warning') && !isDietEnded(diet);
        }).length;
    }, [diets]);

    // Resetowanie filtrów
    const handleResetFilters = () => {
        setSelectedUserId(null);
        setSearchQuery('');
        setSortBy('newest');
        setShowOnlyWarnings(false);
        setShowOnlyGaps(false);
    };

    // Odświeżanie diet
    const handleRefreshDiets = async () => {
        setIsRefreshing(true);
        try {
            await refreshDiets();
            toast.success('Diety zostały odświeżone');
        } catch (error) {
            toast.error('Nie udało się odświeżyć diet');
        } finally {
            setIsRefreshing(false);
        }
    };

    // Aktualizacja diety
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

    // Usuwanie diety
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

    // Lista aktywnych użytkowników
    const activeUsers = React.useMemo(() => {
        const uniqueUserIds = new Set(diets.map(diet => diet.userId));
        return users.filter(user => uniqueUserIds.has(user.id));
    }, [diets, users]);

    // Renderowanie podczas ładowania
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
            <SectionHeader
                title="Zarządzanie dietami"
                description="Zarządzaj dietami wszystkich użytkowników"
            />

            {/* Przyciski szybkich filtrów i odświeżania */}
            <div className="flex gap-2 flex-wrap">
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
                            showOnlyWarnings={showOnlyWarnings}
                            onWarningsChange={setShowOnlyWarnings}
                            showOnlyGaps={showOnlyGaps}
                            onGapsChange={setShowOnlyGaps}
                            warningCount={warningCount}
                            gapsCount={gapsCount}
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
                            : showOnlyGaps
                                ? 'Nie ma żadnych diet bez kontynuacji. Wszystkie diety mają zaplanowaną kontynuację.'
                                : 'Nie znaleziono diet spełniających kryteria wyszukiwania. Spróbuj zmienić filtry lub odświeżyć listę.'}
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {filteredAndSortedDiets.map(diet => (
                        <DietCard
                            key={diet.id}
                            diet={diet}
                            allDiets={diets}
                            onViewClick={() => setSelectedDiet(diet)}
                            onEditClick={() => setEditingDiet(diet)}
                            onDeleteClick={handleDietDelete}
                        />
                    ))}
                </div>
            )}

            {/* Modalne okna podglądu i edycji */}
            {selectedDiet && (
                <DietView
                    diet={selectedDiet}
                    allDiets={diets}
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