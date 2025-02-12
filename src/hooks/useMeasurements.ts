import {useEffect, useState} from "react";
import {BodyMeasurements} from "../types/measurements";
import {collection, getDocs, orderBy, query, where} from "firebase/firestore";
import {db} from "../config/firebase";
import {toast} from "sonner";

export const useMeasurements = (userId: string) => {
    const [measurements, setMeasurements] = useState<BodyMeasurements[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    const fetchMeasurements = async () => {
        try {
            setLoading(true);
            setError(null);

            const measurementsRef = collection(db, 'bodyMeasurements');
            const q = query(
                measurementsRef,
                where('userId', '==', userId),
                orderBy('date', 'desc')
            );

            const snapshot = await getDocs(q);
            const measurementsData = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data()
            })) as BodyMeasurements[];

            setMeasurements(measurementsData);
        } catch (error) {
            console.error('Error fetching measurements:', error);
            setError(error as Error);
            toast.error('Błąd podczas pobierania pomiarów użytkownika');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (userId) {
            fetchMeasurements().catch(console.error);
        }
    }, [userId]);

    return {
        measurements,
        loading,
        error,
        refetch: fetchMeasurements
    };
};