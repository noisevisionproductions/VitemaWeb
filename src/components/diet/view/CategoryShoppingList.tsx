import React, { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { formatDate} from "../../../utils/dateFormatters";
import { ShoppingListV3, CategorizedShoppingListItem} from "../../../types";
import LoadingSpinner from "../../common/LoadingSpinner";
import { getCategoryLabel} from "../../../utils/productUtils";

interface CategoryShoppingListProps {
    shoppingList: ShoppingListV3;
    loading: boolean;
}

const CategoryShoppingList: React.FC<CategoryShoppingListProps> = ({
                                                                       shoppingList,
                                                                       loading
                                                                   }) => {
    const [isExpanded, setIsExpanded] = useState(false);

    if (loading) {
        return (
            <div className="flex justify-center py-4">
                <LoadingSpinner />
            </div>
        );
    }

    const totalItems = Object.values(shoppingList.items)
        .reduce((sum, items) => sum + items.length, 0);

    return (
        <div className="bg-gray-50 p-4 rounded-lg">
            <div
                className="flex justify-between items-center mb-3 cursor-pointer"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <h3 className="text-lg font-medium flex items-center gap-2">
                    Lista zakupów
                    {!isExpanded && (
                        <span className="text-sm text-gray-500">
                            ({totalItems} produktów)
                        </span>
                    )}
                </h3>
                <div className="text-sm text-gray-600">
                    {formatDate(shoppingList.startDate)} - {formatDate(shoppingList.endDate)}
                    <ChevronDown
                        className={`ml-2 inline-block transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                        size={16}
                    />
                </div>
            </div>

            {isExpanded && (
                <div className="space-y-4">
                    {Object.entries(shoppingList.items).map(([categoryId, items]) => (
                        <CategorySection
                            key={categoryId}
                            categoryId={categoryId}
                            items={items}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

const CategorySection: React.FC<{
    categoryId: string;
    items: CategorizedShoppingListItem[];
}> = ({ categoryId, items }) => {
    const [isExpanded, setIsExpanded] = useState(true);

    return (
        <div className="bg-white p-3 rounded-lg shadow-sm">
            <div
                className="flex justify-between items-center cursor-pointer"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <h4 className="font-medium text-gray-700">
                    {getCategoryLabel(categoryId)}
                </h4>
                <ChevronDown
                    className={`transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                    size={14}
                />
            </div>

            {isExpanded && (
                <ul className="mt-2 space-y-1">
                    {items.map((item, index) => (
                        <li key={index} className="text-gray-600 flex items-center gap-2">
                            <span className="text-sm">
                                {item.quantity} {item.unit}
                            </span>
                            <span>{item.name}</span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default CategoryShoppingList;