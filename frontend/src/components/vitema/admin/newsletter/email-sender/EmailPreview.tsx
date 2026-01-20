import React, { useState, useEffect } from 'react';
import LoadingSpinner from "../../../../shared/common/LoadingSpinner";

interface EmailPreviewProps {
    subject: string;
    content: string;
    isLoading: boolean;
    className?: string;
}

const EmailPreview: React.FC<EmailPreviewProps> = ({
                                                       subject,
                                                       content,
                                                       isLoading,
                                                       className = ''
                                                   }) => {
    // Stan przechowujący ostatnią poprawną zawartość
    const [cachedContent, setCachedContent] = useState(content);
    const [cachedSubject, setCachedSubject] = useState(subject);

    // Aktualizacja cache gdy nowa zawartość jest dostępna i nie jest pusta
    useEffect(() => {
        if (!isLoading && content) {
            setCachedContent(content);
        }
    }, [content, isLoading]);

    useEffect(() => {
        if (!isLoading && subject) {
            setCachedSubject(subject);
        }
    }, [subject, isLoading]);

    // Decydujemy jaką zawartość wyświetlić - aktualizujemy cache tylko gdy mamy nową zawartość
    const displaySubject = cachedSubject || subject || '[Brak tematu]';
    const displayContent = cachedContent || content || '<p>[Brak treści]</p>';

    return (
        <div className={`bg-white shadow sm:rounded-lg overflow-hidden ${className}`}>
            <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Podgląd wiadomości</h3>
                <p className="mt-1 text-sm text-gray-500">
                    Tak będzie wyglądała Twoja wiadomość dla subskrybentów
                </p>
            </div>

            {/* Stała wysokość kontenera podglądu */}
            <div className="p-6" style={{ minHeight: '400px', position: 'relative' }}>
                {/* Zawartość jest zawsze widoczna - używamy zcachowanej lub aktualnej */}
                <div className="border border-gray-200 rounded-md p-4 h-full overflow-auto">
                    <h2 className="text-xl font-semibold mb-4">{displaySubject}</h2>
                    <div
                        className="prose max-w-none"
                        dangerouslySetInnerHTML={{ __html: displayContent }}
                    />
                </div>

                {/* Nakładka ładowania */}
                {isLoading && (
                    <div className="absolute inset-0 bg-white bg-opacity-70 flex justify-center items-center">
                        <div className="bg-white p-3 rounded-lg shadow-sm flex items-center">
                            <LoadingSpinner size="sm" />
                            <span className="ml-2 text-gray-700 font-medium">Generowanie podglądu...</span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default EmailPreview;