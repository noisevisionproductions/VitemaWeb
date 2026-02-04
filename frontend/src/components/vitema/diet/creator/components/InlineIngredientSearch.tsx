import React, {useState, useCallback, useEffect, useRef} from 'react';
import {ParsedProduct} from '../../../../../types/product';
import {useDebounce} from '../../../../../hooks/useDebounce';
import {toast} from '../../../../../utils/toast';
import {Check, Loader2, Package, Plus, Scale, Search, X} from 'lucide-react';
import {DietCreatorService} from '../../../../../services/diet/creator/DietCreatorService';

interface InlineIngredientSearchProps {
    onSelect: (ingredient: ParsedProduct) => void;
    placeholder?: string;
    trainerId?: string;
    initialIngredient?: ParsedProduct | null;
    onCancelEdit?: () => void;
}

const AVAILABLE_UNITS = ['g', 'ml', 'szt', 'opakowanie', 'łyżka', 'łyżeczka', 'szklanka', 'plaster', 'porcja'];

const InlineIngredientSearch: React.FC<InlineIngredientSearchProps> = ({
                                                                           onSelect,
                                                                           placeholder = "Wpisz składnik...",
                                                                           trainerId,
                                                                           initialIngredient,
                                                                           onCancelEdit
                                                                       }) => {
    // --- SEARCH STATES ---
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<ParsedProduct[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const [showResults, setShowResults] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);

    // --- QUANTITY & UNIT EDITING STATES ---
    const [draftIngredient, setDraftIngredient] = useState<ParsedProduct | null>(null);
    const [quantityInput, setQuantityInput] = useState<string>('100');
    const [unitInput, setUnitInput] = useState<string>('g');

    // --- REFS ---
    const quantityInputRef = useRef<HTMLInputElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);

    const debouncedSearchQuery = useDebounce(searchQuery, 300);

    useEffect(() => {
        if (initialIngredient) {
            setDraftIngredient(initialIngredient);
            setQuantityInput(initialIngredient.quantity.toString());
            setUnitInput(initialIngredient.unit);

            setTimeout(() => {
                quantityInputRef.current?.focus();
                quantityInputRef.current?.select();
            }, 50);
        } else {
            if (onCancelEdit) {
                setDraftIngredient(null);
                setSearchQuery('');
            }
        }
    }, [initialIngredient, onCancelEdit]);

    // Handle click outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                if (!draftIngredient && !initialIngredient) {
                    setShowResults(false);
                    setSelectedIndex(-1);
                }
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [draftIngredient, initialIngredient]);

    // Search effect
    useEffect(() => {
        if (debouncedSearchQuery.trim().length < 2) {
            setSearchResults([]);
            setShowResults(false);
            return;
        }
        if (!draftIngredient) {
            searchIngredients().catch(console.error);
        }
    }, [debouncedSearchQuery]);

    const searchIngredients = async () => {
        setIsSearching(true);
        try {
            const products = await DietCreatorService.searchIngredients(
                debouncedSearchQuery,
                trainerId,
                8
            );
            const parsedResults = products.map(p => DietCreatorService.convertProductToParsedProduct(p));
            setSearchResults(parsedResults);
            setShowResults(parsedResults.length > 0 || debouncedSearchQuery.trim().length >= 2);
        } catch (error) {
            console.error('Error searching ingredients:', error);
            try {
                // Fallback to legacy search
                const legacyResults = await DietCreatorService.searchIngredientsLegacy(debouncedSearchQuery, 8);
                setSearchResults(legacyResults);
                setShowResults(legacyResults.length > 0);
            } catch (e) {
                setSearchResults([]);
            }
            setSearchResults([]);
        } finally {
            setIsSearching(false);
        }
    };

    const handleInitialSelect = (ingredient: ParsedProduct) => {
        setDraftIngredient(ingredient);

        const isCountable = ['szt', 'porcja', 'opakowanie', 'plaster', 'łyżka', 'łyżeczka', 'szklanka'].some(u =>
            ingredient.unit.toLowerCase().includes(u)
        );
        setQuantityInput(isCountable ? '1' : '100');
        let defaultUnit = ingredient.unit || 'g';
        if (!AVAILABLE_UNITS.includes(defaultUnit)) {
            defaultUnit = isCountable ? 'szt' : 'g';
        }
        setUnitInput(ingredient.unit || defaultUnit);

        setShowResults(false);
        setSearchQuery('');

        setTimeout(() => {
            quantityInputRef.current?.focus();
            quantityInputRef.current?.select();
        }, 50);
    };

    const confirmSelection = () => {
        if (!draftIngredient) return;
        const qty = parseInt(quantityInput, 10);
        if (isNaN(qty) || qty <= 0) {
            toast.error('Podaj poprawną ilość');
            quantityInputRef.current?.focus();
            return;
        }

        const finalIngredient: ParsedProduct = {
            ...draftIngredient,
            quantity: qty,
            unit: unitInput
        };

        onSelect(finalIngredient);

        setDraftIngredient(null);
        setSearchQuery('');
        if (onCancelEdit) onCancelEdit();
    };

    const handleCancel = () => {
        setDraftIngredient(null);
        setSearchQuery('');
        if (onCancelEdit) onCancelEdit();
    };

    const handleCreateFromText = useCallback(async () => {
        if (!searchQuery.trim()) return;
        const tempIngredient: ParsedProduct = {
            id: `temp-${Date.now()}`,
            name: searchQuery.trim(),
            quantity: 1,
            unit: 'szt',
            original: searchQuery.trim(),
            hasCustomUnit: false
        };
        handleInitialSelect(tempIngredient);
    }, [searchQuery]);

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (draftIngredient && e.key === 'Enter') {
            e.preventDefault();
            confirmSelection();
            return;
        }

        const totalOptions = searchResults.length + (searchQuery.trim() ? 1 : 0);
        if (!showResults && totalOptions === 0) return;

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
                    handleInitialSelect(searchResults[selectedIndex]);
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

    if (draftIngredient) {
        return (
            <div
                className="p-3 bg-green-50 border border-green-200 rounded-xl animate-in fade-in zoom-in-95 duration-200 shadow-sm">
                <div className="flex items-start justify-between mb-2 gap-2">
                    <div className="font-medium text-gray-800 break-words leading-tight">
                        {draftIngredient.name}
                    </div>
                    <button onClick={handleCancel} className="text-gray-400 hover:text-red-500 transition-colors">
                        <X className="h-4 w-4"/>
                    </button>
                </div>

                {/* Inputs Section: Quantity + Unit */}
                <div className="flex items-center gap-2">
                    <div
                        className="flex-1 flex items-center gap-0 bg-white rounded-lg border border-gray-300 shadow-sm focus-within:ring-2 focus-within:ring-green-500 focus-within:border-transparent overflow-hidden">
                        <div
                            className="pl-2 text-gray-400 bg-gray-50 h-full flex items-center border-r border-gray-100">
                            <Scale className="h-3.5 w-3.5"/>
                        </div>

                        <input
                            ref={quantityInputRef}
                            type="number"
                            min="1"
                            value={quantityInput}
                            onChange={(e) => setQuantityInput(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && confirmSelection()}
                            className="w-full min-w-[60px] flex-1 px-2 py-2 text-gray-900 font-semibold outline-none"
                            placeholder="Ilość"
                        />

                        <div className="w-px h-6 bg-gray-200"></div>

                        <select
                            value={unitInput}
                            onChange={(e) => setUnitInput(e.target.value)}
                            className="bg-transparent text-sm font-medium text-gray-700 py-2 pl-2 pr-1 outline-none cursor-pointer hover:bg-gray-50 transition-colors"
                        >
                            {AVAILABLE_UNITS.map(u => (
                                <option key={u} value={u}>{u}</option>
                            ))}
                            {!AVAILABLE_UNITS.includes(unitInput) && (
                                <option value={unitInput}>{unitInput}</option>
                            )}
                        </select>
                    </div>

                    <button
                        onClick={confirmSelection}
                        className="p-2.5 bg-green-500 hover:bg-green-600 text-white rounded-lg shadow-sm transition-all active:scale-95 flex items-center justify-center"
                        title="Zatwierdź"
                    >
                        <Check className="h-5 w-5"/>
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div ref={containerRef} className="relative">
            <div className="relative">
                <div
                    className="absolute left-3 top-1/2 transform -translate-y-1/2 bg-gray-100 p-1 rounded-md text-gray-400 group-focus-within:text-primary group-focus-within:bg-primary/10 transition-colors">
                    <Search className="h-4 w-4"/>
                </div>
                <input
                    ref={inputRef}
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    onFocus={() => (searchResults.length > 0 || searchQuery.trim().length >= 2) && setShowResults(true)}
                    placeholder={placeholder}
                    className="w-full pl-11 pr-10 py-3 text-sm border border-gray-200 bg-gray-50/50 hover:bg-white focus:bg-white rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
                {isSearching && (
                    <Loader2
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4 animate-spin"/>
                )}
                {!isSearching && searchQuery && (
                    <button
                        onClick={handleCreateFromText}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-primary hover:text-primary-dark transition-colors p-1"
                        title="Add as new ingredient"
                    >
                        <Plus className="h-4 w-4"/>
                    </button>
                )}
            </div>

            {showResults && (searchResults.length > 0 || searchQuery.trim()) && (
                <div
                    className="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-xl z-50 max-h-64 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-150">
                    <div className="px-3 py-2 bg-gray-50 border-b border-gray-100 flex items-center gap-2">
                        <Package className="h-3 w-3 text-gray-500"/>
                        <span className="text-xs font-medium text-gray-500">
                            {searchResults.length > 0 ? 'Found ingredients' : 'Create new'}
                        </span>
                    </div>

                    <div className="max-h-48 overflow-y-auto">
                        {searchQuery.trim() && (
                            <button
                                onClick={handleCreateFromText}
                                className={`w-full text-left px-3 py-2 border-b border-gray-100 transition-colors flex items-center gap-2 ${
                                    selectedIndex === searchResults.length ? 'bg-primary/10' : 'hover:bg-gray-50'
                                }`}
                            >
                                <div className="bg-primary/10 p-1 rounded">
                                    <Plus className="h-4 w-4 text-primary"/>
                                </div>
                                <div>
                                    <div className="font-medium text-sm text-primary">Add "{searchQuery}"</div>
                                    <div className="text-xs text-gray-500">Set quantity and unit</div>
                                </div>
                            </button>
                        )}

                        <div className="max-h-56 overflow-y-auto custom-scrollbar">
                            {searchResults.map((ingredient, index) => (
                                <button
                                    key={index}
                                    onClick={() => handleInitialSelect(ingredient)}
                                    className="w-full text-left px-4 py-2.5 hover:bg-gray-50 transition-colors border-b border-gray-50 last:border-0"
                                >
                                    <div className="font-medium text-gray-800">{ingredient.name}</div>
                                    <div className="text-xs text-gray-500 mt-0.5">
                                        Domyślnie: <span
                                        className="font-medium">{ingredient.quantity} {ingredient.unit}</span>
                                    </div>
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default InlineIngredientSearch;