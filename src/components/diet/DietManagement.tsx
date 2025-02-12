import React, {useState} from 'react';
import DietView from './view/DietView';
import DietEditModal from "./edit/DietEditModal";
import LoadingSpinner from "../common/LoadingSpinner";
import DietCard from "./DietCard";
import useUsers from "../../hooks/useUsers";
import DietFilter from "./DietFilter";
import {Diet} from "../../types";
import {useDiets} from "../../hooks/useDiets";

interface DietWithUser extends Diet {
    userEmail?: string;
}

const DietManagement: React.FC = () => {
    const {users, loading: usersLoading} = useUsers();
    const {diets, loading: dietsLoading} = useDiets(users, usersLoading);

    const [selectedDiet, setSelectedDiet] = useState<DietWithUser | null>(null);
    const [editingDiet, setEditingDiet] = useState<DietWithUser | null>(null);
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');

    const filteredDiets = diets.filter(diet => {
        const matchesUser = selectedUserId ? diet.userId === selectedUserId : true;
        const matchesSearch = diet.userEmail?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            diet.metadata?.fileName?.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesUser && matchesSearch;
    });

    const handleResetFilters = () => {
        setSelectedUserId(null);
        setSearchQuery('');
    };


    const handleDietUpdate = () => {
        setEditingDiet(null);
    };

    const handleDietDelete = () => {
        setSelectedDiet(null);
        setEditingDiet(null);
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
        <div className="space-y-6">
            <DietFilter
                activeUsers={activeUsers}
                selectedUserId={selectedUserId}
                onUserSelect={setSelectedUserId}
                searchQuery={searchQuery}
                onSearchChange={setSearchQuery}
                onReset={handleResetFilters}
            />

            <div className="flex justify-between items-center">
                <span className="text-gray-500">
                    Liczba diet: {filteredDiets.length}
                </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredDiets.map(diet => (
                    <DietCard
                        key={diet.id}
                        diet={diet}
                        onViewClick={() => setSelectedDiet(diet)}
                        onEditClick={() => setEditingDiet(diet)}
                    />
                ))}
            </div>

            {selectedDiet && (
                <DietView
                    diet={selectedDiet}
                    onClose={() => setSelectedDiet(null)}
                    onDelete={handleDietDelete}
                />
            )}

            {editingDiet && (
                <DietEditModal
                    diet={editingDiet}
                    onClose={() => setEditingDiet(null)}
                    onUpdate={handleDietUpdate}
                />
            )}
        </div>
    );
};

export default DietManagement;