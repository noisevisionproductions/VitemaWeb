import { NutritionalValues } from './recipe';

/** Result from unified search (recipes + products in one response). */
export type UnifiedSearchResultType = 'RECIPE' | 'PRODUCT';

export interface UnifiedSearchResult {
    id: string;
    name: string;
    type: UnifiedSearchResultType;
    nutritionalValues?: NutritionalValues;
    /** Product default unit (for PRODUCT type). */
    unit?: string;
    /** Recipe photos (for RECIPE type). */
    photos?: string[];
    authorId?: string;
}
