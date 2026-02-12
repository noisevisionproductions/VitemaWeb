import React from "react";
import {useShoppingList} from "../../../../hooks/shopping/useShoppingList";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";
import ShoppingListView from "./ShoppingListView";
import {AlertCircle} from "lucide-react";

interface DietShoppingListProps {
    dietId: string;
}

const DietShoppingList: React.FC<DietShoppingListProps> = ({dietId}) => {
    const {shoppingList, loading, error} = useShoppingList(dietId);

    if (loading) {
        return (
            <div className="flex justify-center items-center py-12">
                <LoadingSpinner/>
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-4 bg-red-50 text-red-600 rounded-lg flex items-center gap-2">
                <AlertCircle className="h-5 w-5"/>
                <span>Nie udało się pobrać listy zakupów.</span>
            </div>
        );
    }

    if (!shoppingList) {
        return (
            <div className="text-center py-8">
                <p className="text-gray-500">Brak wygenerowanej listy zakupów dla tej diety.</p>
            </div>
        );
    }

    return (
        <div className="animate-in fade-in duration-500">
            <ShoppingListView items={shoppingList.items}/>
        </div>
    );
};

export default DietShoppingList;