import React, {useCallback, useEffect, useRef, useState} from "react";
import {useDebounce} from "../../../../../hooks/useDebounce";
import {DietCreatorService} from "../../../../../services/diet/creator/DietCreatorService";
import {toast} from "../../../../../utils/toast";
import {Check, Package, ChefHat, Info, Search, X} from "lucide-react";
import LoadingSpinner from "../../../../shared/common/LoadingSpinner";
import ColoredNutritionBadges from "./ColoredNutritionBadges";
import type {UnifiedSearchResult} from "../../../../../types";

interface MealNameSearchFieldProps {
    value: string;
    onChange: (value: string) => void;
    /** Called when user selects a recipe or product from unified search. */
    onUnifiedResultSelect?: (result: UnifiedSearchResult) => void;
    placeholder?: string;
    className?: string;
    showSaveOptions?: boolean;
    onSavePreference?: (shouldSave: boolean) => void;
    /** Trainer ID for custom products in unified search. */
    trainerId?: string;
}

const MealNameSearchField: React.FC<MealNameSearchFieldProps> = ({
                                                                     value,
                                                                     onChange,
                                                                     onUnifiedResultSelect,
                                                                     placeholder = "Wpisz nazwę posiłku lub produktu...",
                                                                     className = "",
                                                                     showSaveOptions = true,
                                                                     onSavePreference,
                                                                     trainerId,
                                                                 }) => {
    const [suggestions, setSuggestions] = useState<UnifiedSearchResult[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const [showSaveDialog, setShowSaveDialog] = useState(false);
    const [savePreference, setSavePreference] = useState(true);

    const inputRef = useRef<HTMLInputElement>(null);
    const suggestionsRef = useRef<HTMLDivElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    const isSelectingRef = useRef(false);

    const debouncedValue = useDebounce(value, 300);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
                setSelectedIndex(-1);
            }
        };

        if (showSuggestions) {
            document.addEventListener("mousedown", handleClickOutside);
            return () => document.removeEventListener("mousedown", handleClickOutside);
        }
    }, [showSuggestions]);

    useEffect(() => {
        if (isSelectingRef.current) {
            isSelectingRef.current = false;
            return;
        }

        if (debouncedValue.trim().length >= 2) {
            searchUnified(debouncedValue.trim()).catch(console.error);
        } else {
            setSuggestions([]);
            setShowSuggestions(false);
        }
    }, [debouncedValue]);

    const searchUnified = async (query: string) => {
        setIsSearching(true);
        try {
            const results = await DietCreatorService.searchUnified(query, trainerId);
            setSuggestions(results);
            setShowSuggestions(results.length > 0);
            setSelectedIndex(-1);

            if (showSaveOptions) {
                if (results.length === 0) {
                    setShowSaveDialog(true);
                } else {
                    setShowSaveDialog(false);
                }
            } else {
                setShowSaveDialog(false);
            }
        } catch (error) {
            console.error("Unified search error:", error);
            toast.error("Nie udało się wyszukać");
            setSuggestions([]);
            setShowSuggestions(false);
        } finally {
            setIsSearching(false);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        onChange(newValue);
        if (newValue.trim().length < 2) {
            setShowSaveDialog(false);
        }
    };

    const handleSuggestionSelect = useCallback(
        (result: UnifiedSearchResult) => {
            isSelectingRef.current = true;

            setShowSuggestions(false);
            setShowSaveDialog(false);
            setSelectedIndex(-1);
            setSuggestions([]);

            if (result.type === "RECIPE") {
                onChange(result.name);
            }
            if (result.type === "PRODUCT" && !value.trim()) {
                onChange(result.name);
            }

            if (onUnifiedResultSelect) {
                onUnifiedResultSelect(result);
            }
        },
        [onChange, onUnifiedResultSelect, value]
    );

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (!showSuggestions || suggestions.length === 0) return;

        switch (e.key) {
            case "ArrowDown":
                e.preventDefault();
                setSelectedIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : prev));
                break;
            case "ArrowUp":
                e.preventDefault();
                setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
                break;
            case "Enter":
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
                    handleSuggestionSelect(suggestions[selectedIndex]);
                }
                break;
            case "Escape":
                setShowSuggestions(false);
                setSelectedIndex(-1);
                setSuggestions([]);
                break;
        }
    };

    const handleInputFocus = () => {
        if (suggestions.length > 0 && !isSelectingRef.current) {
            setShowSuggestions(true);
        }
    };

    const handleSavePreferenceChange = (shouldSave: boolean) => {
        setSavePreference(shouldSave);
        if (onSavePreference) {
            onSavePreference(shouldSave);
        }
        setShowSaveDialog(false);
    };

    const getTypeIcon = (type: UnifiedSearchResult["type"]) => {
        switch (type) {
            case "RECIPE":
                return <ChefHat className="h-3 w-3 text-blue-600"/>;
            case "PRODUCT":
                return <Package className="h-3 w-3 text-green-600"/>;
            default:
                return null;
        }
    };

    const getTypeLabel = (type: UnifiedSearchResult["type"]) => {
        return type === "RECIPE" ? "Przepis" : "Produkt";
    };

    return (
        <div ref={containerRef} className={`relative ${className}`}>
            <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 transform text-gray-400"/>
                <input
                    ref={inputRef}
                    type="text"
                    value={value}
                    onChange={handleInputChange}
                    onKeyDown={handleKeyDown}
                    onFocus={handleInputFocus}
                    placeholder={placeholder}
                    className="w-full border border-gray-300 py-2 pl-10 pr-10 text-sm focus:border-primary focus:ring-primary"
                />
                {isSearching && (
                    <LoadingSpinner
                        size="sm"
                        className="absolute right-3 top-1/2 -translate-y-1/2 transform"
                    />
                )}
            </div>

            {showSaveDialog && value.trim().length >= 2 && (
                <div className="mt-2 rounded-lg border border-blue-200 bg-blue-50 p-3">
                    <div className="flex items-start gap-2">
                        <Info className="mt-0.5 h-4 w-4 flex-shrink-0 text-blue-600"/>
                        <div className="min-w-0 flex-1">
                            <p className="text-sm font-medium text-blue-800">
                                Nie znaleziono podobnego posiłku
                            </p>
                            <p className="mt-1 text-xs text-blue-700">
                                Posiłek &quot;{value}&quot; zostanie zapisany jako nowy szablon do
                                wykorzystania w przyszłości.
                            </p>
                            <div className="mt-2 flex items-center gap-3">
                                <button
                                    onClick={() => handleSavePreferenceChange(true)}
                                    className={`flex items-center gap-1 rounded-md px-3 py-1 text-xs transition-colors ${
                                        savePreference
                                            ? "border border-blue-200 bg-white text-blue-600"
                                            : "bg-blue-600 text-white"
                                    }`}
                                >
                                    <Check className="h-3 w-3"/>
                                    Zapisz szablon
                                </button>
                                <button
                                    onClick={() => handleSavePreferenceChange(false)}
                                    className={`flex items-center gap-1 rounded-md px-3 py-1 text-xs transition-colors ${
                                        !savePreference
                                            ? "border border-gray-200 bg-white text-gray-600"
                                            : "bg-gray-600 text-white"
                                    }`}
                                >
                                    <X className="h-3 w-3"/>
                                    Nie zapisuj
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {showSuggestions && suggestions.length > 0 && (
                <div
                    ref={suggestionsRef}
                    className="absolute left-0 right-0 top-full z-50 mt-1 max-h-80 overflow-hidden rounded-xl border-2 border-primary-light bg-white shadow-xl"
                    style={{
                        background: "linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)",
                    }}
                >
                    <div className="border-b border-primary-light/20 bg-primary-light/10 px-4 py-2">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 animate-pulse rounded-full bg-primary"/>
                            <span className="text-xs font-medium text-primary-dark">
                                Znalezione: przepisy i produkty ({suggestions.length})
                            </span>
                        </div>
                    </div>

                    <div className="max-h-72 overflow-y-auto">
                        {suggestions.map((result, index) => (
                            <div
                                key={`${result.type}-${result.id}`}
                                onClick={() => handleSuggestionSelect(result)}
                                className={`cursor-pointer border-b border-gray-100 p-3 last:border-b-0 transition-all duration-150 ${
                                    index === selectedIndex
                                        ? "scale-[1.02] border-l-4 border-l-primary bg-primary-light/20"
                                        : "hover:border-l-4 hover:border-l-primary-light hover:bg-gray-50"
                                }`}
                            >
                                <div className="flex items-start justify-between gap-3">
                                    <div className="min-w-0 flex-1">
                                        <div className="mb-1 flex items-center gap-2">
                                            {getTypeIcon(result.type)}
                                            <h4 className="truncate text-sm font-medium text-gray-900">
                                                {result.name}
                                            </h4>
                                            <span
                                                className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600">
                                                {getTypeLabel(result.type)}
                                            </span>
                                        </div>

                                        {result.nutritionalValues && (
                                            <div className="mb-2">
                                                <ColoredNutritionBadges
                                                    nutritionalValues={result.nutritionalValues}
                                                    size="sm"
                                                    layout="horizontal"
                                                />
                                            </div>
                                        )}
                                        {result.type === "PRODUCT" && result.unit && (
                                            <p className="text-xs text-gray-500">
                                                Jednostka: {result.unit}
                                            </p>
                                        )}
                                    </div>

                                    {result.photos && result.photos.length > 0 && (
                                        <div className="relative">
                                            <img
                                                src={result.photos[0]}
                                                alt={result.name}
                                                className="h-12 w-12 flex-shrink-0 rounded-lg border border-gray-200 object-cover"
                                                onError={(e) => {
                                                    e.currentTarget.style.display = "none";
                                                }}
                                            />
                                            {result.photos.length > 1 && (
                                                <span
                                                    className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-primary text-xs font-bold text-white">
                                                    {result.photos.length}
                                                </span>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="border-t border-gray-100 bg-gray-50 px-4 py-2">
                        <p className="text-center text-xs text-gray-500">
                            Przepis: uzupełnia posiłek. Produkt: dodaje do składników. ↑↓ Enter.
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MealNameSearchField;
