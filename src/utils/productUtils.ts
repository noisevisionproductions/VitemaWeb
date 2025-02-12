import { ParsedProduct } from '../types/product';
import { DEFAULT_CATEGORIES } from '../data/productCategories';

export const createSafeProduct = (input: string | Partial<ParsedProduct>): ParsedProduct => ({
    name: typeof input === 'string' ? input : input.name ?? 'unknown',
    quantity: typeof input === 'string' ? 1 : input.quantity ?? 1,
    unit: typeof input === 'string' ? 'szt' : input.unit ?? 'szt',
    original: typeof input === 'string' ? input : input.original ?? 'unknown',
    hasCustomUnit: typeof input === 'string' ? false : input.hasCustomUnit ?? false
});

export const getCategoryLabel = (categoryId: string): string => {
    const category = DEFAULT_CATEGORIES.find(cat => cat.id === categoryId);
    return category?.name || 'Nieznana kategoria';
};