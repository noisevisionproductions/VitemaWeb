import { useState, useEffect } from 'react';
import { collection, getDocs, onSnapshot } from 'firebase/firestore';
import { db } from '../config/firebase';
import { Diet } from '../types';
import { toast } from 'sonner';
import { User } from '../types/user';

export interface DietWithUser extends Diet {
    userEmail?: string;
}

interface UsersMap {
    [key: string]: User;
}

export const useDiets = (users: User[], usersLoading: boolean) => {
    const [diets, setDiets] = useState<DietWithUser[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchDiets = async () => {
        try {
            const dietsCollection = collection(db, 'diets');
            const dietsSnapshot = await getDocs(dietsCollection);
            const dietsData = dietsSnapshot.docs.map(doc => {
                const data = doc.data();
                return {
                    id: doc.id,
                    ...data,
                    metadata: data.metadata || { totalDays: data.days?.length || 0 },
                    days: data.days || []
                } as Diet;
            });

            const usersMap = users.reduce<UsersMap>((acc, user) => ({
                ...acc,
                [user.id]: user
            }), {});

            const dietsWithUsers = dietsData.map(diet => ({
                ...diet,
                userEmail: usersMap[diet.userId]?.email || 'Nieznany użytkownik'
            }));

            setDiets(dietsWithUsers);
        } catch (error) {
            console.error('Error fetching diets:', error);
            toast.error('Błąd podczas pobierania diet');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (usersLoading) return;

        const dietsCollection = collection(db, 'diets');
        const unsubscribe = onSnapshot(dietsCollection,
            () => {
                fetchDiets().catch(console.error);
            },
            (error) => {
                console.error('Error in diets subscription:', error);
                toast.error('Błąd podczas aktualizacji diet');
            }
        );

        return () => unsubscribe();
    }, [usersLoading, users]);

    return {
        diets,
        loading,
        refreshDiets: fetchDiets
    };
};