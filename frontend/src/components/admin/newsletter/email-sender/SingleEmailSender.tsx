import React, {useState, useEffect} from 'react';
import {ExternalRecipient} from '../../../../types/sendGrid';
import {toast} from '../../../../utils/toast';
import {SendGridService} from '../../../../services/newsletter/SendGridService';
import {ExternalRecipientService} from '../../../../services/newsletter/ExternalRecipientService';
import LoadingSpinner from '../../../common/LoadingSpinner';
import EmailContentEditor from './EmailContentEditor';
import EmailPreview from './EmailPreview';
import EmailTemplateSelector from './EmailTemplateSelector';
import SavedTemplatesSelector from './SavedTemplatesSelector';
import {User, Search} from 'lucide-react';
import {useEmailComposer} from '../../../../hooks/email/useEmailComposer';

const SingleEmailSender: React.FC = () => {
    // Używamy naszego hook dla logiki emaila
    const emailComposer = useEmailComposer();

    // Dodatkowe stany specyficzne dla SingleEmailSender
    const [updateLastContact, setUpdateLastContact] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<ExternalRecipient[]>([]);
    const [selectedRecipient, setSelectedRecipient] = useState<ExternalRecipient | null>(null);
    const [isSearching, setIsSearching] = useState(false);
    const [isSending, setIsSending] = useState(false);

    // Wyszukiwanie odbiorców przy zmianie zapytania
    useEffect(() => {
        if (searchQuery.trim().length >= 3) {
            searchRecipients().catch(console.error);
        } else {
            setSearchResults([]);
        }
    }, [searchQuery]);

    const searchRecipients = async () => {
        try {
            setIsSearching(true);
            const recipients = await ExternalRecipientService.getAllRecipients();

            // Filtruj odbiorców lokalnie
            const filtered = recipients.filter(
                (r) =>
                    r.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    (r.name && r.name.toLowerCase().includes(searchQuery.toLowerCase()))
            );

            setSearchResults(filtered.slice(0, 10));
        } catch (error) {
            console.error('Błąd podczas wyszukiwania odbiorców:', error);
        } finally {
            setIsSearching(false);
        }
    };

    const handleSelectRecipient = (recipient: ExternalRecipient) => {
        setSelectedRecipient(recipient);
        setSearchQuery('');
        setSearchResults([]);
    };

    const handleSendEmail = async () => {
        const {subject, content, useTemplate, templateType, selectedSavedTemplateId} = emailComposer;

        if (!selectedRecipient) {
            toast.error('Wybierz odbiorcę wiadomości');
            return;
        }

        if (!subject.trim()) {
            toast.error('Podaj temat wiadomości');
            return;
        }

        if (!content.trim()) {
            toast.error('Wpisz treść wiadomości');
            return;
        }

        try {
            setIsSending(true);

            await SendGridService.sendSingleEmail({
                recipientEmail: selectedRecipient.email,
                recipientName: selectedRecipient.name,
                externalRecipientId: selectedRecipient.id,
                subject,
                content,
                useTemplate,
                templateType: useTemplate ? templateType : undefined,
                savedTemplateId: selectedSavedTemplateId || undefined,
                updateLastContactDate: updateLastContact,
            });

            toast.success(`Wiadomość została wysłana do ${selectedRecipient.name || selectedRecipient.email}`);

            // Resetuj formularz po wysłaniu
            emailComposer.resetForm();
            setSelectedRecipient(null);
        } catch (error) {
            console.error('Błąd podczas wysyłania emaila:', error);
            toast.error('Wystąpił błąd podczas wysyłania wiadomości');
        } finally {
            setIsSending(false);
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

    return (
        <div className="space-y-6">
            <div className="bg-white shadow sm:rounded-lg">
                <div className="p-6">
                    <div className="flex flex-wrap justify-between items-center mb-6">
                        <h3 className="text-lg font-medium text-gray-900">Wyślij pojedynczy email</h3>
                    </div>

                    <div className="space-y-6">
                        {/* Wybór odbiorcy */}
                        <div className="bg-gray-50 p-4 rounded-md">
                            <h4 className="text-sm font-medium text-gray-700 mb-3">Wybierz odbiorcę</h4>

                            <div className="relative mb-4">
                                <div className="flex items-center">
                                    <input
                                        type="text"
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                        placeholder="Wyszukaj odbiorcę (minimum 3 znaki)..."
                                        className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-primary focus:border-primary"
                                    />
                                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                                        {isSearching ? (
                                            <LoadingSpinner size="sm"/>
                                        ) : (
                                            <Search size={18} className="text-gray-400"/>
                                        )}
                                    </div>
                                </div>

                                {searchResults.length > 0 && (
                                    <div
                                        className="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md border border-gray-200 max-h-60 overflow-auto">
                                        {searchResults.map((recipient) => (
                                            <button
                                                key={recipient.id}
                                                className="w-full block px-4 py-2 text-left text-sm hover:bg-gray-100 border-b border-gray-100 last:border-0"
                                                onClick={() => handleSelectRecipient(recipient)}
                                            >
                                                <div className="flex flex-col">
                                                    <span className="font-medium">{recipient.email}</span>
                                                    {recipient.name &&
                                                        <span className="text-gray-500">{recipient.name}</span>}
                                                </div>
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {selectedRecipient && (
                                <div className="bg-white p-3 rounded-md border border-gray-200">
                                    <div className="flex items-start">
                                        <User size={20} className="text-primary mt-0.5 mr-2"/>
                                        <div className="flex-1">
                                            <div className="flex flex-wrap items-center justify-between">
                                                <div>
                                                    <h4 className="font-medium">{selectedRecipient.name || selectedRecipient.email}</h4>
                                                    {selectedRecipient.name && (
                                                        <p className="text-sm text-gray-500">{selectedRecipient.email}</p>
                                                    )}
                                                </div>
                                                <div className="flex items-center space-x-2">
                                                    <span className="text-sm text-gray-500">Status:</span>
                                                    {getStatusBadge(selectedRecipient.status)}
                                                </div>
                                            </div>

                                            <div className="flex items-center mt-2 text-sm text-gray-500">
                                                <span>Kategoria: {selectedRecipient.category}</span>
                                                {selectedRecipient.tags && selectedRecipient.tags.length > 0 && (
                                                    <div className="ml-4 flex flex-wrap gap-1">
                                                        {selectedRecipient.tags.map((tag) => (
                                                            <span
                                                                key={tag}
                                                                className="text-xs bg-gray-100 px-1.5 py-0.5 rounded-full text-gray-600"
                                                            >
                                {tag}
                              </span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Edytor i podgląd */}
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            {/* Lewa kolumna - edytor */}
                            <div className="space-y-4">
                                <EmailContentEditor
                                    subject={emailComposer.subject}
                                    content={emailComposer.content}
                                    onSubjectChange={emailComposer.setSubject}
                                    onContentChange={emailComposer.setContent}
                                    isLoading={isSending}
                                    useTemplate={emailComposer.useTemplate}
                                />

                                <div className="pt-4 border-t">
                                    <div className="flex items-center space-x-2 mb-4">
                                        <input
                                            type="checkbox"
                                            id="useTemplate"
                                            checked={emailComposer.useTemplate}
                                            onChange={(e) => emailComposer.setUseTemplate(e.target.checked)}
                                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                        <label htmlFor="useTemplate" className="text-sm font-medium text-gray-700">
                                            Użyj szablonu systemowego
                                        </label>
                                    </div>

                                    {emailComposer.useTemplate && (
                                        <EmailTemplateSelector
                                            templates={emailComposer.availableTemplates}
                                            selectedTemplate={emailComposer.templateType}
                                            onTemplateSelect={emailComposer.setTemplateType}
                                            disabled={isSending}
                                            isLoading={emailComposer.isLoadingTemplates}
                                        />
                                    )}
                                </div>

                                <div className="pt-4 border-t">
                                    <SavedTemplatesSelector
                                        templates={emailComposer.savedTemplates}
                                        selectedId={emailComposer.selectedSavedTemplateId}
                                        onSelect={emailComposer.handleSelectSavedTemplate}
                                        isLoading={emailComposer.isLoadingSavedTemplates}
                                        onRefresh={emailComposer.fetchSavedTemplates}
                                        compact={true}
                                    />
                                </div>

                                <div className="pt-4 border-t">
                                    <div className="flex items-center space-x-2">
                                        <input
                                            type="checkbox"
                                            id="updateLastContact"
                                            checked={updateLastContact}
                                            onChange={(e) => setUpdateLastContact(e.target.checked)}
                                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                        <label htmlFor="updateLastContact"
                                               className="text-sm font-medium text-gray-700">
                                            Zaktualizuj datę ostatniego kontaktu
                                        </label>
                                    </div>
                                </div>
                            </div>

                            {/* Prawa kolumna - podgląd */}
                            <div>
                                <EmailPreview
                                    subject={emailComposer.subject}
                                    content={emailComposer.previewContent}
                                    isLoading={emailComposer.isLoadingPreview}
                                />
                            </div>
                        </div>

                        {/* Przyciski akcji */}
                        <div className="flex justify-end space-x-3 pt-4 border-t">
                            <button
                                type="button"
                                onClick={() => emailComposer.handleSaveTemplate()}
                                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                                disabled={isSending || !emailComposer.subject || !emailComposer.content}
                            >
                                Zapisz jako szablon
                            </button>

                            <button
                                type="button"
                                onClick={handleSendEmail}
                                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark flex items-center space-x-2"
                                disabled={isSending || !emailComposer.subject || !emailComposer.content || !selectedRecipient}
                            >
                                {isSending ? (
                                    <>
                                        <LoadingSpinner size="sm"/>
                                        <span>Wysyłanie...</span>
                                    </>
                                ) : (
                                    <>
                                        <User size={16} className="mr-1"/>
                                        <span>Wyślij do {selectedRecipient?.name || selectedRecipient?.email || 'odbiorcy'}</span>
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SingleEmailSender;