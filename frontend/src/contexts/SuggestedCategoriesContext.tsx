import React, { createContext, PropsWithChildren, useContext } from 'react';
import { ParsedProduct } from '../types/product';
import { useSuggestedCategories} from "../hooks/shopping/useSuggestedCategories";

interface SuggestedCategoriesContextType {
    getSuggestion: (product: ParsedProduct) => Promise<string>;
    refreshSuggestions: (products: ParsedProduct[]) => Promise<Record<string, string>>;
    clearSuggestions: () => void;
    suggestionCache: Record<string, string>;
}

export const SuggestedCategoriesContext = createContext<SuggestedCategoriesContextType>({
    getSuggestion: async () => 'other',
    refreshSuggestions: async () => ({}),
    clearSuggestions: () => {},
    suggestionCache: {}
});

export const SuggestedCategoriesProvider: React.FC<PropsWithChildren<{}>> = ({ children }) => {
    const { getSuggestion, refreshSuggestions, clearSuggestions, suggestionCache } = useSuggestedCategories();

    return (
        <SuggestedCategoriesContext.Provider
            value={{
                getSuggestion,
                refreshSuggestions,
                clearSuggestions,
                suggestionCache
            }}
        >
            {children}
        </SuggestedCategoriesContext.Provider>
    );
};

export const useSuggestedCategoriesContext = () => useContext(SuggestedCategoriesContext);