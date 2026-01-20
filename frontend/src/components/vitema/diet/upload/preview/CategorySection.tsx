import React, {useEffect, useState} from "react";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import {ParsedProduct} from "../../../../../types/product";
import {useProductCategories} from "../../../../../hooks/shopping/useProductCategories";
import ProductCategorizationLayout from "../../../products/ProductCategorizationLayout";
import ParserGuide from "../../../products/ParserGuide";
import LoadingSpinner from "../../../../shared/common/LoadingSpinner";
import {ArrowLeft, ArrowRight, Loader2} from "lucide-react";
import {useSuggestedCategoriesContext} from "../../../../../contexts/SuggestedCategoriesContext";
import SectionHeader from "../../../../shared/common/SectionHeader";
import {FloatingActionButton, FloatingActionButtonGroup} from "../../../../shared/common/FloatingActionButton";

interface CategorySectionProps {
    uncategorizedProducts: ParsedProduct[];
    categorizedProducts: Record<string, ParsedProduct[]>;
    onProductDrop: (categoryId: string, product: ParsedProduct) => void;
    onProductRemove: (product: ParsedProduct) => void;
    onProductEdit: (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => void;
    onComplete: () => Promise<void>;
    onCancel: () => void;
    selectedUserEmail: string;
    showBackButton?: boolean;
    onBack?: () => void;
}

const CategorySection: React.FC<CategorySectionProps> = ({
                                                             uncategorizedProducts,
                                                             categorizedProducts,
                                                             onProductDrop,
                                                             onProductRemove,
                                                             onProductEdit,
                                                             onComplete,
                                                             onCancel,
                                                             selectedUserEmail,
                                                             showBackButton,
                                                             onBack
                                                         }) => {
    const [isSaving, setIsSaving] = useState(false);
    const {categories, loading: loadingCategories} = useProductCategories();
    const {refreshSuggestions} = useSuggestedCategoriesContext();

    useEffect(() => {
        if (uncategorizedProducts.length > 0) {
            refreshSuggestions(uncategorizedProducts).catch(console.error);
        }
    }, [uncategorizedProducts, refreshSuggestions]);

    const handleComplete = async () => {
        if (uncategorizedProducts.length > 0) {
            return;
        }

        setIsSaving(true);
        try {
            await onComplete();
        } finally {
            setIsSaving(false);
        }
    };

    const totalProducts = Object.values(categorizedProducts).reduce(
        (sum, products) => sum + products.length,
        0
    ) + uncategorizedProducts.length;

    const categorizedCount = Object.values(categorizedProducts).reduce(
        (sum, products) => sum + products.length,
        0
    );

    if (loadingCategories) {
        return (
            <div className="flex items-center justify-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <SectionHeader
                title="Kategoryzacja produktów"
                description={`Przypisz ${totalProducts} produktów do odpowiednich kategorii (${categorizedCount}/${totalProducts} skategoryzowane)`}
                rightContent={
                    <div className="text-sm text-gray-600 bg-blue-50 px-3 py-2 rounded-lg">
                        <span className="font-medium">Użytkownik:</span> {selectedUserEmail}
                    </div>
                }
            />

            {/* Progress indicator */}
            <div className="bg-white p-4 rounded-lg shadow-sm border">
                <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-gray-700">
                        Postęp kategoryzacji
                    </span>
                    <span className="text-sm text-gray-600">
                        {categorizedCount} z {totalProducts}
                    </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                        className="bg-primary h-2 rounded-full transition-all duration-300"
                        style={{
                            width: totalProducts > 0 ? `${(categorizedCount / totalProducts) * 100}%` : '0%'
                        }}
                    />
                </div>
                {uncategorizedProducts.length > 0 && (
                    <p className="text-sm text-amber-600 mt-2">
                        Pozostało {uncategorizedProducts.length} produktów do skategoryzowania
                    </p>
                )}
            </div>

            <ParserGuide/>

            <DndProvider backend={HTML5Backend}>
                <ProductCategorizationLayout
                    categories={categories}
                    uncategorizedProducts={uncategorizedProducts}
                    categorizedProducts={categorizedProducts}
                    onProductDrop={onProductDrop}
                    onProductRemove={onProductRemove}
                    onProductEdit={onProductEdit}
                    loading={isSaving}
                />
            </DndProvider>

            {/* Floating action buttons */}
            <div className="fixed bottom-6 right-6 flex gap-3 z-10">
                <FloatingActionButtonGroup position="bottom-right">
                    {showBackButton && onBack && (
                        <FloatingActionButton
                            label="Poprzedni krok"
                            onClick={onBack}
                            disabled={isSaving}
                            variant="secondary"
                            icon={<ArrowLeft className="h-5 w-5"/>}
                        />
                    )}

                    <FloatingActionButton
                        label="Anuluj"
                        onClick={onCancel}
                        disabled={isSaving}
                        variant="secondary"
                    />

                    <FloatingActionButton
                        label="Przejdź do podglądu"
                        onClick={handleComplete}
                        disabled={uncategorizedProducts.length > 0 || isSaving}
                        isLoading={isSaving}
                        loadingLabel="Zapisywanie kategoryzacji..."
                        icon={<ArrowRight className="h-5 w-5"/>}
                        loadingIcon={<Loader2 className="h-5 w-5 animate-spin"/>}
                        variant="primary"
                    />
                </FloatingActionButtonGroup>
            </div>
        </div>
    );
};

export default CategorySection;