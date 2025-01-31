import React, { useState } from 'react';
import { Diet } from '../../../types/diet';
import { toast } from 'sonner';
import { X } from 'lucide-react';
import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle} from "../../ui/sheet";
import {doc, Timestamp, updateDoc} from 'firebase/firestore';
import { db } from '../../../config/firebase';
import LoadingSpinner from '../../common/LoadingSpinner';
import { FirebaseService } from "../../../services/FirebaseService";
import { useRecipes } from '../../../hooks/useRecipes';
import DietDayEditor from "./DietDayEditor";
import DietEditActions from "./DietEditActions";

interface DietEditModalProps {
    diet: Diet;
    onClose: () => void;
    onUpdate: () => Promise<void>;
}

const DietEditModal: React.FC<DietEditModalProps> = ({
                                                         diet,
                                                         onClose,
                                                         onUpdate
                                                     }) => {
    const [loading, setLoading] = useState(false);
    const [editedDiet, setEditedDiet] = useState<Diet>(diet);
    const [deleteConfirm, setDeleteConfirm] = useState(false);
    const { recipes, isLoadingRecipes } = useRecipes(editedDiet.days);

    const handleSave = async () => {
        setLoading(true);
        try {
            const dietRef = doc(db, 'diets', diet.id);
            await updateDoc(dietRef, {
                days: editedDiet.days,
                updatedAt: Timestamp.fromDate(new Date())
            });
            await onUpdate();
            toast.success('Dieta została zaktualizowana');
            onClose();
        } catch (error) {
            console.error('Error updating diet:', error);
            toast.error('Błąd podczas aktualizacji diety');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async () => {
        if (!deleteConfirm) {
            setDeleteConfirm(true);
            return;
        }

        setLoading(true);
        try {
            await FirebaseService.deleteDietWithRelatedData(diet.id);
            await onUpdate();
            toast.success('Dieta została usunięta');
            onClose();
        } catch (error) {
            console.error('Error deleting diet:', error);
            toast.error('Błąd podczas usuwania diety');
        } finally {
            setLoading(false);
        }
    };

    const handleMealTimeUpdate = (dayIndex: number, mealIndex: number, newTime: string) => {
        const updatedDiet = {...editedDiet};
        if (updatedDiet.days?.[dayIndex]?.meals?.[mealIndex]) {
            updatedDiet.days[dayIndex].meals[mealIndex].time = newTime;
            setEditedDiet(updatedDiet);
        }
    };

    const handleDateUpdate = (dayIndex: number, newDate: string) => {
        const updatedDiet = {...editedDiet};
        if (updatedDiet.days?.[dayIndex]) {
            const [year, month, day] = newDate.split('-').map(Number);
            const date = new Date(year, month - 1, day);
            updatedDiet.days[dayIndex].date = Timestamp.fromDate(date);
            setEditedDiet(updatedDiet);
        }
    };

    const renderContent = () => {
        if (isLoadingRecipes) {
            return (
                <div className="flex justify-center py-8">
                    <LoadingSpinner/>
                </div>
            );
        }

        if (!editedDiet.days || editedDiet.days.length === 0) {
            return (
                <div className="text-center py-8 text-gray-500">
                    Brak przypisanych dni do tej diety.
                </div>
            );
        }

        return editedDiet.days.map((day, dayIndex) => (
            <DietDayEditor
                key={dayIndex}
                day={day}
                dayIndex={dayIndex}
                recipes={recipes}
                onDateUpdate={handleDateUpdate}
                onMealTimeUpdate={handleMealTimeUpdate}
            />
        ));
    };

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent
                className="w-full sm:max-w-3xl overflow-y-auto"
                aria-describedby="diet-edit-description"
            >
                <SheetHeader>
                    <div className="flex justify-between items-center border-b pb-4">
                        <SheetTitle>Edycja Diety</SheetTitle>
                        <SheetDescription id="diet-edit-description">
                            Edytuj szczegóły diety, w tym daty i godziny posiłków
                        </SheetDescription>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-500"
                            aria-label="Zamknij edycję diety"
                        >
                            <X className="h-6 w-6"/>
                        </button>
                    </div>
                </SheetHeader>

                <div className="mt-6 space-y-6" role="form" aria-label="Formularz edycji diety">
                    {renderContent()}
                    <DietEditActions
                        loading={loading}
                        deleteConfirm={deleteConfirm}
                        onDelete={handleDelete}
                        onClose={onClose}
                        onSave={handleSave}
                        hasDays={!!editedDiet.days?.length}
                    />
                </div>
            </SheetContent>
        </Sheet>
    );
};

export default DietEditModal;