import { useState } from 'react';
import { toast} from "../../../utils/toast";
import { AdminNewsletterService} from "../../../services/newsletter";

const BulkEmailSender = () => {
    const [subject, setSubject] = useState('');
    const [content, setContent] = useState('');
    const [isSending, setIsSending] = useState(false);
    const [showPreview, setShowPreview] = useState(false);

    const handleSendEmail = async () => {
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
            await AdminNewsletterService.sendBulkEmail({ subject, content });
            toast.success('Wiadomość została wysłana do wszystkich aktywnych i zweryfikowanych subskrybentów');
            setSubject('');
            setContent('');
            setShowPreview(false);
        } catch (error) {
            console.error('Error sending bulk email:', error);
            toast.error('Wystąpił błąd podczas wysyłania wiadomości');
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="bg-white shadow sm:rounded-lg">
                <div className="p-6">
                    <h3 className="text-lg font-medium text-gray-900">Wyślij wiadomość do subskrybentów</h3>
                    <p className="mt-1 text-sm text-gray-500">
                        Wiadomość zostanie wysłana do wszystkich aktywnych i zweryfikowanych subskrybentów newslettera.
                    </p>

                    <div className="mt-6 space-y-4">
                        <div>
                            <label htmlFor="subject" className="block text-sm font-medium text-gray-700">
                                Temat wiadomości
                            </label>
                            <input
                                type="text"
                                id="subject"
                                value={subject}
                                onChange={(e) => setSubject(e.target.value)}
                                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-primary focus:border-primary"
                                placeholder="Wprowadź temat wiadomości..."
                                disabled={isSending}
                            />
                        </div>

                        <div>
                            <label htmlFor="content" className="block text-sm font-medium text-gray-700">
                                Treść wiadomości (HTML)
                            </label>
                            <div className="mt-1">
                                {/* Simple HTML textarea editor with styling controls */}
                                <div className="border border-gray-300 rounded-md overflow-hidden">
                                    <div className="bg-gray-50 p-2 border-b border-gray-300 flex flex-wrap gap-2">
                                        {/* Basic formatting buttons */}
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<strong></strong>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            Bold
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<em></em>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            Italic
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<u></u>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            Underline
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<a href=""></a>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            Link
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<h2></h2>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            Heading
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setContent(content + '<ul><li></li></ul>')}
                                            className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                            disabled={isSending}
                                        >
                                            List
                                        </button>
                                    </div>
                                    <textarea
                                        value={content}
                                        onChange={(e) => setContent(e.target.value)}
                                        className="w-full p-3 min-h-64 focus:outline-none focus:ring-2 focus:ring-primary"
                                        placeholder="Wprowadź treść wiadomości HTML..."
                                        disabled={isSending}
                                    />
                                </div>
                                <p className="text-xs text-gray-500 mt-1">
                                    Możesz używać znaczników HTML (np. &lt;strong&gt;pogrubienie&lt;/strong&gt;)
                                </p>
                            </div>
                        </div>

                        <div className="flex justify-end space-x-3 pt-4">
                            <button
                                type="button"
                                onClick={() => setShowPreview(!showPreview)}
                                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                                disabled={isSending}
                            >
                                {showPreview ? 'Ukryj podgląd' : 'Pokaż podgląd'}
                            </button>

                            <button
                                type="button"
                                onClick={handleSendEmail}
                                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary-dark disabled:opacity-50"
                                disabled={isSending}
                            >
                                {isSending ? 'Wysyłanie...' : 'Wyślij wiadomość'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {showPreview && (
                <div className="bg-white shadow sm:rounded-lg overflow-hidden">
                    <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                        <h3 className="text-lg font-medium text-gray-900">Podgląd wiadomości</h3>
                        <p className="mt-1 text-sm text-gray-500">
                            Tak będzie wyglądała Twoja wiadomość dla subskrybentów
                        </p>
                    </div>
                    <div className="p-6">
                        <div className="border border-gray-200 rounded-md p-4">
                            <h2 className="text-xl font-semibold mb-4">{subject || '[Brak tematu]'}</h2>
                            <div
                                className="prose max-w-none"
                                dangerouslySetInnerHTML={{ __html: content || '<p>[Brak treści]</p>' }}
                            />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BulkEmailSender;