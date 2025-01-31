import React, {useMemo} from "react";
import {User} from "../../types/user";
import LoadingSpinner from "../common/LoadingSpinner";
import {useDietInfo} from "../../hooks/useDietInfo";
import {formatDate} from "../../utils/dateFormatters";

interface UserSelectorTableProps {
    users: User[];
    selectedUser: User | null;
    onUserSelect: (user: User | null) => void;
    loading: boolean;
}

const UserSelectorTable: React.FC<UserSelectorTableProps> = ({
                                                                 users,
                                                                 selectedUser,
                                                                 onUserSelect,
                                                                 loading
                                                             }) => {
    const userIds = useMemo(() => users.map(user => user.id), [users]);
    const { dietInfo, loading: dietLoading } = useDietInfo(userIds);

    if (loading || dietLoading) {
        return (
            <div className="flex justify-center p-4">
                <LoadingSpinner/>
            </div>
        );
    }

    const renderDietStatus = (userId: string) => {
        const info = dietInfo[userId];
        if (!info || !info.hasDiet) {
            return (
                <span className="text-gray-500 text-xs">
                Brak przypisanej diety
            </span>
            );
        }

        return (
            <div className="text-xs">
                <div className="text-green-600 font-medium">
                    Dieta przypisana
                </div>
                {info.startDate && info.endDate && (
                    <div className="text-gray-500 text-xs">
                        {formatDate(info.startDate)} - {formatDate(info.endDate)}
                    </div>
                )}
            </div>
        );
    };

    return (
        <div className="h-96 overflow-y-auto"> {/* Zwiększona wysokość kontenera */}
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50 sticky top-0 z-10">
                <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Wybór
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Email/Nick
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status profilu
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status diety
                    </th>
                </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                {users.map((user) => (
                    <tr
                        key={user.id}
                        className={`hover:bg-gray-50 cursor-pointer ${
                            selectedUser?.id === user.id ? 'bg-blue-50' : ''
                        }`}
                        onClick={() => onUserSelect(user)}
                    >
                        <td className="px-4 py-2">
                            <input
                                type="radio"
                                name="selectedUser"
                                checked={selectedUser?.id === user.id}
                                onChange={() => onUserSelect(user)}
                                className="h-3 w-3 text-blue-600"
                                onClick={(e) => e.stopPropagation()}
                            />
                        </td>
                        <td className="px-4 py-2">
                            <div className="text-xs font-medium text-gray-900">
                                {user.email}
                            </div>
                            <div className="text-xs text-gray-500">
                                {user.nickname}
                            </div>
                        </td>
                        <td className="px-4 py-2">
                                <span className={`px-2 inline-flex text-xs leading-4 font-semibold rounded-full ${
                                    user.profileCompleted
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-yellow-100 text-yellow-800'
                                }`}>
                                    {user.profileCompleted ? 'Kompletny' : 'Niekompletny'}
                                </span>
                        </td>
                        <td className="px-4 py-2">
                            {renderDietStatus(user.id)}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default UserSelectorTable;