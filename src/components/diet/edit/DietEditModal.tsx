import React from 'react';
import {Sheet, SheetContent, SheetHeader, SheetTitle} from "../../ui/sheet";
import {Diet} from "../../../types";
import DietEditTabs from "./DietEditTabs";
import {useDietEditor} from "../../../hooks/useDietEditor";
import LoadingSpinner from "../../common/LoadingSpinner";

interface DietEditModalProps {
    diet: Diet;
    onClose: () => void;
    onUpdate: () => void;
}

const DietEditModal: React.FC<DietEditModalProps> = ({
                                                         diet,
                                                         onClose,
                                                         onUpdate
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

                    </div>
                </SheetHeader>

                {loading ? (
                    <div className="flex justify-center items-center h-[400px]">
                        <LoadingSpinner />
                    </div>
                ) : editableDiet && (
                    <div className="mt-4">
                        <DietEditTabs
                            diet={editableDiet}
                            recipes={recipes}
                            shoppingList={shoppingList}
                            onUpdate={async (updatedData) => {
                                await updateDiet(updatedData);
                                onUpdate();
                            }}
                        />
                    </div>
                )}
            </SheetContent>
        </Sheet>
    );
};

export default DietEditModal;