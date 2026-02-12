import React, {useMemo, useState} from "react";
import {ParsedDietData} from "../../../../../types";
import {ParsedProduct} from "../../../../../types/product";
import {
    Loader2, User, CalendarDays, ChevronDown, ChevronUp, ShoppingBag, Utensils, FileText,
    PieChart, Flame, Heart
} from "lucide-react";
import {formatTimestamp} from "../../../../../utils/dateFormatters";
import DietMealPreview from "./DietMealPreview";
import {getPolishDayForm, getPolishMealForm, getPolishProductForm} from "../../../../../utils/declensionsOfNouns";
import {v4 as uuidv4} from "uuid";
import {FloatingActionButton, FloatingActionButtonGroup} from "../../../../shared/common/FloatingActionButton";
import ShoppingListView from "../../shopping/ShoppingListView";

interface PreviewSectionProps {
    parsedData: ParsedDietData;
    categorizedProducts: Record<string, ParsedProduct[]>;
    onSave: () => Promise<void>;
    onCancel: () => void;
    isSaving: boolean;
    selectedUserEmail: string;
    fileName: string | undefined;
    disabled?: boolean;
}

const PreviewSection: React.FC<PreviewSectionProps> = ({
                                                           parsedData,
                                                           categorizedProducts,
                                                           onSave,
                                                           onCancel,
                                                           isSaving,
                                                           selectedUserEmail,
                                                           fileName,
                                                           disabled = false
                                                       }) => {
    const [showShoppingList, setShowShoppingList] = useState(true);
    const [expandedDays, setExpandedDays] = useState<number[]>([0]);

    const toggleDayExpand = (dayIndex: number) => {
        setExpandedDays(prev =>
            prev.includes(dayIndex)
                ? prev.filter(idx => idx !== dayIndex)
                : [...prev, dayIndex]
        );
    };

    const handleImageAdd = (dayIndex: number, mealIndex: number, imageUrl: string) => {
        const updatedDays = [...parsedData.days];
        if (!updatedDays[dayIndex].meals[mealIndex].photos) {
            updatedDays[dayIndex].meals[mealIndex].photos = [];
        }
        updatedDays[dayIndex].meals[mealIndex].photos?.push(imageUrl);

        if (!updatedDays[dayIndex].meals[mealIndex].originalRecipeId) {
            updatedDays[dayIndex].meals[mealIndex].originalRecipeId = `temp-recipe-${uuidv4()}`;
        }
    };

    const handleSaveWithImages = async () => {
        try {
            await onSave();
        } catch (error) {
            console.error("Błąd podczas zapisywania diety ze zdjęciami:", error);
        }
    };

    const shoppingListItems = useMemo(() => {
        if (Object.keys(categorizedProducts).length > 0) {
            return categorizedProducts;
        }

        if (parsedData.categorizedProducts && Object.keys(parsedData.categorizedProducts).length > 0) {
            const converted: Record<string, any[]> = {};

            Object.entries(parsedData.categorizedProducts).forEach(([category, items]) => {
                converted[category] = items.map(itemStr => ({
                    name: itemStr,
                    quantity: 0,
                    unit: '',
                    original: itemStr
                }));
            });
            return converted;
        }

        return {};
    }, [categorizedProducts, parsedData.categorizedProducts]);

    const totalProducts = Object.values(categorizedProducts).length > 0
        ? Object.values(categorizedProducts).reduce((sum, products) => sum + products.length, 0)
        : (parsedData.shoppingList?.length || 0);

    const nutritionSummary = useMemo(() => {
        if (!parsedData?.days?.length) return null;
        const totals = {calories: 0, protein: 0, carbs: 0, fat: 0};
        let mealCount = 0;
        parsedData.days.forEach(day => {
            day.meals.forEach(meal => {
                if (meal.nutritionalValues) {
                    totals.calories += Number(meal.nutritionalValues.calories || 0);
                    totals.protein += Number(meal.nutritionalValues.protein || 0);
                    totals.carbs += Number(meal.nutritionalValues.carbs || 0);
                    totals.fat += Number(meal.nutritionalValues.fat || 0);
                    mealCount++;
                }
            });
        });
        const daysCount = parsedData.days.length || 1;
        return {
            perDay: {
                calories: Math.round(totals.calories / daysCount),
                protein: Math.round(totals.protein / daysCount),
                carbs: Math.round(totals.carbs / daysCount),
                fat: Math.round(totals.fat / daysCount)
            },
            hasMealNutrition: mealCount > 0
        };
    }, [parsedData]);


    return (
        <div className="space-y-6 pb-16 relative">
            <div
                className="bg-white p-6 rounded-lg shadow-sm flex flex-col md:flex-row md:justify-between md:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold mb-2">Podgląd diety przed zapisem</h2>
                    <div className="flex flex-col gap-1">
                        <div className="flex items-center gap-2 text-blue-600 font-medium text-lg">
                            <User className="h-5 w-5"/>
                            <span>{selectedUserEmail}</span>
                        </div>
                        {fileName && (
                            <div className="flex items-center gap-2 text-gray-600">
                                <div className="w-5 flex justify-center">
                                    <FileText className="h-4 w-4"/>
                                </div>
                                <span className="font-medium text-sm">{fileName}</span>
                            </div>
                        )}
                    </div>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div className="flex items-center gap-2 bg-blue-50 p-2 rounded-md">
                        <CalendarDays className="h-5 w-5 text-blue-600"/>
                        <span>
                            <strong>{parsedData.days.length}</strong> {getPolishDayForm(parsedData.days.length)} diety
                        </span>
                    </div>
                    <div className="flex items-center gap-2 bg-green-50 p-2 rounded-md">
                        <Utensils className="h-5 w-5 text-green-600"/>
                        <span>
                            <strong>{parsedData.mealsPerDay}</strong> {getPolishMealForm(parsedData.mealsPerDay)} dziennie
                        </span>
                    </div>
                    <div className="flex items-center gap-2 bg-purple-50 p-2 rounded-md md:col-span-2">
                        <ShoppingBag className="h-5 w-5 text-purple-600"/>
                        <span>
                            <strong>{totalProducts}</strong> {getPolishProductForm(totalProducts)} na liście zakupów
                        </span>
                    </div>
                </div>
            </div>

            {nutritionSummary?.hasMealNutrition && (
                <div className="bg-white p-6 rounded-lg shadow-sm">
                    <h3 className="text-lg font-medium mb-4 flex items-center gap-2">
                        <PieChart className="h-5 w-5 text-amber-600"/>
                        Podsumowanie wartości odżywczych (dziennie)
                    </h3>
                    <div className="overflow-x-auto">
                        <table className="w-full border-collapse">
                            <thead>
                            <tr className="bg-gray-50">
                                <th className="text-left py-2 px-3 font-medium border-b">
                                    <div className="flex items-center gap-2"><Flame className="h-4 w-4 text-green-700"/>Kalorie
                                    </div>
                                </th>
                                <th className="text-left py-2 px-3 font-medium border-b">
                                    <div className="flex items-center gap-2"><Heart className="h-4 w-4 text-blue-700"/>Białko
                                    </div>
                                </th>
                                <th className="text-left py-2 px-3 font-medium border-b">
                                    <div className="flex items-center gap-2"><Heart className="h-4 w-4 text-red-700"/>Tłuszcze
                                    </div>
                                </th>
                                <th className="text-left py-2 px-3 font-medium border-b">
                                    <div className="flex items-center gap-2"><Heart
                                        className="h-4 w-4 text-yellow-700"/>Węglowodany
                                    </div>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td className="py-2 px-3 border-b"><span
                                    className="font-medium text-green-700">{nutritionSummary.perDay.calories}</span> kcal
                                </td>
                                <td className="py-2 px-3 border-b"><span
                                    className="font-medium text-blue-700">{nutritionSummary.perDay.protein}</span> g
                                </td>
                                <td className="py-2 px-3 border-b"><span
                                    className="font-medium text-red-700">{nutritionSummary.perDay.fat}</span> g
                                </td>
                                <td className="py-2 px-3 border-b"><span
                                    className="font-medium text-yellow-700">{nutritionSummary.perDay.carbs}</span> g
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            <div className="space-y-4">
                <h3 className="text-xl font-semibold flex items-center gap-2 mb-4">
                    <CalendarDays className="h-5 w-5 text-blue-600"/>
                    Harmonogram diety
                </h3>
                {parsedData.days.map((day, dayIndex) => (
                    <div key={dayIndex} className="bg-white rounded-lg shadow-sm overflow-hidden">
                        <div
                            className="p-4 border-b flex justify-between items-center cursor-pointer hover:bg-gray-50"
                            onClick={() => toggleDayExpand(dayIndex)}
                        >
                            <h3 className="text-lg font-semibold flex items-center gap-2">
                                <span
                                    className="flex justify-center items-center h-6 w-6 rounded-full bg-blue-600 text-white text-sm font-bold">
                                  {dayIndex + 1}
                                </span>
                                {formatTimestamp(day.date)}
                            </h3>
                            {expandedDays.includes(dayIndex) ? <ChevronUp className="h-5 w-5 text-gray-500"/> :
                                <ChevronDown className="h-5 w-5 text-gray-500"/>}
                        </div>

                        {expandedDays.includes(dayIndex) && (
                            <div className="p-4">
                                <div className="space-y-4">
                                    {day.meals.map((meal, mealIndex) => (
                                        <DietMealPreview
                                            key={mealIndex}
                                            meal={meal}
                                            mealIndex={mealIndex}
                                            onImageAdd={(mealIndex, imageUrl) => handleImageAdd(dayIndex, mealIndex, imageUrl)}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            <div className="bg-white rounded-lg shadow-sm overflow-hidden">
                <div
                    className="p-4 border-b flex justify-between items-center cursor-pointer hover:bg-gray-50"
                    onClick={() => setShowShoppingList(!showShoppingList)}
                >
                    <h3 className="text-xl font-semibold flex items-center gap-2">
                        <ShoppingBag className="h-5 w-5 text-purple-600"/>
                        Lista zakupów według kategorii
                    </h3>
                    {showShoppingList ? (
                        <ChevronUp className="h-5 w-5 text-gray-500"/>
                    ) : (
                        <ChevronDown className="h-5 w-5 text-gray-500"/>
                    )}
                </div>

                {showShoppingList && (
                    <div className="p-4">
                        <ShoppingListView
                            items={shoppingListItems}
                            emptyMessage="Brak skategoryzowanych produktów"
                        />
                    </div>
                )}
            </div>

            <div className="fixed bottom-6 right-6 flex gap-3 z-10">
                <FloatingActionButtonGroup position="bottom-right">
                    <FloatingActionButton
                        label="Anuluj"
                        onClick={onCancel}
                        disabled={disabled || isSaving}
                        variant="secondary"
                    />
                    <FloatingActionButton
                        label="Zapisz dietę"
                        onClick={handleSaveWithImages}
                        disabled={disabled || isSaving}
                        isLoading={isSaving}
                        loadingLabel="Zapisywanie..."
                        icon={<ShoppingBag className="h-5 w-5"/>}
                        loadingIcon={<Loader2 className="h-5 w-5 animate-spin"/>}
                        variant="primary"
                    />
                </FloatingActionButtonGroup>
            </div>
        </div>
    );
};

export default PreviewSection;