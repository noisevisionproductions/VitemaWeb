import React, {useState} from "react";
import {Diet, DietTemplate, Recipe, ShoppingList} from "../../../../../types";
import {toast} from "sonner";
import {useMealConfiguration} from "../../../../../hooks/useMealConfiguration";
import {doc, Timestamp, updateDoc} from "firebase/firestore";
import {useConfirmation} from "../../../../../hooks/useConfirmation";
import ConfirmationDialog from "../../../../common/ConfirmationDialog";
import {db} from "../../../../../config/firebase";
import MealConfigSection from "./MealConfiguration";
import DietStartDateSection from "./DietStartDateSection";
import DietDayItem from "./DietDayItem";

interface DietDaysEditorProps {
    diet: Diet;
    recipes: { [key: string]: Recipe };
    shoppingList: ShoppingList | null;
    onUpdate: (updatedDiet: Diet) => Promise<void>;
}

interface TimeChangeData {
    dayIndex: number;
    mealIndex: number;
    newTime: string;
}

interface ConfigurationChangeData {
    updatedDays: Diet['days'];
}

interface DateChangeData {
    newDateStr: string;
}

const DietDaysEditor: React.FC<DietDaysEditorProps> = ({
                                                           diet,
                                                           recipes,
                                                           shoppingList,
                                                           onUpdate
                                                       }) => {
    const [expandedDays, setExpandedDays] = useState<number[]>([0]);
    const [mealConfig, setMealConfig] = useState<DietTemplate>({
        mealsPerDay: diet.days[0].meals.length,
        startDate: diet.days[0].date,
        duration: diet.days.length,
        mealTimes: diet.days[0].meals.reduce((acc, meal, index) => ({
            ...acc,
            [`meal_${index}`]: meal.time
        }), {}),
        mealTypes: diet.days[0].meals.map(meal => meal.mealType)
    });

    const {
        handleMealTypeChange,
        handleMealTimeChange,
        applyMealConfiguration
    } = useMealConfiguration(mealConfig, setMealConfig);

    const {
        isConfirmationOpen: isTimeChangeConfirmationOpen,
        confirmationData: timeChangeData,
        openConfirmation: openTimeChangeConfirmation,
        closeConfirmation: closeTimeChangeConfirmation
    } = useConfirmation<TimeChangeData>();

    const {
        isConfirmationOpen: isConfigurationConfirmationOpen,
        confirmationData: configurationChangeData,
        openConfirmation: openConfigurationConfirmation,
        closeConfirmation: closeConfigurationConfirmation
    } = useConfirmation<ConfigurationChangeData>();

    const {
        isConfirmationOpen: isDateChangeConfirmationOpen,
        confirmationData: dateChangeData,
        openConfirmation: openDateChangeConfirmation,
        closeConfirmation: closeDateChangeConfirmation
    } = useConfirmation<DateChangeData>();

    const handleApplyConfiguration = async () => {
        try {
            const updatedDays = applyMealConfiguration(diet.days);
            openConfigurationConfirmation({updatedDays});
        } catch (error) {
            toast.error('Błąd podczas przygotowywania konfiguracji');
            console.error('Error preparing configuration:', error);
        }
    };

    const handleConfirmConfiguration = async () => {
        if (!configurationChangeData) return;

        try {
            await onUpdate({
                ...diet,
                days: configurationChangeData.updatedDays
            });
            toast.success('Konfiguracja posiłków została zaktualizowana');
            closeConfigurationConfirmation();
        } catch (error) {
            toast.error('Błąd podczas aktualizacji konfiguracji');
            console.error('Error applying configuration:', error);
        }
    };

    const toggleDay = (dayIndex: number) => {
        setExpandedDays(prev =>
            prev.includes(dayIndex)
                ? prev.filter(i => i !== dayIndex)
                : [...prev, dayIndex]
        );
    };

    const handleTimeChange = (dayIndex: number, mealIndex: number, newTime: string) => {
        openTimeChangeConfirmation({dayIndex, mealIndex, newTime});
    };

    const handleConfirmTimeChange = async () => {
        if (!timeChangeData) return;

        try {
            const updatedDays = [...diet.days];
            updatedDays[timeChangeData.dayIndex].meals[timeChangeData.mealIndex].time = timeChangeData.newTime;

            await onUpdate({
                ...diet,
                days: updatedDays
            });
            closeTimeChangeConfirmation();
        } catch (error) {
            toast.error('Błąd podczas aktualizacji czasu posiłku');
            console.error('Error changing time:', error);
        }
    };

    const handleStartDateChange = (newDateStr: string) => {
        openDateChangeConfirmation({newDateStr});
    };

    const handleConfirmDateChange = async () => {
        if (!dateChangeData) return;

        try {
            const startDate = new Date(dateChangeData.newDateStr);
            const updatedDays = diet.days.map((day, index) => {
                const newDate = new Date(startDate);
                newDate.setDate(startDate.getDate() + index);
                return {
                    ...day,
                    date: Timestamp.fromDate(newDate)
                };
            });

            const endDate = new Date(startDate);
            endDate.setDate(startDate.getDate() + diet.days.length - 1);

            await onUpdate({
                ...diet,
                days: updatedDays
            });

            if (shoppingList?.id) {
                const shoppingListRef = doc(db, 'shopping_lists', shoppingList.id);
                await updateDoc(shoppingListRef, {
                    startDate: Timestamp.fromDate(startDate),
                    endDate: Timestamp.fromDate(endDate)
                });
                toast.success('Daty zostały zaktualizowane w diecie i liście zakupów');
            } else {
                toast.success('Daty zostały zaktualizowane');
            }

            closeDateChangeConfirmation();
        } catch (error) {
            console.error('Error updating dates:', error);
            toast.error('Błąd podczas aktualizacji dat');
        }
    };

    return (
        <div className="space-y-6">
            <MealConfigSection
                mealConfig={mealConfig}
                onMealTypeChange={handleMealTypeChange}
                onMealTimeChange={handleMealTimeChange}
                onApplyConfig={handleApplyConfiguration}
            />

            <DietStartDateSection
                currentDate={diet.days[0].date}
                onDateChange={handleStartDateChange}
            />

            <div className="space-y-4">
                {diet.days.map((day, dayIndex) => (
                    <DietDayItem
                        key={dayIndex}
                        day={day}
                        dayIndex={dayIndex}
                        recipes={recipes}
                        isExpanded={expandedDays.includes(dayIndex)}
                        onToggle={() => toggleDay(dayIndex)}
                        onTimeChange={(mealIndex, newTime) =>
                            handleTimeChange(dayIndex, mealIndex, newTime)}
                    />
                ))}
            </div>

            <ConfirmationDialog
                isOpen={isTimeChangeConfirmationOpen}
                onClose={closeTimeChangeConfirmation}
                onConfirm={handleConfirmTimeChange}
                title="Potwierdź zmianę czasu"
                description="Czy na pewno chcesz zmienić czas tego posiłku?"
                confirmLabel="Zmień czas"
                variant="warning"
            />

            <ConfirmationDialog
                isOpen={isConfigurationConfirmationOpen}
                onClose={closeConfigurationConfirmation}
                onConfirm={handleConfirmConfiguration}
                title="Potwierdź zmianę konfiguracji"
                description="Czy na pewno chcesz zastosować nową konfigurację posiłków do wszystkich dni? Ta akcja zaktualizuje typy i czasy posiłków dla całej diety."
                confirmLabel="Zastosuj zmiany"
                variant="warning"
            />

            <ConfirmationDialog
                isOpen={isDateChangeConfirmationOpen}
                onClose={closeDateChangeConfirmation}
                onConfirm={handleConfirmDateChange}
                title="Potwierdź zmianę daty"
                description="Czy na pewno chcesz zmienić datę rozpoczęcia diety? Ta akcja zaktualizuje daty wszystkich dni."
                confirmLabel="Zmień datę"
                variant="warning"
            />
        </div>
    );
};

export default DietDaysEditor;