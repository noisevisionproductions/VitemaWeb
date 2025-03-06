import React, {useState} from "react";
import {ParsedDietData} from "../../../../types";
import {ParsedProduct} from "../../../../types/product";
import {getCategoryLabel} from "../../../../utils/productUtils";
import {useProductCategories} from "../../../../hooks/shopping/useProductCategories";
import {
    Loader2, User, CalendarDays, ChevronDown, ChevronUp, ShoppingBag, Utensils
} from "lucide-react";
import {formatTimestamp} from "../../../../utils/dateFormatters";
import DietMealPreview from "./DietMealPreview";
import {getPolishDayForm, getPolishMealForm, getPolishProductForm} from "../../../../utils/declensionsOfNouns";

interface PreviewSectionProps {
    parsedData: ParsedDietData;
    categorizedProducts: Record<string, ParsedProduct[]>;
    onSave: () => Promise<void>;
    onCancel: () => void;
    isSaving: boolean;
    selectedUserEmail: string;
    disabled?: boolean;
}

const PreviewSection: React.FC<PreviewSectionProps> = ({
                                                           parsedData,
                                                           categorizedProducts,
                                                           onSave,
                                                           onCancel,
                                                           isSaving,
                                                           selectedUserEmail,
                                                           disabled = false
                                                       }) => {
    const {categories} = useProductCategories();
    const [showShoppingList, setShowShoppingList] = useState(true);
    const [expandedDays, setExpandedDays] = useState<number[]>([0]);

    // Funkcja do przełączania rozwinięcia dnia
    const toggleDayExpand = (dayIndex: number) => {
        setExpandedDays(prev =>
            prev.includes(dayIndex)
                ? prev.filter(idx => idx !== dayIndex)
                : [...prev, dayIndex]
        );
    };

    // Zsumuj liczbę produktów we wszystkich kategoriach
    const totalProducts = Object.values(categorizedProducts)
        .reduce((sum, products) => sum + products.length, 0);

    return (
        <div className="space-y-6">
            {/* Nagłówek z informacjami o użytkowniku */}
            <div
                className="bg-white p-6 rounded-lg shadow-sm flex flex-col md:flex-row md:justify-between md:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold mb-2">Podgląd diety przed zapisem</h2>
                    <div className="flex items-center gap-2 text-blue-600 font-medium text-lg">
                        <User className="h-5 w-5"/>
                        <span>{selectedUserEmail}</span>
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

            {/* Przegląd dni i posiłków */}
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
                            {expandedDays.includes(dayIndex) ? (
                                <ChevronUp className="h-5 w-5 text-gray-500"/>
                            ) : (
                                <ChevronDown className="h-5 w-5 text-gray-500"/>
                            )}
                        </div>

                        {expandedDays.includes(dayIndex) && (
                            <div className="p-4">
                                <div className="space-y-4">
                                    {day.meals.map((meal, mealIndex) => (
                                        <DietMealPreview
                                            key={mealIndex}
                                            meal={meal}
                                            mealIndex={mealIndex}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* Lista zakupów z kategoriami */}
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
                        {Object.entries(categorizedProducts).length === 0 ? (
                            <div className="text-center py-8 text-gray-500">
                                Brak skategoryzowanych produktów
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {Object.entries(categorizedProducts).map(([categoryId, products]) => {
                                    const category = categories.find(c => c.id === categoryId);
                                    const categoryName = category?.name || getCategoryLabel(categoryId);
                                    const categoryColor = category?.color || '#9e9e9e';

                                    return (
                                        <div key={categoryId} className="rounded-lg border overflow-hidden">
                                            <div
                                                className="font-medium px-4 py-3"
                                                style={{backgroundColor: `${categoryColor}20`}}
                                            >
                                                {categoryName} ({products.length})
                                            </div>
                                            <div className="p-4">
                                                <ul className="space-y-2">
                                                    {products.map((product, index) => (
                                                        <li key={index} className="flex items-start gap-2">
                                                            <span className="text-purple-600 mt-1">•</span>
                                                            <div>
                                                                <span className="font-medium">{product.name}</span>
                                                                <span className="text-gray-600 ml-2">
                                  {product.quantity} {product.unit}
                                </span>
                                                            </div>
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Przyciski akcji */}
            <div className="flex justify-end space-x-4 pt-4 sticky bottom-0 bg-white p-4 border-t shadow-md">
                <button
                    onClick={onCancel}
                    disabled={disabled || isSaving}
                    className="px-4 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    Anuluj
                </button>
                <button
                    onClick={onSave}
                    disabled={isSaving || disabled}
                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                    {isSaving ? (
                        <>
                            <Loader2 className="h-5 w-5 animate-spin"/>
                            <span>Zapisywanie...</span>
                        </>
                    ) : (
                        <>
                            <ShoppingBag className="h-5 w-5"/>
                            <span>Zapisz dietę</span>
                        </>
                    )}
                </button>
            </div>
        </div>
    );
};

export default PreviewSection;