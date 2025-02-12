import React, {useState} from "react";
import {getMealTypeLabel} from "../../../utils/mealTypeUtils";
import {formatDate} from "../../../utils/dateFormatters";
import {ParsedDietData} from "../../../types";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import {toast} from "sonner";
import ProductCategorizationLayout from "../../products/ProductCategorizationLayout";
import {useProductCategories} from "../../../hooks/useProductCategories";
import {ParsedProduct} from "../../../types/product";
import {ProductParsingService} from "../../../services/categorization/ProductParsingService";
import {createSafeProduct, getCategoryLabel} from "../../../utils/productUtils";
import {ProductCategorizationService} from "../../../services/categorization/ProductCategorizationService";
import ParserGuide from "../../products/ParserGuide";

interface DietPreviewProps {
    parsedData: ParsedDietData;
    onConfirm: () => Promise<void>;
    onCancel: () => void;
    selectedUserEmail: string;
}

const DietPreview: React.FC<DietPreviewProps> = ({
                                                     parsedData,
                                                     onConfirm,
                                                     onCancel,
                                                     selectedUserEmail
                                                 }) => {
    const [isSaving, setIsSaving] = useState(false);
    const [step, setStep] = useState<'categorization' | 'preview'>('categorization');
    const [categorizedProducts, setCategorizedProducts] = React.useState<Record<string, ParsedProduct[]>>({});
    const [uncategorizedProducts, setUncategorizedProducts] = React.useState<ParsedProduct[]>([]);
    const [isSavingCategorization, setIsSavingCategorization] = useState(false);
    const {categories} = useProductCategories();

    // Parsowanie listy zakupów do odpowiedniego formatu
    const parsedProducts = parsedData.shoppingList
        .map(product => {
            const parseResult = ProductParsingService.parseProduct(product);
            return parseResult.success ? parseResult.product : createSafeProduct(product);
        })
        .filter((product): product is ParsedProduct => product !== undefined);

    React.useEffect(() => {
        setUncategorizedProducts(parsedProducts);
    }, [parsedData.shoppingList]);

    const handleProductDrop = (categoryId: string, product: ParsedProduct) => {
        setUncategorizedProducts(prev =>
            prev.filter(p => p.original !== product.original)
        );
        setCategorizedProducts(prev => ({
            ...prev,
            [categoryId]: [...(prev[categoryId] || []), product]
        }));
    };

    const handleProductRemove = (product: ParsedProduct) => {
        const categoryId = Object.entries(categorizedProducts).find(
            ([_, products]) => products.some(p => p.original === product.original)
        )?.[0];

        if (categoryId) {
            setCategorizedProducts(prev => ({
                ...prev,
                [categoryId]: prev[categoryId].filter(p => p.original !== product.original)
            }));
            setUncategorizedProducts(prev => [...prev, product]);
        }
    };

    const handleConfirm = async () => {
        if (uncategorizedProducts.length > 0) {
            toast.error('Musisz skategoryzować wszystkie produkty');
            return;
        }

        parsedData.categorizedProducts = Object.entries(categorizedProducts).reduce(
            (acc, [categoryId, products]) => ({
                ...acc,
                [categoryId]: products.map(p => p.original)
            }),
            {} as Record<string, string[]>
        );

        try {
            setIsSaving(true);
            await onConfirm();
        } catch (error) {
            console.error('Błąd podczas zapisywania diety:', error);
            toast.error('Wystąpił błąd podczas zapisywania');
        } finally {
            setIsSaving(false);
        }
    };

    const handleProductEdit = (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => {
        setCategorizedProducts(prev => ({
            ...prev,
            [categoryId]: prev[categoryId].map(p =>
                p.original === oldProduct.original ? newProduct : p
            )
        }));
    };

    const handleCategorizeComplete = async () => {
        if (uncategorizedProducts.length > 0) {
            toast.error('Musisz skategoryzować wszystkie produkty');
            return;
        }

        setIsSavingCategorization(true);
        try {
            await ProductCategorizationService.bulkSaveProductCategorizations(categorizedProducts);
            toast.success('Kategoryzacja została zapisana');

            parsedData.categorizedProducts = Object.entries(categorizedProducts).reduce(
                (acc, [categoryId, products]) => ({
                    ...acc,
                    [categoryId]: products.map(p => p.original)
                }),
                {} as Record<string, string[]>
            );

            setStep('preview');
        } catch (error) {
            console.error('Błąd podczas zapisywania kategoryzacji:', error);
            toast.error('Wystąpił błąd podczas zapisywania kategoryzacji');
        } finally {
            setIsSavingCategorization(false);
        }
    };

    if (step === 'categorization') {
        return (
            <div className="space-y-6">
                <div className="flex justify-between items-center">
                    <h2 className="text-2xl font-bold">Kategoryzacja produktów</h2>
                    <div className="text-gray-600">
                        Użytkownik: {selectedUserEmail}
                    </div>
                </div>

                <ParserGuide />

                <DndProvider backend={HTML5Backend}>
                    <ProductCategorizationLayout
                        categories={categories}
                        uncategorizedProducts={uncategorizedProducts}
                        categorizedProducts={categorizedProducts}
                        onProductDrop={handleProductDrop}
                        onProductRemove={handleProductRemove}
                        onProductEdit={handleProductEdit}
                    />
                </DndProvider>

                <div className="flex justify-end space-x-4 pt-4">
                    <button
                        onClick={onCancel}
                        className="px-4 py-2 mt-7 border rounded-lg hover:bg-gray-50"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={handleCategorizeComplete}
                        disabled={uncategorizedProducts.length > 0 || isSavingCategorization}
                        className="px-4 py-2 mt-7 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isSavingCategorization ? 'Zapisywanie kategoryzacji...' : 'Przejdź do podglądu'}
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">Podgląd diety przed zapisem</h2>
                <div className="text-gray-600">
                    <div>Użytkownik: {selectedUserEmail}</div>
                    <div className="text-sm text-green-600">
                        Produkty skategoryzowane ✓
                    </div>
                </div>
            </div>


            {/* Przegląd dni i posiłków */}
            <div className="space-y-6">
                {parsedData.days.map((day, dayIndex) => (
                    <div key={dayIndex} className="border rounded-lg p-4">
                        <h3 className="text-lg font-semibold mb-4">
                            Dzień {dayIndex + 1} - {formatDate(day.date)}
                        </h3>
                        <div className="space-y-4">
                            {day.meals.map((meal, mealIndex) => (
                                <div key={mealIndex} className="bg-gray-50 p-4 rounded-lg">
                                    <div className="font-medium">
                                        {getMealTypeLabel(meal.mealType)} - {meal.time}
                                    </div>
                                    <div className="mt-2">
                                        <div className="font-medium text-gray-900">{meal.name}</div>
                                        <div className="text-sm text-gray-600 mt-1">{meal.instructions}</div>
                                      {/*  <div className="mt-2 space-y-1">
                                            <div className="text-sm font-medium">Składniki:</div>
                                            <div className="text-sm text-gray-600">
                                                {meal.ingredients.join(', ')}
                                            </div>
                                        </div>*/}
                                        {meal.nutritionalValues && (
                                            <div className="mt-2 text-sm text-gray-600">
                                                Wartości odżywcze: {meal.nutritionalValues.calories} kcal,{' '}
                                                B: {meal.nutritionalValues.protein}g,{' '}
                                                T: {meal.nutritionalValues.fat}g,{' '}
                                                W: {meal.nutritionalValues.carbs}g
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {/* Lista zakupów z kategoriami */}
            <div className="border rounded-lg p-4">
                <h3 className="text-lg font-semibold mb-4">Lista zakupów według kategorii</h3>
                {Object.entries(categorizedProducts).map(([categoryId, products]) => (
                    <div key={categoryId} className="mb-4">
                        <h4 className="font-medium mb-2">{getCategoryLabel(categoryId)}</h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                            {products.map((product, index) => (
                                <div key={index} className="text-gray-600">
                                    • {product.original}
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {/* Przyciski akcji */}
            <div className="flex justify-end space-x-4 pt-4">
                <button
                    onClick={onCancel}
                    className="px-4 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    Anuluj
                </button>
                {/*  <button
                    onClick={onCancel}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                    Przejdź do kategoryzacji produktów
                </button>*/}
                <button
                    onClick={handleConfirm}
                    disabled={isSaving}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                >
                    {isSaving ? (
                        <>
                            <svg
                                className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                            >
                                <circle
                                    className="opacity-25"
                                    cx="12"
                                    cy="12"
                                    r="10"
                                    stroke="currentColor"
                                    strokeWidth="4"
                                />
                                <path
                                    className="opacity-75"
                                    fill="currentColor"
                                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                                />
                            </svg>
                            <span>Zapisywanie...</span>
                        </>
                    ) : (
                        "Zapisz dietę"
                    )}
                </button>
            </div>
        </div>
    );
};

export default DietPreview;