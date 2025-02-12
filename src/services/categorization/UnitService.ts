import { ProductUnit} from "../../types/product";

export class UnitService {
    private static readonly UNITS: ProductUnit[] = [
        // Jednostki wagi
        {
            value: 'g',
            label: 'gram',
            type: 'weight',
            baseUnit: 'g',
            conversionFactor: 1
        },
        {
            value: 'kg',
            label: 'kilogram',
            type: 'weight',
            baseUnit: 'g',
            conversionFactor: 1000
        },
        {
            value: 'dag',
            label: 'dekagram',
            type: 'weight',
            baseUnit: 'g',
            conversionFactor: 10
        },

        // Jednostki objętości
        {
            value: 'ml',
            label: 'mililitr',
            type: 'volume',
            baseUnit: 'ml',
            conversionFactor: 1
        },
        {
            value: 'l',
            label: 'litr',
            type: 'volume',
            baseUnit: 'ml',
            conversionFactor: 1000
        },

        // Jednostki sztukowe
        {
            value: 'szt',
            label: 'sztuka',
            type: 'piece'
        },
        {
            value: 'opak',
            label: 'opakowanie',
            type: 'piece'
        },

        // Jednostki kuchenne
        {
            value: 'łyżka',
            label: 'łyżka',
            type: 'kitchen',
            baseUnit: 'ml',
            conversionFactor: 15
        },
        {
            value: 'łyżeczka',
            label: 'łyżeczka',
            type: 'kitchen',
            baseUnit: 'ml',
            conversionFactor: 5
        },
        {
            value: 'szklanka',
            label: 'szklanka',
            type: 'kitchen',
            baseUnit: 'ml',
            conversionFactor: 250
        },
        {
            value: 'garść',
            label: 'garść',
            type: 'kitchen',
            baseUnit: 'g',
            conversionFactor: 30
        }
    ];

    private static readonly UNIT_PATTERNS = [
        // Waga
        {
            pattern: /\b(\d+(?:[.,]\d+)?)\s*(kg|g|dag|dkg)\b/i,
            type: 'weight'
        },
        // Objętość
        {
            pattern: /\b(\d+(?:[.,]\d+)?)\s*(ml|l|litr(?:y|ów)?)\b/i,
            type: 'volume'
        },
        // Sztuki
        {
            pattern: /\b(\d+(?:[.,]\d+)?)\s*(szt|sztuk[ia]?|opak(?:owanie)?)\b/i,
            type: 'piece'
        },
        // Miary kuchenne
        {
            pattern: /\b(\d+(?:[.,]\d+)?)\s*(łyżk[ai]|łyżeczk[ai]|szklank[ai]|garść|garści)\b/i,
            type: 'kitchen'
        }
    ];

    private static readonly UNIT_ALIASES: Record<string, string> = {
        // Waga
        'gram': 'g',
        'gramów': 'g',
        'kilogram': 'kg',
        'kilogramów': 'kg',
        'dekagram': 'dag',
        'dekagramów': 'dag',
        'deko': 'dag',
        'dkg': 'dag',

        // Objętość
        'mililitr': 'ml',
        'mililitrów': 'ml',
        'litr': 'l',
        'litrów': 'l',

        // Opakowania
        'sztuka': 'szt',
        'sztuk': 'szt',
        'opakowanie': 'opak',
        'opakowań': 'opak',
        'opakowania': 'opak',
        'op.': 'opak',

        // Miary kuchenne
        'łyżka': 'łyżka',
        'łyżek': 'łyżka',
        'łyżki': 'łyżka',
        'łyżeczka': 'łyżeczka',
        'łyżeczek': 'łyżeczka',
        'łyżeczki': 'łyżeczka',
        'szklanka': 'szklanka',
        'szklanek': 'szklanka',
        'szklanki': 'szklanka',
        'garść': 'garść',
        'garści': 'garść'
    };

    static getUnit(value: string): ProductUnit | undefined {
        return this.UNITS.find(unit => unit.value === value);
    }

    static convertToBaseUnit(value: number, fromUnit: string): number | null {
        const unit = this.getUnit(fromUnit);
        if (!unit || !unit.conversionFactor || !unit.baseUnit) {
            return null;
        }
        return value * unit.conversionFactor;
    }

    static isValidUnit(unit: string): boolean {
        return this.UNITS.some(u => u.value === unit);
    }

    static normalizeToBaseUnit(value: number, unit: string): { value: number; unit: string } | null {
        const unitInfo = this.getUnit(unit);
        if (!unitInfo || !unitInfo.baseUnit || !unitInfo.conversionFactor) {
            return null;
        }

        return {
            value: value * unitInfo.conversionFactor,
            unit: unitInfo.baseUnit
        };
    }

    static detectUnitInText(text: string): {
        unit: string;
        type: ProductUnit['type'];
        match: boolean;
    } {
        // Najpierw sprawdź wzorce
        for (const {pattern} of this.UNIT_PATTERNS) {
            const match = text.match(pattern);
            if (match) {
                const detectedUnit = match[2].toLowerCase();
                const normalizedUnit = this.normalizeUnitAlias(detectedUnit);
                // Sprawdź, czy znormalizowana jednostka jest poprawna
                const validUnit = this.getUnit(normalizedUnit);
                if (validUnit) {
                    return {
                        unit: normalizedUnit,
                        type: validUnit.type,
                        match: true
                    };
                }
            }
        }

        // Sprawdź bezpośrednio alias
        const normalizedFromAlias = this.normalizeUnitAlias(text.toLowerCase());
        const validUnitFromAlias = this.getUnit(normalizedFromAlias);
        if (validUnitFromAlias) {
            return {
                unit: normalizedFromAlias,
                type: validUnitFromAlias.type,
                match: true
            };
        }

        // Jeśli nic nie znaleziono, zwróć null lub obiekt wskazujący na brak dopasowania
        return {
            unit: '',  // Zmiana z 'szt' na pusty string
            type: 'piece',
            match: false
        };
    }

    static normalizeUnitAlias(unit: string): string {
        return this.UNIT_ALIASES[unit.toLowerCase()] || unit.toLowerCase();
    }

    // Nowa metoda do sprawdzania, czy można bezpiecznie połączyć ilości
    static canCombineQuantities(unit1: string, unit2: string): boolean {
        const unitInfo1 = this.getUnit(unit1);
        const unitInfo2 = this.getUnit(unit2);

        if (!unitInfo1 || !unitInfo2) return false;

        // Jeśli jednostki są takie same
        if (unit1 === unit2) return true;

        if (!unitInfo1.baseUnit || !unitInfo2.baseUnit) return false;

        return unitInfo1.baseUnit === unitInfo2.baseUnit;
    }

    // Nowa metoda do łączenia ilości z różnymi jednostkami
    static combineQuantities(value1: number, unit1: string, value2: number, unit2: string): {
        value: number;
        unit: string;
    } | null {
        if (!this.canCombineQuantities(unit1, unit2)) {
            return null;
        }

        const unitInfo1 = this.getUnit(unit1);
        const unitInfo2 = this.getUnit(unit2);

        if (!unitInfo1 || !unitInfo2) {
            return null;
        }

        // Jeśli jednostki są takie same
        if (unit1 === unit2) {
            return { value: value1 + value2, unit: unit1 };
        }

        // Jeśli mają tę samą jednostkę bazową, konwertuj do niej
        if (unitInfo1.baseUnit && unitInfo2.baseUnit && unitInfo1.baseUnit === unitInfo2.baseUnit) {
            const baseValue1 = this.convertToBaseUnit(value1, unit1);
            const baseValue2 = this.convertToBaseUnit(value2, unit2);

            if (baseValue1 === null || baseValue2 === null) {
                return null;
            }

            return {
                value: baseValue1 + baseValue2,
                unit: unitInfo1.baseUnit
            };
        }

        return null;
    }
}