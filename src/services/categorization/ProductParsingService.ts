import {ParsingResult} from "../../types/product";
import {UnitService} from "./UnitService";
import {QuantityParser} from "./QuantityParser";

export class ProductParsingService {

    /*
        private static readonly KITCHEN_MEASURES: Record<string, number> = {
            'łyżka': 15,
            'łyżeczka': 5,
            'szklanka': 250,
            'garść': 30
        };
    */

    static parseProduct(input: string): ParsingResult {
        try {
            const cleanInput = this.cleanInputString(input);
            const quantityInfo = this.extractQuantityAndUnit(cleanInput);

            if (!quantityInfo.success || !quantityInfo.quantity || !quantityInfo.remainingText) {
                return {
                    success: false,
                    error: 'Nie znaleziono poprawnej ilości'
                };
            }

            const {quantity, potentialUnit, remainingText} = quantityInfo;

            if (!potentialUnit && !remainingText) {
                const nameParts = cleanInput.split(' ').filter(part => !this.isNumeric(part));
                return {
                    success: true,
                    product: {
                        name: this.cleanProductName(nameParts.join(' ')),
                        quantity,
                        unit: 'szt',
                        original: input,
                        hasCustomUnit: false
                    }
                };
            }

            const {unit, name, foundKnownUnit} = this.processUnitAndName(potentialUnit || '', remainingText);

            // Normalizuj wartość do jednostki bazowej
            const normalized = UnitService.normalizeToBaseUnit(quantity, unit);

            if (normalized) {
                return {
                    success: true,
                    product: {
                        name: this.cleanProductName(name),
                        quantity: normalized.value,
                        unit: normalized.unit,
                        original: input,
                        hasCustomUnit: !foundKnownUnit && !UnitService.isValidUnit(unit)
                    }
                };
            }

            // Jeśli nie można znormalizować, zwróć oryginalne wartości
            return {
                success: true,
                product: {
                    name: this.cleanProductName(name),
                    quantity,
                    unit,
                    original: input,
                    hasCustomUnit: !foundKnownUnit && !UnitService.isValidUnit(unit)
                }
            };

        } catch (error) {
            return {
                success: false,
                error: error instanceof Error ? error.message : 'Nieznany błąd parsowania'
            };
        }
    }

    private static cleanInputString(input: string): string {
        return input
            .trim()
            .replace(/^[•\-]\s*/, '')  // Usuń znaki wypunktowania
            .replace(/\s+/g, ' ');     // Normalizuj spacje
    }

    private static extractQuantityAndUnit(input: string): {
        success: boolean;
        quantity?: number;
        potentialUnit?: string;
        remainingText?: string;
    } {
        const patterns = [
            // Pattern dla "pół kg mąki", "2-3 kg mąki", "1/2 kg mąki"
            /^(pół|półtorej|ćwierć|jedna|jeden|dwa|trzy|cztery|pięć|\d+(?:[.,]\d+)?(?:\s*-\s*\d+(?:[.,]\d+)?)?|\d+\/\d+|\d+\s+\d+\/\d+)\s*([^\d\s]+)?\s*(.+)?/i,
            // Pattern dla "mąka pół kg", "mąka 2-3 kg"
            /^(.+?)\s+(pół|półtorej|ćwierć|jedna|jeden|dwa|trzy|cztery|pięć|\d+(?:[.,]\d+)?(?:\s*-\s*\d+(?:[.,]\d+)?)?|\d+\/\d+|\d+\s+\d+\/\d+)\s*([^\d\s]+)?$/i,
        ];

        for (const pattern of patterns) {
            const match = input.match(pattern);
            if (match) {
                let quantityStr: string;
                let potentialUnit: string | undefined;
                let remainingText: string | undefined;

                if (pattern === patterns[0]) {
                    // Format "2-3 kg mąki"
                    quantityStr = match[1];
                    potentialUnit = match[2]?.toLowerCase();
                    remainingText = match[3];
                } else {
                    // Format "mąka 2-3 kg"
                    quantityStr = match[2];
                    potentialUnit = match[3]?.toLowerCase();
                    remainingText = match[1];
                }

                const quantity = QuantityParser.parseQuantity(quantityStr);
                if (quantity === null) continue;

                if (!remainingText && potentialUnit) {
                    remainingText = potentialUnit;
                    potentialUnit = undefined;
                }

                return {
                    success: true,
                    quantity,
                    potentialUnit,
                    remainingText: remainingText?.trim()
                };
            }
        }

        return {success: false};
    }

    private static processUnitAndName(potentialUnit: string | undefined, remainingText: string): {
        unit: string;
        name: string;
        foundKnownUnit: boolean;
    } {
        let unit = '';
        let name = remainingText;
        let foundKnownUnit = false;

        // Najpierw sprawdź potencjalną jednostkę
        if (potentialUnit) {
            const unitInfo = UnitService.detectUnitInText(potentialUnit);
            if (unitInfo.match) {
                unit = unitInfo.unit;
                foundKnownUnit = true;
            }
        }

        // Jeśli nie znaleziono jednostki w potencjalnej jednostce,
        // sprawdź w pozostałym tekście
        if (!foundKnownUnit && remainingText) {
            const unitInfo = UnitService.detectUnitInText(remainingText);
            if (unitInfo.match) {
                unit = unitInfo.unit;
                foundKnownUnit = true;
                // Usuń jednostkę z nazwy produktu
                name = remainingText.replace(new RegExp(`\\b${unit}\\b`, 'i'), '').trim();
            }
        }

        // Ustaw jednostkę domyślną, tylko jeśli naprawdę nie znaleziono żadnej innej
        if (!unit || unit === '') {
            unit = 'szt';
            foundKnownUnit = true;
        }

        return {unit, name, foundKnownUnit};
    }

    static cleanProductName(name: string): string {
        return name
            .replace(/\s*\([^)]+\)/g, '')  // Usuń nawiasy z zawartością
            .replace(/\s+/g, ' ')           // Normalizuj spacje
            .trim()
            .toLowerCase();                 // Konwertuj na małe litery dla spójności
    }

    static calculateSimilarity(product1: string, product2: string): number {
        const name1 = this.cleanProductName(product1);
        const name2 = this.cleanProductName(product2);

        // Użyj algorytmu Levenshtein distance do porównania nazw
        const distance = this.calculateLevenshteinDistance(name1, name2);
        return 1 - (distance / Math.max(name1.length, name2.length));
    }

    private static calculateLevenshteinDistance(str1: string, str2: string): number {
        const matrix: number[][] = [];

        for (let i = 0; i <= str1.length; i++) {
            matrix[i] = [i];
        }

        for (let j = 0; j <= str2.length; j++) {
            matrix[0][j] = j;
        }

        for (let i = 1; i <= str1.length; i++) {
            for (let j = 1; j <= str2.length; j++) {
                if (str1[i - 1] === str2[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = Math.min(
                        matrix[i - 1][j - 1] + 1,
                        matrix[i][j - 1] + 1,
                        matrix[i - 1][j] + 1
                    );
                }
            }
        }

        return matrix[str1.length][str2.length];
    }

    private static isNumeric(str: string): boolean {
        return !isNaN(parseFloat(str)) && isFinite(Number(str));
    }
}