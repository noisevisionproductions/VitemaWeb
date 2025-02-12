import React, {useEffect, useState} from "react";
import {ParsedProduct} from "../../types/product";
import {ProductCategorizationService} from "../../services/categorization/ProductCategorizationService";
import {ArrowRight, Lightbulb} from "lucide-react";
import {getCategoryById} from "../../data/productCategories";

interface CategorySuggestionProps {
    product: ParsedProduct;
    onSuggestionAccept: (categoryId: string) => void;
}

const CategorySuggestion: React.FC<CategorySuggestionProps> = ({
                                                                   product,
                                                                   onSuggestionAccept
                                                               }) => {
    const [suggestedCategoryId, setSuggestedCategoryId] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const loadSuggestion = async () => {
            setIsLoading(true);
            const suggestion = await ProductCategorizationService.suggestCategory(product);
            setSuggestedCategoryId(suggestion);
            setIsLoading(false);
        };

        loadSuggestion().catch(console.error);
    }, [product]);

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

    const suggestedCategory = getCategoryById(suggestedCategoryId);
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