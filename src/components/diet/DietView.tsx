import React, {useEffect, useState} from "react";
import {Diet, MealType, Recipe} from "../../types/diet";
import {toast} from "sonner";
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
} from "../ui/sheet"
import {X} from "lucide-react";
import LoadingSpinner from "../common/LoadingSpinner";
import {useShoppingList} from "../../hooks/useShoppingList";
import {doc, getDoc} from "firebase/firestore";
import {db} from "../../config/firebase";
import {formatDate, formatTimestamp} from "../../utils/dateFormatters";

interface DietViewProps {
    diet: Diet;
    onClose: () => void;
}

const DietView: React.FC<DietViewProps> = ({ diet, onClose }) => {
    const [recipes, setRecipes] = useState<{ [key: string]: Recipe }>({});
    const [loading, setLoading] = useState(true);
    const { shoppingList, loading: shoppingListLoading } =
        useShoppingList(diet.id);

    useEffect(() => {
        const fetchRecipes = async () => {
            try {
                if (!diet.days || diet.days.length === 0) {
                    setLoading(false);
                    return;
                }

                const recipeIds = new Set(
                    diet.days.flatMap((day) => day.meals.map((meal) => meal.recipeId))
                );

                const recipesData: { [key: string]: Recipe } = {};
                for (const recipeId of recipeIds) {
                    if (!recipeId) continue;

                    const recipeDoc = await getDoc(doc(db, "recipes", recipeId));
                    if (recipeDoc.exists()) {
                        recipesData[recipeId] = {
                            id: recipeDoc.id,
                            ...recipeDoc.data(),
                        } as Recipe;
                    }
                }
                setRecipes(recipesData);
            } catch (error) {
                console.error("Error fetching recipes:", error);
                toast.error("Błąd podczas pobierania przepisów");
            } finally {
                setLoading(false);
            }
        };

        fetchRecipes().catch();
    }, [diet]);

    const getMealTypeLabel = (mealType: MealType) => {
        const labels: { [key in MealType]: string } = {
            [MealType.BREAKFAST]: 'Śniadanie',
            [MealType.SECOND_BREAKFAST]: 'Drugie śniadanie',
            [MealType.LUNCH]: 'Obiad',
            [MealType.SNACK]: 'Przekąska',
            [MealType.DINNER]: 'Kolacja'
        };
        return labels[mealType];
    };

    const renderShoppingList = () => {
        if (shoppingListLoading) {
            return (
                <div className="flex justify-center py-4">
                    <LoadingSpinner/>
                </div>
            );
        }

        if (!shoppingList) {
            return null;
        }

        return (
            <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex justify-between items-center mb-3">
                    <h3 className="text-lg font-medium">Lista zakupów</h3>
                    <div className="text-sm text-gray-600">
                        {formatDate(shoppingList.startDate)} - {formatDate(shoppingList.endDate)}
                    </div>
                </div>
                <ul className="list-disc list-inside space-y-1">
                    {shoppingList.items.map((item, index) => (
                        <li key={index} className="text-gray-700">{item.name}</li>
                    ))}
                </ul>
            </div>
        );
    }

    const renderMetadata = () => {
        if (!diet.metadata) return null;

        return (
            <div className="text-sm text-gray-600 space-y-2">
                <p>
                    <span className="font-medium">
                        Liczba dni:
                    </span>
                    {' '}
                    {diet.metadata.totalDays || diet.days?.length || 0}
                </p>
                {diet.metadata.fileName && (
                    <p>
                        <span className="font-medium">
                            Nazwa pliku:
                        </span>
                        {' '}
                        {diet.metadata.fileName}
                    </p>
                )}
                {diet.createdAt && (
                    <p>
                        <span className="font-medium">
                            Data utworzenia:
                        </span>
                        {' '}
                        {formatTimestamp(diet.createdAt)}
                    </p>
                )}
            </div>
        );
    };

    const renderContent = () => {
        if (loading) {
            return (
                <div className="flex justify-center py-8">
                    <LoadingSpinner/>
                </div>
            );
        }

        if (!diet.days || diet.days.length === 0) {
            return (
                <div className="text-center py-8 text-gray-500">
                    Brak przypisanych posiłków do tej diety.
                </div>
            );
        }

        return diet.days.map((day, index) => (
            <div key={index} className="border-b pb-6 last:border-b-0">
                <h3 className="text-lg font-medium mb-4">
                    Dzień {index + 1} - {formatDate(day.date)}
                </h3>
                <div className="space-y-4">
                    {day.meals?.map((meal, mealIndex) => {
                        const recipe = recipes[meal.recipeId];
                        if (!recipe) return null;

                        return (
                            <div key={mealIndex} className="bg-gray-50 p-4 rounded-lg">
                                <div className="flex justify-between mb-2">
                                    <span className="font-medium">
                                        {getMealTypeLabel(meal.mealType)} - {meal.time}
                                    </span>
                                </div>
                                <div className="space-y-2">
                                    <p className="font-medium">{recipe.name}</p>
                                    <p className="text-sm text-gray-600">
                                        {recipe.instructions}
                                    </p>
                                    <div className="text-sm">
                                        <p className="font-medium">Wartości odżywcze:</p>
                                        <p>
                                            Kalorie: {recipe.nutritionalValues?.calories || 0} kcal,{' '}
                                            Białko: {recipe.nutritionalValues?.protein || 0}g,{' '}
                                            Tłuszcze: {recipe.nutritionalValues?.fat || 0}g,{' '}
                                            Węglowodany: {recipe.nutritionalValues?.carbs || 0}g
                                        </p>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        ));
    };

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent className="w-full sm:max-w-3xl overflow-y-auto">
                <SheetHeader>
                    <div className="flex justify-between items-center border-b pb-4">
                        <SheetTitle>Szczegóły Diety</SheetTitle>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-500"
                        >
                            <X className="h-6 w-6" />
                        </button>
                    </div>
                </SheetHeader>

                <div className="mt-6 space-y-6">
                    {renderMetadata()}
                    {renderShoppingList()}
                    {renderContent()}
                </div>
            </SheetContent>
        </Sheet>
    );
};


export default DietView;