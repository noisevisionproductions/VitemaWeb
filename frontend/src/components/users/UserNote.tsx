import React, {useState} from "react";
import {CheckIcon, PencilIcon, XIcon} from "lucide-react";

interface UserNoteProps {
    note?: string;
    onSave: (note: string) => Promise<void>;
}

const UserNote: React.FC<UserNoteProps> = ({note = '', onSave}) => {
    const [isEditing, setIsEditing] = useState(false);
    const [noteText, setNoteText] = useState(note || '');
    const [isSaving, setIsSaving] = useState(false);

    React.useEffect(() => {
        setNoteText(note || '');
    }, [note]);

    const handleSave = async () => {
        if (noteText === note) {
            setIsEditing(false);
            return;
        }

        setIsSaving(true);
        try {
            await onSave(noteText);
            setIsEditing(false);
        } catch (error) {
            console.error('Błąd podczas zapisywania notatki:', error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCancel = () => {
        setNoteText(note || '');
        setIsEditing(false);
    };

    if (isEditing) {
        return (
            <div className="flex flex-col space-y-2">
                <textarea
                    value={noteText}
                    onChange={(e) => setNoteText(e.target.value)}
                    className="px-2 py-1 border rounded-md text-sm w-full min-h-[80px]"
                    placeholder="Dodaj notatkę..."
                    disabled={isSaving}
                />
                <div className="flex space-x-2">
                    <button
                        onClick={handleSave}
                        disabled={isSaving}
                        className="p-1 bg-green-100 text-green-700 rounded hover:bg-green-200 transition-colors"
                    >
                        {isSaving ? (
                            <span className="flex items-center">
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-green-700"
                                     xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                            strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor"
                                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Zapisywanie
                            </span>
                        ) : (
                            <CheckIcon className="h-4 w-4"/>
                        )}
                    </button>
                    <button
                        onClick={handleCancel}
                        disabled={isSaving}
                        className="p-1 bg-red-100 text-red-700 rounded hover:bg-red-200 transition-colors"
                    >
                        <XIcon className="h-4 w-4"/>
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div
            className="group flex items-start space-x-1 cursor-pointer"
            onClick={() => setIsEditing(true)}
        >
            <div className="max-w-xs text-sm text-gray-600 truncate">
                {note ? note : <span className="text-gray-400 italic">Brak notatki</span>}
            </div>
            <PencilIcon className="h-4 w-4 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity"/>
        </div>
    );
};

export default UserNote;