import {useEffect, useState} from "react";
import {collection, getDocs, query, where} from "firebase/firestore";
import {db} from "../config/firebase";
import {ShoppingListV3} from "../types";

export const useShoppingList = (dietId: string) => {
    const [shoppingList, setShoppingList] = useState<ShoppingListV3 | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        const fetchShoppingList = async () => {
            try {
                const q = query(
                    collection(db, 'shopping_lists'),
                    where('dietId', '==', dietId),
                    where('version', '==', 3)
                );

                const querySnapshot = await getDocs(q);
                if (!querySnapshot.empty) {
                    const doc = querySnapshot.docs[0];
                    setShoppingList({
                        id: doc.id,
                        ...doc.data()
                    } as ShoppingListV3);
                }
            } catch (err) {
                setError(err as Error);
            } finally {
                setLoading(false);
            }
        };

        if (dietId) {
            setLoading(true);
            setError(null);
            fetchShoppingList().catch((err) => {
                console.error('Error in fetchShoppingList:', err);
                setError(err as Error);
                setLoading(false);
            });
        }
    }, [dietId]);

    return {
        shoppingList,
        loading,
        error
    };
};