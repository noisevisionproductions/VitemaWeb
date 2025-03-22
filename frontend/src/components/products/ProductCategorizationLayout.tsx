import React, {useState} from "react";
import {Category} from "../../types/product-categories";
import {ParsedProduct} from "../../types/product";
import DraggableProduct from "./DraggableProduct";
import {Tabs, TabsContent, TabsList} from "../ui/Tabs";
import {getLucideIcon} from "../../utils/icons";
import CategoryDropZone from "./CategoryDropZone";
import {Brain, Loader2} from "lucide-react";
import {DroppableTabsTrigger} from "../ui/DroppableTabsTrigger";
import {toast} from "../../utils/toast";
import {useSuggestedCategoriesContext} from "../../contexts/SuggestedCategoriesContext";

interface ProductCategorizationLayoutProps {
    categories: Category[];
    uncategorizedProducts: ParsedProduct[];
    categorizedProducts: Record<string, ParsedProduct[]>;
    onProductDrop: (categoryId: string, product: ParsedProduct) => void;
    onProductRemove: (product: ParsedProduct) => void;
    onProductEdit?: (categoryId: string, oldProduct: ParsedProduct, newProduct: ParsedProduct) => void;
    loading?: boolean;
}

const ProductCategorizationLayout: React.FC<ProductCategorizationLayoutProps> = ({
                                                                                     categories,
                                                                                     uncategorizedProducts,
                                                                                     categorizedProducts,
                                                                                     onProductDrop,
                                                                                     onProductRemove,
                                                                                     onProductEdit,
                                                                                     loading = false
                                                                                 }) => {
    const [activeCategory, setActiveCategory] = useState<string>(
        categories.length > 0 ? categories[0].id : ''
    );
    const [isAutoCategorizing, setIsAutoCategorizing] = useState<boolean>(false);
    const { refreshSuggestions } = useSuggestedCategoriesContext();

    const handleProductCategorize = (product: ParsedProduct, categoryId: string) => {
        onProductDrop(categoryId, {
            ...product,
            categoryId
        });

        setActiveCategory(categoryId);
    };

    const handleUncategorizedProductEdit = (oldProduct: ParsedProduct, newProduct: ParsedProduct) => {
        onProductEdit?.('', oldProduct, newProduct);
    };

    const handleAutoCategorizeBatch = async () => {
        if (uncategorizedProducts.length === 0) return;

        setIsAutoCategorizing(true);

        try {
            const suggestions = await refreshSuggestions(uncategorizedProducts);

            let categorizedCount = 0;

            for (const product of uncategorizedProducts) {
                const key = product.original || product.name;
                const suggestedCategory = suggestions[key];

                if (suggestedCategory) {
                    onProductDrop(suggestedCategory, {
                        ...product,
                        categoryId: suggestedCategory
                    });
                    categorizedCount++;
                }
            }

            if (categorizedCount > 0) {
                toast.success(`Automatycznie skategoryzowano ${categorizedCount} produktów`);
            } else {
                toast.info('Nie udało się automatycznie skategoryzować żadnego produktu');
            }
        } catch (error) {
            toast.error('Wystąpił błąd podczas automatycznej kategoryzacji');
            console.error('Błąd podczas auto-kategoryzacji:', error);
        } finally {
            setIsAutoCategorizing(false);
        }
    };

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
                                        {count > 0 && (
                                            <span className="ml-2 text-xs bg-gray-100 px-2 py-0.5 rounded-full">
                                                {count}
                                            </span>
                                        )}
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
                                    <span className="text-sm text-gray-500">
                                        {uncategorizedProducts.length} pozostało
                                    </span>
                                </h3>
                                {uncategorizedProducts.length > 0 && (
                                    <button
                                        onClick={handleAutoCategorizeBatch}
                                        disabled={isAutoCategorizing || loading}
                                        className="flex items-center gap-2 px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 text-xs"
                                    >
                                        {isAutoCategorizing ? (
                                            <Loader2 className="h-4 w-4 animate-spin"/>
                                        ) : (
                                            <Brain className="h-4 w-4"/>
                                        )}
                                        {isAutoCategorizing ? 'Kategoryzowanie...' : 'Automatycznie'}
                                    </button>
                                )}
                            </div>

                            <div className="p-4 overflow-y-auto h-[calc(100%-60px)]">
                                <div className="space-y-2">
                                    {uncategorizedProducts.map((product, index) => (
                                        <DraggableProduct
                                            key={product.id || `${product.original}-${index}`}
                                            product={product}
                                            onCategorize={handleProductCategorize}
                                            onEdit={handleUncategorizedProductEdit}
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