export interface PreliminaryValidationData {
    // Całkowita liczba posiłków znalezionych w pliku
    totalRows: number;

    // Flaga wskazująca czy plik przeszedł wstępną walidację
    isValid: boolean;

    // Flaga wskazująca czy znaleziono listę zakupów
    hasShoppingList: boolean;

    // Lista błędów znalezionych podczas wstępnej walidacji
    errors: Array<{
        row: number;
        errors: string[];
    }>;

    // Struktura posiłków wyekstrahowana z pliku
    structure: {
        meals: Array<{
            name: string;           // Nazwa posiłku
            preparation: string;    // Sposób przygotowania
            ingredients: string;    // Lista składników
            nutritionalValues: string; // Wartości odżywcze
        }>;
    };
}