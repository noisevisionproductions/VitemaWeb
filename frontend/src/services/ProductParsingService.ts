import { ParsedProduct } from "../types/product";

export class ProductParsingService {

    static parseProduct(productText: string): { success: boolean; product?: ParsedProduct; error?: string } {
        try {
            let quantity = 1;
            let unit = 'szt';
            let name = productText.trim();

            // Try to find quantity (number)
            const quantityMatch = productText.match(/(\d+(?:[.,]\d+)?)/);
            if (quantityMatch) {
                quantity = parseFloat(quantityMatch[1].replace(',', '.'));

                // Try to find unit (1-3 characters after number)
                const unitMatch = productText.match(/\d+(?:[.,]\d+)?\s*([a-zA-Zśńółźżąęć]{1,3})\b/i);
                if (unitMatch) {
                    unit = unitMatch[1].toLowerCase();
                }
            }

            return {
                success: true,
                product: {
                    name: name,
                    quantity,
                    unit,
                    original: productText.trim(),
                    hasCustomUnit: false
                }
            };
        } catch (error) {
            return {
                success: false,
                error: error instanceof Error ? error.message : 'Nieznany błąd parsowania'
            };
        }
    }
}