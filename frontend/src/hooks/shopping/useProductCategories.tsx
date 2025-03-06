import React, {createContext, useContext, useState, useEffect} from 'react';
import {Category} from "../../types/product-categories";
import {DietCategorizationService} from "../../services/DietCategorizationService";
import {toast} from 'sonner';

interface ProductCategoriesContextType {
    categories: Category[];
    loading: boolean;
    refreshCategories: () => Promise<void>;
}

const ProductCategoriesContext = createContext<ProductCategoriesContextType | null>(null);

export const useProductCategories = () => {
    const context = useContext(ProductCategoriesContext);
    if (!context) {
        throw new Error('useProductCategories must be used within a ProductCategoriesProvider');
    }
    return context;
};

export const ProductCategoriesProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);

    const refreshCategories = async () => {
        try {
            setLoading(true);
            const data = await DietCategorizationService.getCategories();
            setCategories(data.sort((a, b) => a.order - b.order));
        } catch (error) {
            console.error('Error fetching categories:', error);
            toast.error('Nie udało się pobrać kategorii produktów');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        refreshCategories().catch(console.error);
    }, []);

    return (
        <ProductCategoriesContext.Provider value={{categories, loading, refreshCategories}}>
            {children}
        </ProductCategoriesContext.Provider>
    );
};