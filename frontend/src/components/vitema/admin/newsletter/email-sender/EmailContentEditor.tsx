import React from 'react';

interface EmailContentEditorProps {
    subject: string;
    content: string;
    onSubjectChange: (value: string) => void;
    onContentChange: (value: string) => void;
    isLoading: boolean;
    useTemplate: boolean;
}

const EmailContentEditor: React.FC<EmailContentEditorProps> = ({
                                                                   subject,
                                                                   content,
                                                                   onSubjectChange,
                                                                   onContentChange,
                                                                   isLoading,
                                                                   useTemplate
                                                               }) => {
    const handleAddTag = (tag: string) => {
        onContentChange(content + tag);
    };

    return (
        <div className="space-y-4">
            {/* Temat wiadomości */}
            <div>
                <label htmlFor="subject" className="block text-sm font-medium text-gray-700">
                    Temat wiadomości
                </label>
                <input
                    type="text"
                    id="subject"
                    value={subject}
                    onChange={(e) => onSubjectChange(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-primary focus:border-primary"
                    placeholder="Wprowadź temat wiadomości..."
                    disabled={isLoading}
                />
            </div>

            {/* Treść wiadomości */}
            <div>
                <label htmlFor="content" className="block text-sm font-medium text-gray-700">
                    Treść wiadomości (HTML)
                </label>
                <div className="mt-1">
                    <div className="border border-gray-300 rounded-md overflow-hidden">
                        <div className="bg-gray-50 p-2 border-b border-gray-300 flex flex-wrap gap-2">
                            <button
                                type="button"
                                onClick={() => handleAddTag('<strong></strong>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Bold
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<em></em>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Italic
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<u></u>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Underline
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<a href=""></a>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Link
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<h2></h2>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Heading
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<ul><li></li></ul>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                List
                            </button>
                            <button
                                type="button"
                                onClick={() => handleAddTag('<div style="background-color:#f5f5f5; padding:15px; border-radius:4px; margin:10px 0;"></div>')}
                                className="px-2 py-1 text-sm border border-gray-300 rounded hover:bg-gray-200"
                                disabled={isLoading}
                            >
                                Box
                            </button>
                        </div>
                        <textarea
                            value={content}
                            onChange={(e) => onContentChange(e.target.value)}
                            className="w-full p-3 min-h-64 focus:outline-none focus:ring-2 focus:ring-primary"
                            placeholder="Wprowadź treść wiadomości HTML..."
                            disabled={isLoading}
                        />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                        Możesz używać znaczników HTML (np. &lt;strong&gt;pogrubienie&lt;/strong&gt;)
                    </p>
                    <p className="text-xs text-gray-500">
                        {useTemplate
                            ? 'Treść zostanie wstawiona w wybrany szablon z headerem i footerem.'
                            : 'Treść zostanie wysłana bez dodatkowego szablonu.'}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default EmailContentEditor;