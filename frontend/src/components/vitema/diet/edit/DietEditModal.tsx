import React from 'react';
import {Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription} from "../../../shared/ui/Sheet";
import {Diet} from "../../../../types";
import DietEditTabs from "./DietEditTabs";
import {useDietEditor} from "../../../../hooks/diet/useDietEditor";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";

interface DietEditModalProps {
    diet: Diet;
    onClose: () => void;
    onUpdate: (diet: Diet) => Promise<void>;
    onDelete: (dietId: string) => Promise<void>;
}

const DietEditModal: React.FC<DietEditModalProps> = ({
                                                         diet,
                                                         onClose,
                                                         onUpdate,
                                                         onDelete
                                                     }) => {
    const {
        diet: editableDiet,
        recipes,
        shoppingList,
        loading,
        updateDiet
    } = useDietEditor(diet.id);

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent className="w-full sm:max-w-4xl overflow-y-auto">
                <SheetHeader>
                    <div className="flex justify-between items-center border-b pb-2">
                        <SheetTitle>Edycja Diety</SheetTitle>
                        <SheetDescription className="sr-only">
                            Formularz edycji szczegółów diety, w którym możesz modyfikować
                            posiłki, przepisy i listę zakupów
                        </SheetDescription>
                    </div>
                </SheetHeader>

                {loading ? (
                    <div className="flex justify-center items-center h-[400px]">
                        <LoadingSpinner/>
                    </div>
                ) : editableDiet && (
                    <div className="mt-4">
                        <DietEditTabs
                            diet={editableDiet}
                            recipes={recipes}
                            shoppingList={shoppingList}
                            onUpdate={async (updatedData) => {
                                await updateDiet(updatedData);
                                await onUpdate(updatedData);
                            }}
                            onClose={onClose}
                            onDelete={onDelete}
                        />
                    </div>
                )}
            </SheetContent>
        </Sheet>
    );
};

export default DietEditModal;