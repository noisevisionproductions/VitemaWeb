import React, {useState} from 'react';
import {useInvitations} from '../../../hooks/useInvitations';
import {Invitation, InvitationStatus} from '../../../types'; // Sprawdź importy
import {format} from 'date-fns';
import {pl} from 'date-fns/locale';
import SendInvitationModal from './SendInvitationModal';
import {CheckIcon, ClipboardDocumentIcon, EnvelopeIcon, TrashIcon} from '@heroicons/react/24/outline';
import RefreshButton from "../../shared/common/RefreshButton"; // Upewnij się co do ścieżki

interface TrainerInvitationsListProps {
    hideHeader?: boolean;
}

const TrainerInvitationsList: React.FC<TrainerInvitationsListProps> = ({ hideHeader = false }) => {
    const {invitations, isLoading, error, refetch, deleteInvitation, isDeleting} = useInvitations();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [copiedCode, setCopiedCode] = useState<string | null>(null);

    const handleCopyCode = async (code: string) => {
        try {
            await navigator.clipboard.writeText(code);
            setCopiedCode(code);
            setTimeout(() => setCopiedCode(null), 2000);
        } catch (err) {
            console.error('Failed to copy code:', err);
        }
    };

    const handleDeleteInvitation = (invitation: Invitation) => {
        const confirmed = window.confirm(
            `Czy na pewno chcesz usunąć zaproszenie dla ${invitation.clientEmail}?\n\nKod: ${invitation.code}`
        );

        if (confirmed) {
            deleteInvitation(invitation.id);
        }
    };

    const getStatusBadge = (invitation: Invitation) => {
        const now = Date.now();
        const isExpired = now > invitation.expiresAt;

        if (invitation.status === InvitationStatus.ACCEPTED) {
            return (
                <span
                    className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    Zaakceptowane
                </span>
            );
        }

        if (isExpired) {
            return (
                <span
                    className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                    Wygasłe
                </span>
            );
        }

        return (
            <span
                className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                Oczekuje
            </span>
        );
    };

    const formatDate = (timestamp: number) => {
        return format(new Date(timestamp), 'dd MMM yyyy, HH:mm', {locale: pl});
    };

    const getDaysUntilExpiration = (expiresAt: number) => {
        const now = Date.now();
        const diff = expiresAt - now;
        const days = Math.ceil(diff / (1000 * 60 * 60 * 24));

        if (days < 0) return 'Wygasło';
        if (days === 0) return 'Wygasa dzisiaj';
        if (days === 1) return '1 dzień';
        return `${days} dni`;
    };

    if (isLoading && invitations.length === 0) {
        return (
            <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex flex-col items-center gap-2">
                <p className="text-red-800 text-center">
                    Wystąpił błąd podczas pobierania zaproszeń.
                </p>
                <button
                    onClick={() => refetch()}
                    className="text-sm text-red-600 hover:text-red-800 underline"
                >
                    Spróbuj ponownie
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {!hideHeader && (
                <div className="flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900">Zaproszenia</h2>
                        <p className="mt-1 text-sm text-gray-600">
                            Zarządzaj zaproszeniami dla swoich podopiecznych
                        </p>
                    </div>
                    <RefreshButton onRefresh={async () => { await refetch() }} isLoading={isLoading} />
                </div>
            )}

            {/* Stats */}
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
                <div className="bg-white overflow-hidden shadow rounded-lg border border-gray-100">
                    <div className="px-4 py-5 sm:p-6">
                        <dt className="text-sm font-medium text-gray-500 truncate">
                            Wszystkie
                        </dt>
                        <dd className="mt-1 text-3xl font-semibold text-gray-900">
                            {invitations.length}
                        </dd>
                    </div>
                </div>
                <div className="bg-white overflow-hidden shadow rounded-lg border border-gray-100">
                    <div className="px-4 py-5 sm:p-6">
                        <dt className="text-sm font-medium text-gray-500 truncate">
                            Oczekujące
                        </dt>
                        <dd className="mt-1 text-3xl font-semibold text-yellow-600">
                            {invitations.filter(inv =>
                                inv.status === InvitationStatus.PENDING &&
                                Date.now() <= inv.expiresAt
                            ).length}
                        </dd>
                    </div>
                </div>
                <div className="bg-white overflow-hidden shadow rounded-lg border border-gray-100">
                    <div className="px-4 py-5 sm:p-6">
                        <dt className="text-sm font-medium text-gray-500 truncate">
                            Zaakceptowane
                        </dt>
                        <dd className="mt-1 text-3xl font-semibold text-green-600">
                            {invitations.filter(inv =>
                                inv.status === InvitationStatus.ACCEPTED
                            ).length}
                        </dd>
                    </div>
                </div>
            </div>

            {/* Table */}
            <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
                {invitations.length === 0 ? (
                    <div className="text-center py-12">
                        <EnvelopeIcon className="mx-auto h-12 w-12 text-gray-400"/>
                        <h3 className="mt-2 text-sm font-medium text-gray-900">
                            Brak zaproszeń
                        </h3>
                        <p className="mt-1 text-sm text-gray-500">
                            Zacznij od wysłania pierwszego zaproszenia.
                        </p>
                        <div className="mt-6">
                            <button
                                onClick={() => setIsModalOpen(true)}
                                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
                            >
                                <EnvelopeIcon className="h-5 w-5 mr-2"/>
                                Wyślij zaproszenie
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Email
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Kod
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Status
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Data utworzenia
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Wygasa
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Akcje
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {invitations.map((invitation) => (
                                <tr key={invitation.id} className="hover:bg-gray-50 transition-colors">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm font-medium text-gray-900">
                                            {invitation.clientEmail}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center space-x-2">
                                            <code
                                                className="px-2 py-1 text-sm font-mono bg-gray-100 rounded border border-gray-200 text-gray-600">
                                                {invitation.code}
                                            </code>
                                            <button
                                                onClick={() => handleCopyCode(invitation.code)}
                                                className="text-gray-400 hover:text-primary transition-colors p-1 rounded-full hover:bg-gray-100"
                                                title="Kopiuj kod"
                                            >
                                                {copiedCode === invitation.code ? (
                                                    <CheckIcon className="h-4 w-4 text-green-500"/>
                                                ) : (
                                                    <ClipboardDocumentIcon className="h-4 w-4"/>
                                                )}
                                            </button>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        {getStatusBadge(invitation)}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {formatDate(invitation.createdAt)}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm text-gray-900">
                                            {getDaysUntilExpiration(invitation.expiresAt)}
                                        </div>
                                        <div className="text-xs text-gray-500">
                                            {formatDate(invitation.expiresAt)}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                        {invitation.status === InvitationStatus.PENDING && (
                                            <button
                                                onClick={() => handleDeleteInvitation(invitation)}
                                                disabled={isDeleting}
                                                className="text-red-400 hover:text-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors p-1 rounded-full hover:bg-red-50"
                                                title="Usuń zaproszenie"
                                            >
                                                <TrashIcon className="h-5 w-5"/>
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Modal */}
            <SendInvitationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </div>
    );
};

export default TrainerInvitationsList;