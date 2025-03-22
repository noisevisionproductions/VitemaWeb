import React, {useState} from "react";
import {Diet, Recipe, ShoppingListV3} from "../../../types";
import {
    Tabs,
    TabsContent,
    TabsList,
    TabsTrigger,
} from "../../ui/Tabs";
import DietGeneralInfoEditor from "./tabs/DietGeneralInfoEditor";
import DietDaysEditor from "./tabs/datesAndMeals/DietDaysEditor";
import DietShoppingListEditor from "./tabs/shoppingList/DietShoppingListEditor";
import DietMetadataEditor from "./tabs/DietMetadataEditor";
import {useUndoableState} from "../../../hooks/useUndoableState";
import {Redo2, Trash2, Undo2} from "lucide-react";
import {useNavigate} from "react-router-dom";
import {toast} from "../../../utils/toast";
import ConfirmationDialog from "../../common/ConfirmationDialog";

interface DietEditTabsProps {
    diet: Diet;
    recipes: { [key: string]: Recipe };
    shoppingList: ShoppingListV3 | null;
    onUpdate: (updatedDiet: Diet) => Promise<void>;
    onDelete: (dietId: string) => Promise<void>;
    onClose?: () => void;
}

const DietEditTabs: React.FC<DietEditTabsProps> = ({
                                                       diet: initialDiet,
                                                       recipes,
                                                       shoppingList,
                                                       onUpdate,
                                                       onDelete,
                                                       onClose
                                                   }) => {
    const navigate = useNavigate();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

    const {
        state: diet,
        updateState: updateDiet,
        undo,
        redo,
        canUndo,
        canRedo
    } = useUndoableState(initialDiet);

    const handleUpdate = async (updatedDiet: Diet) => {
        updateDiet(updatedDiet);
        await onUpdate(updatedDiet);
    };

    const handleDelete = async () => {
        try {
            setIsDeleting(true);
            await onDelete(diet.id);
            toast.success('Dieta została usunięta');

            if (onClose) {
                onClose();
            }

            if (navigate) {
                navigate('/dashboard');
            }
        } catch (error) {
            console.error('Error deleting diet:', error);
            toast.error('Wystąpił błąd podczas usuwania diety');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirmation(false);
        }
    };

    return (
        <div className="space-y-2">
            <div className="flex justify-between items-center">
                <div className="flex gap-1">
                    <button
                        onClick={undo}
                        disabled={!canUndo}
                        className="p-1 rounded-md hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        title="Cofnij zmianę"
                    >
                        <Undo2 className="h-4 w-4"/>
                    </button>
                    <button
                        onClick={redo}
                        disabled={!canRedo}
                        className="p-1 rounded-md hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                        title="Ponów zmianę"
                    >
                        <Redo2 className="h-4 w-4"/>
                    </button>
                </div>
                <button
                    onClick={() => setShowDeleteConfirmation(true)}
                    className="flex items-center gap-1 px-2 py-1 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-md transition-colors"
                    disabled={isDeleting}
                >
                    <Trash2 className="h-4 w-4"/>
                    <span>Usuń dietę</span>
                </button>
            </div>

            <Tabs defaultValue="general" className="w-full">
                <TabsList className="grid grid-cols-4 gap-1 w-full">
                    <TabsTrigger value="general" className="py-2 rounded-md">
                        Informacje ogólne
                    </TabsTrigger>
                    <TabsTrigger value="days" className="py-2 rounded-md">
                        Dni i dania
                    </TabsTrigger>
                    <TabsTrigger value="shopping" className="py-2 rounded-md">
                        Lista zakupów
                    </TabsTrigger>
                    <TabsTrigger value="metadata" className="py-2 rounded-md">
                        Metadane
                    </TabsTrigger>
                </TabsList>

                <TabsContent value="general" className="mt-2">
                    <DietGeneralInfoEditor diet={diet} onUpdate={handleUpdate}/>
                </TabsContent>

                <TabsContent value="days" className="mt-2">
                    <DietDaysEditor diet={diet} onUpdate={handleUpdate} recipes={recipes} shoppingList={shoppingList}/>
                </TabsContent>

                <TabsContent value="shopping" className="mt-2">
                    <DietShoppingListEditor
                        diet={diet}
                        onUpdate={handleUpdate}
                        shoppingList={shoppingList}
                    />
                </TabsContent>

                <TabsContent value="metadata" className="mt-2">
                    <DietMetadataEditor diet={diet} onUpdate={handleUpdate}/>
                </TabsContent>
            </Tabs>

            <ConfirmationDialog
                isOpen={showDeleteConfirmation}
                onClose={() => setShowDeleteConfirmation(false)}
                onConfirm={handleDelete}
                title="Potwierdzenie usunięcia"
                description="Czy na pewno chcesz usunąć tę dietę? Ta operacja jest nieodwracalna i spowoduje usunięcie wszystkich powiązanych danych, w tym listy zakupów i referencji do przepisów."
                confirmLabel="Usuń dietę"
                variant="destructive"
            />
        </div>
    );
};

export default DietEditTabs;