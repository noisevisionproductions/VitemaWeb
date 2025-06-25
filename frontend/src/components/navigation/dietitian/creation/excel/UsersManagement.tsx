import React, {useEffect, useState} from "react";
import UsersList from "../../../../users/UsersList";
import UserDetailsModal from "../../../../users/UserDetailsModal";
import {User} from "../../../../../types/user";
import useUsers from "../../../../../hooks/useUsers";
import LoadingSpinner from "../../../../common/LoadingSpinner";
import SectionHeader from "../../../../common/SectionHeader";

const UsersManagement: React.FC = () => {
    const {users, loading, fetchUsers} = useUsers();
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    useEffect(() => {
        const initFetch = async () => {
            try {
                await fetchUsers();
            } catch (error) {
                console.error('Error in initial fetch:', error);
            }
        };

        initFetch().catch();
    }, []);


    return (
        <div className="space-y-6 pb-8">
            <SectionHeader title="Lista użytkowników"
                           description="Wgląd do informacji Twoich klientów"
            />

            {loading ? (
                <div className="flex justify-center items-center h-64">
                    <LoadingSpinner/>
                </div>
            ) : (
                <UsersList
                    users={users}
                    onUserSelect={setSelectedUser}
                    onUpdate={fetchUsers}
                />
            )}

            {selectedUser && (
                <UserDetailsModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={fetchUsers}
                />
            )}
        </div>
    );
};

export default UsersManagement;