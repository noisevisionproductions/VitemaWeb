import {useState, useEffect} from 'react';
import {Diet} from '../../types';
import {User} from '../../types/user';
import {DietService} from "../../services/diet/DietService";
import {useToast} from "../../contexts/ToastContext";

export interface DietWithUser extends Diet {
    userEmail?: string;
}

export const useDiets = (_users: User[], usersLoading: boolean) => {
    const {showToast} = useToast();
    const [diets, setDiets] = useState<DietWithUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    const fetchDiets = async () => {
        try {
            setLoading(true);
            const response = await DietService.getDiets();
            setDiets(response);
        } catch (error) {
            console.error('Error fetching diets:', error);
            showToast('Błąd podczas pobierania diet', 'error');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (usersLoading) return;
        fetchDiets().catch(console.error);
    }, [usersLoading, page, size]);

    const deleteDiet = async (id: string) => {
        try {
            setDiets(prevDiets => prevDiets.filter(diet => diet.id !== id));
            await DietService.deleteDiet(id);
            showToast('Dieta została usunięta', 'success');
        } catch (error) {
            console.error('Diet deletion failed:', error);
            showToast('Błąd podczas usuwania diety', 'error');
            fetchDiets().catch(console.error);
            throw error;
        }
    };

    const updateDiet = async (id: string, dietData: Partial<Diet>) => {
        try {
            await DietService.updateDiet(id, dietData);
            showToast('Dieta została zaktualizowana', 'success');
            await fetchDiets();
        } catch (error) {
            console.error('Error updating diet:', error);
            showToast('Błąd podczas aktualizacji diety', 'error');
        }
    };

    const createDiet = async (dietData: Omit<Diet, 'id'>) => {
        try {
            const response = await DietService.createDiet(dietData);
            showToast('Dieta została utworzona', 'success');
            await fetchDiets();
            return response;
        } catch (error) {
            console.error('Error creating diet:', error);
            showToast('Błąd podczas tworzenia diety', 'error');
            throw error;
        }
    };

    return {
        diets,
        loading,
        page,
        setPage,
        deleteDiet,
        updateDiet,
        createDiet,
        refreshDiets: fetchDiets
    };
};