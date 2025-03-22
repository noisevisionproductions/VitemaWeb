import React, {useState, useEffect} from 'react';
import {AdminContactService} from '../../../services/contact/AdminContactService';
import {format} from 'date-fns';
import {pl} from 'date-fns/locale';
import LoadingSpinner from '../../common/LoadingSpinner';
import {ContactMessage, ContactMessageStatus} from "../../../types/contact";

interface ContactMessageDetailProps {
    id: string;
    onBack: () => void;
}

const ContactMessageDetail: React.FC<ContactMessageDetailProps> = ({id, onBack}) => {
    const [message, setMessage] = useState<ContactMessage | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (id) {
            fetchMessage(id).catch(console.error);
        }
    }, [id]);

    const fetchMessage = async (messageId: string) => {
        try {
            setLoading(true);
            const data = await AdminContactService.getContactMessage(messageId);
            setMessage(data);
        } catch (error) {
            console.error('Error fetching contact message:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (status: ContactMessageStatus) => {
        if (!message) return;

        try {
            await AdminContactService.updateMessageStatus(message.id, status);
            setMessage({...message, status});
        } catch (error) {
            console.error('Error updating message status:', error);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center py-8">
                <LoadingSpinner/>
            </div>
        );
    }

    if (!message) {
        return (
            <div className="text-center py-8 text-gray-500">
                Nie znaleziono wiadomości
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <button
                    onClick={onBack}
                    className="text-primary hover:text-primary-dark"
                >
                    &larr; Wróć do listy
                </button>

                <div className="flex space-x-3">
                    <button
                        onClick={() => updateStatus('PROCESSING')}
                        className={`px-4 py-2 text-sm rounded-md ${message.status === 'PROCESSING'
                            ? 'bg-status-warning/30 text-status-warning cursor-default'
                            : 'bg-status-warning/10 text-status-warning hover:bg-status-warning/20'}`}
                        disabled={message.status === 'PROCESSING'}
                    >
                        Oznacz jako "W trakcie"
                    </button>
                    <button
                        onClick={() => updateStatus('COMPLETED')}
                        className={`px-4 py-2 text-sm rounded-md ${message.status === 'COMPLETED'
                            ? 'bg-status-success/30 text-status-success cursor-default'
                            : 'bg-status-success/10 text-status-success hover:bg-status-success/20'}`}
                        disabled={message.status === 'COMPLETED'}
                    >
                        Oznacz jako "Zakończone"
                    </button>
                </div>
            </div>

            <div className="bg-white shadow overflow-hidden rounded-lg">
                <div className="px-4 py-5 sm:px-6 border-b">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">
                        Szczegóły wiadomości
                    </h3>
                    <p className="mt-1 max-w-2xl text-sm text-gray-500">
                        Wysłano: {format(message.createdAt.toDate(), 'dd MMMM yyyy, HH:mm', {locale: pl})}
                    </p>
                </div>
                <div className="border-t border-gray-200">
                    <dl>
                        <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                            <dt className="text-sm font-medium text-gray-500">
                                Nadawca
                            </dt>
                            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                                {message.name}
                            </dd>
                        </div>
                        <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                            <dt className="text-sm font-medium text-gray-500">
                                Email
                            </dt>
                            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                                <a href={`mailto:${message.email}`} className="text-primary hover:underline">
                                    {message.email}
                                </a>
                            </dd>
                        </div>
                        {message.phone && (
                            <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                                <dt className="text-sm font-medium text-gray-500">
                                    Telefon
                                </dt>
                                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                                    <a href={`tel:${message.phone}`} className="text-primary hover:underline">
                                        {message.phone}
                                    </a>
                                </dd>
                            </div>
                        )}
                        <div
                            className={`${message.phone ? 'bg-gray-50' : 'bg-white'} px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6`}>
                            <dt className="text-sm font-medium text-gray-500">
                                Status
                            </dt>
                            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                                {message.status === 'NEW' && <span className="text-status-info">Nowa</span>}
                                {message.status === 'PROCESSING' &&
                                    <span className="text-status-warning">W trakcie</span>}
                                {message.status === 'COMPLETED' &&
                                    <span className="text-status-success">Zakończona</span>}
                            </dd>
                        </div>
                        <div className="bg-white px-4 py-5 sm:px-6">
                            <dt className="text-sm font-medium text-gray-500 mb-3">
                                Wiadomość
                            </dt>
                            <dd className="mt-1 text-sm text-gray-900 sm:mt-0 whitespace-pre-wrap bg-gray-50 p-4 rounded-lg">
                                {message.message}
                            </dd>
                        </div>
                    </dl>
                </div>
            </div>
        </div>
    );
};

export default ContactMessageDetail;