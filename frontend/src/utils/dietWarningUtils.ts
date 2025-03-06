import {Diet} from "../types";
import {Timestamp} from "firebase/firestore";

export type WarningStatus = 'critical' | 'warning' | 'normal';

// Funkcja pomocnicza do obsługi różnych formatów timestamp
export function getTimestampSeconds(timestamp: any): number {
    if (!timestamp) return 0;

    if (timestamp.seconds !== undefined) {
        return timestamp.seconds;
    }

    if (timestamp._seconds !== undefined) {
        return timestamp._seconds;
    }

    if (typeof timestamp === 'string') {
        try {
            const parsed = JSON.parse(timestamp);
            return parsed._seconds || parsed.seconds || 0;
        } catch {
            return 0;
        }
    }

    return 0;
}

/*
* Funkcja oblicza liczbę dni między dwiema datami
* */
export function calculateDaysBetween(date1: Date, date2: Date): number {
    const utc1 = Date.UTC(date1.getFullYear(), date1.getMonth(), date1.getDate());
    const utc2 = Date.UTC(date2.getFullYear(), date2.getMonth(), date2.getDate());

    const MS_PER_DAY = 1000 * 60 * 60 * 24;
    return Math.floor((utc2 - utc1) / MS_PER_DAY);
}

/*
* Funkcja zwraca ostatni dzień diety
* */

export function getLastDayOfDiet(diet: Diet): Timestamp | null {
    if (!diet.days || diet.days.length === 0) {
        return null;
    }

    // Filtrujemy dni bez daty
    const daysWithDate = diet.days.filter(day => day.date);
    if (daysWithDate.length === 0) {
        return null;
    }

    // Sortujemy dni według daty
    const sortedDays = [...daysWithDate].sort((a, b) => {
        return getTimestampSeconds(a.date) - getTimestampSeconds(b.date);
    });

    return sortedDays[sortedDays.length - 1].date;
}

/*
* Funkcja zwraca pierwszy dzień diety
* */
export function getFirstDayOfDiet(diet: Diet): Timestamp | null {
    if (!diet.days || diet.days.length === 0) {
        return null;
    }

    const sortedDays = [...diet.days].sort((a, b) => {
        if (!a.date || !b.date) return 0;
        return a.date.seconds - b.date.seconds;
    });

    return sortedDays[0].date;
}

/*
* Funkcja zwraca liczbę dni pozostałych do końca diety
* */
export function getDaysRemainingToDietEnd(diet: Diet): number {
    const lastDayTimestamp = getLastDayOfDiet(diet);

    if (!lastDayTimestamp) {
        return -1;
    }

    try {
        const seconds = getTimestampSeconds(lastDayTimestamp);

        if (!seconds) {
            return -1;
        }

        const today = new Date();
        const lastDay = new Date(seconds * 1000);

        if (isNaN(lastDay.getTime())) {
            return -1;
        }

        today.setHours(0, 0, 0, 0);
        lastDay.setHours(0, 0, 0, 0);

        // Jeśli ostatni dzień jest w przeszłości, zwracamy -1
        if (lastDay < today) {
            return -1;
        }

        return calculateDaysBetween(today, lastDay);
    } catch (error) {
        console.error(`Diet ${diet.id}: Error calculating days remaining:`, error);
        return -1;
    }
}

/*
* Funkcja określa status ostrzeżenia dla diety na podstawie pozostałych dni
* */
export function getDietWarningStatus(diet: Diet): WarningStatus {
    const daysRemaining = getDaysRemainingToDietEnd(diet);

    if (daysRemaining <= 1 && daysRemaining >= 0) {
        return 'critical';
    } else if (daysRemaining <= 3 && daysRemaining > 0) {
        return 'warning';
    } else {
        return 'normal';
    }
}

/*
* Funkcja sprawdza, czy dieta jest już zakończona
* */
export function isDietEnded(diet: Diet): boolean {
    const daysRemaining = getDaysRemainingToDietEnd(diet);
    return daysRemaining < 0;
}

/*
* Funkcja zwraca tekst opisujący status ostrzeżenia
* */
export function getWarningStatusText(diet: Diet): string {
    const daysRemaining = getDaysRemainingToDietEnd(diet);

    if (daysRemaining < 0) {
        return 'Zakończona';
    } else if (daysRemaining === 0) {
        return 'Kończy się dzisiaj!';
    } else if (daysRemaining === 1) {
        return 'Kończy się jutro!';
    } else if (daysRemaining <= 3) {
        return `Kończy się za ${daysRemaining} dni`;
    } else {
        return '';
    }
}