// Funkcje pomocnicze do odmiany rzeczowników przez liczby
export const getPolishDayForm = (count: number): string => {
    if (count === 1) return "dzień";

    // Dla 2, 3, 4 oprócz 12, 13, 14
    if ((count % 10 >= 2 && count % 10 <= 4) && (count % 100 < 10 || count % 100 > 20)) {
        return "dni";
    }

    return "dni";
};

export const getPolishMealForm = (count: number): string => {
    if (count === 1) return "posiłek";

    // Dla 2, 3, 4 oprócz 12, 13, 14
    if ((count % 10 >= 2 && count % 10 <= 4) && (count % 100 < 10 || count % 100 > 20)) {
        return "posiłki";
    }

    return "posiłków";
};

export const getPolishProductForm = (count: number): string => {
    if (count === 1) return "produkt";

    // Dla 2, 3, 4 oprócz 12, 13, 14
    if ((count % 10 >= 2 && count % 10 <= 4) && (count % 100 < 10 || count % 100 > 20)) {
        return "produkty";
    }

    return "produktów";
};