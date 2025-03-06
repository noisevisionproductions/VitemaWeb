import React, {useState} from "react";
import {User} from "../../../types/user";
import useUsers from "../../../hooks/useUsers";
import UserSelectorTable from "../../users/UserSelectorTable";
import SearchInput from "../../common/SearchInput";

interface UserSelectorProps {
    selectedUser: User | null,
    onUserSelect: (user: User) => void;
}

const UserSelector: React.FC<UserSelectorProps> = ({selectedUser, onUserSelect}) => {
    const [searchQuery, setSearchQuery] = useState("");
    const {users, loading} = useUsers();

    const filteredUsers = users.filter(user => {
        if (!user || !user.email || !user.nickname) return false;

        const lowercaseQuery = searchQuery.toLowerCase();
        return user.email.toLowerCase().includes(lowercaseQuery) ||
            user.nickname.toLowerCase().includes(lowercaseQuery);
    });

    return (
        <div className="space-y-4">
            <SearchInput
                value={searchQuery}
                onChange={setSearchQuery}
                placeholder="Szukaj użytkownika..."
            />

            <div className="border rounded-lg overflow-hidden">
                <div className="max-h-[600px] overflow-y-auto">
                    <UserSelectorTable
                        users={filteredUsers}
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