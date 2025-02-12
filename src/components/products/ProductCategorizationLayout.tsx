import React, {useEffect, useState} from "react";
import {Category} from "../../types/product-categories";
import {ParsedProduct} from "../../types/product";
import DraggableProduct from "./DraggableProduct";
import {Tabs, TabsContent, TabsList} from "../ui/Tabs";
import {getLucideIcon} from "../../utils/icons";
import CategoryDropZone from "./CategoryDropZone";
import {ProductCategorizationService} from "../../services/categorization/ProductCategorizationService";
import {toast} from "sonner";
import {Brain} from "lucide-react";
import {DroppableTabsTrigger} from "../ui/DroppableTabsTrigger";

interface ProductCategorizationLayoutProps {
    categories: Category[];
    uncategorizedProducts: ParsedProduct[];
    categorizedProducts: Record<string, ParsedProduct[]>;
    onProductDrop: (categoryId: string, product: ParsedProduct) => void;
    onProductRemove: (product: ParsedProduct) => void;
    onProductEdit?: (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => void;
}

const ProductCategorizationLayout: React.FC<ProductCategorizationLayoutProps> = ({
                                                                                     categories,
                                                                                     uncategorizedProducts,
                                                                                     categorizedProducts,
                                                                                     onProductDrop,
                                                                                     onProductRemove,
                                                                                     onProductEdit
                                                                                 }) => {
    const [activeCategory, setActiveCategory] = useState(categories[0]?.id);
    const [categoryStats, setCategoryStats] = useState<Record<string, number>>({});
    const [isAutoCategorizing, setIsAutoCategorizing] = useState(false);

    useEffect(() => {
        const hasAnyProducts = Object.values(categorizedProducts).some(products => products.length > 0);
        if (hasAnyProducts) {
            loadCategoryStats().catch(console.error);
        }
    }, [categorizedProducts]);

    const loadCategoryStats = async () => {
        try {
            const stats = await ProductCategorizationService.getCategoryStats();
            setCategoryStats(stats);
        } catch (error) {
            console.error('Błąd podczas ładowania statystyk:', error);
        }
    };

    const handleProductCategorize = (product: ParsedProduct, categoryId: string) => {
        onProductDrop(categoryId, product);
        setActiveCategory(categoryId);
    };

    const handleAutoCategorizeBatch = async () => {
        setIsAutoCategorizing(true);
        try {
            let categorizedCount = 0;
            for (const product of uncategorizedProducts) {
                const suggestedCategory = await ProductCategorizationService.suggestCategory(product);
                if (suggestedCategory) {
                    onProductDrop(suggestedCategory, product);
                    categorizedCount++;
                }
            }
            toast.success(`Automatycznie skategoryzowano ${categorizedCount} produktów`);
        } catch (error) {
            toast.error('Wystąpił błąd podczas automatycznej kategoryzacji');
            console.error('Error during auto-categorization:', error);
        } finally {
            setIsAutoCategorizing(false);
        }
    };

    /*   const handleProductEdit = (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => {
           onProductEdit?.(categoryId, oldProduct, newProduct);
       };*/
    return (
        <div className="grid grid-cols-12 gap-6 h-[calc(100vh-200px)]">
            {/* Kategorie jako zakładki */}
            <div className="col-span-12">
                <Tabs
                    value={activeCategory}
                    onValueChange={setActiveCategory}
                    className="flex flex-col h-full"
                >
                    <div className="flex justify-between items-center mb-4">
                        <TabsList
                            className="w-full flex flex-wrap gap-2 shrink-0 p-2 bg-gray-100 rounded-lg shadow-inner min-h-[150px]">
                            {categories.map(category => {
                                const Icon = getLucideIcon(category.icon);
                                const count = categorizedProducts[category.id]?.length || 0;
                                const showStats = Object.values(categorizedProducts).some(p => p.length > 0);
                                const totalUsage = showStats ? (categoryStats[category.id] || 0) : 0;

                                return (
                                    <DroppableTabsTrigger
                                        key={category.id}
                                        value={category.id}
                                        categoryColor={category.color}
                                        onDrop={(product) => onProductDrop(category.id, product)}
                                        className="flex items-center gap-2 px-4 py-2 bg-white rounded-lg shadow hover:bg-gray-50 transition"
                                    >
                                        {Icon && <Icon className="h-4 w-4"/>}
                                        {category.name}
                                        <div className="flex gap-2">
                                            {count > 0 && (
                                                <span className="ml-2 text-xs bg-gray-100 px-2 py-0.5 rounded-full">
                                            {count}
                                        </span>
                                            )}
                                            {showStats && totalUsage > 0 && (
                                                <span
                                                    className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full"
                                                    title="Całkowita liczba użyć">
                                                    {totalUsage}
                                                </span>
                                            )}
                                        </div>
                                    </DroppableTabsTrigger>
                                );
                            })}
                        </TabsList>
                    </div>

                    {/* Zawartość kategorii */}
                    <div className="grid grid-cols-12 gap-6 mt-6">
                        {/* Lista produktów do skategoryzowania */}
                        <div className="col-span-4 bg-white rounded-lg shadow overflow-hidden h-[calc(100vh-350px)]">
                            <div className="p-4 border-b flex justify-between items-center">
                                <h3 className="font-medium flex items-center gap-2">
                                    Produkty do przypisania
                                    <span
                                        className="text-sm text-gray-500">{uncategorizedProducts.length} pozostało
                                    </span>
                                </h3>
                                {uncategorizedProducts.length > 0 && (
                                    <button
                                        onClick={handleAutoCategorizeBatch}
                                        disabled={isAutoCategorizing}
                                        className="flex items-center gap-2 px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 text-xs"
                                    >
                                        <Brain className="h-4 w-4"/>
                                        {isAutoCategorizing ? 'Kategoryzowanie...' : 'Automatyczna kategoryzacja'}
                                    </button>
                                )}
                            </div>

                            <div className="p-4 overflow-y-auto h-[calc(100%-60px)]">
                                <div className="space-y-2">
                                    {uncategorizedProducts.map(product => (
                                        <DraggableProduct
                                            key={product.original}
                                            product={product}
                                            onCategorize={handleProductCategorize}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Pole do kategoryzacji */}
                        <div className="col-span-8 h-[calc(100vh-350px)]">
                            {categories.map(category => (
                                <TabsContent
                                    key={category.id}
                                    value={category.id}
                                    className="h-full w-full"
                                >
                                    <CategoryDropZone
                                        category={category}
                                        products={categorizedProducts[category.id] || []}
                                        onProductDrop={(product) => onProductDrop(category.id, product)}
                                        onProductRemove={onProductRemove}
                                        onProductEdit={(oldProduct, newProduct) =>
                                            onProductEdit?.(category.id, oldProduct, newProduct)
                                        }
                                    />
                                </TabsContent>
                            ))}
                        </div>
                    </div>
                </Tabs>
            </div>
        </div>
    );
};

export default ProductCategorizationLayout;