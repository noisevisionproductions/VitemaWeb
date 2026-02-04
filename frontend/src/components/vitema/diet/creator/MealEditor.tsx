import React, {useCallback, useState} from "react";
import {NutritionalValues, ParsedMeal} from "../../../../types";
import {ParsedProduct} from "../../../../types/product";
import {toast} from "../../../../utils/toast";
import type {UnifiedSearchResult} from "../../../../types";
import {
    convertParsedProductsToMealIngredients,
    convertRecipeIngredientsToParsedProducts,
} from "../../../../utils/mealConverters";
import {RecipeService} from "../../../../services/RecipeService";
import {MealSuggestionService} from "../../../../services/diet/manual/MealSuggestionService";
import {
    MealEditorHeader,
    MealEditorIngredients,
    MealEditorInstructions,
    MealEditorPhotos,
    MealEditorNutrition
} from "./steps/Planning/components/MealEditor";

interface MealEditorProps {
    meal: ParsedMeal;
    dayIndex: number;
    mealIndex: number;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
    onUpdateIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number, ingredient: ParsedProduct) => void; // Poprawiona sygnatura
    enableTemplateFeatures?: boolean;
    trainerId?: string;
}

const MealEditor: React.FC<MealEditorProps> = ({
                                                   meal,
                                                   dayIndex,
                                                   mealIndex,
                                                   onUpdateMeal,
                                                   onAddIngredient,
                                                   onRemoveIngredient,
                                                   onUpdateIngredient,
                                                   enableTemplateFeatures = true,
                                                   trainerId
                                               }) => {
    const [hasSaved, setHasSaved] = useState(false);
    const [isManualSaving, setIsManualSaving] = useState(false);
    const [isApplyingTemplate, setIsApplyingTemplate] = useState(false);

    const handleMealUpdate = useCallback((updates: Partial<ParsedMeal>) => {
        onUpdateMeal(dayIndex, mealIndex, {...meal, ...updates});
    }, [dayIndex, mealIndex, meal, onUpdateMeal]);

    const handleAddIngredient = useCallback((ingredient: ParsedProduct) => {
        const ingredientWithId = {
            ...ingredient,
            id: ingredient.id || `ingredient-${Date.now()}-${Math.random()}`
        };
        onAddIngredient(dayIndex, mealIndex, ingredientWithId);
    }, [dayIndex, mealIndex, onAddIngredient]);

    const handleUpdateIngredientLocal = useCallback((index: number, updatedIngredient: ParsedProduct) => {
        onUpdateIngredient(dayIndex, mealIndex, index, updatedIngredient);
    }, [dayIndex, mealIndex, onUpdateIngredient]);

    const handleMealNameChange = useCallback((name: string) => {
        handleMealUpdate({name});
        setHasSaved(false);
    }, [handleMealUpdate]);

    /** Obsługa wyboru z wyszukiwarki (przepis lub produkt) */
    const handleUnifiedResultSelect = useCallback(
        async (result: UnifiedSearchResult) => {
            if (result.type === "RECIPE") {
                setIsApplyingTemplate(true);
                try {
                    const recipe = await RecipeService.getRecipeById(result.id);
                    const ingredients = convertRecipeIngredientsToParsedProducts(recipe.ingredients ?? []);

                    handleMealUpdate({
                        name: recipe.name,
                        instructions: recipe.instructions ?? "",
                        ingredients,
                        photos: recipe.photos ?? [],
                        nutritionalValues: recipe.nutritionalValues,
                        originalRecipeId: recipe.id,
                    });
                    toast.success("Wczytano przepis");
                } catch (error) {
                    console.error("Błąd podczas ładowania przepisu:", error);
                    toast.error("Nie udało się załadować przepisu");
                } finally {
                    setIsApplyingTemplate(false);
                }
                return;
            }

            if (result.type === "PRODUCT") {
                const ingredient: ParsedProduct = {
                    id: result.id,
                    name: result.name,
                    quantity: 1,
                    unit: result.unit ?? "szt",
                    original: result.name,
                    hasCustomUnit: false,
                };
                handleAddIngredient(ingredient);
            }
        },
        [handleMealUpdate, handleAddIngredient]
    );

    const handleManualSaveTemplate = useCallback(async () => {
        if (!enableTemplateFeatures) return;

        const mealName = meal.name?.trim();
        if (!mealName || mealName.length < 3) {
            toast.error("Podaj nazwę posiłku przed zapisaniem (min. 3 znaki)");
            return;
        }

        try {
            setIsManualSaving(true);
            const convertedIngredients = meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : [];

            await MealSuggestionService.saveMealTemplate({
                name: mealName,
                instructions: meal.instructions,
                nutritionalValues: meal.nutritionalValues,
                photos: meal.photos,
                ingredients: convertedIngredients,
                isPublic: false,
                shouldSave: true
            });

            toast.success(`Zapisano "${mealName}" w Twoich posiłkach`);
            setHasSaved(true);
        } catch (error) {
            console.error('Błąd podczas zapisywania szablonu:', error);
            toast.error('Nie udało się zapisać szablonu');
        } finally {
            setIsManualSaving(false);
        }
    }, [meal, enableTemplateFeatures]);

    const updateNutritionalValue = useCallback((field: keyof NutritionalValues, value: string) => {
        const numValue = parseFloat(value) || undefined;
        const currentValues = meal.nutritionalValues || {};
        handleMealUpdate({
            nutritionalValues: {
                ...currentValues,
                [field]: numValue
            }
        });
    }, [meal.nutritionalValues, handleMealUpdate]);

    return (
        <div className="space-y-4">
            <MealEditorHeader
                meal={meal}
                enableTemplateFeatures={enableTemplateFeatures}
                isSavingTemplate={isManualSaving}
                hasSaved={hasSaved}
                isApplyingTemplate={isApplyingTemplate}
                onMealNameChange={handleMealNameChange}
                onUnifiedResultSelect={handleUnifiedResultSelect}
                onManualSaveTemplate={handleManualSaveTemplate}
                onTimeChange={(time) => handleMealUpdate({time})}
                onMealTypeChange={(mealType) => handleMealUpdate({mealType})}
                trainerId={trainerId}
            />

            <MealEditorIngredients
                ingredients={meal.ingredients || []}
                onAddIngredient={handleAddIngredient}
                onRemoveIngredient={(index) => onRemoveIngredient(dayIndex, mealIndex, index)}
                onUpdateIngredient={handleUpdateIngredientLocal}
                trainerId={trainerId}
            />

            <MealEditorInstructions
                instructions={meal.instructions}
                onChange={(instructions) => handleMealUpdate({instructions})}
            />

            <MealEditorPhotos
                photos={meal.photos || []}
                mealName={meal.name}
                recipeId={meal.originalRecipeId}
                onPhotosUpdate={(photos) => handleMealUpdate({photos})}
            />

            <MealEditorNutrition
                nutritionalValues={meal.nutritionalValues}
                onUpdate={updateNutritionalValue}
            />
        </div>
    );
};

export default MealEditor;