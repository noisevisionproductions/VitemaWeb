import React, {useCallback, useEffect, useMemo, useState} from "react";
import {NutritionalValues, ParsedMeal} from "../../../../types";
import {ParsedProduct} from "../../../../types/product";
import {MealSuggestion} from "../../../../types/mealSuggestions";
import {MealSuggestionService} from "../../../../services/diet/manual/MealSuggestionService";
import {toast} from "../../../../utils/toast";
import {
    convertParsedProductsToMealIngredients,
    convertMealIngredientsToParsedProducts
} from "../../../../utils/mealConverters";
import {TemplateChangeTracker} from "../../../../services/diet/manual/TemplateChangeTracker";
import {RecipeService} from "../../../../services/RecipeService";
import TemplateChangeManager from "./components/TemplateChangeManager";
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
    enableTemplateFeatures?: boolean;
    trainerId?: string; // Optional trainerId for custom products
}

const MealEditor: React.FC<MealEditorProps> = ({
                                                   meal,
                                                   dayIndex,
                                                   mealIndex,
                                                   onUpdateMeal,
                                                   onAddIngredient,
                                                   onRemoveIngredient,
                                                   enableTemplateFeatures = true,
                                                   trainerId
                                               }) => {
    const [isApplyingTemplate, setIsApplyingTemplate] = useState(false);
    const [saveAsTemplate, setSaveAsTemplate] = useState(true);
    const [isSavingTemplate, setIsSavingTemplate] = useState(false);

    // Template updates
    const [changeTracker] = useState(() => new TemplateChangeTracker());
    const [appliedTemplate, setAppliedTemplate] = useState<MealSuggestion | null>(null);
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
    const [showChangeManager, setShowChangeManager] = useState(false);

    const currentSummary = useMemo(() => {
        if (appliedTemplate && changeTracker) {
            changeTracker.detectChanges({
                name: meal.name,
                instructions: meal.instructions,
                ingredients: meal.ingredients,
                nutritionalValues: meal.nutritionalValues,
                photos: meal.photos
            });

            return changeTracker.getUpdateSummary();
        }
        return null;
    }, [meal.name, meal.instructions, meal.ingredients, meal.nutritionalValues, meal.photos, appliedTemplate, changeTracker]);

    useEffect(() => {
        setShowChangeManager(currentSummary?.hasSignificantChanges || false);
        setHasUnsavedChanges((currentSummary?.changes?.length || 0) > 0);
    }, [currentSummary]);

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

    const handleMealNameChange = useCallback((name: string) => {
        handleMealUpdate({name});

        if (enableTemplateFeatures && saveAsTemplate && name.trim().length > 3) {
            // Debounce auto-save (możesz dodać useDebounce hook)
            const timer = setTimeout(() => {
                handleAutoSaveTemplate(name).catch(console.error);
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [handleMealUpdate, enableTemplateFeatures, saveAsTemplate]);

    const handleAutoSaveTemplate = useCallback(async (mealName: string) => {
        if (!enableTemplateFeatures || !saveAsTemplate || isSavingTemplate) return;

        if (!mealName.trim() || mealName.trim().length < 3) return;

        try {
            const mealForValidation = {
                name: mealName,
                instructions: meal.instructions,
                ingredients: meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : []
            };

            const validation = MealSuggestionService.validateMealForTemplate(mealForValidation);
            if (!validation.isValid) return;

            setIsSavingTemplate(true);
            await MealSuggestionService.saveMealTemplate({
                name: mealName,
                instructions: meal.instructions,
                nutritionalValues: meal.nutritionalValues,
                photos: meal.photos,
                ingredients: meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : [],
                isPublic: false,
                shouldSave: true
            });
        } catch (error) {
            console.error('Błąd podczas auto-zapisywania szablonu:', error);
        } finally {
            setIsSavingTemplate(false);
        }
    }, [meal, enableTemplateFeatures, saveAsTemplate, isSavingTemplate]);

    const handleSaveChangesToTemplate = useCallback(async () => {
        const summary = changeTracker.getUpdateSummary();
        if (!summary) return;

        console.log('🔍 Zapisywanie szablonu:', {
            templateId: summary.templateId,
            source: summary.source,
            photos: meal.photos,
            photosLength: meal.photos?.length || 0
        });

        try {
            if (summary.source === 'TEMPLATE') {
                await MealSuggestionService.updateMealTemplate(summary.templateId, {
                    name: meal.name,
                    instructions: meal.instructions || '',
                    nutritionalValues: meal.nutritionalValues,
                    photos: meal.photos || [],
                    ingredients: meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : [],
                    isPublic: false,
                    shouldSave: true
                });
            } else {
                await RecipeService.updateRecipe(summary.templateId.replace('recipe-', ''), {
                    name: meal.name,
                    instructions: meal.instructions || '',
                    nutritionalValues: meal.nutritionalValues,
                    photos: meal.photos || []
                });
            }

            changeTracker.reset();
            setShowChangeManager(false);
            setHasUnsavedChanges(false);
            toast.success('Szablon został zaktualizowany');
        } catch (error) {
            console.error('Błąd podczas aktualizacji szablonu:', error);
            toast.error('Nie udało się zaktualizować szablonu');
        }
    }, [meal, changeTracker]);

    const handleMealSelect = useCallback(async (suggestion: MealSuggestion) => {
        if (!enableTemplateFeatures) {
            handleMealUpdate({name: suggestion.name});
            return;
        }

        setIsApplyingTemplate(true);
        try {
            const appliedMeal = await MealSuggestionService.applyMealTemplate(suggestion.id, meal);

            changeTracker.startTracking(suggestion);
            setAppliedTemplate(suggestion);
            setHasUnsavedChanges(false);

            const newMeal: Partial<ParsedMeal> = {
                name: appliedMeal.name,
                instructions: appliedMeal.instructions || '',
                nutritionalValues: appliedMeal.nutritionalValues,
                photos: appliedMeal.photos || [],
                ingredients: appliedMeal.ingredients ?
                    convertMealIngredientsToParsedProducts(appliedMeal.ingredients) : []
            };

            handleMealUpdate(newMeal);
        } catch (error) {
            console.error('Błąd podczas aplikowania szablonu:', error);
            toast.error('Nie udało się zastosować szablonu');

            handleMealUpdate({
                name: suggestion.name,
                instructions: '',
                photos: [],
                ingredients: []
            });
        } finally {
            setIsApplyingTemplate(false);
        }
    }, [meal, handleMealUpdate, enableTemplateFeatures, changeTracker]);


    const handleManualSaveTemplate = useCallback(async () => {
        if (!enableTemplateFeatures) return;

        try {
            setIsSavingTemplate(true);

            const mealForValidation = {
                name: meal.name,
                instructions: meal.instructions,
                ingredients: meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : []
            };

            const validation = MealSuggestionService.validateMealForTemplate(mealForValidation);
            if (!validation.isValid) {
                toast.error(`Nie można zapisać szablonu: ${validation.errors.join(', ')}`);
                return;
            }

            const convertedIngredients = meal.ingredients ? convertParsedProductsToMealIngredients(meal.ingredients) : [];

            const preview = await MealSuggestionService.previewMealSave({
                name: meal.name,
                instructions: meal.instructions,
                nutritionalValues: meal.nutritionalValues,
                photos: meal.photos,
                ingredients: convertedIngredients
            });

            if (preview.foundSimilar && preview.recommendedAction === 'USE_EXISTING') {
                toast.info('Podobny szablon już istnieje. Zostanie wykorzystany istniejący.');
                return;
            }

            await MealSuggestionService.saveMealTemplate({
                name: meal.name,
                instructions: meal.instructions,
                nutritionalValues: meal.nutritionalValues,
                photos: meal.photos,
                ingredients: convertedIngredients,
                isPublic: false,
                shouldSave: true
            });

            toast.success('Szablon posiłku został zapisany');
        } catch (error) {
            console.error('Błąd podczas zapisywania szablonu:', error);
            toast.error('Nie udało się zapisać szablonu');
        } finally {
            setIsSavingTemplate(false);
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
            {/* Template Change Manager */}
            {showChangeManager && changeTracker.getUpdateSummary() && (
                <TemplateChangeManager
                    updateSummary={changeTracker.getUpdateSummary()!}
                    onSaveChanges={handleSaveChangesToTemplate}
                    onDiscardChanges={() => {
                        if (appliedTemplate) {
                            handleMealSelect(appliedTemplate).catch(console.error);
                        }
                    }}
                    onKeepLocal={() => {
                        setShowChangeManager(false);
                        changeTracker.reset();
                    }}
                    isVisible={showChangeManager}
                />
            )}

            {/* Header: Meal Name, Time, Type */}
            <MealEditorHeader
                meal={meal}
                enableTemplateFeatures={enableTemplateFeatures}
                saveAsTemplate={saveAsTemplate}
                isSavingTemplate={isSavingTemplate}
                hasUnsavedChanges={hasUnsavedChanges}
                isApplyingTemplate={isApplyingTemplate}
                onMealNameChange={handleMealNameChange}
                onMealSelect={handleMealSelect}
                onManualSaveTemplate={handleManualSaveTemplate}
                onSavePreference={setSaveAsTemplate}
                onTimeChange={(time) => handleMealUpdate({time})}
                onMealTypeChange={(mealType) => handleMealUpdate({mealType})}
            />

            {/* Ingredients: Search & List */}
            <MealEditorIngredients
                ingredients={meal.ingredients || []}
                onAddIngredient={handleAddIngredient}
                onRemoveIngredient={(index) => onRemoveIngredient(dayIndex, mealIndex, index)}
                trainerId={trainerId}
            />

            {/* Instructions: Larger Textarea */}
            <MealEditorInstructions
                instructions={meal.instructions}
                onChange={(instructions) => handleMealUpdate({instructions})}
            />

            {/* Photos: Gallery & Upload */}
            <MealEditorPhotos
                photos={meal.photos || []}
                mealName={meal.name}
                recipeId={meal.recipeId}
                onPhotosUpdate={(photos) => handleMealUpdate({photos})}
            />

            {/* Nutritional Values: Editable Inputs */}
            <MealEditorNutrition
                nutritionalValues={meal.nutritionalValues}
                onUpdate={updateNutritionalValue}
            />
        </div>
    );
};

export default MealEditor;
