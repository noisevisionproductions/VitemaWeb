import React from 'react';
import {Gender, User, UserRole} from '../../../types/user';
import {formatAge} from '../../../utils/dateFormatters';
import UserNote from "./UserNote";
import {UserService} from "../../../services/UserService";
import {toast} from "../../../utils/toast";

interface UsersListProps {
    users: User[];
    onUserSelect: (user: User) => void;
    onUpdate: () => Promise<void>;
}

const UsersList: React.FC<UsersListProps> = ({users, onUserSelect, onUpdate}) => {

    const getGenderLabel = (gender: Gender | null) => {
        switch (gender) {
            case Gender.MALE:
                return 'Mężczyzna';
            case Gender.FEMALE:
                return 'Kobieta';
            default:
                return 'Nie podano';
        }
    };

    const handleNoteSave = async (userId: string, note: string) => {
        try {
            await UserService.updateUserNote(userId, note);
            await onUpdate();
            toast.success('Notatka została zaktualizowana');
        } catch (error) {
            console.error('Error updating note:', error);
            toast.error('Błąd podczas aktualizacji notatki');
            throw error;
        }
    };

    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Email/Nick
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Wiek
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Płeć
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Rola
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Notatka
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Akcje
                    </th>
                </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                {users.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm font-medium text-gray-900">
                                {user.email}
                            </div>
                            <div className="text-sm text-gray-500">
                                {user.nickname}
                            </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm text-gray-900">
                                {formatAge(user.birthDate)}
                            </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm text-gray-900">
                                {getGenderLabel(user.gender)}
                            </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                                <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                    user.profileCompleted
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-yellow-100 text-yellow-800'
                                }`}>
                                    {user.profileCompleted ? 'Kompletny' : 'Niekompletny'}
                                </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                                <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                    user.role === UserRole.ADMIN
                                        ? 'bg-purple-100 text-purple-800'
                                        : 'bg-blue-100 text-blue-800'
                                }`}>
                                    {user.role === UserRole.ADMIN ? 'Admin' : 'Użytkownik'}
                                </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                            <UserNote
                                note={user.note}
                                onSave={(note) => handleNoteSave(user.id, note)}
                            />
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <button
                                onClick={() => onUserSelect(user)}
                                className="text-blue-600 hover:text-blue-900"
                            >
                                Szczegóły
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default UsersList;