import React, {useState} from 'react';
import {Plus} from 'lucide-react';
import {CategorizedShoppingListItem} from "../../../../../../types";
import {toast} from "../../../../../../utils/toast";

interface AddProductButtonProps {
    categoryId: string;
    onAdd: (item: CategorizedShoppingListItem) => Promise<void>;
}

const AddProductButton: React.FC<AddProductButtonProps> = ({onAdd}) => {
    const [isAdding, setIsAdding] = useState(false);
    const [newItem, setNewItem] = useState<CategorizedShoppingListItem>({
        name: '',
        quantity: 1,
        unit: 'g',
        original: ''
    });

    const handleSubmit = async () => {
        if (!newItem.name.trim()) {
            toast.error('Nazwa produktu nie może być pusta');
            return;
        }

        try {
            await onAdd({
                ...newItem,
                original: `${newItem.quantity} ${newItem.unit} ${newItem.name}`
            });
            setIsAdding(false);
            setNewItem({name: '', quantity: 1, unit: 'g', original: ''});
        } catch (error) {
            console.error('Error adding item:', error);
            toast.error('Błąd podczas dodawania produktu');
        }
    };

    if (isAdding) {
        return (
            <div className="bg-gray-50 p-4 rounded-lg space-y-3">
                <div className="flex flex-wrap gap-2">
                    <div className="w-full sm:w-auto">
                        <label className="block text-sm text-gray-600 mb-1">Ilość</label>
                        <input
                            type="number"
                            value={newItem.quantity}
                            onChange={(e) => setNewItem({
                                ...newItem,
                                quantity: parseFloat(e.target.value) || 0
                            })}
                            className="w-full sm:w-24 border rounded px-2 py-1.5 focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div className="w-full sm:w-auto">
                        <label className="block text-sm text-gray-600 mb-1">Jednostka</label>
                        <input
                            type="text"
                            value={newItem.unit}
                            onChange={(e) => setNewItem({
                                ...newItem,
                                unit: e.target.value
                            })}
                            className="w-full sm:w-24 border rounded px-2 py-1.5 focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div className="flex-1">
                        <label className="block text-sm text-gray-600 mb-1">Nazwa produktu</label>
                        <input
                            type="text"
                            value={newItem.name}
                            onChange={(e) => setNewItem({
                                ...newItem,
                                name: e.target.value
                            })}
                            className="w-full border rounded px-2 py-1.5 focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                </div>
                <div className="flex justify-end gap-2 pt-2">
                    <button
                        onClick={() => setIsAdding(false)}
                        className="px-4 py-1.5 border rounded-lg hover:bg-gray-50"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={handleSubmit}
                        className="px-4 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    >
                        Dodaj
                    </button>
                </div>
            </div>
        );
    }

    return (
        <button
            onClick={() => setIsAdding(true)}
            className="flex items-center gap-2 p-2 text-blue-600 hover:bg-blue-50 rounded-lg w-full"
        >
            <Plus className="h-4 w-4"/>
            <span>Dodaj produkt</span>
        </button>
    );
};

export default AddProductButton;