import {Timestamp} from "firebase/firestore";

export const getTimestamp = (date: any): number => {
    if (!date) return 0;

    if (date instanceof Timestamp) {
        return date.toMillis();
    }

    if (date._seconds !== undefined) {
        return date._seconds * 1000 + (date._nanoseconds || 0) / 1000000;
    }

    if (typeof date === 'number') {
        return date;
    }

    try {
        return new Date(date).getTime();
    } catch {
        return 0;
    }
};