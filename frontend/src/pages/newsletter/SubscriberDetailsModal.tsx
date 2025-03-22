import {NewsletterSubscriber} from "../../types/newsletter";
import React from "react";
import Modal from "../../components/common/Modal";
import {formatTimestamp} from "../../utils/dateFormatters";

interface SubscriberDetailsModalProps {
    subscriber: NewsletterSubscriber;
    isOpen: boolean;
    onClose: () => void;
}

const SubscriberDetailsModal: React.FC<SubscriberDetailsModalProps> = ({
                                                                           subscriber,
                                                                           isOpen,
                                                                           onClose
                                                                       }) => {
    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Szczegóły subskrybenta">
            <div className="space-y-4">
                <div>
                    <p className="text-sm font-medium text-gray-500">Email:</p>
                    <p className="mt-1">{subscriber.email}</p>
                </div>

                <div>
                    <p className="text-sm font-medium text-gray-500">Rola:</p>
                    <p className="mt-1">{subscriber.role === 'DIETITIAN' ? 'Dietetyk' : 'Firma'}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <p className="text-sm font-medium text-gray-500">Status:</p>
                        <p className="mt-1">
                            {subscriber.active ? 'Aktywny' : 'Nieaktywny'}
                            {subscriber.active && (
                                <span className="ml-2">
                                    ({subscriber.verified ? 'Zweryfikowany' : 'Niezweryfikowany'})
                                </span>
                            )}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm font-medium text-gray-500">Data rejestracji:</p>
                        <p className="mt-1">{formatTimestamp(subscriber.createdAt)}</p>
                    </div>
                </div>

                {subscriber.verified && subscriber.verifiedAt && (
                    <div>
                        <p className="text-sm font-medium text-gray-500">Data weryfikacji:</p>
                        <p className="mt-1">{formatTimestamp(subscriber.verifiedAt)}</p>
                    </div>
                )}

                {subscriber.lastEmailSent && (
                    <div>
                        <p className="text-sm font-medium text-gray-500">Ostatni wysłany email:</p>
                        <p className="mt-1">{formatTimestamp(subscriber.lastEmailSent)}</p>
                    </div>
                )}

                {subscriber.metadata && Object.keys(subscriber.metadata).length > 0 && (
                    <div>
                        <p className="text-sm font-medium text-gray-500">Metadane:</p>
                        <div className="mt-1 bg-gray-50 p-2 rounded">
                            {Object.entries(subscriber.metadata).map(([key, value]) => (
                                <div key={key} className="grid grid-cols-2">
                                    <span className="text-sm font-medium">{key}:</span>
                                    <span className="text-sm">{value}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            <div className="mt-6 flex justify-end space-x-3">
                <button
                    onClick={onClose}
                    className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                >
                    Zamknij
                </button>
            </div>
        </Modal>
    );
};

export default SubscriberDetailsModal;