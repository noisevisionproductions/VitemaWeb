import React from 'react';
import { User, Gender } from '../../types/user';
import {calculateAge, formatTimestamp} from '../../utils/dateFormatters';
import { Dialog } from '@headlessui/react';
import { X } from 'lucide-react';

interface UserDetailsModalProps {
    user: User;
    onClose: () => void;
    onUpdate: () => Promise<void>;
}

const UserDetailsModal: React.FC<UserDetailsModalProps> = ({ user, onClose}) => {
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

    return (
        <Dialog open={true} onClose={onClose} className="relative z-50">
            <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

            <div className="fixed inset-0 flex items-center justify-center p-4">
                <div className="mx-auto max-w-2xl w-full bg-white rounded-lg shadow-xl">
                    <div className="flex justify-between items-center p-6 border-b">
                        <h2 className="text-lg font-medium">
                            Szczegóły Użytkownika
                        </h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-500"
                        >
                            <X className="h-6 w-6" />
                        </button>
                    </div>

                    <div className="p-6 space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Email
                                </label>
                                <div className="mt-1 text-sm text-gray-900">
                                    {user.email}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Nick
                                </label>
                                <div className="mt-1 text-sm text-gray-900">
                                    {user.nickname}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Data utworzenia
                                </label>
                                <div className="mt-1 text-sm text-gray-900">
                                    {formatTimestamp(user.createdAt)}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Wiek
                                </label>
                                <div className="mt-1 text-sm text-gray-900">
                                    {calculateAge(user.birthDate)} lat
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Płeć
                                </label>
                                <div className="mt-1 text-sm text-gray-900">
                                    {getGenderLabel(user.gender)}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    Status profilu
                                </label>
                                <div className="mt-1">
                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                        user.profileCompleted
                                            ? 'bg-green-100 text-green-800'
                                            : 'bg-yellow-100 text-yellow-800'
                                    }`}>
                                        {user.profileCompleted ? 'Kompletny' : 'Niekompletny'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Dialog>
    );
};

export default UserDetailsModal;