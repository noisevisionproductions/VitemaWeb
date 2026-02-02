import React, {useEffect, useState} from "react";
import {User} from "../../../../../../types/user";
import useUsers from "../../../../../../hooks/useUsers";
import UserSelectorTable from "../../../../users/UserSelectorTable";
import SearchInput from "../../../../../shared/common/SearchInput";

interface UserSelectorProps {
    selectedUser: User | null,
    onUserSelect: (user: User) => void;
}

const UserSelector: React.FC<UserSelectorProps> = ({selectedUser, onUserSelect}) => {
    const [searchQuery, setSearchQuery] = useState("");
    const {users, loading} = useUsers();
    const [filteredUsers, setFilteredUsers] = useState<User[]>([]);

    useEffect(() => {
        if (!users) return;

        if (searchQuery.trim() === "") {
            setFilteredUsers(users);
            return;
        }

        const lowercaseQuery = searchQuery.toLowerCase();
        const filtered = users.filter(user => {
            const emailMatch = user.email ? user.email.toLowerCase().includes(lowercaseQuery) : false;
            const nicknameMatch = user.nickname ? user.nickname.toLowerCase().includes(lowercaseQuery) : false;

            return emailMatch || nicknameMatch;
        });

        setFilteredUsers(filtered);
    }, [users, searchQuery]);

    const userCountInfo = loading
        ? "Ładowanie użytkowników..."
        : `Znaleziono ${filteredUsers.length} z ${users.length} użytkowników`;

    return (
        <div className="space-y-4 py-5">
            <SearchInput
                value={searchQuery}
                onChange={setSearchQuery}
                placeholder="Szukaj użytkownika..."
            />

            <div className="text-sm text-gray-500 mb-2">
                {userCountInfo}
            </div>

            <div className="border rounded-lg overflow-hidden">
                <div className="max-h-[600px] overflow-y-auto">
                    <UserSelectorTable
                        users={filteredUsers}
                        isFiltered={searchQuery.length > 0}
                        selectedUser={selectedUser}
                        onUserSelect={onUserSelect}
                        loading={loading}
                    />
                </div>
            </div>
        </div>
    );
};

export default UserSelector;