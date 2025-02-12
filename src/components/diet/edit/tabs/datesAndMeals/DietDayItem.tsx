import {MinusCircle, PlusCircle} from "lucide-react";
import {getMealTypeLabel} from "../../../../../utils/mealTypeUtils";
import {formatDate} from "../../../../../utils/dateFormatters";
import React from "react";
import {Recipe, Day} from "../../../../../types";
import MealItem from "./MealItem";
import {db} from '../../../../../config/firebase';
import {doc, updateDoc} from "firebase/firestore";

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
                                                 }) => (
    <div className="border rounded-lg overflow-hidden">
        <div
            className="flex items-center justify-between p-4 bg-gray-50 cursor-pointer"
            onClick={onToggle}
        >
            <div className="flex items-center gap-4">
                <span className="font-medium">Dzień {dayIndex + 1}</span>
                <span className="text-gray-600">{formatDate(day.date)}</span>
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
                                try {
                                    const recipeRef = doc(db, 'recipes', meal.recipeId);
                                    await updateDoc(recipeRef, updatedRecipe);
                                    onRecipeUpdated?.(meal.recipeId);
                                } catch (error) {
                                    throw error;
                                }
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

export default DietDayItem;