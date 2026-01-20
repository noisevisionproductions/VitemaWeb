import React from "react";
import {Category} from "../../../types/productCategories";
import {useDrop} from "react-dnd";
import DraggableProduct from "./DraggableProduct";
import {ParsedProduct} from "../../../types/product";

interface CategoryDropZoneProps {
    category: Category;
    products: ParsedProduct[];
    onProductDrop: (product: ParsedProduct) => void;
    onProductRemove: (product: ParsedProduct) => void;
    onProductEdit?: (oldProduct: ParsedProduct, newProduct: ParsedProduct) => void;
}

type DropItem = {
    product: ParsedProduct;
    fromCategory: boolean;
};

const CategoryDropZone: React.FC<CategoryDropZoneProps> = ({
                                                               category,
                                                               products,
                                                               onProductDrop,
                                                               onProductRemove,
                                                               onProductEdit
                                                           }) => {
    const [{ isOver }, drop] = useDrop<DropItem, void, { isOver: boolean }>(() => ({
        accept: 'PRODUCT',
        drop: (item) => {
            if (!item.fromCategory) {
                onProductDrop(item.product);
            }
        },
        collect: (monitor) => ({
            isOver: monitor.isOver()
        })
    }));

    return (
        <div
            ref={drop}
            className={`h-full p-4 rounded-lg shadow ${isOver ? 'bg-blue-50' : 'bg-white'} flex flex-col`}
            style={{backgroundColor: isOver ? undefined : category.color + '15'}}
        >
            <div className="mb-4 px-4 py-2 border-b">
                <h3 className="font-medium text-gray-700">
                    {category.name}
                </h3>
            </div>

            {products.length === 0 ? (
                <div
                    className="flex-1 flex items-center justify-center text-gray-400 border-2 border-dashed rounded-lg">
                    Przeciągnij produkty tutaj
                </div>
            ) : (
                <div className="flex-1 space-y-2 overflow-y-auto">
                    {products.map((product, index) => (
                        <DraggableProduct
                            key={`${category.id}-${product.original}-${index}`}
                            product={product}
                            inCategory={true}
                            onRemove={onProductRemove}
                            onEdit={(oldProduct, newProduct) =>
                                onProductEdit?.(oldProduct, newProduct)
                            }
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

export default CategoryDropZone;