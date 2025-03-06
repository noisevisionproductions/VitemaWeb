import React from "react";
import {Sheet, SheetClose, SheetContent, SheetDescription, SheetHeader, SheetTitle,} from "../../ui/sheet"
import LoadingSpinner from "../../common/LoadingSpinner";
import {useShoppingList} from "../../../hooks/shopping/useShoppingList";
import {formatTimestamp} from "../../../utils/dateFormatters";
import {Diet, Recipe, ShoppingListV3} from "../../../types";
import CategoryShoppingList from "./CategoryShoppingList";
import {useRecipes} from "../../../hooks/useRecipes";
import {getMealTypeLabel} from "../../../utils/mealTypeUtils";
import {getDietWarningStatus, getWarningStatusText, isDietEnded} from "../../../utils/dietWarningUtils";
import DietWarningIndicator from "../../common/DietWarningIndicator";

interface DietViewProps {
    diet: Diet;
    onClose: () => void;
}

const DietView: React.FC<DietViewProps> = ({diet, onClose}) => {
    const {recipes, isLoadingRecipes} = useRecipes(diet.days);
    const {shoppingList, loading: shoppingListLoading} = useShoppingList(diet.id);

    const warningStatus = getDietWarningStatus(diet);
    const isEnded = isDietEnded(diet);
    const hasWarning = warningStatus !== 'normal' || isEnded;

    const renderShoppingList = () => {
        if (shoppingListLoading) {
            return (
                <div className="flex justify-center py-4">
                    <LoadingSpinner/>
                </div>
            );
        }

        if (!shoppingList || shoppingList.version !== 3) {
            return null;
        }

        return (
            <CategoryShoppingList
                shoppingList={shoppingList as ShoppingListV3}
                loading={shoppingListLoading}
            />
        );
    };

    const renderMetadata = () => {
        if (!diet.metadata) return null;

        return (
            <div className="text-sm text-gray-600 space-y-2">
                <p>
                    <span className="font-medium">
                        Liczba dni:
                    </span>
                    {' '}
                    {diet.metadata.totalDays || diet.days?.length || 0}
                </p>
                {diet.metadata.fileName && (
                    <p>
                        <span className="font-medium">
                            Nazwa pliku:
                        </span>
                        {' '}
                        {diet.metadata.fileName}
                    </p>
                )}
                {diet.createdAt && (
                    <p>
                        <span className="font-medium">
                            Data utworzenia:
                        </span>
                        {' '}
                        {formatTimestamp(diet.createdAt)}
                    </p>
                )}

                {hasWarning && (
                    <div
                        className={`mt-4 p-3 rounded-md ${isEnded ? 'bg-gray-100' : (warningStatus === 'critical' ? 'bg-red-50' : 'bg-amber-50')}`}>
                        <div className="flex items-center">
                            <DietWarningIndicator
                                status={warningStatus}
                                diet={diet}
                                size="md"
                            />
                            <span className="ml-2 font-medium">
                                {isEnded
                                    ? 'Dieta zakończona'
                                    : getWarningStatusText(diet)
                                }
                            </span>
                        </div>
                        {!isEnded && (
                            <p className="text-xs mt-1 opacity-80">
                                {warningStatus === 'critical'
                                    ? 'Zalecane jest niezwłoczne skontaktowanie się z klientem w celu ustalenia kontynuacji diety.'
                                    : 'Wkrótce warto skontaktować się z klientem w sprawie przedłużenia diety.'
                                }
                            </p>
                        )}
                    </div>
                )}
            </div>
        );
    };

    const renderRecipeDetails = (recipe: Recipe) => (
        <div className="space-y-2">
            <p className="font-medium">{recipe.name}</p>
            <p className="text-sm text-gray-600">
                {recipe.instructions}
            </p>
            <div className="text-sm">
                <p className="font-medium">Wartości odżywcze:</p>
                <p>
                    Kalorie: {recipe.nutritionalValues?.calories || 0} kcal,{' '}
                    Białko: {recipe.nutritionalValues?.protein || 0}g,{' '}
                    Tłuszcze: {recipe.nutritionalValues?.fat || 0}g,{' '}
                    Węglowodany: {recipe.nutritionalValues?.carbs || 0}g
                </p>
            </div>
        </div>
    );

    const renderMeal = (meal: Diet['days'][0]['meals'][0], mealIndex: number) => {
        const recipe = recipes[meal.recipeId];
        if (!recipe) return null;

        return (
            <div key={mealIndex} className="bg-gray-50 p-4 rounded-lg">
                <div className="flex justify-between mb-2">
                    <span className="font-medium">
                        {getMealTypeLabel(meal.mealType)} - {meal.time}
                    </span>
                </div>
                {renderRecipeDetails(recipe)}
            </div>
        );
    };

    const renderDay = (day: Diet['days'][0], index: number) => {
        const dayDate = new Date(day.date.seconds * 1000);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const isPastDay = dayDate < today;

        return (
            <div key={index} className={`border-b pb-6 last:border-b-0 ${isPastDay ? 'opacity-70' : ''}`}>
                <h3 className={`text-lg font-medium mb-4 flex items-center ${isPastDay ? 'line-through text-gray-500' : ''}`}>
                    Dzień {index + 1} - {formatTimestamp(day.date)}
                    {isPastDay && (
                        <span className="ml-2 text-xs bg-gray-200 text-gray-600 py-0.5 px-2 rounded-full">
                            Zakończony
                        </span>
                    )}
                </h3>
                <div className="space-y-4">
                    {day.meals?.map((meal, mealIndex) => renderMeal(meal, mealIndex))}
                </div>
            </div>
        );
    };
    const renderContent = () => {
        if (isLoadingRecipes) {
            return (
                <div className="flex justify-center py-8">
                    <LoadingSpinner/>
                </div>
            );
        }

        if (!diet.days || diet.days.length === 0) {
            return (
                <div className="text-center py-8 text-gray-500">
                    Brak przypisanych posiłków do tej diety.
                </div>
            );
        }

        const sortedDays = [...diet.days].sort((a, b) => {
            return a.date.seconds - b.date.seconds;
        });

        return sortedDays.map((day, index) => renderDay(day, index));
    };

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent className="w-full sm:max-w-3xl overflow-y-auto">
                <SheetHeader>
                    <div className="flex justify-between items-center border-b pb-4">
                        <div className="flex items-center gap-2">
                            <SheetTitle>Szczegóły Diety</SheetTitle>
                            {hasWarning && (
                                <DietWarningIndicator
                                    status={warningStatus}
                                    diet={diet}
                                    size="sm"
                                />
                            )}
                        </div>
                        <SheetClose className="text-gray-400 hover:text-gray-500"/>
                    </div>
                    <SheetDescription className="sr-only">
                        Szczegółowy widok diety zawierający informacje o posiłkach i liście zakupów
                    </SheetDescription>
                </SheetHeader>

                <div className="mt-6 space-y-6">
                    {renderMetadata()}
                    {renderShoppingList()}
                    {renderContent()}
                </div>
            </SheetContent>
        </Sheet>
    );
};

export default DietView;