export class QuantityParser {
    private static readonly PREFIX_MULTIPLIERS: Record<string, number> = {
        'pół': 0.5,
        'półtorej': 1.5,
        'ćwierć': 0.25,
        'półtora': 1.5,
        'jedna': 1,
        'jeden': 1,
        'dwa': 2,
        'trzy': 3,
        'cztery': 4,
        'pięć': 5
    };

    static parseQuantity(input: string): number | null {
        const cleanInput = input.trim().toLowerCase();

        // Sprawdź przedrostki
        for (const [prefix, multiplier] of Object.entries(this.PREFIX_MULTIPLIERS)) {
            if (cleanInput.startsWith(prefix)) {
                return multiplier;
            }
        }

        // Obsługa zakresów (np. "2-3")
        const rangeMatch = cleanInput.match(/^(\d+(?:[.,]\d+)?)\s*-\s*(\d+(?:[.,]\d+)?)$/);
        if (rangeMatch) {
            const min = parseFloat(rangeMatch[1].replace(',', '.'));
            const max = parseFloat(rangeMatch[2].replace(',', '.'));
            return (min + max) / 2; // Bierzemy średnią
        }

        // Obsługa ułamków (np. "1/2")
        const fractionMatch = cleanInput.match(/^(\d+)\/(\d+)$/);
        if (fractionMatch) {
            return Number(fractionMatch[1]) / Number(fractionMatch[2]);
        }

        // Obsługa liczb mieszanych (np. "1 1/2")
        const mixedMatch = cleanInput.match(/^(\d+)\s+(\d+)\/(\d+)$/);
        if (mixedMatch) {
            return Number(mixedMatch[1]) + Number(mixedMatch[2]) / Number(mixedMatch[3]);
        }

        // Standardowa konwersja z obsługą przecinka
        const numberMatch = cleanInput.match(/^(\d+(?:[.,]\d+)?)$/);
        if (numberMatch) {
            return parseFloat(numberMatch[1].replace(',', '.'));
        }

        return null;
    }
}