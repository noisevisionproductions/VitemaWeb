import {Timestamp} from 'firebase/firestore';

/**
 * Reprezentuje różne formaty timestamp w aplikacji
 */
export type TimestampLike =
    | Timestamp
    | Date
    | number
    | string
    | { seconds: number; nanoseconds: number }
    | { seconds: number; nanos: number }
    | { _seconds: number; _nanoseconds: number };

/**
 * Konwertuje różne formaty timestamp na obiekt Date
 * Obsługuje Firestore Timestamp, Date, liczby (milliseconds),
 * stringi oraz obiekty z różnymi formatami timestamp
 */
export const timestampToDate = (timestamp: TimestampLike | null | undefined): Date | null => {
    if (!timestamp) {
        return null;
    }

    try {
        // Obsługa obiektu Timestamp z Firebase
        if (timestamp instanceof Timestamp) {
            return timestamp.toDate();
        }

        // Obsługa obiektu Date
        if (timestamp instanceof Date) {
            return timestamp;
        }

        // Obsługa string (ISO format lub inny)
        if (typeof timestamp === 'string') {
            const date = new Date(timestamp);
            return isNaN(date.getTime()) ? null : date;
        }

        // Obsługa liczby (milliseconds)
        if (typeof timestamp === 'number') {
            return new Date(timestamp);
        }

        // Obsługa różnych formatów obiektów timestamp
        if (typeof timestamp === 'object') {
            // Format z Firebase Admin SDK: { _seconds, _nanoseconds }
            if ('_seconds' in timestamp) {
                return new Date((timestamp as any)._seconds * 1000);
            }

            // Format z Firebase Client SDK: { seconds, nanoseconds }
            if ('seconds' in timestamp && 'nanoseconds' in timestamp) {
                return new Date((timestamp as any).seconds * 1000);
            }

            // Alternatywny format: { seconds, nanos }
            if ('seconds' in timestamp && 'nanos' in timestamp) {
                return new Date((timestamp as any).seconds * 1000);
            }
        }

        // Nie rozpoznany format
        console.warn('Nieobsługiwany format timestamp:', timestamp);
        return null;
    } catch (error) {
        console.error('Błąd podczas konwersji timestamp:', error, timestamp);
        return null;
    }
};

/**
 * Formatuje datę z backendu (obsługuje zarówno nowy format ISO String, jak i stary Array)
 */
export const formatPostgresTimestamp = (postgresTimestamp: any): string => {
    if (!postgresTimestamp) return '-';

    try {
        if (typeof postgresTimestamp === 'string') {
            const date = new Date(postgresTimestamp);
            if (isNaN(date.getTime())) return postgresTimestamp;

            return date.toLocaleString('pl-PL', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        if (Array.isArray(postgresTimestamp) && postgresTimestamp.length >= 6) {
            const [year, month, day, hour, minute, second] = postgresTimestamp;
            const date = new Date(year, month - 1, day, hour, minute, second);
            return date.toLocaleString('pl-PL', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        return String(postgresTimestamp);
    } catch (error) {
        console.error('Error formatting timestamp:', error);
        return '-';
    }
};

/**
 * NOWA FUNKCJA: Inteligentne parsowanie dla danych z zewnętrznych systemów (np. Android/iOS),
 * które mogą mieszać sekundy z milisekundami.
 * Używaj tego do: Pomiary, Logi aktywności, Daty utworzenia rekordów.
 * NIE UŻYWAJ do: Dat urodzenia (chyba że masz pewność).
 */
export const parseFlexibleDate = (timestamp: TimestampLike | null | undefined): Date | null => {
    if (!timestamp) return null;

    let date = timestampToDate(timestamp);
    if (!date) return null;

    const year = date.getFullYear();

    let originalNumber: number | null = null;
    if (typeof timestamp === 'number') originalNumber = timestamp;
    if (typeof timestamp === 'string' && !isNaN(Number(timestamp))) originalNumber = Number(timestamp);

    if (year === 1970 && originalNumber && originalNumber > 1000000000) {
        return new Date(originalNumber * 1000);
    }

    return date;
};

/**
 * Konwertuje różne formaty timestamp na Firestore Timestamp
 */
export const toFirestoreTimestamp = (timestamp: TimestampLike | null | undefined): Timestamp | null => {
    if (!timestamp) {
        return null;
    }

    if (timestamp instanceof Timestamp) {
        return timestamp;
    }

    const date = timestampToDate(timestamp);
    if (!date) {
        return null;
    }

    return Timestamp.fromDate(date);
};

/**
 * Formatuje timestamp do czytelnego formatu lokalnego
 */
export const formatTimestamp = (
    timestamp: TimestampLike | null | undefined,
    includeTime: boolean = false
): string => {
    const date = timestampToDate(timestamp);

    if (!date) {
        return 'Nieprawidłowa data';
    }

    const options: Intl.DateTimeFormatOptions = {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };

    if (includeTime) {
        options.hour = '2-digit';
        options.minute = '2-digit';
    }

    return date.toLocaleDateString('pl-PL', options);
};

export const formatMonthYear = (date: Date): string => {
    return `${date.getMonth() + 1}/${date.getFullYear()}`;
};

export const stringToTimestamp = (dateString: string) => {
    const date = new Date(dateString);
    return Timestamp.fromDate(date);
};

/**
 * Konwertuje timestamp na format ISO (YYYY-MM-DD)
 */
export const toISODate = (timestamp: TimestampLike | null | undefined): string => {
    const date = timestampToDate(timestamp);

    if (!date) {
        return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};

/**
 * Oblicza wiek na podstawie daty urodzenia
 */
export const calculateAge = (birthDate: TimestampLike | null | undefined): number => {
    if (!birthDate) return 0;

    const birthDateObj = timestampToDate(birthDate);
    if (!birthDateObj) return 0;

    const today = new Date();
    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDiff = today.getMonth() - birthDateObj.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }

    return age;
};

export const formatAge = (
    birthDate: TimestampLike | null | undefined,
    emptyLabel: string = '-'
): string => {
    const age = calculateAge(birthDate);
    return age > 0 ? `${age} lat` : emptyLabel;
};