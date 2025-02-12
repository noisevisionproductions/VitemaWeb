import React, {useState} from "react";
import {useDrag} from "react-dnd";
import {Package, Pencil, X} from "lucide-react";
import {ParsedProduct} from "../../types/product";
import CategorySuggestion from "./CategorySuggestion";

interface DraggableProductProps {
    product: ParsedProduct;
    inCategory?: boolean;
    onRemove?: (product: ParsedProduct) => void;
    onCategorize?: (product: ParsedProduct, categoryId: string) => void;
    onEdit?: (oldProduct: ParsedProduct, editedProduct: ParsedProduct) => void;
}

type DragItem = {
    product: ParsedProduct;
    fromCategory: boolean;
};

const DraggableProduct: React.FC<DraggableProductProps> = ({
                                                               product,
                                                               inCategory = false,
                                                               onRemove,
                                                               onCategorize,
                                                               onEdit
                                                           }) => {
    const [{isDragging}, drag] = useDrag<DragItem, unknown, { isDragging: boolean }>(() => ({
        type: 'PRODUCT',
        item: {product, fromCategory: inCategory},
        collect: (monitor) => ({
            isDragging: monitor.isDragging()
        })
    }));

    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [editedQuantity, setEditedQuantity] = useState<number>(product.quantity);
    const [editedUnit, setEditedUnit] = useState<string>(product.unit);
    const [editedName, setEditedName] = useState<string>(product.name);

    const handleSuggestionAccept = (categoryId: string) => {
        onCategorize?.(product, categoryId);
    };

    const handleEditSave = () => {
        const editedProduct: ParsedProduct = {
            ...product,
            quantity: editedQuantity,
            unit: editedUnit,
            name: editedName
        };

        onEdit?.(product, editedProduct);
        setIsEditing(false);
    };

    return (
        <div className={`space-y-2 ${isDragging ? 'opacity-50' : 'opacity-100'}`}>
            <div
                ref={drag}
                className={`flex items-center gap-2 p-2 bg-white rounded border border-gray-200 
                    ${!isEditing && 'cursor-move'}
                    ${inCategory ? 'hover:bg-red-50 hover:border-red-200' : ''}`}
            >
                <Package className="h-4 w-4 text-gray-400 shrink-0"/>

                {isEditing ? (
                    <div className="flex items-center gap-2 flex-1">
                        <div className="flex gap-1 min-w-[120px]">
                            <input
                                type="number"
                                value={editedQuantity}
                                onChange={(e) => setEditedQuantity(Number(e.target.value))}
                                className="w-16 px-1 py-0.5 border rounded text-sm"
                            />
                            <input
                                type="text"
                                value={editedUnit}
                                onChange={(e) => setEditedUnit(e.target.value)}
                                className="w-12 px-1 py-0.5 border rounded text-sm"
                            />
                        </div>
                        <input
                            type="text"
                            value={editedName}
                            onChange={(e) => setEditedName(e.target.value)}
                            className="flex-1 px-1 py-0.5 border rounded text-sm"
                        />
                        <div className="flex gap-1">
                            <button
                                onClick={handleEditSave}
                                className="px-2 py-1 text-xs bg-green-500 text-white rounded hover:bg-green-600"
                            >
                                Zapisz
                            </button>
                            <button
                                onClick={() => setIsEditing(false)}
                                className="px-2 py-1 text-xs bg-gray-500 text-white rounded hover:bg-gray-600"
                            >
                                Anuluj
                            </button>
                        </div>
                    </div>
                ) : (
                    <>
                        <div className="flex items-center gap-2 min-w-[100px] text-gray-500 text-sm">
                            <span>{product.quantity}</span>
                            <span>{product.unit}</span>
                        </div>
                        <span className="text-sm flex-1">
                            {product.name}
                        </span>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setIsEditing(true)}
                                className="p-1 text-gray-400 hover:text-gray-600"
                                title="Edytuj"
                            >
                                <Pencil className="h-4 w-4"/>
                            </button>
                            {inCategory && (
                                <button
                                    onClick={() => onRemove?.(product)}
                                    className="p-1 text-gray-400 hover:text-red-600"
                                    title="Usuń"
                                >
                                    <X className="h-4 w-4"/>
                                </button>
                            )}
                        </div>
                    </>
                )}
            </div>

            {!inCategory && (
                <CategorySuggestion
                    product={product}
                    onSuggestionAccept={handleSuggestionAccept}
                />
            )}
        </div>

    );
};

export default DraggableProduct;