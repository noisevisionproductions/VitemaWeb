import React, {useState, useCallback, useEffect, useRef} from 'react';
import {ParsedProduct} from '../../../../../../types/product';
import {useDebounce} from '../../../../../../hooks/useDebounce';
import {toast} from '../../../../../../utils/toast';
import {Loader2, Plus, Search} from 'lucide-react';
import {ManualDietService} from '../../../../../../services/diet/ManualDietService';

interface InlineIngredientSearchProps {
    onSelect: (ingredient: ParsedProduct) => void;
    placeholder?: string;
}

const InlineIngredientSearch: React.FC<InlineIngredientSearchProps> = ({
                                                                           onSelect,
                                                                           placeholder = "Wpisz składnik, np. 'mleko 200ml'..."
                                                                       }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<ParsedProduct[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const [showResults, setShowResults] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const inputRef = useRef<HTMLInputElement>(null);

    const debouncedSearchQuery = useDebounce(searchQuery, 300);

    useEffect(() => {
        if (debouncedSearchQuery.trim().length < 2) {
            setSearchResults([]);
            setShowResults(false);
            return;
        }

        searchIngredients().catch(console.error);
    }, [debouncedSearchQuery]);

    const searchIngredients = async () => {
        setIsSearching(true);
        try {
            const results = await ManualDietService.searchIngredients(debouncedSearchQuery, 8);
            setSearchResults(results);
            setShowResults(results.length > 0);
            setSelectedIndex(-1);
        } catch (error) {
            console.error('Błąd wyszukiwania składników:', error);
            setSearchResults([]);
            setShowResults(false);
        } finally {
            setIsSearching(false);
        }
    };

    const handleSelect = useCallback((ingredient: ParsedProduct) => {
        onSelect(ingredient);

        setSearchQuery('');
        setSearchResults([]);
        setShowResults(false);
        setSelectedIndex(-1);

        if (inputRef.current) {
            inputRef.current.focus();
        }
    }, [onSelect]);

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (!showResults) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev =>
                    prev < searchResults.length - 1 ? prev + 1 : prev
                );
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < searchResults.length) {
                    handleSelect(searchResults[selectedIndex]);
                } else if (searchQuery.trim()) {
                    handleCreateFromText().catch(console.error);
                }
                break;
            case 'Escape':
                setShowResults(false);
                setSelectedIndex(-1);
                break;
        }
    };

    const handleCreateFromText = useCallback(async () => {
        if (!searchQuery.trim()) return;

        try {
            const ingredient: ParsedProduct = {
                id: `temp-${Date.now()}-${Math.random()}`,
                name: searchQuery.trim(),
                quantity: 1,
                unit: 'szt',
                original: searchQuery.trim(),
                hasCustomUnit: false
            };

            try {
                const createdIngredient = await ManualDietService.createIngredient(ingredient);
                handleSelect(createdIngredient);
                toast.success('Składnik został dodany');
            } catch (error) {
                console.warn('API nie dostępne, używam lokalnego składnika:', error);
                handleSelect(ingredient);
            }
        } catch (error) {
            console.error('Błąd podczas tworzenia składnika:', error);
            toast.error('Nie udało się dodać składnika');
        }
    }, [searchQuery, handleSelect]);

    return (
        <div className="relative">
            <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4"/>
                <input
                    ref={inputRef}
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    onFocus={() => setShowResults(searchResults.length > 0)}
                    onBlur={() => setTimeout(() => setShowResults(false), 200)}
                    placeholder={placeholder}
                    className="w-full pl-10 pr-10 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                />
                {isSearching && (
                    <Loader2
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4 animate-spin"/>
                )}
                {!isSearching && searchQuery && (
                    <button
                        onClick={handleCreateFromText}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-primary hover:text-primary-dark"
                        title="Dodaj jako nowy składnik"
                    >
                        <Plus className="h-4 w-4"/>
                    </button>
                )}
            </div>

            {/* Results dropdown */}
            {showResults && searchResults.length > 0 && (
                <div
                    className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-10 max-h-48 overflow-y-auto">
                    {searchResults.map((ingredient, index) => (
                        <button
                            key={`${ingredient.id}-${index}`}
                            onClick={() => handleSelect(ingredient)}
                            className={`w-full text-left px-3 py-2 hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition-colors ${
                                index === selectedIndex ? 'bg-blue-50' : ''
                            }`}
                        >
                            <div className="font-medium text-sm">{ingredient.name}</div>
                            <div className="text-xs text-gray-600">
                                {ingredient.quantity} {ingredient.unit}
                            </div>
                        </button>
                    ))}

                    {searchQuery.trim() && (
                        <button
                            onClick={handleCreateFromText}
                            className="w-full text-left px-3 py-2 hover:bg-green-50 border-t border-gray-200 text-green-700 transition-colors"
                        >
                            <div className="flex items-center gap-2">
                                <Plus className="h-4 w-4"/>
                                <span className="font-medium text-sm">
                                    Dodaj "{searchQuery}" jako nowy składnik
                                </span>
                            </div>
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};

export default InlineIngredientSearch;