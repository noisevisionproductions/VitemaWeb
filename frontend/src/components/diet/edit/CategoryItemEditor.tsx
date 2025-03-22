import React, {useEffect, useState} from 'react';
import { Edit2, ShoppingBag, Trash2 } from 'lucide-react';
import { CategorizedShoppingListItem} from "../../../types";
import {toast} from "../../../utils/toast";

interface CategoryItemEditorProps {
    item: CategorizedShoppingListItem;
    onEdit: (newValue: CategorizedShoppingListItem) => Promise<void>;
    onDelete: () => Promise<void>;
}

const CategoryItemEditor: React.FC<CategoryItemEditorProps> = ({
                                                                   item,
                                                                   onEdit,
                                                                   onDelete
                                                               }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editData, setEditData] = useState(item);

    useEffect(() => {
        setEditData(item);
    }, [item]);

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Escape' && isEditing) {
            e.stopPropagation();
            setEditData(item);
            setIsEditing(false);
        }
    };

    const handleSave = async () => {
        if (editData.name.trim() === '') {
            toast.error('Nazwa produktu nie może być pusta');
            return;
        }

        try {
            await onEdit(editData);
            setIsEditing(false);
        } catch (error) {
            console.error('Error updating item:', error);
        }
    };

    return (
        <div className="group">
            {isEditing ? (
                <div className="bg-gray-50 p-2 rounded-lg space-y-2" onKeyDown={handleKeyDown}>
                    <div className="flex flex-wrap gap-1">
                        <div className="w-full sm:w-auto">
                            <label className="block text-xs text-gray-600 mb-1">Ilość</label>
                            <input
                                type="number"
                                value={editData.quantity}
                                onChange={(e) => setEditData({
                                    ...editData,
                                    quantity: parseFloat(e.target.value) || 0
                                })}
                                className="w-full sm:w-24 border rounded px-1 py-1 focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <label className="block text-xs text-gray-600 mb-1">Jednostka</label>
                            <input
                                type="text"
                                value={editData.unit}
                                onChange={(e) => setEditData({
                                    ...editData,
                                    unit: e.target.value
                                })}
                                className="w-full sm:w-24 border rounded px-1 py-1 focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="flex-1">
                            <label className="block text-xs text-gray-600 mb-1">Nazwa produktu</label>
                            <input
                                type="text"
                                value={editData.name}
                                onChange={(e) => setEditData({
                                    ...editData,
                                    name: e.target.value
                                })}
                                className="w-full border rounded px-1 py-1 focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                    <div className="flex justify-end gap-1 pt-1">
                        <button
                            onClick={() => {
                                setEditData(item);
                                setIsEditing(false);
                            }}
                            className="px-2 py-1 border rounded-lg hover:bg-gray-50"
                        >
                            Anuluj
                        </button>
                        <button
                            onClick={handleSave}
                            className="px-2 py-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                        >
                            Zapisz
                        </button>
                    </div>
                </div>
            ) : (
                <div className="p-2 bg-gray-50 rounded-lg space-y-1">
                    <div className="flex items-center gap-1">
                        <ShoppingBag className="h-3 w-3 text-gray-400"/>
                        <span>
                            {item.quantity} {item.unit} {item.name}
                        </span>
                    </div>
                    <div className="flex items-center gap-1 pt-0.5">
                        <button
                            onClick={() => setIsEditing(true)}
                            className="p-1 text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded"
                        >
                            <Edit2 className="h-4 w-4"/>
                        </button>
                        <button
                            onClick={onDelete}
                            className="p-1 text-red-600 hover:text-red-700 hover:bg-red-50 rounded"
                        >
                            <Trash2 className="h-3 w-3"/>
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CategoryItemEditor;