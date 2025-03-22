import React, {useMemo} from "react";
import {User} from "../../types/user";
import LoadingSpinner from "../common/LoadingSpinner";
import {useDietInfo} from "../../hooks/diet/useDietInfo";
import {formatTimestamp} from "../../utils/dateFormatters";
import {StickyNote} from "lucide-react";

interface UserSelectorTableProps {
    users: User[];
    selectedUser: User | null;
    onUserSelect: (user: User) => void;
    loading: boolean;
}

const UserSelectorTable: React.FC<UserSelectorTableProps> = ({
                                                                 users,
                                                                 selectedUser,
                                                                 onUserSelect,
                                                                 loading
                                                             }) => {
    const userIds = useMemo(() => users.map(user => user.id), [users]);
    const {dietInfo, loading: dietLoading, loadingStates} = useDietInfo(userIds);

    if (loading || dietLoading) {
        return (
            <div className="flex justify-center p-4">
                <LoadingSpinner/>
            </div>
        );
    }

    const renderDietStatus = (userId: string) => {
        if (loadingStates?.[userId]) {
            return (
                <div className="flex justify-start items-center h-8">
                    <LoadingSpinner size="sm"/>
                </div>
            );
        }

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
                        {formatTimestamp(info.startDate)} - {formatTimestamp(info.endDate)}
                    </div>
                )}
            </div>
        );
    };

    const renderNote = (note?: string) => {
        if (!note) return null;

        return (
            <div className="flex items-center gap-1 text-xs text-gray-600">
                <StickyNote className="w-3 h-3"/>
                <span className="truncate max-w-[200px]" title={note}>
                    {note}
                </span>
            </div>
        )
    }

    return (
        <div className="h-100 overflow-y-auto"> {/* Zwiększona wysokość kontenera */}
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
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Notatka
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
                                {user.email || "Brak adresu e-mail"}
                            </div>
                            <div className="text-xs text-gray-500">
                                {user.nickname || "Brak pseudonimu"}
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
                        <td className="px-4 py-2">
                            {renderNote(user.note)}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default UserSelectorTable;