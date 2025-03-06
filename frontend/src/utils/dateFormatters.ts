import {Timestamp} from 'firebase/firestore';
import {User} from "../types/user";

/**
 * Uniwersalna funkcja do formatowania dat w aplikacji
 */
interface FirestoreTimestamp {
    _seconds: number;
    _nanoseconds: number;
}

export const formatTimestamp = (timestamp: number | Date | Timestamp | FirestoreTimestamp | string) => {
    let date: Date;

    if (timestamp instanceof Date) {
        date = timestamp;
    } else if (typeof timestamp === 'number') {
        date = new Date(timestamp);
    } else if (timestamp instanceof Timestamp) {
        date = timestamp.toDate();
    } else if (typeof timestamp === 'string') {
        // Handle string ISO and dd-mm-yyyy format
        if (timestamp.includes('-')) {
            const parts = timestamp.split('-');
            if (parts.length === 3 && parts[0].length === 2) {
                // dd-mm-yyyy format
                const [day, month, year] = parts.map(Number);
                date = new Date(year, month - 1, day);
            } else {
                // ISO format
                date = new Date(timestamp);
            }
        } else {
            date = new Date(timestamp);
        }
    } else if (timestamp && typeof timestamp === 'object') {
        // Handle Firestore timestamp format
        if ('_seconds' in timestamp && '_nanoseconds' in timestamp) {
            const firestoreTimestamp = timestamp as FirestoreTimestamp;
            date = new Date(firestoreTimestamp._seconds * 1000);
        } else if ('seconds' in timestamp && 'nanoseconds' in timestamp) {
            date = new Date((timestamp as any).seconds * 1000);
        } else {
            console.warn('Invalid timestamp format:', timestamp);
            date = new Date();
        }
    } else {
        console.warn('Invalid timestamp format:', timestamp);
        date = new Date();
    }

    if (isNaN(date.getTime())) {
        console.warn('Invalid date created from timestamp:', timestamp);
        date = new Date();
    }

    return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

export const toFirestoreTimestamp = (date: Date | string | number | Timestamp): Timestamp => {
    if (date instanceof Timestamp) {
        return date;
    }
    if (date instanceof Date) {
        return Timestamp.fromDate(date);
    }
    if (typeof date === 'string') {
        return Timestamp.fromDate(new Date(date));
    }
    return Timestamp.fromMillis(date);
};

/**
 * Konwersja Timestamp na format ISO (YYYY-MM-DD)
 */
export const toISODate = (timestamp: Timestamp | Date | string | number | null | undefined): string => {
    if (!timestamp) {
        return '';
    }

    let date: Date;

    try {
        if (timestamp instanceof Timestamp) {
            date = timestamp.toDate();
        } else if (timestamp instanceof Date) {
            date = timestamp;
        } else if (typeof timestamp === 'string') {
            date = new Date(timestamp);
        } else if (typeof timestamp === 'object' && '_seconds' in timestamp) {
            const firestoreTimestamp = timestamp as any;
            date = new Date(firestoreTimestamp._seconds * 1000);
        } else {
            date = new Date(timestamp as any);
        }

        if (isNaN(date.getTime())) {
            console.warn('Invalid date created from timestamp:', timestamp);
            return '';
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    } catch (error) {
        console.error('Error converting timestamp to ISO date:', error, timestamp);
        return '';
    }
};

export const formatMonthYear = (date: Date): string => {
    return `${date.getMonth() + 1}/${date.getFullYear()}`;
};

export const stringToTimestamp = (dateString: string) => {
    const date = new Date(dateString);
    return Timestamp.fromDate(date);
};

export const calculateAge = (birthDate: Timestamp | number | null): number => {
    if (!birthDate) return 0;

    const today = new Date();
    let birthDateObj: Date;

    if (birthDate instanceof Timestamp) {
        birthDateObj = birthDate.toDate();
    } else {
        birthDateObj = new Date(birthDate);
    }

    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDiff = today.getMonth() - birthDateObj.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }

    return age;
};

export const displayAge = (user: User): string => {
    if (user.storedAge && user.storedAge > 0) {
        return `${user.storedAge} lat`;
    }

    const calculatedAge = calculateAge(user.birthDate);
    return calculatedAge > 0 ? `${calculatedAge} lat` : 'Nie podano';
};