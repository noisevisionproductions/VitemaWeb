import { useRef } from 'react';
import { ParsedProduct} from "../../types/product";
import { DietCategorizationService} from "../../services/DietCategorizationService";

export function useSuggestedCategories() {

    const suggestionCache = useRef<Record<string, string>>({});

    /**
     * Pobiera sugestię kategorii dla pojedynczego produktu, używając cache
     */
    const getSuggestion = async (product: ParsedProduct): Promise<string> => {
        const key = product.original || product.name;

        if (suggestionCache.current[key]) {
            return suggestionCache.current[key];
        }

        try {
            const categoryId = await DietCategorizationService.suggestCategory(product);
            suggestionCache.current[key] = categoryId;
            return categoryId;
        } catch (error) {
            console.error('Błąd pobierania sugestii kategorii:', error);
            return 'other';
        }
    };

    /**
     * Wyczyść cache sugestii
     */
    const clearSuggestions = () => {
        suggestionCache.current = {};
    };

    return {
        getSuggestion,
        clearSuggestions,

    };
}