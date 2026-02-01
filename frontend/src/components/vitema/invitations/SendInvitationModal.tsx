import React, { useState } from 'react';
import Modal from '../../shared/common/Modal';
import { useInvitations } from '../../../hooks/useInvitations';
import { InvitationRequest } from '../../../types';

interface SendInvitationModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const SendInvitationModal: React.FC<SendInvitationModalProps> = ({ isOpen, onClose }) => {
    const [email, setEmail] = useState('');
    const [emailError, setEmailError] = useState('');
    const { sendInvitation, isSending } = useInvitations();

    const validateEmail = (email: string): boolean => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!email.trim()) {
            setEmailError('Email jest wymagany');
            return false;
        }
        if (!emailRegex.test(email)) {
            setEmailError('Podaj prawidłowy adres email');
            return false;
        }
        setEmailError('');
        return true;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!validateEmail(email)) {
            return;
        }

        const request: InvitationRequest = { email: email.trim() };
        
        sendInvitation(request, {
            onSuccess: () => {
                setEmail('');
                setEmailError('');
                onClose();
            }
        });
    };

    const handleClose = () => {
        setEmail('');
        setEmailError('');
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={handleClose} title="Wyślij zaproszenie" size="md">
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                        Email podopiecznego
                    </label>
                    <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setEmailError('');
                        }}
                        onBlur={() => email && validateEmail(email)}
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                            emailError ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="client@example.com"
                        disabled={isSending}
                        autoFocus
                    />
                    {emailError && (
                        <p className="mt-1 text-sm text-red-600">{emailError}</p>
                    )}
                    <p className="mt-2 text-sm text-gray-500">
                        Podopieczny otrzyma email z kodem zaproszenia, który będzie mógł użyć w aplikacji mobilnej.
                    </p>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                    <button
                        type="button"
                        onClick={handleClose}
                        disabled={isSending}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                    >
                        Anuluj
                    </button>
                    <button
                        type="submit"
                        disabled={isSending || !email}
                        className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isSending ? (
                            <span className="flex items-center">
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Wysyłanie...
                            </span>
                        ) : (
                            'Wyślij zaproszenie'
                        )}
                    </button>
                </div>
            </form>
        </Modal>
    );
};

export default SendInvitationModal;
