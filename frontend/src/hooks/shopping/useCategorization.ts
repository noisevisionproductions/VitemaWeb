import { useState, useEffect } from "react";
import { ParsedProduct } from "../../types/product";
import { ProductParsingService } from "../../services/ProductParsingService";
import { createSafeProduct } from "../../utils/productUtils";
import {DietUploadService} from "../../services/DietUploadService";
import {toast} from "sonner";

export const useCategorization = (shoppingList: string[]) => {
    const [categorizedProducts, setCategorizedProducts] = useState<Record<string, ParsedProduct[]>>({});
    const [uncategorizedProducts, setUncategorizedProducts] = useState<ParsedProduct[]>([]);

    useEffect(() => {
        if (!shoppingList || !Array.isArray(shoppingList)) {
            console.warn('shoppingList nie jest tablicą:', shoppingList);
            setUncategorizedProducts([]);
            return;
        }

        const parsedProducts = shoppingList
            .map(product => {
                try {
                    const parseResult = ProductParsingService.parseProduct(product);
                    return parseResult.success ? parseResult.product : createSafeProduct(product);
                } catch (error) {
                    console.error('Błąd podczas przetwarzania produktu:', product, error);
                    return createSafeProduct('unknown');
                }
            })
            .filter((product): product is ParsedProduct => product !== undefined && product !== null);

        setUncategorizedProducts(parsedProducts);
    }, [shoppingList]);

    // Funkcja pomocnicza do identyfikacji produktów
    const getProductIdentifier = (product: ParsedProduct) => {
        return product.id || product.original || `${product.name}-${product.quantity}-${product.unit}`;
    };

    // Obsługa przeciągania produktu do kategorii
    const handleProductDrop = (categoryId: string, product: ParsedProduct) => {
        const productId = getProductIdentifier(product);

        setUncategorizedProducts(prev =>
            prev.filter(p => getProductIdentifier(p) !== productId)
        );

        setCategorizedProducts(prev => {
            const updatedProduct = { ...product, categoryId };

            return {
                ...prev,
                [categoryId]: [...(prev[categoryId] || []), updatedProduct]
            };
        });
    };

    // Obsługa usuwania produktu z kategorii
    const handleProductRemove = (product: ParsedProduct) => {
        const productId = getProductIdentifier(product);

        if (product.categoryId) {
            setCategorizedProducts(prev => {
                const newProducts = { ...prev };

                if (newProducts[product.categoryId!]) {
                    newProducts[product.categoryId!] = newProducts[product.categoryId!].filter(
                        p => getProductIdentifier(p) !== productId
                    );

                    if (newProducts[product.categoryId!].length === 0) {
                        delete newProducts[product.categoryId!];
                    }
                }

                return newProducts;
            });
        }

        setUncategorizedProducts(prev => [
            ...prev,
            { ...product, categoryId: undefined }
        ]);
    };

    // Obsługa edycji produktu
    const handleProductEdit = async (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => {
        try {
            const updatedProduct = await DietUploadService.updateProduct(oldProduct, newProduct);

            if (categoryId) {
                setCategorizedProducts(prev => {
                    const newProducts = { ...prev };

                    if (newProducts[categoryId]) {
                        newProducts[categoryId] = newProducts[categoryId].map(p =>
                            getProductIdentifier(p) === getProductIdentifier(oldProduct) ? updatedProduct : p
                        );
                    }

                    return newProducts;
                });
            } else {
                setUncategorizedProducts(prev =>
                    prev.map(p => getProductIdentifier(p) === getProductIdentifier(oldProduct) ? updatedProduct : p)
                );
            }
        } catch (error) {
            console.error('Błąd podczas aktualizacji produktu:', error);
            toast.error('Nie udało się zaktualizować produktu');
        }
    };

    return {
        categorizedProducts,
        uncategorizedProducts,
        handleProductDrop,
        handleProductRemove,
        handleProductEdit
    };
};