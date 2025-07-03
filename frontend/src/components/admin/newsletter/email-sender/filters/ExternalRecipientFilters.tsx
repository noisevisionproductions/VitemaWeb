import React, {useState} from 'react';
import {FilterIcon, List, Tag, X, Search} from 'lucide-react';
import {Button} from '../../../../ui/button';
import {Dialog, DialogContent, DialogHeader, DialogTitle} from '../../../../ui/dialog';
import LoadingSpinner from '../../../../common/LoadingSpinner';
import {
    ExternalRecipientService
} from "../../../../../services/newsletter/ExternalRecipientService";
import {ExternalRecipient} from '../../../../../types/sendGrid';

interface ExternalRecipientFiltersProps {
    filters: {
        category: string;
        status: string;
        tags: string[];
    };
    onChange: (filters: {
        category: string;
        status: string;
        tags: string[];
    }) => void;
    categories: string[];
    isLoading: boolean;
    useSelectedIds: boolean;
    onToggleSelectedIds: (use: boolean) => void;
    selectedIds: string[];
    onSelectIds: (ids: string[]) => void;
}

const ExternalRecipientFilters: React.FC<ExternalRecipientFiltersProps> = ({
                                                                               filters,
                                                                               onChange,
                                                                               categories,
                                                                               isLoading,
                                                                               useSelectedIds,
                                                                               onToggleSelectedIds,
                                                                               selectedIds,
                                                                               onSelectIds
                                                                           }) => {
    const [isRecipientSelectorOpen, setIsRecipientSelectorOpen] = useState(false);
    const [newTag, setNewTag] = useState('');

    const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onChange({
            ...filters,
            category: e.target.value
        });
    };

    const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onChange({
            ...filters,
            status: e.target.value
        });
    };

    const handleAddTag = () => {
        if (newTag.trim() && !filters.tags.includes(newTag.trim())) {
            onChange({
                ...filters,
                tags: [...filters.tags, newTag.trim()]
            });
            setNewTag('');
        }
    };

    const handleRemoveTag = (tag: string) => {
        onChange({
            ...filters,
            tags: filters.tags.filter(t => t !== tag)
        });
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleAddTag();
        }
    };

    const handleToggleSelectedIds = () => {
        onToggleSelectedIds(!useSelectedIds);
    };

    return (
        <div className="bg-gray-50 p-4 rounded-md border border-gray-200">
            <div className="flex items-start mb-4">
                <FilterIcon className="mr-2 h-5 w-5 text-gray-500 mt-0.5"/>
                <div>
                    <h3 className="font-medium text-gray-900">Filtry zewnętrznych odbiorców</h3>
                    <p className="text-sm text-gray-500">
                        Wybierz kryteria dla zewnętrznych odbiorców lub wybierz konkretnych odbiorców
                    </p>
                </div>
            </div>

            <div className="flex items-center mb-4">
                <div className="flex-1">
                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            id="useSelectedIds"
                            checked={useSelectedIds}
                            onChange={handleToggleSelectedIds}
                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                            disabled={isLoading}
                        />
                        <label htmlFor="useSelectedIds" className="ml-2 block text-sm text-gray-700">
                            Wybierz konkretnych odbiorców
                        </label>
                    </div>
                </div>

                {useSelectedIds && (
                    <Button
                        onClick={() => setIsRecipientSelectorOpen(true)}
                        variant="outline"
                        size="sm"
                        className="ml-2"
                    >
                        <List className="mr-1 h-4 w-4"/>
                        Wybierz odbiorców ({selectedIds.length})
                    </Button>
                )}
            </div>

            {!useSelectedIds && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                            Kategoria
                        </label>
                        <select
                            id="category"
                            name="category"
                            value={filters.category}
                            onChange={handleCategoryChange}
                            className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
                            disabled={isLoading}
                        >
                            <option value="all">Wszystkie kategorie</option>
                            {categories.map(category => (
                                <option key={category} value={category}>{category}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
                            Status
                        </label>
                        <select
                            id="status"
                            name="status"
                            value={filters.status}
                            onChange={handleStatusChange}
                            className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
                            disabled={isLoading}
                        >
                            <option value="all">Wszystkie statusy</option>
                            <option value="new">Nowy</option>
                            <option value="contacted">Skontaktowano</option>
                            <option value="responded">Odpowiedział</option>
                            <option value="subscribed">Zapisany</option>
                            <option value="rejected">Odrzucony</option>
                        </select>
                    </div>

                    <div>
                        <label htmlFor="tags" className="block text-sm font-medium text-gray-700 mb-1">
                            Tagi (przynajmniej jeden z)
                        </label>
                        <div className="flex rounded-md shadow-sm">
                            <input
                                type="text"
                                id="tags"
                                value={newTag}
                                onChange={(e) => setNewTag(e.target.value)}
                                onKeyDown={handleKeyDown}
                                className="flex-1 min-w-0 block w-full rounded-l-md border-gray-300 focus:border-primary focus:ring-primary sm:text-sm"
                                placeholder="Dodaj tag"
                                disabled={isLoading}
                            />
                            <button
                                type="button"
                                onClick={handleAddTag}
                                className="inline-flex items-center px-3 py-2 border border-l-0 border-gray-300 bg-gray-50 text-gray-500 rounded-r-md hover:bg-gray-100"
                                disabled={isLoading || !newTag.trim()}
                            >
                                <Tag className="h-4 w-4"/>
                            </button>
                        </div>

                        {filters.tags.length > 0 && (
                            <div className="mt-2 flex flex-wrap gap-2">
                                {filters.tags.map(tag => (
                                    <span
                                        key={tag}
                                        className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800"
                                    >
                                        {tag}
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveTag(tag)}
                                            className="ml-1 inline-flex h-4 w-4 flex-shrink-0 items-center justify-center rounded-full text-blue-800 hover:bg-blue-200 hover:text-blue-900 focus:outline-none"
                                        >
                                            <X className="h-3 w-3"/>
                                        </button>
                                    </span>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}

            {useSelectedIds && selectedIds.length > 0 && (
                <div className="mt-3 p-3 bg-gray-100 rounded-md">
                    <div className="flex justify-between items-center mb-2">
                        <span className="text-sm font-medium">Wybrani odbiorcy ({selectedIds.length})</span>
                        <button
                            type="button"
                            onClick={() => onSelectIds([])}
                            className="text-xs text-gray-500 hover:text-gray-700"
                        >
                            Wyczyść wszystkich
                        </button>
                    </div>
                    <p className="text-xs text-gray-500">
                        Kliknij "Wybierz odbiorców" aby dodać lub usunąć odbiorców z listy.
                    </p>
                </div>
            )}

            {isRecipientSelectorOpen && (
                <RecipientSelector
                    isOpen={isRecipientSelectorOpen}
                    onClose={() => setIsRecipientSelectorOpen(false)}
                    selectedIds={selectedIds}
                    onSelectIds={onSelectIds}
                />
            )}
        </div>
    );
};

// Komponent do wyboru konkretnych odbiorców
const RecipientSelector: React.FC<{
    isOpen: boolean;
    onClose: () => void;
    selectedIds: string[];
    onSelectIds: (ids: string[]) => void;
}> = ({isOpen, onClose, selectedIds, onSelectIds}) => {
    const [recipients, setRecipients] = useState<ExternalRecipient[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [localSelectedIds, setLocalSelectedIds] = useState<string[]>([...selectedIds]);

    React.useEffect(() => {
        const fetchRecipients = async () => {
            try {
                setLoading(true);
                const recipients = await ExternalRecipientService.getAllRecipients();
                setRecipients(recipients);
            } catch (error) {
                console.error('Error fetching recipients:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchRecipients().catch(console.error);
    }, []);

    const filteredRecipients = recipients.filter(recipient => {
        if (searchTerm) {
            const searchLower = searchTerm.toLowerCase();
            return (
                recipient.email.toLowerCase().includes(searchLower) ||
                (recipient.name && recipient.name.toLowerCase().includes(searchLower)) ||
                recipient.category.toLowerCase().includes(searchLower)
            );
        }
        return true;
    });

    const handleToggleRecipient = (id: string) => {
        if (localSelectedIds.includes(id)) {
            setLocalSelectedIds(localSelectedIds.filter(selectedId => selectedId !== id));
        } else {
            setLocalSelectedIds([...localSelectedIds, id]);
        }
    };

    const handleSelectAll = () => {
        setLocalSelectedIds(filteredRecipients.map(r => r.id));
    };

    const handleDeselectAll = () => {
        setLocalSelectedIds([]);
    };

    const handleSave = () => {
        onSelectIds(localSelectedIds);
        onClose();
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-4xl">
                <DialogHeader>
                    <DialogTitle>Wybierz odbiorców</DialogTitle>
                </DialogHeader>

                <div className="mb-4">
                    <div className="relative rounded-md shadow-sm">
                        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                            <Search className="h-4 w-4 text-gray-400"/>
                        </div>
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="block w-full rounded-md border-gray-300 pl-10 focus:border-primary focus:ring-primary sm:text-sm"
                            placeholder="Szukaj po emailu, nazwie lub kategorii..."
                        />
                    </div>
                </div>

                <div className="mb-2 flex justify-between">
                    <div className="text-sm text-gray-500">
                        Wybrano: <span className="font-medium">{localSelectedIds.length}</span> z <span
                        className="font-medium">{recipients.length}</span>
                    </div>
                    <div className="space-x-2">
                        <button
                            type="button"
                            onClick={handleSelectAll}
                            className="text-xs text-primary hover:text-primary-dark"
                        >
                            Zaznacz wszystkie
                        </button>
                        <button
                            type="button"
                            onClick={handleDeselectAll}
                            className="text-xs text-gray-500 hover:text-gray-700"
                        >
                            Odznacz wszystkie
                        </button>
                    </div>
                </div>

                <div className="max-h-96 overflow-y-auto border border-gray-200 rounded-md">
                    {loading ? (
                        <div className="flex justify-center items-center py-12">
                            <LoadingSpinner/>
                            <span className="ml-2 text-gray-500">Ładowanie odbiorców...</span>
                        </div>
                    ) : filteredRecipients.length === 0 ? (
                        <div className="text-center py-8 text-gray-500">
                            {searchTerm ? 'Brak odbiorców pasujących do wyszukiwania' : 'Brak odbiorców'}
                        </div>
                    ) : (
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="w-12 px-3 py-3"></th>
                                <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nazwa</th>
                                <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Kategoria</th>
                                <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {filteredRecipients.map(recipient => (
                                <tr
                                    key={recipient.id}
                                    onClick={() => handleToggleRecipient(recipient.id)}
                                    className={`
                                            cursor-pointer hover:bg-gray-50
                                            ${localSelectedIds.includes(recipient.id) ? 'bg-blue-50' : ''}
                                        `}
                                >
                                    <td className="px-3 py-4 whitespace-nowrap">
                                        <input
                                            type="checkbox"
                                            checked={localSelectedIds.includes(recipient.id)}
                                            onChange={() => {
                                            }}
                                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                    </td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{recipient.email}</td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500">{recipient.name || '-'}</td>
                                    <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500">{recipient.category}</td>
                                    <td className="px-3 py-4 whitespace-nowrap">
                                            <span className={`
                                                px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                                ${recipient.status === 'new' ? 'bg-gray-100 text-gray-800' : ''}
                                                ${recipient.status === 'contacted' ? 'bg-blue-100 text-blue-800' : ''}
                                                ${recipient.status === 'responded' ? 'bg-green-100 text-green-800' : ''}
                                                ${recipient.status === 'subscribed' ? 'bg-purple-100 text-purple-800' : ''}
                                                ${recipient.status === 'rejected' ? 'bg-red-100 text-red-800' : ''}
                                            `}>
                                                {recipient.status === 'new' && 'Nowy'}
                                                {recipient.status === 'contacted' && 'Skontaktowano'}
                                                {recipient.status === 'responded' && 'Odpowiedział'}
                                                {recipient.status === 'subscribed' && 'Zapisany'}
                                                {recipient.status === 'rejected' && 'Odrzucony'}
                                            </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="mt-4 flex justify-end space-x-3">
                    <Button
                        onClick={onClose}
                        variant="outline"
                    >
                        Anuluj
                    </Button>
                    <Button
                        onClick={handleSave}
                        disabled={loading}
                    >
                        Zapisz wybrane ({localSelectedIds.length})
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default ExternalRecipientFilters;