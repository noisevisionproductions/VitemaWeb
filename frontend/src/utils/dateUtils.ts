import {Timestamp} from "firebase/firestore";

export const getTimestamp = (date: any): number => {
    if (!date) return 0;

    // Obsługa obiektów Timestamp z Firebase
    if (date instanceof Timestamp) {
        return date.toMillis();
    }

    if (date._seconds !== undefined) {
        return date._seconds * 1000 + (date._nanoseconds || 0) / 1000000;
    }

    // Jeśli to już liczba (timestamp), zwróć ją
    if (typeof date === 'number') {
        return date;
    }

    // Obsługa obiektu Timestamp z Google Cloud
    if (date.seconds !== undefined) {
        return date.seconds * 1000 + (date.nanos || 0) / 1000000;
    }

    // Ostatnia próba - konwersja z string lub Date na timestamp
    try {
        const dateObj = date instanceof Date ? date : new Date(date);
        return dateObj.getTime();
    } catch {
        console.warn('Could not parse date:', date);
        return 0;
    }
};