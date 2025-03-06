import React, { createContext, PropsWithChildren } from 'react';
import { ParsedProduct } from '../types/product';
import { useSuggestedCategories} from "../hooks/shopping/useSuggestedCategories";

interface SuggestedCategoriesContextType {
    getSuggestion: (product: ParsedProduct) => Promise<string>;
    clearSuggestions: () => void;
}

export const SuggestedCategoriesContext = createContext<SuggestedCategoriesContextType>({
    getSuggestion: async () => 'other',
    clearSuggestions: () => {},
});

export const SuggestedCategoriesProvider: React.FC<PropsWithChildren<{}>> = ({ children }) => {
    const { getSuggestion,  clearSuggestions } = useSuggestedCategories();

    return (
        <SuggestedCategoriesContext.Provider
            value={{
                getSuggestion,
                clearSuggestions,
            }}
        >
            {children}
        </SuggestedCategoriesContext.Provider>
    );
};