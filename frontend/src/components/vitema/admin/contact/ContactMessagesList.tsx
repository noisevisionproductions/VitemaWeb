import React, {useEffect, useState} from "react";
import {ContactMessage, ContactMessageStatus} from "../../../../types/contact";
import {AdminContactService} from "../../../../services/contact/AdminContactService";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";
import {formatTimestamp} from "../../../../utils/dateFormatters";

interface ContactMessagesListProps {
    onSelectMessage: (id: string) => void;
}

const ContactMessagesList: React.FC<ContactMessagesListProps> = ({onSelectMessage}) => {
    const [messages, setMessages] = useState<ContactMessage[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchMessages().catch(console.error);
    }, []);

    const fetchMessages = async () => {
        try {
            setLoading(true);
            const data = await AdminContactService.getContactMessages();
            setMessages(data);
        } catch (error) {
            console.error('Error fetching contact messages:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id: string, status: ContactMessageStatus) => {
        try {
            await AdminContactService.updateMessageStatus(id, status);
            setMessages(messages.map(msg =>
                msg.id === id ? {...msg, status} : msg
            ));
        } catch (error) {
            console.error('Error updating message status:', error);
        }
    };

    const getStatusBadge = (status: ContactMessageStatus) => {
        switch (status) {
            case 'NEW':
                return <span className="px-2 py-1 text-xs rounded-full bg-status-info/10 text-status-info">Nowa</span>;
            case 'PROCESSING':
                return <span className="px-2 py-1 text-xs rounded-full bg-status-warning/10 text-status-warning">W trakcie</span>;
            case 'COMPLETED':
                return <span
                    className="px-2 py-1 text-xs rounded-full bg-status-success/10 text-status-success">Zakończona</span>;
            default:
                return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600">Nieznany</span>;
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center py-8">
                <LoadingSpinner/>
            </div>
        );
    }

    if (messages.length === 0) {
        return (
            <div className="text-center py-8 text-gray-500">
                Brak wiadomości kontaktowych
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-semibold">Wiadomości kontaktowe</h2>
                <button
                    onClick={fetchMessages}
                    className="px-4 py-2 text-sm bg-primary text-white rounded-md hover:bg-primary-dark"
                >
                    Odśwież
                </button>
            </div>

            <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 rounded-lg">
                <table className="min-w-full divide-y divide-gray-300">
                    <thead className="bg-gray-50">
                    <tr>
                        <th scope="col"
                            className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-6">Nadawca
                        </th>
                        <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Kontakt
                        </th>
                        <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Data</th>
                        <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Status
                        </th>
                        <th scope="col" className="px-3 py-3.5 text-right text-sm font-semibold text-gray-900">Akcje
                        </th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                    {messages.map((message) => (
                        <tr key={message.id}>
                            <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm sm:pl-6">
                                <div
                                    className="font-medium text-gray-900 hover:text-primary cursor-pointer"
                                    onClick={() => onSelectMessage(message.id)}
                                >
                                    {message.name}
                                </div>
                            </td>
                            <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                                <div>{message.email}</div>
                                {message.phone && <div className="text-xs text-gray-400">{message.phone}</div>}
                            </td>
                            <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                                {formatTimestamp(message.createdAt)}
                            </td>
                            <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                                {getStatusBadge(message.status)}
                            </td>
                            <td className="whitespace-nowrap px-3 py-4 text-sm text-right">
                                <div className="flex justify-end space-x-2">
                                    <button
                                        onClick={() => updateStatus(message.id, 'PROCESSING')}
                                        className="text-xs text-status-warning hover:text-status-warning-dark"
                                        disabled={message.status === 'PROCESSING'}
                                    >
                                        W trakcie
                                    </button>
                                    <button
                                        onClick={() => updateStatus(message.id, 'COMPLETED')}
                                        className="text-xs text-status-success hover:text-status-success-dark"
                                        disabled={message.status === 'COMPLETED'}
                                    >
                                        Zakończ
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ContactMessagesList;