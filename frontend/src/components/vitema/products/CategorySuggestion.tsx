import React, {useContext, useEffect, useState} from "react";
import {ParsedProduct} from "../../../types/product";
import {ArrowRight, Lightbulb} from "lucide-react";
import {useProductCategories} from "../../../hooks/shopping/useProductCategories";
import { SuggestedCategoriesContext } from "../../../contexts/SuggestedCategoriesContext";

interface CategorySuggestionProps {
    product: ParsedProduct;
    onSuggestionAccept: (categoryId: string) => void;
}

const CategorySuggestion: React.FC<CategorySuggestionProps> = ({
                                                                   product,
                                                                   onSuggestionAccept
                                                               }) => {
    const [suggestedCategoryId, setSuggestedCategoryId] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const {categories} = useProductCategories();
    const {getSuggestion} = useContext(SuggestedCategoriesContext);

    useEffect(() => {
        const loadSuggestion = async () => {
            if (!product) return;

            setIsLoading(true);
            try {
                const suggestion = await getSuggestion(product);
                setSuggestedCategoryId(suggestion);
            } catch (error) {
                console.error('Error getting category suggestion:', error);
                setSuggestedCategoryId(null);
            } finally {
                setIsLoading(false);
            }
        };

        loadSuggestion().catch(console.error);
    }, [product.original]);

    if (isLoading) {
        return (
            <div className="flex items-center gap-2 text-gray-400 animate-pulse">
                <Lightbulb className="h-4 w-4"/>
                <span>
                    Szukam sugestii...
                </span>
            </div>
        );
    }

    if (!suggestedCategoryId) {
        return null;
    }

    const suggestedCategory = categories.find(cat => cat.id === suggestedCategoryId);
    if (!suggestedCategory) return null;

    return (
        <div className="flex items-center gap-2 p-2 bg-blue-50 rounded-lg">
            <Lightbulb className="h-4 w-4 text-blue-500"/>
            <span className="text-sm">
                Sugerowana kategoria:
                <span className="font-medium ml-1" style={{color: suggestedCategory.color}}>
                    {suggestedCategory.name}
                </span>
            </span>
            <button
                onClick={() => onSuggestionAccept(suggestedCategoryId)}
                className="ml-auto flex items-center gap-1 text-sm text-blue-600 hover:text-blue-700"
            >
                Zastosuj
                <ArrowRight className="h-4 w-4"/>
            </button>
        </div>
    );
};

export default CategorySuggestion;