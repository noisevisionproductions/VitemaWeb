import {useEffect, useState} from "react";
import {ProductCategorization, UncategorizedProduct} from "../types/product-categories";
import {ProductCategorizationService} from "../services/categorization/ProductCategorizationService";
import {toast} from "sonner";
import {DEFAULT_CATEGORIES} from "../data/productCategories";

export const useProductCategories = () => {
    const [productCategories, setProductCategories] = useState<ProductCategorization[]>([]);
    const [uncategorizedProducts, setUncategorizedProducts] = useState<UncategorizedProduct[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchData = async () => {
        try {
            const [productCategoriesData, uncategorizedData] = await Promise.all([
                ProductCategorizationService.getProductCategories(),
                ProductCategorizationService.getUncategorizedProducts()
            ]);

            setProductCategories(productCategoriesData);
            setUncategorizedProducts(uncategorizedData);
        } catch (error) {
            console.error('Error fetching categories:', error);
            toast.error('Błąd podczas pobierania kategorii produktów');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const loadData = async () => {
            await fetchData();
        };

        loadData().catch(console.error);
    }, []);

    return {
        categories: DEFAULT_CATEGORIES,
        productCategories,
        uncategorizedProducts,
        loading,
        refetch: fetchData
    };
};