import React, {useMemo} from "react";
import {User} from "../../../types/user";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import {useDietInfo} from "../../../hooks/diet/useDietInfo";
import {formatTimestamp, timestampToDate} from "../../../utils/dateFormatters";
import {StickyNote} from "lucide-react";
import {EmptyUsersState} from "./EmptyUsersState";

interface UserSelectorTableProps {
    users: User[];
    isFiltered?: boolean;
    selectedUser: User | null;
    onUserSelect: (user: User) => void;
    loading: boolean;
}

const UserSelectorTable: React.FC<UserSelectorTableProps> = ({
                                                                 users,
                                                                 isFiltered = false,
                                                                 selectedUser,
                                                                 onUserSelect,
                                                                 loading
                                                             }) => {
    const userIds = useMemo(() => users.map(user => user.id), [users]);
    const {dietInfo, loading: dietLoading, loadingStates} = useDietInfo(userIds);

    if (loading || dietLoading) {
        return (
            <div className="flex justify-center p-2">
                <LoadingSpinner/>
            </div>
        );
    }

    if (!loading && !dietLoading && users.length === 0) {
        return <EmptyUsersState isSearching={isFiltered}/>;
    }

    const renderDietStatus = (userId: string) => {
        if (loadingStates?.[userId]) {
            return <LoadingSpinner size="sm"/>;
        }

        const info = dietInfo[userId];
        if (!info || !info.hasDiet) {
            return <span className="text-xs text-gray-500 dark:text-gray-400">Brak diety</span>;
        }

        // Bezpieczna konwersja dat
        const now = new Date();
        const startDate = timestampToDate(info.startDate);
        const endDate = timestampToDate(info.endDate);

        // Bezpieczne formatowanie dla wyświetlania
        const formattedStartDate = startDate ? formatTimestamp(startDate, false) : "Nieprawidłowa data";
        const formattedEndDate = endDate ? formatTimestamp(endDate, false) : "Nieprawidłowa data";

        // Określenie statusu diety
        let statusStyle = "text-gray-600 dark:text-gray-400";
        let statusText = "Dieta przypisana";
        let daysMessage = "";

        if (startDate && endDate) {
            if (startDate <= now && endDate >= now) {
                // Aktywna dieta
                statusStyle = "text-green-600 dark:text-green-400 font-medium";
                statusText = "Aktywna dieta";
                const daysLeft = Math.ceil((endDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
                daysMessage = `Pozostało ${daysLeft} dni`;
            } else if (startDate > now) {
                // Przyszła dieta
                statusStyle = "text-blue-600 dark:text-blue-400";
                statusText = "Zaplanowana dieta";
                const daysToStart = Math.ceil((startDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
                daysMessage = `Za ${daysToStart} dni`;
            } else {
                // Zakończona dieta
                statusStyle = "text-gray-400 dark:text-gray-500";
                statusText = "Zakończona dieta";
            }
        }

        return (
            <div className="flex flex-col text-xs">
                <div className={`flex items-center ${statusStyle}`}>
                    <span>{statusText}</span>
                </div>
                {daysMessage && <span className="text-xs text-gray-500 dark:text-gray-400">{daysMessage}</span>}
                <div className="text-xs text-gray-400 dark:text-gray-500 cursor-help"
                     title={`${formattedStartDate} - ${formattedEndDate}`}>
                    {formattedStartDate}
                </div>
            </div>
        );
    };

    return (
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-800 sticky top-0 z-10">
            <tr>
                <th className="px-2 py-1 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-10">

                </th>
                <th className="px-2 py-1 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Użytkownik
                </th>
                <th className="px-2 py-1 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Status diety
                </th>
                <th className="px-2 py-1 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Notatka
                </th>
            </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            {users.map((user) => (
                <tr
                    key={user.id}
                    className={`hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer ${
                        selectedUser?.id === user.id ? 'bg-blue-50 dark:bg-blue-900/30' : ''
                    }`}
                    onClick={() => onUserSelect(user)}
                >
                    <td className="px-2 py-1.5">
                        <input
                            type="radio"
                            name="selectedUser"
                            checked={selectedUser?.id === user.id}
                            onChange={() => onUserSelect(user)}
                            className="h-3 w-3 text-blue-600 dark:text-blue-400"
                            onClick={(e) => e.stopPropagation()}
                        />
                    </td>
                    <td className="px-2 py-1.5">
                        <div className="text-xs font-medium text-gray-900 dark:text-gray-100 truncate max-w-[180px]" title={user.email}>
                            {user.email || "Brak adresu e-mail"}
                        </div>
                        {user.nickname && (
                            <div className="text-xs text-gray-500 dark:text-gray-400 truncate max-w-[180px]" title={user.nickname}>
                                {user.nickname}
                            </div>
                        )}
                    </td>
                    <td className="px-2 py-1.5">
                        {renderDietStatus(user.id)}
                    </td>
                    <td className="px-2 py-1.5">
                        {user.note && (
                            <div className="flex items-center gap-1 text-xs text-gray-600 dark:text-gray-400">
                                <StickyNote className="w-3 h-3 flex-shrink-0"/>
                                <span className="truncate max-w-[120px]" title={user.note}>
                                        {user.note}
                                    </span>
                            </div>
                        )}
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};

export default UserSelectorTable;