import React, {useState} from "react";
import {toast} from "../../../../utils/toast";
import {ParsedDietData} from "../../../../types";
import {DietCategorizationService} from "../../../../services/diet/DietCategorizationService";
import CategorySection from "./CategorySection";
import PreviewSection from "./PreviewSection";
import {useCategorization} from "../../../../hooks/shopping/useCategorization";
import {ParsedProduct} from "../../../../types/product";

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
    const [previewProducts, setPreviewProducts] = useState<Record<string, ParsedProduct[]>>({});

    const {
        categorizedProducts,
        uncategorizedProducts,
        handleProductDrop,
        handleProductRemove,
        handleProductEdit
    } = useCategorization(parsedData.shoppingList);

    const handleCategorizeComplete = async () => {
        if (uncategorizedProducts.length > 0) {
            toast.error('Musisz skategoryzować wszystkie produkty');
            return;
        }

        try {
            setIsSaving(true);

            await DietCategorizationService.updateCategories(categorizedProducts);

            const simplifiedData: Record<string, string[]> = {};

            Object.entries(categorizedProducts).forEach(([categoryId, products]) => {
                simplifiedData[categoryId] = products.map(product =>
                    product.original || `${product.name} ${product.quantity} ${product.unit}`
                );
            });

            parsedData.categorizedProducts = simplifiedData;

            setPreviewProducts(categorizedProducts);

            toast.success('Kategoryzacja została zapisana');
            setStep('preview');
        } catch (error) {
            console.error('Błąd podczas zapisywania kategoryzacji:', error);
            toast.error('Wystąpił błąd podczas zapisywania kategoryzacji');
        } finally {
            setIsSaving(false);
        }
    };

    const handleConfirm = async () => {
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

    return step === 'categorization' ? (
        <CategorySection
            uncategorizedProducts={uncategorizedProducts}
            categorizedProducts={categorizedProducts}
            onProductDrop={handleProductDrop}
            onProductRemove={handleProductRemove}
            onProductEdit={handleProductEdit}
            onComplete={handleCategorizeComplete}
            onCancel={onCancel}
            selectedUserEmail={selectedUserEmail}
        />
    ) : (
        <PreviewSection
            parsedData={parsedData}
            categorizedProducts={previewProducts}
            onSave={handleConfirm}
            onCancel={onCancel}
            isSaving={isSaving}
            selectedUserEmail={selectedUserEmail}
        />
    );
};

export default DietPreview;