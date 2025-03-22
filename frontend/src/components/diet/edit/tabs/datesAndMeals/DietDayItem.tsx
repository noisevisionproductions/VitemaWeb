import { MinusCircle, PlusCircle } from "lucide-react";
import { getMealTypeLabel } from "../../../../../utils/mealTypeUtils";
import { formatTimestamp } from "../../../../../utils/dateFormatters";
import React, { useCallback } from "react";
import { Recipe, Day } from "../../../../../types";
import MealItem from "./MealItem";
import { RecipeService } from "../../../../../services/RecipeService";
import { toast} from "../../../../../utils/toast";

interface DietDayItemProps {
    day: Day;
    dayIndex: number;
    recipes: { [key: string]: Recipe };
    isExpanded: boolean;
    onToggle: () => void;
    onTimeChange: (mealIndex: number, newTime: string) => void;
    onRecipeUpdated?: (recipeId: string) => void;
}

const DietDayItem: React.FC<DietDayItemProps> = ({
                                                     day,
                                                     dayIndex,
                                                     recipes,
                                                     isExpanded,
                                                     onToggle,
                                                     onTimeChange,
                                                     onRecipeUpdated
                                                 }) => {
    const handleRecipeUpdate = useCallback(async (recipeId: string, updatedRecipe: Partial<Recipe>) => {
        try {
            await RecipeService.updateRecipe(recipeId, updatedRecipe);
            onRecipeUpdated?.(recipeId);
            toast.success('Przepis został zaktualizowany');
        } catch (error) {
            console.error('Error updating recipe:', error);
            toast.error('Błąd podczas aktualizacji przepisu');
        }
    }, [onRecipeUpdated]);

    return (
        <div className="border rounded-lg overflow-hidden">
            <div
                className="flex items-center justify-between p-4 bg-gray-50 cursor-pointer"
                onClick={onToggle}
            >
                <div className="flex items-center gap-4">
                    <span className="font-medium">Dzień {dayIndex + 1}</span>
                    <span className="text-gray-600">{formatTimestamp(day.date)}</span>
                </div>
                {isExpanded ? (
                    <MinusCircle className="h-5 w-5 text-gray-400"/>
                ) : (
                    <PlusCircle className="h-5 w-5 text-gray-400"/>
                )}
            </div>

            {isExpanded && (
                <div className="p-4 space-y-3">
                    {day.meals.map((meal, mealIndex) => {
                        const recipe = recipes[meal.recipeId];
                        return recipe ? (
                            <MealItem
                                key={mealIndex}
                                recipe={recipe}
                                time={meal.time}
                                mealType={meal.mealType}
                                mealTypeLabel={getMealTypeLabel(meal.mealType)}
                                onTimeChange={async (newTime) => onTimeChange(mealIndex, newTime)}
                                onRecipeUpdate={async (updatedRecipe) => {
                                    await handleRecipeUpdate(meal.recipeId, updatedRecipe);
                                }}
                                onRecipeUpdated={onRecipeUpdated}
                            />
                        ) : (
                            <div key={mealIndex} className="p-3 bg-gray-100 rounded-lg text-gray-500">
                                Przepis niedostępny
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default DietDayItem;