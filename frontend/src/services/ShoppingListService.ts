import api from "../config/axios";
import {ShoppingListV3, CategorizedShoppingListItem} from "../types";
import {Timestamp} from "firebase/firestore";

export class ShoppingListService {
    private static readonly BASE_URL = '/shopping-lists';

    static async getShoppingListByDietId(dietId: string): Promise<ShoppingListV3> {
        const response = await api.get(`${this.BASE_URL}/diet/${dietId}`);
        return response.data;
    }

    static async updateItems(
        id: string,
        items: Record<string, CategorizedShoppingListItem[]>
    ): Promise<ShoppingListV3> {
        const response = await api.put(`${this.BASE_URL}/${id}/items`, {
            items: items
        });
        return response.data;
    }

    static async updateDates(
        shoppingListId: string,
        startDate: Timestamp,
        endDate: Timestamp
    ) {
        try {
            const response = await api.patch(`/shopping-lists/${shoppingListId}/dates`, {
                startDate,
                endDate
            });
            return response.data;
        } catch (error) {
            console.error('Error updating shopping list dates:', error);
            throw error;
        }
    }

    static async removeItem(
        id: string,
        categoryId: string,
        itemIndex: number
    ): Promise<void> {
        await api.delete(
            `${this.BASE_URL}/${id}/categories/${categoryId}/items/${itemIndex}`
        );
    }

    static async addItem(
        id: string,
        categoryId: string,
        item: CategorizedShoppingListItem
    ): Promise<ShoppingListV3> {

        const requestItem = {
            name: item.name || "",
            quantity: item.quantity || 0,
            unit: item.unit || "",
            original: item.original || item.name || ""
        };

        try {
            const response = await api.post(
                `${this.BASE_URL}/${id}/categories/${categoryId}/items`,
                requestItem
            );
            return response.data;
        } catch (error) {
            console.error("Error adding item:", error);
            throw error;
        }
    }
}