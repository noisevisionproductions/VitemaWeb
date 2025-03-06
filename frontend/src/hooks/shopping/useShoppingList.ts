import {useState, useEffect} from "react";
import {ShoppingListV3} from "../../types";
import {toast} from "sonner";
import {ShoppingListService} from "../../services/categorization/ShoppingListService";
import {CategorizedShoppingListItem} from "../../types";

export const useShoppingList = (dietId: string) => {
    const [shoppingList, setShoppingList] = useState<ShoppingListV3 | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    const fetchShoppingList = async () => {
        try {
            setLoading(true);
            const response = await ShoppingListService.getShoppingListByDietId(dietId);
            setShoppingList(response || null);
            setError(null);
        } catch (err) {
            console.error('Error fetching shopping list:', err);
            setError(err as Error);
            toast.error('Błąd podczas pobierania listy zakupów');
        } finally {
            setLoading(false);
        }
    };

    const updateItems = async (items: Record<string, CategorizedShoppingListItem[]>) => {
        if (!shoppingList?.id) {
            toast.error('Lista zakupów nie istnieje');
            return null;
        }

        try {
            const updated = await ShoppingListService.updateItems(shoppingList.id, items);
            if (!updated) {
                toast.error('Otrzymano pustą odpowiedź z serwera');
                return null;
            }
            setShoppingList(updated);
            toast.success('Lista zakupów została zaktualizowana');
            return updated;
        } catch (err) {
            console.error('Error updating shopping list:', err);
            toast.error('Błąd podczas aktualizacji listy zakupów');
            return null;
        }
    };

    const removeItem = async (categoryId: string, itemIndex: number) => {
        if (!shoppingList?.id) {
            toast.error('Lista zakupów nie istnieje');
            return null;
        }

        try {
            await ShoppingListService.removeItem(
                shoppingList.id,
                categoryId,
                itemIndex
            );

            const updatedItems = { ...shoppingList.items };
            if (updatedItems[categoryId]) {
                updatedItems[categoryId] = updatedItems[categoryId].filter((_, index) => index !== itemIndex);

                // Usuń kategorię, jeśli jest pusta
                if (updatedItems[categoryId].length === 0) {
                    delete updatedItems[categoryId];
                }
            }

            const updatedShoppingList = {
                ...shoppingList,
                items: updatedItems
            };

            setShoppingList(updatedShoppingList);
            toast.success('Produkt został usunięty z listy');
            return updatedShoppingList;
        } catch (err) {
            console.error('Error removing item:', err);
            toast.error('Błąd podczas usuwania produktu');
            return null;
        }
    };

    const addItem = async (categoryId: string, item: CategorizedShoppingListItem) => {
        if (!shoppingList?.id) {
            toast.error('Lista zakupów nie istnieje');
            return null;
        }

        try {
            const updated = await ShoppingListService.addItem(
                shoppingList.id,
                categoryId,
                item
            );
            setShoppingList(updated);
            setError(null);
            toast.success('Produkt został dodany do listy');
            return updated;
        } catch (err) {
            console.error('Error adding item:', err);
            setError(err as Error);
            toast.error('Błąd podczas dodawania produktu');
            return null;
        }
    };

    useEffect(() => {
        if (dietId) {
            void fetchShoppingList();
        }
    }, [dietId]);

    return {
        shoppingList,
        loading,
        error,
        updateItems,
        removeItem,
        addItem,
        refresh: fetchShoppingList
    };
};