import {Timestamp} from "firebase/firestore";

export const formatDate = (date: string | Timestamp) => {
    let dateObject: Date;

    if (date instanceof Timestamp) {
        dateObject = date.toDate();
    } else {
        const [day, month, year] = date.split('-').map(Number);
        dateObject = new Date(year, month - 1, day);
    }

    return dateObject.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

export const formatTimestamp = (timestamp: number | Date | Timestamp) => {
    let date: Date;

    if (timestamp instanceof Date) {
        date = timestamp;
    } else if (typeof timestamp === 'number') {
        date = new Date(timestamp);
    } else {
        date = timestamp.toDate();
    }

    return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

export const formatDateForDateInput = (date: string | Timestamp): string => {
    let dateObject: Date;

    if (date instanceof Timestamp) {
        dateObject = date.toDate();
    } else {
        const [day, month, year] = date.split('-').map(Number);
        dateObject = new Date(year, month - 1, day);
    }

    return dateObject.toISOString().split('T')[0];
};

export const formatDateFromInput = (dateString: string): string => {
    if (!dateString) return '';

    const date = new Date(dateString);
    return `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
};

export const calculateAge = (birthDate: number | null): number => {
    if (!birthDate) return 0;

    const today = new Date();
    const birthDateObj = new Date(birthDate);

    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDiff = today.getMonth() - birthDateObj.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }

    return age;
};