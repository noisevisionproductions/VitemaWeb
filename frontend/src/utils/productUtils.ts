import { ParsedProduct } from '../types/product';
import { DEFAULT_CATEGORIES } from '../data/productCategories';
import {Category} from "../types/product-categories";

export const createSafeProduct = (productText: any): ParsedProduct => {
    // Sprawdź typ wartości i konwertuj na string, jeśli to nie jest string
    let text: string;

    if (typeof productText === 'string') {
        text = productText;
    } else if (productText && typeof productText === 'object') {
        // Jeśli to obiekt, próbuj wydobyć sensowną reprezentację
        if ('name' in productText) {
            text = String(productText.name);
        } else if ('original' in productText) {
            text = String(productText.original);
        } else {
            // Ostatecznie użyj JSON lub pierwszego klucza
            const keys = Object.keys(productText);
            text = keys.length > 0 ? String(keys[0]) : 'unknown';
        }
    } else {
        // Jeśli to ani string, ani obiekt, użyj domyślnej wartości
        text = 'unknown';
    }

    const trimmedText = text.trim();

    // Reszta logiki parsowania jak wcześniej...
    // Próba znalezienia ilości i jednostki
    const quantityUnitMatch = trimmedText.match(/(\d+(?:[.,]\d+)?)\s*([a-zA-Zóąśłżźćńę]{1,4})\b/i);

    if (quantityUnitMatch) {
        // Mamy dopasowanie ilości i jednostki
        const quantityStr = quantityUnitMatch[1].replace(',', '.');
        const quantity = parseFloat(quantityStr);
        const unit = quantityUnitMatch[2].toLowerCase();

        // Usunięcie ilości i jednostki z nazwy
        const name = trimmedText
            .replace(quantityUnitMatch[0], '')
            .trim();

        return {
            name: name || trimmedText,
            quantity: isNaN(quantity) ? 1 : quantity,
            unit: unit || 'szt',
            original: trimmedText,
            hasCustomUnit: false
        };
    }

    // Jeśli nie znaleziono wzorca ilości, zwróć tekst jako nazwę
    return {
        name: trimmedText,
        quantity: 1,
        unit: 'szt',
        original: trimmedText,
        hasCustomUnit: false
    };
};

export const getCategoryLabel = (categoryId: string, categoriesList?: Category[]): string => {
    // Jeśli przekazano listę kategorii, najpierw szukamy w niej
    if (categoriesList && categoriesList.length > 0) {
        const category = categoriesList.find(cat => cat.id === categoryId);
        if (category) return category.name;
    }

    // W przeciwnym razie szukamy w domyślnych kategoriach
    const category = DEFAULT_CATEGORIES.find(cat => cat.id === categoryId);
    return category?.name || 'Nieznana kategoria';
};