import React, {useEffect, useState} from "react";
import {Recipe, RecipeIngredient} from "../../../types";
import {RecipeService} from "../../../services/RecipeService";
import {toast} from "../../../utils/toast";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import {Dialog, DialogContent, DialogFooter, DialogHeader} from "../../shared/ui/Dialog";
import {ScrollArea} from "../../shared/ui/ScrollArea";
import RecipeImageGallery from "./RecipeImageGallery";
import ImageUploadDialog from "../../shared/common/image/ImageUploadDialog";
import {ParsedProduct} from "../../../types/product";
import RecipeBasicInfo from "./components/RecipeBasicInfo";
import RecipeIngredientsList from "./components/RecipeIngredientsList";
import RecipeNutritionalInfo from "./components/RecipeNutritionalInfo";
import RecipeMetadata from "./components/RecipeMetadata";
import RecipeModalHeader from "./components/RecipeModalHeader";
import RecipeModalFooter from "./components/RecipeModalFooter";

interface RecipeModalProps {
    recipeId: string | null;
    isOpen: boolean;
    onClose: () => void;
    onRecipeUpdate?: (updatedRecipe: Recipe) => void;
    isCreateMode?: boolean;
}

const RecipeModal: React.FC<RecipeModalProps> = ({
                                                     recipeId,
                                                     isOpen,
                                                     onClose,
                                                     onRecipeUpdate,
                                                     isCreateMode = false
                                                 }) => {
    const [recipe, setRecipe] = useState<Recipe | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [editedRecipe, setEditedRecipe] = useState<Partial<Recipe>>({});
    const [showImageUpload, setShowImageUpload] = useState(false);

    useEffect(() => {
        if (isOpen) {
            if (isCreateMode) {
                // Initialize empty recipe for creation
                const emptyRecipe: Partial<Recipe> = {
                    name: '',
                    instructions: '',
                    photos: [],
                    ingredients: [],
                    nutritionalValues: {
                        calories: 0,
                        protein: 0,
                        fat: 0,
                        carbs: 0
                    },
                    parentRecipeId: null
                };
                setEditedRecipe(emptyRecipe);
                setEditMode(true);
                setLoading(false);
            } else if (recipeId) {
                fetchRecipe().catch(console.error);
            }
        } else {
            setRecipe(null);
            setEditMode(false);
            setLoading(true);
            setShowImageUpload(false);
            setEditedRecipe({});
        }
    }, [isOpen, recipeId, isCreateMode]);

    const fetchRecipe = async () => {
        try {
            setLoading(true);
            const recipeData = await RecipeService.getRecipeById(recipeId!);
            setRecipe(recipeData);
            setEditedRecipe(recipeData);
        } catch (error) {
            console.error("Błąd podczas pobierania przepisu:", error);
            toast.error("Nie udało się pobrać szczegółów przepisu");
            onClose();
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        if (!editedRecipe) return;

        // Validation for create mode
        if (isCreateMode) {
            if (!editedRecipe.name || editedRecipe.name.trim() === '') {
                toast.error("Nazwa przepisu jest wymagana");
                return;
            }
        }

        try {
            setSaving(true);
            let savedRecipe: Recipe;

            if (isCreateMode) {
                // Create new recipe
                savedRecipe = await RecipeService.createRecipe(editedRecipe);
                toast.success("Przepis został utworzony");
            } else if (recipe) {
                // Update existing recipe
                savedRecipe = await RecipeService.updateRecipe(recipe.id, editedRecipe);
                toast.success("Przepis został zaktualizowany");
            } else {
                return;
            }

            setRecipe(savedRecipe);
            setEditedRecipe(savedRecipe);
            setEditMode(false);

            if (onRecipeUpdate) {
                onRecipeUpdate(savedRecipe);
            }

            // Close modal after creating
            if (isCreateMode) {
                onClose();
            }
        } catch (error) {
            console.error("Błąd podczas zapisywania przepisu:", error);
            toast.error("Nie udało się zapisać przepisu");
        } finally {
            setSaving(false);
        }
    };

    const handleNutritionalValueUpdate = (name: string, value: number) => {
        setEditedRecipe((prev) => ({
            ...prev,
            nutritionalValues: {
                ...(prev.nutritionalValues || {calories: 0, protein: 0, fat: 0, carbs: 0}),
                [name]: value,
            },
        }));
    };

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const {name, value} = e.target;
        setEditedRecipe((prev) => ({...prev, [name]: value}));
    };

    const handleAddIngredient = (product: ParsedProduct) => {
        const newIngredient: RecipeIngredient = {
            id: product.id?.startsWith('temp-') ? undefined : product.id,
            name: product.name,
            quantity: product.quantity,
            unit: product.unit,
            original: product.original,
            hasCustomUnit: product.hasCustomUnit
        };

        setEditedRecipe((prev) => ({
            ...prev,
            ingredients: [...(prev.ingredients || []), newIngredient]
        }));
    };

    const handleRemoveIngredient = (index: number) => {
        setEditedRecipe((prev) => ({
            ...prev,
            ingredients: (prev.ingredients || []).filter((_, i) => i !== index)
        }));
    };

    const handleUpdateIngredient = (index: number, field: keyof RecipeIngredient, value: any) => {
        setEditedRecipe((prev) => ({
            ...prev,
            ingredients: (prev.ingredients || []).map((ing, i) =>
                i === index ? {...ing, [field]: value} : ing
            )
        }));
    };

    const handleRemoveImage = async (imageUrl: string) => {
        if (!recipe) return;

        try {
            const updatedPhotos = recipe.photos.filter(
                (photo) => photo !== imageUrl
            );

            const updatedRecipe = {
                ...recipe,
                photos: updatedPhotos,
            };

            setRecipe(updatedRecipe);
            setEditedRecipe((prev) => ({
                ...prev,
                photos: updatedPhotos,
            }));

            await RecipeService.deleteRecipeImage(recipe.id, imageUrl);
            toast.success("Zdjęcie zostało usunięte");

            if (onRecipeUpdate) {
                onRecipeUpdate(updatedRecipe);
            }
        } catch (error) {
            console.error("Błąd podczas usuwania zdjęcia:", error);
            toast.error("Nie udało się usunąć zdjęcia");

            fetchRecipe().catch(console.error);
        }
    };

    const handleImageUploadSuccess = async (imageUrl: string) => {
        if (!recipe) return;

        try {
            const updatedPhotos = [...(recipe.photos || []), imageUrl];

            const updatedRecipe = {
                ...recipe,
                photos: updatedPhotos,
            };

            setRecipe(updatedRecipe);
            setEditedRecipe((prev) => ({
                ...prev,
                photos: updatedPhotos,
            }));

            setShowImageUpload(false);
            toast.success("Zdjęcie zostało dodane");

            if (onRecipeUpdate) {
                onRecipeUpdate(updatedRecipe);
            }
        } catch (error) {
            console.error("Błąd podczas dodawania zdjęcia:", error);
            toast.error("Nie udało się dodać zdjęcia");

            fetchRecipe().catch(console.error);
        }
    };

    if (!isOpen) {
        return null;
    }

    const recipeTitle = isCreateMode
        ? "Nowy przepis"
        : (loading ? "Ładowanie..." : (recipe?.name || ''));

    const recipeDescription = isCreateMode
        ? "Utwórz nowy przepis kulinarny"
        : (recipe ? "Szczegóły przepisu kulinarnego" : "Ładowanie szczegółów przepisu");

    return (
        <>
            <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
                <DialogContent className="max-w-4xl max-h-[90vh] p-0 bg-white">
                    <DialogHeader className="p-4 pb-2 bg-white border-b">
                        <RecipeModalHeader
                            title={recipeTitle}
                            description={recipeDescription}
                            loading={loading}
                            editMode={editMode}
                            isCreateMode={isCreateMode}
                            onEdit={() => setEditMode(true)}
                            onUploadImage={() => setShowImageUpload(true)}
                        />
                    </DialogHeader>

                    <ScrollArea className="p-6 max-h-[calc(90vh-10rem)]">
                        {loading ? (
                            <div className="flex justify-center items-center h-64">
                                <LoadingSpinner/>
                            </div>
                        ) : (
                            <div className="space-y-6">
                                {/* Basic Info */}
                                <RecipeBasicInfo
                                    name={editedRecipe.name || ''}
                                    instructions={editedRecipe.instructions || ''}
                                    editMode={editMode || isCreateMode}
                                    onChange={handleInputChange}
                                />

                                {/* Ingredients */}
                                <RecipeIngredientsList
                                    ingredients={editedRecipe.ingredients || []}
                                    editMode={editMode || isCreateMode}
                                    onAdd={handleAddIngredient}
                                    onRemove={handleRemoveIngredient}
                                    onUpdate={handleUpdateIngredient}
                                />

                                {/* Nutritional Values */}
                                <RecipeNutritionalInfo
                                    values={editedRecipe.nutritionalValues || {
                                        calories: 0,
                                        protein: 0,
                                        fat: 0,
                                        carbs: 0
                                    }}
                                    editMode={editMode || isCreateMode}
                                    onChange={handleNutritionalValueUpdate}
                                />

                                {/* Images & Metadata (only for existing recipes) */}
                                {!isCreateMode && recipe && (
                                    <>
                                        <div className="bg-white p-6 rounded-lg shadow-sm">
                                            <h3 className="text-lg font-semibold mb-3">Zdjęcia</h3>
                                            <RecipeImageGallery
                                                images={recipe.photos || []}
                                                editable={!editMode}
                                                onRemove={handleRemoveImage}
                                            />
                                        </div>

                                        <RecipeMetadata
                                            id={recipe.id}
                                            createdAt={recipe.createdAt}
                                        />
                                    </>
                                )}
                            </div>
                        )}
                    </ScrollArea>

                    <DialogFooter className="p-4 border-t bg-white">
                        <RecipeModalFooter
                            editMode={editMode}
                            isCreateMode={isCreateMode}
                            saving={saving}
                            onSave={handleSave}
                            onCancel={() => {
                                if (isCreateMode) {
                                    onClose();
                                } else {
                                    setEditMode(false);
                                    setEditedRecipe(recipe || {});
                                }
                            }}
                            onClose={onClose}
                        />
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Image upload dialog */}
            {showImageUpload && recipe && (
                <ImageUploadDialog
                    isOpen={showImageUpload}
                    onClose={() => setShowImageUpload(false)}
                    recipeId={recipe.id}
                    onSuccess={handleImageUploadSuccess}
                />
            )}
        </>
    );
};

export default RecipeModal;