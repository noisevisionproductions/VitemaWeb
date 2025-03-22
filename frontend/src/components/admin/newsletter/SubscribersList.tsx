import React, {useState, useEffect} from 'react';
import {toast} from "../../../utils/toast";
import {AdminNewsletterService} from '../../../services/newsletter';
import LoadingSpinner from '../../common/LoadingSpinner';
import ConfirmationDialog from "../../common/ConfirmationDialog";
import SubscriberDetailsModal from "../../../pages/newsletter/SubscriberDetailsModal";
import {NewsletterSubscriber} from "../../../types/newsletter";
import {formatTimestamp} from "../../../utils/dateFormatters";

interface FilterOptions {
    status: 'all' | 'active' | 'inactive' | 'verified' | 'unverified';
    role: 'all' | 'DIETITIAN' | 'COMPANY';
    search: string;
}

const SubscribersList: React.FC = () => {
    const [subscribers, setSubscribers] = useState<NewsletterSubscriber[]>([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState<FilterOptions>({
        status: 'all',
        role: 'all',
        search: '',
    });

    const [selectedSubscriber, setSelectedSubscriber] = useState<NewsletterSubscriber | null>(null);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

    const [subscriberToDelete, setSubscriberToDelete] = useState<string | null>(null);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    useEffect(() => {
        fetchSubscribers().catch(console.error);
    }, []);

    const fetchSubscribers = async () => {
        try {
            setLoading(true);
            const data = await AdminNewsletterService.getAllSubscribers();
            setSubscribers(data);
        } catch (error) {
            console.error('Error fetching subscribers:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleStatus = async (id: string, currentActive: boolean) => {
        try {
            if (currentActive) {
                await AdminNewsletterService.deactivateSubscriber(id);
                toast.success('Subskrybent został dezaktywowany');
            } else {
                await AdminNewsletterService.activateSubscriber(id);
                toast.success('Subskrybent został aktywowany');
            }
            fetchSubscribers().catch(console.error);
        } catch (error) {
            console.error('Error toggling status:', error);
        }
    };

    const handleVerifyManually = async (id: string) => {
        try {
            await AdminNewsletterService.verifySubscriberManually(id);
            toast.success('Subskrybent został zweryfikowany');
            fetchSubscribers().catch(console.error);
        } catch (error) {
            console.error('Error verifying subscriber:', error);
        }
    };

    const handleDeleteSubscriber = async () => {
        if (!subscriberToDelete) return;

        try {
            await AdminNewsletterService.deleteSubscriber(subscriberToDelete);
            toast.success('Subskrybent został usunięty');
            setIsDeleteModalOpen(false);
            setSubscriberToDelete(null);
            fetchSubscribers().catch(console.error);
        } catch (error) {
            console.error('Error deleting subscriber:', error);
        }
    };

    const handleOpenDetails = (subscriber: NewsletterSubscriber) => {
        setSelectedSubscriber(subscriber);
        setIsDetailsModalOpen(true);
    };

    const filterSubscriber = (subscriber: NewsletterSubscriber) => {
        if (filters.status === 'active' && !subscriber.active) return false;
        if (filters.status === 'inactive' && subscriber.active) return false;
        if (filters.status === 'verified' && !subscriber.verified) return false;
        if (filters.status === 'unverified' && subscriber.verified) return false;
        if (filters.role !== 'all' && subscriber.role !== filters.role) return false;
        return !(filters.search && !subscriber.email.toLowerCase().includes(filters.search.toLowerCase()));
    };

    const handleExportCsv = () => {
        const header = 'Email,Rola,Data utworzenia,Zweryfikowany,Data weryfikacji,Aktywny\n';
        const csvContent = subscribers
            .filter(filterSubscriber)
            .map(sub => {
                return `"${sub.email}","${sub.role === 'DIETITIAN' ? 'Dietetyk' : 'Firma'}","${sub.createdAt}","${
                    sub.verified ? 'Tak' : 'Nie'
                }","${sub.verifiedAt}","${
                    sub.active ? 'Tak' : 'Nie'
                }"`;
            })
            .join('\n');

        const blob = new Blob([header + csvContent], {type: 'text/csv;charset=utf-8;'});
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `newsletter-subscribers-${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const filteredSubscribers = subscribers.filter(filterSubscriber);

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap justify-between items-center gap-4">
                <div className="flex flex-wrap gap-2">
                    <select
                        value={filters.status}
                        onChange={e => setFilters({...filters, status: e.target.value as any})}
                        className="rounded-md border border-gray-300 px-5 py-2 text-sm"
                    >
                        <option value="all">Status</option>
                        <option value="active">Aktywni</option>
                        <option value="inactive">Nieaktywni</option>
                        <option value="verified">Zweryfikowani</option>
                        <option value="unverified">Niezweryfikowani</option>
                    </select>

                    <select
                        value={filters.role}
                        onChange={e => setFilters({...filters, role: e.target.value as any})}
                        className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    >
                        <option value="all">Role</option>
                        <option value="DIETITIAN">Dietetycy</option>
                        <option value="COMPANY">Firmy</option>
                    </select>

                    <input
                        type="text"
                        placeholder="Szukaj po email"
                        value={filters.search}
                        onChange={e => setFilters({...filters, search: e.target.value})}
                        className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    />
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={fetchSubscribers}
                        className="px-3 py-2 bg-primary text-white rounded-md text-sm hover:bg-primary-dark"
                        disabled={loading}
                    >
                        {loading ? 'Odświeżanie...' : 'Odśwież listę'}
                    </button>

                    <button
                        onClick={handleExportCsv}
                        className="px-3 py-2 bg-primary text-white rounded-md text-sm hover:bg-primary-dark"
                    >
                        Eksportuj do CSV
                    </button>
                </div>
            </div>

            {loading ? (
                <div className="flex justify-center py-8">
                    <LoadingSpinner/>
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rola</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Data
                                zapisu
                            </th>
                            <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Akcje</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {filteredSubscribers.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-3 py-4 text-center text-sm text-gray-500">
                                    Brak subskrybentów spełniających kryteria
                                </td>
                            </tr>
                        ) : (
                            filteredSubscribers.map(subscriber => (
                                <tr key={subscriber.id} className={!subscriber.active ? 'bg-gray-50' : ''}>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm">{subscriber.email}</td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm">
                                        {subscriber.role === 'DIETITIAN' ? 'Dietetyk' : 'Firma'}
                                    </td>
                                    <td className="px-3 py-4 whitespace-nowrap">
                                        {!subscriber.active ? (
                                            <span
                                                className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                          Nieaktywny
                        </span>
                                        ) : subscriber.verified ? (
                                            <span
                                                className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                          Zweryfikowany
                        </span>
                                        ) : (
                                            <span
                                                className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                          Oczekuje weryfikacji
                        </span>
                                        )}
                                    </td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {formatTimestamp(subscriber.createdAt)}
                                    </td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm">
                                        <div className="flex space-x-2">
                                            <button
                                                onClick={() => handleOpenDetails(subscriber)}
                                                className="text-primary hover:text-primary-dark"
                                            >
                                                Szczegóły
                                            </button>

                                            {!subscriber.verified && subscriber.active && (
                                                <button
                                                    onClick={() => handleVerifyManually(subscriber.id)}
                                                    className="text-green-600 hover:text-green-800"
                                                >
                                                    Weryfikuj
                                                </button>
                                            )}

                                            <button
                                                onClick={() => handleToggleStatus(subscriber.id, subscriber.active)}
                                                className={subscriber.active ? "text-yellow-600 hover:text-yellow-800" : "text-green-600 hover:text-green-800"}
                                            >
                                                {subscriber.active ? 'Dezaktywuj' : 'Aktywuj'}
                                            </button>

                                            <button
                                                onClick={() => {
                                                    setSubscriberToDelete(subscriber.id);
                                                    setIsDeleteModalOpen(true);
                                                }}
                                                className="text-red-600 hover:text-red-800"
                                            >
                                                Usuń
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Modal szczegółów */}
            {selectedSubscriber && (
                <SubscriberDetailsModal
                    subscriber={selectedSubscriber}
                    isOpen={isDetailsModalOpen}
                    onClose={() => setIsDetailsModalOpen(false)}
                />
            )}

            {/* Modal potwierdzenia usunięcia */}
            <ConfirmationDialog
                isOpen={isDeleteModalOpen}
                onClose={() => {
                    setIsDeleteModalOpen(false);
                    setSubscriberToDelete(null);
                }}
                onConfirm={handleDeleteSubscriber}
                title="Potwierdź usunięcie"
                description="Czy na pewno chcesz całkowicie usunąć tego subskrybenta z bazy danych? Tej operacji nie można cofnąć."
                confirmLabel="Usuń"
                variant="destructive"
            />
        </div>
    );
};

export default SubscribersList;