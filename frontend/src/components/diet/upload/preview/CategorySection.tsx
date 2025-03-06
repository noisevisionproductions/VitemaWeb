import React, {useState} from "react";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import {ParsedProduct} from "../../../../types/product";
import {useProductCategories} from "../../../../hooks/shopping/useProductCategories";
import ProductCategorizationLayout from "../../../products/ProductCategorizationLayout";
import ParserGuide from "../../../products/ParserGuide";
import LoadingSpinner from "../../../common/LoadingSpinner";
import {Loader2} from "lucide-react";

interface CategorySectionProps {
    uncategorizedProducts: ParsedProduct[];
    categorizedProducts: Record<string, ParsedProduct[]>;
    onProductDrop: (categoryId: string, product: ParsedProduct) => void;
    onProductRemove: (product: ParsedProduct) => void;
    onProductEdit: (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => void;
    onComplete: () => Promise<void>;
    onCancel: () => void;
    selectedUserEmail: string;
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
                                                         }) => {
    const [isSaving, setIsSaving] = useState(false);
    const {categories, loading: loadingCategories} = useProductCategories();

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

    if (loadingCategories) {
        return (
            <div className="flex items-center justify-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">Kategoryzacja produktów</h2>
                <div className="text-gray-600">
                    Użytkownik: {selectedUserEmail}
                </div>
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

            <div className="flex justify-end space-x-4 pt-4">
                <button
                    onClick={onCancel}
                    className="px-4 py-2 mt-7 border rounded-lg hover:bg-gray-50"
                    disabled={isSaving}
                >
                    Anuluj
                </button>
                <button
                    onClick={handleComplete}
                    disabled={uncategorizedProducts.length > 0 || isSaving}
                    className="px-4 py-2 mt-7 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isSaving ? (
                        <>
                            <Loader2 className="h-4 w-4 animate-spin mr-2"/>
                            Zapisywanie kategoryzacji...
                        </>
                    ) : (
                        'Przejdź do podglądu'
                    )}
                </button>
            </div>
        </div>
    );
};

export default CategorySection;