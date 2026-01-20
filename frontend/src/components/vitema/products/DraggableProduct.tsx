import React, {useEffect, useState} from "react";
import {useDrag} from "react-dnd";
import {Loader2, Package, Pencil, X} from "lucide-react";
import {ParsedProduct} from "../../../types/product";
import CategorySuggestion from "./CategorySuggestion";
import {useSuggestedCategoriesContext} from "../../../contexts/SuggestedCategoriesContext";
import {useProductCategories} from "../../../hooks/shopping/useProductCategories";

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
    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [isProcessing, setIsProcessing] = useState<boolean>(false);
    const [editedQuantity, setEditedQuantity] = useState<number>(product.quantity);
    const [editedUnit, setEditedUnit] = useState<string>(product.unit);
    const [editedName, setEditedName] = useState<string>(product.name);

    // Dodaj dostęp do kontekstu sugestii kategorii
    const { suggestionCache } = useSuggestedCategoriesContext();
    const { categories } = useProductCategories();

    // Pobierz sugerowaną kategorię z cache
    const productKey = product.original || product.name;
    const suggestedCategoryId = suggestionCache[productKey];

    // Znajdź obiekt kategorii na podstawie ID
    const suggestedCategory = suggestedCategoryId ?
        categories.find(cat => cat.id === suggestedCategoryId) : null;

    // Sprawdź, czy produkt ma sugestię kategorii
    const hasSuggestion = !!suggestedCategory && !inCategory;

    useEffect(() => {
        setEditedQuantity(product.quantity);
        setEditedUnit(product.unit);
        setEditedName(product.name);
    }, [product]);

    const [{isDragging}, drag] = useDrag<DragItem, unknown, { isDragging: boolean }>(() => ({
        type: 'PRODUCT',
        item: {product, fromCategory: inCategory},
        collect: (monitor) => ({
            isDragging: monitor.isDragging()
        })
    }));

    const handleSuggestionAccept = (categoryId: string) => {
        onCategorize?.(product, categoryId);
    };

    const handleEditSave = () => {
        if (!onEdit) return;

        if (editedQuantity <= 0) {
            alert('Ilość musi być większa od zera');
            return;
        }

        if (!editedName.trim()) {
            alert('Nazwa produktu nie może być pusta');
            return;
        }

        setIsProcessing(true);

        const editedProduct: ParsedProduct = {
            ...product,
            name: editedName.trim(),
            quantity: editedQuantity,
            unit: editedUnit.trim(),
            original: product.original
        };

        onEdit(product, editedProduct);

        setIsProcessing(false);
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
                                min="0.01"
                                step="0.01"
                                value={editedQuantity}
                                onChange={(e) => setEditedQuantity(Number(e.target.value))}
                                className="w-16 px-1 py-0.5 border rounded text-sm"
                                disabled={isProcessing}
                            />
                            <input
                                type="text"
                                value={editedUnit}
                                onChange={(e) => setEditedUnit(e.target.value)}
                                className="w-12 px-1 py-0.5 border rounded text-sm"
                                disabled={isProcessing}
                            />
                        </div>
                        <input
                            type="text"
                            value={editedName}
                            onChange={(e) => setEditedName(e.target.value)}
                            className="flex-1 px-1 py-0.5 border rounded text-sm"
                            disabled={isProcessing}
                        />
                        <div className="flex gap-1">
                            <button
                                onClick={handleEditSave}
                                disabled={isProcessing}
                                className="px-2 py-1 text-xs bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50 flex items-center gap-1"
                            >
                                {isProcessing && <Loader2 className="h-3 w-3 animate-spin" />}
                                {isProcessing ? 'Zapisywanie...' : 'Zapisz'}
                            </button>
                            <button
                                onClick={() => setIsEditing(false)}
                                disabled={isProcessing}
                                className="px-2 py-1 text-xs bg-gray-500 text-white rounded hover:bg-gray-600 disabled:opacity-50"
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

            {/* Wyświetl sugestię kategorii bezpośrednio, jeśli istnieje i produkt nie jest w kategorii */}
            {hasSuggestion && (
                <div
                    className="mt-2 text-xs bg-blue-50 text-blue-600 p-2 rounded flex items-center justify-between cursor-pointer hover:bg-blue-100"
                    onClick={() => onCategorize?.(product, suggestedCategoryId)}
                >
                    <span>Sugerowana kategoria: {suggestedCategory.name}</span>
                    <button className="text-blue-700 hover:text-blue-800 font-medium ml-2 px-2 py-0.5 bg-blue-100 rounded">
                        Użyj
                    </button>
                </div>
            )}

            {!inCategory && !hasSuggestion && (
                <CategorySuggestion
                    product={product}
                    onSuggestionAccept={handleSuggestionAccept}
                />
            )}
        </div>
    );
};

export default DraggableProduct;