import { useState, useEffect } from 'react';
import {Diet, Recipe, ShoppingListV3} from '../types';
import { toast } from 'sonner';
import { useRecipes } from './useRecipes';
import { useShoppingList } from './shopping/useShoppingList';
import {DietService} from "../services/DietService";
import axios from "axios";

interface UseDietEditorReturn {
    diet: Diet | null;
    recipes: { [key: string]: Recipe };
    shoppingList: ShoppingListV3 | null;
    loading: boolean;
    error: Error | null;
    updateDiet: (updatedDiet: Partial<Diet>) => Promise<void>;
    refreshDiets: () => Promise<void>;
}

export const useDietEditor = (dietId: string): UseDietEditorReturn => {
    const [diet, setDiet] = useState<Diet | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    const { recipes, isLoadingRecipes } = useRecipes(diet?.days || []);
    const { shoppingList, loading: loadingShoppingList } = useShoppingList(dietId);

    useEffect(() => {
        const fetchDiet = async () => {
            try {
                setLoading(true);
                const dietData = await DietService.getDietById(dietId);
                setDiet(dietData);
            } catch (err) {
                console.error('Błąd podczas pobierania diety:', err);
                setError(err as Error);
                toast.error('Nie udało się pobrać diety');
            } finally {
                setLoading(false);
            }
        };

        if (dietId) {
            void fetchDiet();
        }
    }, [dietId]);

    const updateDiet = async (updatedDiet: Partial<Diet>) => {
        try {
            if (!diet) {
                toast.error('Brak diety do aktualizacji');
                return;
            }

            const updatedData = await DietService.updateDiet(diet.id, {
                ...diet,
                ...updatedDiet
            });

            setDiet(updatedData);
            toast.success('Dieta została zaktualizowana');
        } catch (err) {
            console.error('Błąd podczas aktualizacji diety:', err);
            toast.error('Nie udało się zaktualizować diety');
            throw err;
        }
    };

    const refreshDiets = async () => {
        if (!dietId) return;

        try {
            setLoading(true);
            const dietData = await DietService.getDietById(dietId);
            setDiet(dietData);
        } catch (err) {
            if (axios.isAxiosError(err) && err.response?.status === 404) {
                console.log('Dieta nie istnieje (prawdopodobnie została usunięta)');
                setDiet(null);
            } else {
                console.error('Błąd podczas odświeżania diety:', err);
                setError(err as Error);
            }
        } finally {
            setLoading(false);
        }
    };

    return {
        diet,
        recipes,
        shoppingList,
        loading: loading || isLoadingRecipes || loadingShoppingList,
        error,
        updateDiet,
        refreshDiets
    };
};