import React, {useState, useCallback, useEffect, useRef} from 'react';
import {ParsedProduct} from '../../../../../../../types/product';
import {useDebounce} from '../../../../../../../hooks/useDebounce';
import {toast} from '../../../../../../../utils/toast';
import {Loader2, Package, Plus, Search} from 'lucide-react';
import {ManualDietService} from '../../../../../../../services/diet/manual/ManualDietService';

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
    const containerRef = useRef<HTMLDivElement>(null);

    const debouncedSearchQuery = useDebounce(searchQuery, 300);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setShowResults(false);
                setSelectedIndex(-1);
            }
        };

        if (showResults) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => document.removeEventListener('mousedown', handleClickOutside);
        }
    }, [showResults]);

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
            setShowResults(results.length > 0 || debouncedSearchQuery.trim().length >= 2);
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
        const totalOptions = searchResults.length + (searchQuery.trim() ? 1 : 0);

        if (!showResults || totalOptions === 0) return;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev => prev < totalOptions - 1 ? prev + 1 : prev);
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < searchResults.length) {
                    handleSelect(searchResults[selectedIndex]);
                } else if (selectedIndex === searchResults.length && searchQuery.trim()) {
                    handleCreateFromText().catch(console.error);
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
        <div ref={containerRef} className="relative">
            <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4"/>
                <input
                    ref={inputRef}
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    onFocus={() => (searchResults.length > 0 || searchQuery.trim().length >= 2) && setShowResults(true)}
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
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-primary hover:text-primary-dark transition-colors"
                        title="Dodaj jako nowy składnik"
                    >
                        <Plus className="h-4 w-4"/>
                    </button>
                )}
            </div>

            {/* ULEPSZONA LISTA WYNIKÓW */}
            {showResults && (searchResults.length > 0 || searchQuery.trim()) && (
                <div
                    className="absolute top-full left-0 right-0 mt-1 bg-white border-2 border-secondary-light rounded-xl shadow-xl z-20 max-h-64 overflow-hidden"
                    style={{
                        background: 'linear-gradient(135deg, #ffffff 0%, #f0fdf4 100%)'
                    }}
                >
                    {/* Header listy */}
                    <div className="px-4 py-2 bg-secondary-light/10 border-b border-secondary-light/20">
                        <div className="flex items-center gap-2">
                            <Package className="h-3 w-3 text-secondary-dark"/>
                            <span className="text-xs font-medium text-secondary-dark">
                                {searchResults.length > 0
                                    ? `Znalezione składniki (${searchResults.length})`
                                    : 'Stwórz nowy składnik'
                                }
                            </span>
                        </div>
                    </div>

                    {/* Lista wyników */}
                    <div className="max-h-48 overflow-y-auto">
                        {searchResults.map((ingredient, index) => (
                            <button
                                key={`${ingredient.id}-${index}`}
                                onClick={() => handleSelect(ingredient)}
                                className={`w-full text-left px-3 py-2 border-b border-gray-100 last:border-b-0 transition-all duration-150 ${
                                    index === selectedIndex
                                        ? 'bg-secondary-light/20 border-l-4 border-l-secondary transform scale-[1.02]'
                                        : 'hover:bg-green-50 hover:border-l-4 hover:border-l-secondary-light'
                                }`}
                            >
                                <div className="font-medium text-sm text-gray-900">{ingredient.name}</div>
                                <div className="text-xs text-gray-600 flex items-center gap-1">
                                    <span className="font-semibold text-secondary">{ingredient.quantity}</span>
                                    <span>{ingredient.unit}</span>
                                    {ingredient.original && ingredient.original !== ingredient.name && (
                                        <span className="text-gray-400">• {ingredient.original}</span>
                                    )}
                                </div>
                            </button>
                        ))}

                        {/* Opcja tworzenia nowego składnika */}
                        {searchQuery.trim() && (
                            <button
                                onClick={handleCreateFromText}
                                className={`w-full text-left px-3 py-2 border-t border-gray-200 transition-all duration-150 ${
                                    selectedIndex === searchResults.length
                                        ? 'bg-primary-light/20 border-l-4 border-l-primary transform scale-[1.02]'
                                        : 'hover:bg-blue-50 hover:border-l-4 hover:border-l-primary-light'
                                }`}
                            >
                                <div className="flex items-center gap-2">
                                    <Plus className="h-4 w-4 text-primary"/>
                                    <div>
                                        <div className="font-medium text-sm text-primary">
                                            Dodaj "{searchQuery}" jako nowy składnik
                                        </div>
                                        <div className="text-xs text-gray-500">
                                            Zostanie utworzony z domyślnymi wartościami
                                        </div>
                                    </div>
                                </div>
                            </button>
                        )}
                    </div>

                    {/* Footer listy */}
                    <div className="px-4 py-2 bg-gray-50 border-t border-gray-100">
                        <p className="text-xs text-gray-500 text-center">
                            Użyj strzałek ↑↓ do nawigacji, Enter aby wybrać
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default InlineIngredientSearch;