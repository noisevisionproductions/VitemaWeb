import { useState, useEffect } from 'react';
import {doc, getDoc, Timestamp, updateDoc} from 'firebase/firestore';
import { db } from '../config/firebase';
import {Diet, Recipe, ShoppingListV3} from '../types';
import { toast } from 'sonner';
import { useRecipes } from './useRecipes';
import { useShoppingList } from './useShoppingList';

interface UseDietEditorReturn {
    diet: Diet | null;
    recipes: { [key: string]: Recipe };
    shoppingList: ShoppingListV3 | null;
    loading: boolean;
    error: Error | null;
    updateDiet: (updatedDiet: Partial<Diet>) => Promise<void>;
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
                const dietDoc = await getDoc(doc(db, 'diets', dietId));

                if (!dietDoc.exists()) {
                    setError(new Error('Dieta nie istnieje'));
                    toast.error('Dieta nie istnieje');
                    return;
                }

                const dietData = {
                    id: dietDoc.id,
                    ...dietDoc.data(),
                } as Diet;

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

            const dietRef = doc(db, 'diets', diet.id);
            const updatedData = {
                ...diet,
                ...updatedDiet,
                updatedAt: Timestamp.fromDate(new Date())
            };


            await updateDoc(dietRef, updatedData);
            setDiet(updatedData as Diet);
            toast.success('Dieta została zaktualizowana');
        } catch (err) {
            console.error('Błąd podczas aktualizacji diety:', err);
            toast.error('Nie udało się zaktualizować diety');
            throw err;
        }
    };

    return {
        diet,
        recipes,
        shoppingList,
        loading: loading || isLoadingRecipes || loadingShoppingList,
        error,
        updateDiet
    };
};