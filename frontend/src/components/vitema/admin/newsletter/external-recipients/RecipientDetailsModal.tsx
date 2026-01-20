import React, {useState} from "react";
import {ExternalRecipient} from "../../../../../types/sendGrid";
import {toast} from "../../../../../utils/toast";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "../../../../shared/ui/Dialog";
import {Edit2, Save, X} from "lucide-react";
import {ExternalRecipientService} from "../../../../../services/newsletter/ExternalRecipientService";
import {formatPostgresTimestamp} from "../../../../../utils/dateFormatters";

interface RecipientDetailsModalProps {
    recipient: ExternalRecipient;
    isOpen: boolean;
    onClose: () => void;
}

const RecipientDetailsModal: React.FC<RecipientDetailsModalProps> = ({
                                                                         recipient,
                                                                         isOpen,
                                                                         onClose
                                                                     }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editData, setEditData] = useState({
        name: recipient.name || '',
        notes: recipient.notes || ''
    });
    const [isSaving, setIsSaving] = useState(false);

    const statusLabels = {
        new: 'Nowy',
        contacted: 'Skontaktowano',
        responded: 'Odpowiedział',
        subscribed: 'Zapisany',
        rejected: 'Odrzucony',
    };

    const handleEdit = () => {
        setEditData({
            name: recipient.name || '',
            notes: recipient.notes || '',
        });
        setIsEditing(true);
    };

    const handleCancel = () => {
        setIsEditing(false);
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;
        setEditData({...editData, [name]: value});
    };

    const handleSave = async () => {
        try {
            setIsSaving(true);
            await ExternalRecipientService.updateRecipient(recipient.id, editData);
            toast.success('Zmiany zostały zapisane');
            setIsEditing(false);
            onClose();
        } catch (error) {
            console.error('Error updating recipient:', error);
            toast.error('Nie udało się zapisać zmian');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-lg">
                <DialogHeader className="border-b pb-4">
                    <DialogTitle className="flex items-center justify-between">
                        <span>Szczegóły odbiorcy</span>
                        {!isEditing ? (
                            <button
                                onClick={handleEdit}
                                className="text-primary hover:text-primary-dark rounded-full p-1 transition-colors"
                            >
                                <Edit2 size={18} />
                            </button>
                        ) : (
                            <button
                                onClick={handleCancel}
                                className="text-gray-500 hover:text-gray-700 rounded-full p-1 transition-colors"
                            >
                                <X size={18} />
                            </button>
                        )}
                    </DialogTitle>
                    <DialogDescription>
                        Szczegółowe informacje odbiorcy
                    </DialogDescription>
                </DialogHeader>

                <div className="py-4 space-y-5">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Email</h3>
                            <p className="mt-1 text-sm text-gray-900">{recipient.email}</p>
                        </div>

                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Imię i nazwisko</h3>
                            {isEditing ? (
                                <input
                                    type="text"
                                    name="name"
                                    value={editData.name}
                                    onChange={handleChange}
                                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-primary"
                                    placeholder="Imię i nazwisko"
                                    disabled={isSaving}
                                />
                            ) : (
                                <p className="mt-1 text-sm text-gray-900">{recipient.name || '-'}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Kategoria</h3>
                            <p className="mt-1 text-sm text-gray-900">{recipient.category}</p>
                        </div>

                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Status</h3>
                            <p className="mt-1 text-sm">
                                <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium
                                    ${recipient.status === 'new' ? 'bg-gray-100 text-gray-800' : ''}
                                    ${recipient.status === 'contacted' ? 'bg-blue-100 text-blue-800' : ''}
                                    ${recipient.status === 'responded' ? 'bg-green-100 text-green-800' : ''}
                                    ${recipient.status === 'subscribed' ? 'bg-primary-light bg-opacity-30 text-primary-dark' : ''}
                                    ${recipient.status === 'rejected' ? 'bg-red-100 text-red-800' : ''}
                                `}>
                                    {statusLabels[recipient.status as keyof typeof statusLabels] || recipient.status}
                                </span>
                            </p>
                        </div>
                    </div>

                    {recipient.tags && recipient.tags.length > 0 && (
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Tagi</h3>
                            <div className="mt-1 flex flex-wrap gap-1">
                                {recipient.tags.map(tag => (
                                    <span
                                        key={tag}
                                        className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-800"
                                    >
                                        {tag}
                                    </span>
                                ))}
                            </div>
                        </div>
                    )}

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Notatki</h3>
                        {isEditing ? (
                            <textarea
                                name="notes"
                                value={editData.notes}
                                onChange={handleChange}
                                rows={4}
                                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-primary"
                                placeholder="Dodatkowe informacje o odbiorcy..."
                                disabled={isSaving}
                            />
                        ) : (
                            <p className="mt-1 text-sm text-gray-900 whitespace-pre-wrap">
                                {recipient.notes || '-'}
                            </p>
                        )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Data dodania</h3>
                            <p className="mt-1 text-sm text-gray-900">{formatPostgresTimestamp(recipient.createdAt)}</p>
                        </div>

                        {recipient.lastContactDate && (
                            <div>
                                <h3 className="text-sm font-medium text-gray-500">Ostatni kontakt</h3>
                                <p className="mt-1 text-sm text-gray-900">{formatPostgresTimestamp(recipient.lastContactDate)}</p>
                            </div>
                        )}
                    </div>
                </div>

                {isEditing && (
                    <div className="pt-4 border-t flex justify-end space-x-3">
                        <button
                            onClick={handleCancel}
                            className="px-4 py-2 border border-gray-300 rounded text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                            disabled={isSaving}
                        >
                            Anuluj
                        </button>
                        <button
                            onClick={handleSave}
                            className="px-4 py-2 border border-transparent rounded text-sm font-medium text-white bg-primary hover:bg-primary-dark flex items-center"
                            disabled={isSaving}
                        >
                            {isSaving ? (
                                <>
                                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Zapisywanie...
                                </>
                            ) : (
                                <>
                                    <Save size={16} className="mr-1" />
                                    Zapisz zmiany
                                </>
                            )}
                        </button>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
};

export default RecipientDetailsModal;