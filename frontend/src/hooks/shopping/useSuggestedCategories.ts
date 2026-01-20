import {useCallback, useState} from 'react';
import {ParsedProduct} from "../../types/product";
import {DietCategorizationService} from "../../services/diet/DietCategorizationService";

export function useSuggestedCategories() {

    const [suggestionCache, setSuggestionCache] = useState<Record<string, string>>({});

    /**
     * Pobiera sugestię kategorii dla pojedynczego produktu, używając cache
     */
    const getSuggestion = useCallback(async (product: ParsedProduct): Promise<string> => {
        const key = product.original || product.name;

        if (suggestionCache[key]) {
            return suggestionCache[key];
        }

        try {
            const categoryId = await DietCategorizationService.suggestCategory(product);

            setSuggestionCache(prevCache => ({
                ...prevCache,
                [key]: categoryId
            }));

            return categoryId;
        } catch (error) {
            console.error('Błąd pobierania sugestii kategorii:', error);
            return 'other';
        }
    }, [suggestionCache]);

    const refreshSuggestions = useCallback(async (products: ParsedProduct[]) => {
        try {
            const suggestions = await DietCategorizationService.bulkSuggestCategories(products);

            setSuggestionCache(prevCache => ({
                ...prevCache,
                ...suggestions
            }));

            return suggestions;
        } catch (error) {
            console.error('Błąd podczas odświeżania sugestii:', error);
            return {};
        }
    }, []);


    /**
     * Wyczyść cache sugestii
     */
    const clearSuggestions = useCallback(() => {
        setSuggestionCache({});
    }, []);


    return {
        getSuggestion,
        refreshSuggestions,
        clearSuggestions,
        suggestionCache
    };
}