import React, { useState } from 'react';
import { InvitationService } from '../../../services/InvitationService';
import { toast } from '../../../utils/toast';

interface AcceptInvitationFormProps {
    onSuccess?: () => void;
}

/**
 * Komponent do akceptacji zaproszenia (dla aplikacji mobilnej)
 * 
 * Użycie w aplikacji mobilnej/webowej dla podopiecznych
 */
const AcceptInvitationForm: React.FC<AcceptInvitationFormProps> = ({ onSuccess }) => {
    const [code, setCode] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const formatCode = (value: string): string => {
        // Usuń wszystko oprócz liter i cyfr
        let formatted = value.toUpperCase().replace(/[^A-Z0-9]/g, '');
        
        // Usuń TR- jeśli użytkownik go wpisał
        if (formatted.startsWith('TR')) {
            formatted = formatted.slice(2);
        }
        
        // Ogranicz do 6 znaków
        if (formatted.length > 6) {
            formatted = formatted.slice(0, 6);
        }
        
        // Dodaj TR- prefix
        return formatted ? `TR-${formatted}` : '';
    };

    const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const formatted = formatCode(e.target.value);
        setCode(formatted);
        setError('');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (code.length !== 9) { // TR-XXXXXX = 9 znaków
            setError('Wprowadź prawidłowy kod zaproszenia');
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            const response = await InvitationService.acceptInvitation(code);
            toast.success(response.message || 'Zaproszenie zostało zaakceptowane!');
            setCode('');
            onSuccess?.();
        } catch (err: any) {
            const errorMessage = 
                err.response?.data?.detail || 
                err.response?.data?.message || 
                'Nie udało się zaakceptować zaproszenia';
            
            setError(errorMessage);
            
            // Bardziej szczegółowe komunikaty błędów
            if (err.response?.status === 404) {
                setError('Nieprawidłowy kod zaproszenia');
            } else if (err.response?.status === 410) {
                setError('Zaproszenie wygasło. Poproś trenera o nowe zaproszenie.');
            } else if (err.response?.status === 409) {
                setError('To zaproszenie zostało już wykorzystane');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto">
            <div className="bg-white shadow-lg rounded-lg p-6">
                <div className="text-center mb-6">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
                        <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                        </svg>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-900 mb-2">
                        Wpisz kod zaproszenia
                    </h2>
                    <p className="text-gray-600">
                        Otrzymałeś kod od swojego trenera? Wpisz go poniżej.
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="code" className="block text-sm font-medium text-gray-700 mb-2">
                            Kod zaproszenia
                        </label>
                        <input
                            type="text"
                            id="code"
                            value={code}
                            onChange={handleCodeChange}
                            placeholder="TR-ABC123"
                            maxLength={9}
                            className={`w-full px-4 py-3 text-center text-2xl font-mono tracking-wider border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                                error ? 'border-red-500' : 'border-gray-300'
                            }`}
                            disabled={isLoading}
                            autoComplete="off"
                            autoFocus
                        />
                        {error && (
                            <p className="mt-2 text-sm text-red-600 text-center">{error}</p>
                        )}
                        <p className="mt-2 text-xs text-gray-500 text-center">
                            Format: TR-XXXXXX (6 znaków alfanumerycznych)
                        </p>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || code.length !== 9}
                        className="w-full px-4 py-3 text-white bg-blue-600 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {isLoading ? (
                            <span className="flex items-center justify-center">
                                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Weryfikacja...
                            </span>
                        ) : (
                            'Zaakceptuj zaproszenie'
                        )}
                    </button>
                </form>

                <div className="mt-6 p-4 bg-blue-50 rounded-lg">
                    <p className="text-sm text-blue-800">
                        <strong>Wskazówka:</strong> Kod zaproszenia znajdziesz w emailu wysłanym przez trenera lub możesz go uzyskać bezpośrednio od niego.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AcceptInvitationForm;
