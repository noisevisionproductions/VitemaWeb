import React from 'react';
import {Gender, User} from '../../../types/user';
import {displayAge, formatTimestamp} from '../../../utils/dateFormatters';
import {Dialog} from '@headlessui/react';
import {X} from 'lucide-react';
import {useMeasurements} from "../../../hooks/useMeasurements";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "../../shared/ui/Tabs";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import MeasurementsChart from "../measurements/MeasurementsChart";
import MeasurementsTable from "../measurements/MeasurementsTable";

interface UserDetailsModalProps {
    user: User;
    onClose: () => void;
    onUpdate: () => Promise<void>;
}

const UserDetailsModal: React.FC<UserDetailsModalProps> = ({user, onClose}) => {
    const {measurements, loading, error} = useMeasurements(user.id);

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
            <div className="fixed inset-0 bg-black/30" aria-hidden="true"/>

            <div className="fixed inset-0 flex items-center justify-center p-4">
                <div className="mx-auto max-w-6xl w-full bg-white rounded-lg shadow-xl max-h-[90vh] flex flex-col">
                    <div className="flex justify-between items-center p-6 border-b shrink-0">
                        <h2 className="text-xl font-medium">
                            Szczegóły Użytkownika
                        </h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-500"
                        >
                            <X className="h-6 w-6"/>
                        </button>
                    </div>

                    <div className="overflow-y-auto flex-1">
                        <Tabs defaultValue="info" className="p-6">
                            <TabsList className="mb-6">
                                <TabsTrigger value="info" className="px-6 py-2">
                                    Informacje
                                </TabsTrigger>
                                <TabsTrigger value="measurements" className="px-6 py-2">
                                    Pomiary
                                </TabsTrigger>
                            </TabsList>

                            <TabsContent value="info">
                                <div className="p-6 space-y-4">
                                    <div className="grid grid-cols-3 gap-6">
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
                                                {displayAge(user)}
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
                            </TabsContent>

                            <TabsContent value="measurements">
                                {loading ? (
                                    <div className="flex justify-center py-8">
                                        <LoadingSpinner/>
                                    </div>
                                ) : error ? (
                                    <div className="text-center py-8 text-red-600">
                                        Wystąpił błąd podczas ładowania pomiarów
                                    </div>
                                ) : measurements.length === 0 ? (
                                    <div className="text-center py-8 text-gray-500">
                                        Brak pomiarów dla tego użytkownika
                                    </div>
                                ) : (
                                    <div className="space-y-8">
                                        <div className="bg-gray-50 p-6 rounded-lg">
                                            <h3 className="text-lg font-medium mb-4">Wykres pomiarów</h3>
                                            <MeasurementsChart measurements={measurements}/>
                                        </div>

                                        <div className="bg-gray-50 p-6 rounded-lg">
                                            <h3 className="text-lg font-medium mb-4">Historia pomiarów</h3>
                                            <MeasurementsTable measurements={measurements}/>
                                        </div>
                                    </div>
                                )}
                            </TabsContent>
                        </Tabs>
                    </div>
                </div>
            </div>
        </Dialog>
    );
};

export default UserDetailsModal;