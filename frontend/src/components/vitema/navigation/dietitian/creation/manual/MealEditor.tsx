import React, {useCallback, useEffect, useMemo, useState} from "react";
import {NutritionalValues, ParsedMeal} from "../../../../../../types";
import {ParsedProduct} from "../../../../../../types/product";
import IngredientsList from "./components/IngredientsList";
import InlineIngredientSearch from "./steps/InlineIngredientSearch";
import ColoredNutritionBadges from "./steps/ColoredNutritionBadges";
import {MealSuggestion} from "../../../../../../types/mealSuggestions";
import {MealSuggestionService} from "../../../../../../services/diet/manual/MealSuggestionService";
import {toast} from "../../../../../../utils/toast";
import {Camera, Save, Sparkles} from "lucide-react";
import MealNameSearchField from "./components/MealNameSearchField";
import ImageGallery from "../../../../../shared/common/image/ImageGallery";
import ImageUploadDialog from "../../../../../shared/common/image/ImageUploadDialog";
import {
    convertParsedProductsToMealIngredients,
    convertMealIngredientsToParsedProducts
} from "../../../../../../utils/mealConverters";
import ConfirmationDialog from "../../../../../shared/common/ConfirmationDialog";
import {TemplateChangeTracker} from "../../../../../../services/diet/manual/TemplateChangeTracker";
import {RecipeService} from "../../../../../../services/RecipeService";
import TemplateChangeManager from "./components/TemplateChangeManager";

interface MealEditorProps {
    meal: ParsedMeal;
    dayIndex: number;
    mealIndex: number;
    onUpdateMeal: (dayIndex: number, mealIndex: number, meal: ParsedMeal) => void;
    onAddIngredient: (dayIndex: number, mealIndex: number, ingredient: ParsedProduct) => void;
    onRemoveIngredient: (dayIndex: number, mealIndex: number, ingredientIndex: number) => void;
    enableTemplateFeatures?: boolean;
}

const MealEditor: React.FC<MealEditorProps> = ({
                                                   meal,
                                                   dayIndex,
                                                   mealIndex,
                                                   onUpdateMeal,
                                                   onAddIngredient,
                                                   onRemoveIngredient,
                                                   enableTemplateFeatures = true
                                               }) => {
    const [showImageUpload, setShowImageUpload] = useState(false);
    const [isApplyingTemplate, setIsApplyingTemplate] = useState(false);
    const [saveAsTemplate, setSaveAsTemplate] = useState(true);
    const [isSavingTemplate, setIsSavingTemplate] = useState(false);
    const [confirmDeleteImage, setConfirmDeleteImage] = useState(false);
    const [imageToDelete, setImageToDelete] = useState<number | null>(null);

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

    const handleImageUploadSuccess = useCallback(async (imageUrl: string) => {
        try {
            if (imageUrl.startsWith('data:image/')) {
                imageUrl = await MealSuggestionService.uploadBase64MealImage(imageUrl, meal.recipeId);
            }

            const updatedPhotos = [...(meal.photos || []), imageUrl];
            handleMealUpdate({photos: updatedPhotos});
            setShowImageUpload(false);
            toast.success('Zdjęcie zostało dodane');
        } catch (error) {
            console.error('Błąd podczas przesyłania zdjęcia:', error);
            toast.error('Nie udało się przesłać zdjęcia');
        }
    }, [meal.photos, meal.recipeId, handleMealUpdate]);

    const handleRemoveImage = useCallback((imageIndex: number) => {
        setImageToDelete(imageIndex);
        setConfirmDeleteImage(true);
    }, []);

    const confirmRemoveImage = useCallback(() => {
        if (imageToDelete !== null) {
            const updatedPhotos = (meal.photos || []).filter((_, index) => index !== imageToDelete);
            handleMealUpdate({photos: updatedPhotos});
            toast.success('Zdjęcie zostało usunięte');
        }
        setConfirmDeleteImage(false);
        setImageToDelete(null);
    }, [meal.photos, handleMealUpdate, imageToDelete]);

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

            <div>
                <div className="flex items-center justify-between mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                        Nazwa posiłku
                    </label>
                    <div className="flex items-center gap-2">
                        {hasUnsavedChanges && (
                            <span className="text-xs text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full">
                            • Niezapisane zmiany
                        </span>
                        )}

                        {enableTemplateFeatures && meal.name && meal.name.trim().length > 2 && (
                            <div className="flex items-center gap-2">
                                {isSavingTemplate && (
                                    <span className="text-xs text-gray-500 flex items-center gap-1">
                                    <div
                                        className="w-3 h-3 border border-gray-300 border-t-primary rounded-full animate-spin"></div>
                                    Zapisywanie...
                                </span>
                                )}
                                <button
                                    onClick={handleManualSaveTemplate}
                                    disabled={isSavingTemplate}
                                    className="flex items-center gap-1 px-2 py-1 text-xs text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition-colors disabled:opacity-50"
                                    title="Zapisz jako szablon"
                                >
                                    <Save className="h-3 w-3"/>
                                    Zapisz szablon
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {enableTemplateFeatures ? (
                    <MealNameSearchField
                        value={meal.name}
                        onChange={handleMealNameChange}
                        onMealSelect={handleMealSelect}
                        placeholder="Wpisz nazwę posiłku, np. 'Owsianka z owocami'..."
                        onSavePreference={setSaveAsTemplate}
                    />
                ) : (
                    <input
                        type="text"
                        value={meal.name}
                        onChange={(e) => handleMealNameChange(e.target.value)}
                        placeholder="np. Owsianka z owocami"
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                    />
                )}

                {isApplyingTemplate && (
                    <div className="mt-1 flex items-center gap-2 text-xs text-blue-600">
                        <Sparkles className="h-3 w-3 animate-pulse"/>
                        Aplikowanie szablonu...
                    </div>
                )}

                {enableTemplateFeatures && saveAsTemplate && (
                    <div className="mt-1 text-xs text-green-600 flex items-center gap-1">
                        <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                        Auto-zapisywanie szablonów włączone
                    </div>
                )}
            </div>

            {/* Instructions */
            }
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    Instrukcje przygotowania
                </label>
                <textarea
                    value={meal.instructions}
                    onChange={(e) => handleMealUpdate({instructions: e.target.value})}
                    placeholder="Opisz jak przygotować posiłek..."
                    rows={2}
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary resize-none"
                />
            </div>

            {/* Photos section */
            }
            <div>
                <div className="flex items-center justify-between mb-2">
                    <label className="block text-sm font-medium text-gray-700">
                        Zdjęcia posiłku (opcjonalnie)
                    </label>
                    <button
                        onClick={() => setShowImageUpload(true)}
                        className="flex items-center gap-1 px-3 py-1 text-xs bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                    >
                        <Camera className="h-3 w-3"/>
                        Dodaj zdjęcie
                    </button>
                </div>

                {meal.photos && meal.photos.length > 0 ? (
                    <ImageGallery
                        images={meal.photos}
                        imageSize="md"
                        className="py-1"
                        emptyMessage="Brak zdjęć posiłku"
                        itemAlt={meal.name}
                        onRemoveImage={handleRemoveImage}
                        showRemoveButton={true}
                    />
                ) : (
                    <div
                        className="text-center py-4 text-gray-500 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50/30">
                        <Camera className="h-6 w-6 text-gray-400 mx-auto mb-1"/>
                        <p className="text-sm">Brak zdjęć</p>
                        <p className="text-xs">Kliknij "Dodaj zdjęcie" aby dodać</p>
                    </div>
                )}
            </div>

            {/* Ingredients section */
            }
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    Składniki
                </label>

                <div className="mb-3">
                    <InlineIngredientSearch
                        onSelect={handleAddIngredient}
                        placeholder="Dodaj składnik, np. 'mleko 200ml'..."
                    />
                </div>

                <IngredientsList
                    ingredients={meal.ingredients || []}
                    onRemove={(index) => onRemoveIngredient(dayIndex, mealIndex, index)}
                />
            </div>

            {/* Nutritional values */
            }
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    Wartości odżywcze (opcjonalnie)
                </label>

                {meal.nutritionalValues && (
                    <div className="mb-2">
                        <ColoredNutritionBadges
                            nutritionalValues={meal.nutritionalValues}
                            size="sm"
                            layout="horizontal"
                        />
                    </div>
                )}

                <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
                    <div>
                        <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-calories rounded-full"></span>
                            Kalorie
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.calories || ''}
                            onChange={(e) => updateNutritionalValue('calories', e.target.value)}
                            placeholder="kcal"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-calories focus:border-nutrition-calories"
                        />
                    </div>
                    <div>
                        <label className=" text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-protein rounded-full"></span>
                            Białko
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.protein || ''}
                            onChange={(e) => updateNutritionalValue('protein', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-protein focus:border-nutrition-protein"
                        />
                    </div>
                    <div>
                        <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-fats rounded-full"></span>
                            Tłuszcze
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.fat || ''}
                            onChange={(e) => updateNutritionalValue('fat', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-fats focus:border-nutrition-fats"
                        />
                    </div>
                    <div>
                        <label className="text-xs font-medium text-gray-600 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 bg-nutrition-carbs rounded-full"></span>
                            Węglowodany
                        </label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            value={meal.nutritionalValues?.carbs || ''}
                            onChange={(e) => updateNutritionalValue('carbs', e.target.value)}
                            placeholder="g"
                            className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-nutrition-carbs focus:border-nutrition-carbs"
                        />
                    </div>
                </div>
            </div>

            {/* Image Upload Dialog */
            }
            {
                showImageUpload && (
                    <ImageUploadDialog
                        isOpen={showImageUpload}
                        onClose={() => setShowImageUpload(false)}
                        title={`Dodaj zdjęcie - ${meal.name || 'Posiłek'}`}
                        description="Wybierz zdjęcie posiłku. Zostanie ono zapisane razem z dietą."
                        onSuccess={handleImageUploadSuccess}
                        localMode={true}
                        recipeId={meal.recipeId}
                    />
                )
            }

            {
                confirmDeleteImage && (
                    <ConfirmationDialog
                        isOpen={confirmDeleteImage}
                        onClose={() => {
                            setConfirmDeleteImage(false);
                            setImageToDelete(null);
                        }}
                        onConfirm={confirmRemoveImage}
                        title="Usuń zdjęcie"
                        description="Czy na pewno chcesz usunąć to zdjęcie?"
                        confirmLabel="Usuń"
                        cancelLabel="Anuluj"
                        variant="destructive"
                    />
                )
            }
        </div>
    )
        ;
};

export default MealEditor;