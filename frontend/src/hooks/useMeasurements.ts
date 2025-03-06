import {useEffect, useState} from "react";
import {BodyMeasurements} from "../types/measurements";
import {toast} from "sonner";
import {MeasurementsService} from "../services/MesaurementsService";

export const useMeasurements = (userId: string) => {
    const [measurements, setMeasurements] = useState<BodyMeasurements[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    const fetchMeasurements = async () => {
        try {
            setLoading(true);
            setError(null);

            const measurementsData = await MeasurementsService.getUserMeasurements(userId);
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

    const deleteMeasurement = async (id: string) => {
        try {
            await MeasurementsService.deleteMeasurement(id);
            setMeasurements(prev => prev.filter(m => m.id !== id));
            toast.success('Pomiar został usunięty');
        } catch (error) {
            console.error('Error deleting measurement:', error);
            toast.error('Błąd podczas usuwania pomiaru');
            throw error;
        }
    };

    return {
        measurements,
        loading,
        error,
        refetch: fetchMeasurements,
        deleteMeasurement
    };
};