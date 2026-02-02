import React, {useState, useCallback, useEffect, useRef} from 'react';
import {ParsedProduct} from '../../../../../types/product';
import {useDebounce} from '../../../../../hooks/useDebounce';
import {toast} from '../../../../../utils/toast';
import {Check, Loader2, Package, Plus, Search, X} from 'lucide-react';
import {DietCreatorService} from '../../../../../services/diet/creator/DietCreatorService';

interface InlineIngredientSearchProps {
    onSelect: (ingredient: ParsedProduct) => void;
    placeholder?: string;
    trainerId?: string;
}

// List of available units
const AVAILABLE_UNITS = ['g', 'ml', 'szt', 'opakowanie', 'łyżka', 'łyżeczka', 'szklanka', 'plaster', 'porcja'];

const InlineIngredientSearch: React.FC<InlineIngredientSearchProps> = ({
                                                                           onSelect,
                                                                           placeholder = "Wpisz składnik...",
                                                                           trainerId
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

    // Handle click outside to close results or cancel draft
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                if (!draftIngredient) {
                    setShowResults(false);
                    setSelectedIndex(-1);
                }
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [draftIngredient]);

    // Search ingredients when query changes
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
            const products = await DietCreatorService.searchIngredients(
                debouncedSearchQuery,
                trainerId,
                8
            );
            const parsedResults: ParsedProduct[] = products.map(product =>
                DietCreatorService.convertProductToParsedProduct(product)
            );
            setSearchResults(parsedResults);
            setShowResults(parsedResults.length > 0 || debouncedSearchQuery.trim().length >= 2);
            setSelectedIndex(-1);
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
        } finally {
            setIsSearching(false);
        }
    };

    // --- INITIAL SELECTION ---
    const handleInitialSelect = (ingredient: ParsedProduct) => {
        setDraftIngredient(ingredient);

        // Determine default quantity based on unit type
        // If unit implies a count (pcs, portion, etc.) -> 1, otherwise -> 100
        const isCountable = ['szt', 'porcja', 'opakowanie', 'plaster', 'łyżka', 'łyżeczka', 'szklanka'].some(u =>
            ingredient.unit.toLowerCase().includes(u)
        );

        setQuantityInput(isCountable ? '1' : '100');

        // Determine unit: try to match with available units or fallback to 'g'/'szt'
        let defaultUnit = ingredient.unit || 'g';
        if (!AVAILABLE_UNITS.includes(defaultUnit)) {
            defaultUnit = isCountable ? 'szt' : 'g';
        }
        setUnitInput(ingredient.unit || defaultUnit);

        setShowResults(false);
        setSelectedIndex(-1);

        // Focus quantity input after render
        setTimeout(() => {
            if (quantityInputRef.current) {
                quantityInputRef.current.focus();
                quantityInputRef.current.select();
            }
        }, 50);
    };

    // --- CONFIRM SELECTION ---
    const confirmSelection = () => {
        if (!draftIngredient) return;

        // Parse integer to ensure no decimals are sent if unwanted
        const qty = parseInt(quantityInput, 10);

        if (isNaN(qty) || qty <= 0) {
            toast.error('Please enter a valid quantity (integer > 0)');
            quantityInputRef.current?.focus();
            return;
        }

        const finalIngredient: ParsedProduct = {
            ...draftIngredient,
            quantity: qty,
            unit: unitInput
        };

        onSelect(finalIngredient);
        resetSearch();
    };

    const resetSearch = () => {
        setDraftIngredient(null);
        setSearchQuery('');
        setSearchResults([]);
        setShowResults(false);
        setTimeout(() => inputRef.current?.focus(), 50);
    };

    // Avoids API call to prevent 500 error on incomplete data
    const handleCreateFromText = useCallback(async () => {
        if (!searchQuery.trim()) return;

        // Create a temporary local object
        const tempIngredient: ParsedProduct = {
            id: `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            name: searchQuery.trim(),
            quantity: 1,
            unit: 'szt',
            original: searchQuery.trim(),
            hasCustomUnit: false
        };

        // Pass directly to editing mode
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
                className="flex items-center gap-2 p-1 bg-green-50 border border-green-200 rounded-lg animate-in fade-in duration-200">
                {/* Product Name */}
                <div className="flex-1 px-2 font-medium text-gray-700 truncate" title={draftIngredient.name}>
                    {draftIngredient.name}
                </div>

                {/* Inputs Section: Quantity + Unit */}
                <div
                    className="flex items-center gap-1 bg-white rounded border border-gray-300 p-0.5 shadow-sm focus-within:ring-2 focus-within:ring-green-500 focus-within:border-transparent">
                    {/* Quantity Input (Integers only) */}
                    <input
                        ref={quantityInputRef}
                        type="number"
                        min="1"
                        step="1"
                        value={quantityInput}
                        onChange={(e) => {
                            // Allow digits only
                            const val = e.target.value.replace(/[^0-9]/g, '');
                            setQuantityInput(val);
                        }}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') confirmSelection();
                            if (e.key === 'Escape') resetSearch();
                        }}
                        className="w-14 text-right outline-none text-sm font-semibold text-gray-900 placeholder-gray-300 py-1"
                        placeholder="1"
                    />

                    {/* Separator */}
                    <div className="w-px h-4 bg-gray-200 mx-1"></div>

                    {/* Unit Selector */}
                    <select
                        value={unitInput}
                        onChange={(e) => setUnitInput(e.target.value)}
                        className="text-xs font-medium text-gray-600 bg-transparent outline-none border-none py-1 pr-1 cursor-pointer hover:text-gray-900"
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') confirmSelection();
                        }}
                    >
                        {AVAILABLE_UNITS.map(u => (
                            <option key={u} value={u}>{u}</option>
                        ))}
                        {/* Show custom unit if it exists on the product but is not in the list */}
                        {!AVAILABLE_UNITS.includes(unitInput) && (
                            <option value={unitInput}>{unitInput}</option>
                        )}
                    </select>
                </div>

                {/* Action Buttons */}
                <button
                    onClick={confirmSelection}
                    className="p-1.5 bg-green-500 text-white rounded hover:bg-green-600 transition-colors shadow-sm flex-shrink-0"
                    title="Confirm (Enter)"
                >
                    <Check className="h-4 w-4"/>
                </button>
                <button
                    onClick={resetSearch}
                    className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded transition-all flex-shrink-0"
                    title="Cancel (Esc)"
                >
                    <X className="h-4 w-4"/>
                </button>
            </div>
        );
    }

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
                    className="w-full pl-10 pr-10 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary transition-shadow"
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
                    className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-xl z-50 max-h-64 overflow-hidden">
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

                        {searchResults.map((ingredient, index) => (
                            <button
                                key={`${ingredient.id}-${index}`}
                                onClick={() => handleInitialSelect(ingredient)}
                                className={`w-full text-left px-3 py-2 border-b border-gray-100 last:border-b-0 transition-colors ${
                                    index === selectedIndex ? 'bg-green-50 border-l-4 border-green-500 pl-2' : 'hover:bg-gray-50'
                                }`}
                            >
                                <div className="font-medium text-sm text-gray-900">{ingredient.name}</div>
                                <div className="text-xs text-gray-500">
                                    {ingredient.quantity} {ingredient.unit}
                                    {ingredient.original && ingredient.original !== ingredient.name &&
                                        <span className="text-gray-400"> • {ingredient.original}</span>
                                    }
                                </div>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default InlineIngredientSearch;