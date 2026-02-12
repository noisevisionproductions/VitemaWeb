import React from "react";
import {ParsedProduct} from "../../../../types/product";
import {Check, ShoppingBasket} from "lucide-react";
import {formatCategoryName, getCategoryColor} from "../../../../utils/diet/shoppingListUtils";

interface ShoppingListViewProps {
    items: Record<string, ParsedProduct[] | any[]>;
    emptyMessage?: string;
}

const ShoppingListView: React.FC<ShoppingListViewProps> = ({
                                                               items,
                                                               emptyMessage = "Lista zakupów jest pusta."
                                                           }) => {
    const categories = Object.keys(items);

    if (categories.length === 0) {
        return (
            <div
                className="flex flex-col items-center justify-center py-12 text-gray-500 bg-gray-50 rounded-lg border border-dashed">
                <ShoppingBasket className="h-10 w-10 mb-3 opacity-20"/>
                <p>{emptyMessage}</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {categories.map((categoryKey) => {
                const products = items[categoryKey];
                if (!products || products.length === 0) return null;

                const categoryName = formatCategoryName(categoryKey);

                return (
                    <div key={categoryKey} className="bg-white rounded-xl border shadow-sm overflow-hidden">
                        <div className="bg-gray-50 px-4 py-3 border-b flex items-center gap-2">
                            <div className={`w-2 h-2 rounded-full ${getCategoryColor(categoryKey)}`}/>
                            <h3 className="font-medium text-gray-700 capitalize">
                                {categoryName}
                            </h3>
                            <span className="text-xs text-gray-400 ml-auto bg-white px-2 py-0.5 rounded border">
                                {products.length} poz.
                            </span>
                        </div>
                        <ul className="divide-y divide-gray-100">
                            {products.map((item: any, index: number) => (
                                <li key={index}
                                    className="px-4 py-3 flex items-start gap-3 hover:bg-gray-50 transition-colors group">
                                    <div className="mt-0.5 text-gray-300 group-hover:text-primary transition-colors">
                                        <Check className="h-4 w-4"/>
                                    </div>
                                    <div className="flex-1">
                                        <div className="text-sm font-medium text-gray-900">
                                            {item.name || item.original}
                                        </div>
                                        {(item.quantity > 0) && (
                                            <div className="text-xs text-gray-500">
                                                {item.quantity} {item.unit}
                                            </div>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                );
            })}
        </div>
    );
};

export default ShoppingListView;