import React, {useEffect, useRef, useState} from "react";
import {ExternalRecipient} from "../../../../../types/email";
import {Check, Mail, MessageCircle, MoreHorizontal, Trash2, X} from "lucide-react";
import {cn} from "../../../../../utils/cs";
import ConfirmationDialog from "../../../../shared/common/ConfirmationDialog";
import RecipientDetailsModal from "./RecipientDetailsModal";
import {formatPostgresTimestamp} from "../../../../../utils/dateFormatters";

interface RecipientsListProps {
    recipients: ExternalRecipient[];
    onUpdateStatus: (id: string, status: ExternalRecipient['status']) => Promise<void>;
    onDelete: (id: string) => Promise<void>;
}

const RecipientsList: React.FC<RecipientsListProps> = ({
                                                           recipients,
                                                           onUpdateStatus,
                                                           onDelete
                                                       }) => {
    const [selectedRecipient, setSelectedRecipient] = useState<ExternalRecipient | null>(null);
    const [recipientToDelete, setRecipientToDelete] = useState<string | null>(null);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [statusDropdownOpen, setStatusDropdownOpen] = useState<string | null>(null);

    const dropdownRefs = useRef<{ [key: string]: HTMLDivElement | null }>({});

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (statusDropdownOpen &&
                dropdownRefs.current[statusDropdownOpen] &&
                !dropdownRefs.current[statusDropdownOpen]?.contains(event.target as Node)) {
                setStatusDropdownOpen(null);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [statusDropdownOpen]);

    const handleOpenDetails = (recipient: ExternalRecipient) => {
        setSelectedRecipient(recipient);
        setIsDetailsModalOpen(true);
    };

    const handleStatusChange = async (id: string, status: ExternalRecipient['status']) => {
        await onUpdateStatus(id, status);
        setStatusDropdownOpen(null);
    };

    const handleDeleteConfirm = async () => {
        if (recipientToDelete) {
            await onDelete(recipientToDelete);
            setIsDetailsModalOpen(false);
            setRecipientToDelete(null);
        }
    };

    const getStatusBadge = (status: ExternalRecipient['status']) => {
        switch (status) {
            case 'new':
                return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">Nowy</span>;
            case 'contacted':
                return <span className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">Skontaktowano</span>;
            case 'responded':
                return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">Odpowiedział</span>;
            case 'subscribed':
                return <span
                    className="px-2 py-1 text-xs rounded-full bg-primary-light bg-opacity-30 text-primary-dark">Zapisany</span>;
            case 'rejected':
                return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">Odrzucony</span>;
            default:
                return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">{status}</span>;
        }
    };

    if (recipients.length === 0) {
        return (
            <div className="text-center py-8 text-gray-500">
                Brak odbiorców spełniających kryteria
            </div>
        );
    }

    return (
        <>
            <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                    <thead>
                    <tr className="bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        <th className="px-4 py-3 border-b">Email</th>
                        <th className="px-4 py-3 border-b">Imię</th>
                        <th className="px-4 py-3 border-b">Kategoria</th>
                        <th className="px-4 py-3 border-b">Status</th>
                        <th className="px-4 py-3 border-b">Data dodania</th>
                        <th className="px-4 py-3 border-b">Akcje</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                    {recipients.map((recipient) => (
                        <tr key={recipient.id} className="hover:bg-gray-50">
                            <td className="px-4 py-3 whitespace-nowrap">
                                <div className="flex items-center">
                                    <Mail size={16} className="mr-2 text-gray-400"/>
                                    <span className="text-sm font-medium text-gray-900">{recipient.email}</span>
                                </div>
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                                {recipient.name || '-'}
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap">
                                <span className="text-sm text-gray-900">{recipient.category}</span>
                                {recipient.tags && recipient.tags.length > 0 && (
                                    <div className="flex mt-1 flex-wrap gap-1">
                                        {recipient.tags.slice(0, 2).map((tag) => (
                                            <span
                                                key={tag}
                                                className="text-xs bg-gray-100 px-1.5 py-0.5 rounded-full text-gray-600"
                                            >
                                                    {tag}
                                                </span>
                                        ))}
                                        {recipient.tags.length > 2 && (
                                            <span
                                                className="text-xs bg-gray-100 px-1.5 py-0.5 rounded-full text-gray-600">
                                                    +{recipient.tags.length - 2}
                                                </span>
                                        )}
                                    </div>
                                )}
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap">
                                {getStatusBadge(recipient.status)}
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                                {formatPostgresTimestamp(recipient.createdAt)}
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                                <div className="flex items-center space-x-2">
                                    <button
                                        onClick={() => handleOpenDetails(recipient)}
                                        className="text-primary hover:text-primary-dark"
                                    >
                                        Szczegóły
                                    </button>

                                    <div className="relative" ref={el => (dropdownRefs.current[recipient.id] = el)}>
                                        <button
                                            onClick={() => setStatusDropdownOpen(statusDropdownOpen === recipient.id ? null : recipient.id)}
                                            className="text-gray-600 hover:text-gray-900"
                                        >
                                            <MoreHorizontal size={16}/>
                                        </button>

                                        {statusDropdownOpen === recipient.id && (
                                            <div
                                                className="fixed mt-2 w-48 bg-white rounded-md shadow-lg z-50 border border-gray-200"
                                                style={{
                                                    top: dropdownRefs.current[recipient.id]?.getBoundingClientRect().bottom,
                                                    left: dropdownRefs.current[recipient.id]?.getBoundingClientRect().left,
                                                    transform: 'translateX(-80%)'
                                                }}
                                            >
                                                <div className="py-1">
                                                    <button
                                                        onClick={() => handleStatusChange(recipient.id, 'contacted')}
                                                        className={cn(
                                                            "flex items-center w-full px-4 py-2 text-sm text-left",
                                                            recipient.status === 'contacted'
                                                                ? "bg-blue-50 text-blue-700"
                                                                : "text-gray-700 hover:bg-gray-100"
                                                        )}
                                                    >
                                                        <Mail size={16} className="mr-2"/>
                                                        Skontaktowano
                                                    </button>
                                                    <button
                                                        onClick={() => handleStatusChange(recipient.id, 'responded')}
                                                        className={cn(
                                                            "flex items-center w-full px-4 py-2 text-sm text-left",
                                                            recipient.status === 'responded'
                                                                ? "bg-green-50 text-green-700"
                                                                : "text-gray-700 hover:bg-gray-100"
                                                        )}
                                                    >
                                                        <MessageCircle size={16} className="mr-2"/>
                                                        Odpowiedział
                                                    </button>
                                                    <button
                                                        onClick={() => handleStatusChange(recipient.id, 'subscribed')}
                                                        className={cn(
                                                            "flex items-center w-full px-4 py-2 text-sm text-left",
                                                            recipient.status === 'subscribed'
                                                                ? "bg-primary-light bg-opacity-20 text-primary-dark"
                                                                : "text-gray-700 hover:bg-gray-100"
                                                        )}
                                                    >
                                                        <Check size={16} className="mr-2"/>
                                                        Zapisany
                                                    </button>
                                                    <button
                                                        onClick={() => handleStatusChange(recipient.id, 'rejected')}
                                                        className={cn(
                                                            "flex items-center w-full px-4 py-2 text-sm text-left",
                                                            recipient.status === 'rejected'
                                                                ? "bg-red-50 text-red-700"
                                                                : "text-gray-700 hover:bg-gray-100"
                                                        )}
                                                    >
                                                        <X size={16} className="mr-2"/>
                                                        Odrzucony
                                                    </button>
                                                    <div className="border-t border-gray-100 my-1"></div>
                                                    <button
                                                        onClick={() => {
                                                            setRecipientToDelete(recipient.id);
                                                            setIsDeleteModalOpen(true);
                                                            setStatusDropdownOpen(null);
                                                        }}
                                                        className="flex items-center w-full px-4 py-2 text-sm text-left text-red-600 hover:bg-red-50"
                                                    >
                                                        <Trash2 size={16} className="mr-2"/>
                                                        Usuń
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {selectedRecipient && (
                <RecipientDetailsModal
                    recipient={selectedRecipient}
                    isOpen={isDetailsModalOpen}
                    onClose={() => setIsDetailsModalOpen(false)}
                />
            )}

            <ConfirmationDialog
                isOpen={isDeleteModalOpen}
                onClose={() => {
                    setIsDeleteModalOpen(false);
                    setRecipientToDelete(null);
                }}
                onConfirm={handleDeleteConfirm}
                title="Potwierdź usunięcie"
                description="Czy na pewno chcesz usunąć tego odbiorcę? Tej operacji nie można cofnąć."
                confirmLabel="Usuń"
                variant="destructive"
            />
        </>
    );
};

export default RecipientsList;