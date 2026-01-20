import React, {useEffect, useState} from "react";
import {Save, Edit2, Upload} from "lucide-react";
import {Recipe} from "../../../types";
import {RecipeService} from "../../../services/RecipeService";
import {toast} from "../../../utils/toast";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import {Button} from "../../shared/ui/Button";
import {Input} from "../../shared/ui/Input";
import {Textarea} from "../../shared/ui/Textarea";
import {Label} from "../../shared/ui/Label";
import {formatTimestamp} from "../../../utils/dateFormatters";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogDescription,
} from "../../shared/ui/Dialog";
import {ScrollArea} from "../../shared/ui/ScrollArea";
import NutritionalValues from "../../shared/common/NutritionalValues";
import RecipeImageGallery from "./RecipeImageGallery";
import ImageUploadDialog from "../../shared/common/image/ImageUploadDialog";

interface RecipeModalProps {
    recipeId: string | null;
    isOpen: boolean;
    onClose: () => void;
    onRecipeUpdate?: (updatedRecipe: Recipe) => void;
}

const RecipeModal: React.FC<RecipeModalProps> = ({
                                                     recipeId,
                                                     isOpen,
                                                     onClose,
                                                     onRecipeUpdate
                                                 }) => {
    const [recipe, setRecipe] = useState<Recipe | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [editedRecipe, setEditedRecipe] = useState<Partial<Recipe>>({});
    const [showImageUpload, setShowImageUpload] = useState(false);

    useEffect(() => {
        if (isOpen && recipeId) {
            fetchRecipe().catch(console.error);
        } else {
            setRecipe(null);
            setEditMode(false);
            setLoading(true);
            setShowImageUpload(false);
        }
    }, [isOpen, recipeId]);

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
        if (!recipe || !editedRecipe) return;

        try {
            setSaving(true);
            const updatedRecipe = await RecipeService.updateRecipe(recipe.id, editedRecipe);

            setRecipe(updatedRecipe);
            setEditedRecipe(updatedRecipe);

            setEditMode(false);
            toast.success("Przepis został zapisany");

            if (onRecipeUpdate) {
                onRecipeUpdate(updatedRecipe);
            }
        } catch (error) {
            console.error("Błąd podczas zapisywania przepisu:", error);
            toast.error("Nie udało się zapisać przepisu");
        } finally {
            setSaving(false);
        }
    };

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const {name, value} = e.target;
        setEditedRecipe((prev) => ({...prev, [name]: value}));
    };

    const handleNutritionalValueChange = (
        e: React.ChangeEvent<HTMLInputElement> | { target: { name: string; value: string } }
    ) => {
        const {name, value} = e.target;
        const numValue = parseFloat(value) || 0;

        setEditedRecipe((prev) => {
            const currentNutritionalValues = prev.nutritionalValues || {
                calories: 0,
                protein: 0,
                fat: 0,
                carbs: 0,
            };

            return {
                ...prev,
                nutritionalValues: {
                    ...currentNutritionalValues,
                    [name]: numValue,
                },
            };
        });
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

    const recipeTitle = loading ? "Ładowanie..." : recipe?.name;
    const recipeDescription = recipe ? "Szczegóły przepisu kulinarnego" : "Ładowanie szczegółów przepisu";

    return (
        <>
            <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
                <DialogContent className="max-w-4xl max-h-[90vh] p-0 bg-white">
                    <DialogHeader className="p-4 pb-2 bg-white border-b">
                        <div className="flex justify-between items-center">
                            <DialogTitle className="text-xl">
                                {recipeTitle}
                            </DialogTitle>
                            <DialogDescription className="sr-only">
                                {recipeDescription}
                            </DialogDescription>

                            <div className="pr-5 flex gap-2">
                                {!loading && !editMode && (
                                    <>
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setShowImageUpload(true)}
                                            className="flex items-center gap-2"
                                        >
                                            <Upload size={16}/>
                                            <span>Dodaj zdjęcie</span>
                                        </Button>
                                        <Button
                                            variant="default"
                                            size="sm"
                                            onClick={() => setEditMode(true)}
                                            className="flex items-center gap-2"
                                        >
                                            <Edit2 size={16}/>
                                            <span>Edytuj</span>
                                        </Button>
                                    </>
                                )}
                            </div>
                        </div>
                    </DialogHeader>

                    <ScrollArea className="p-6 max-h-[calc(90vh-10rem)]">
                        {loading ? (
                            <div className="flex justify-center items-center h-64">
                                <LoadingSpinner/>
                            </div>
                        ) : (
                            <div className="space-y-6">
                                {/* Dane podstawowe */}
                                <div className="space-y-4 bg-white p-6 rounded-lg shadow-sm">
                                    {editMode ? (
                                        <>
                                            <div>
                                                <Label htmlFor="name">Nazwa przepisu</Label>
                                                <Input
                                                    id="name"
                                                    name="name"
                                                    value={editedRecipe.name || ""}
                                                    onChange={handleInputChange}
                                                />
                                            </div>
                                            <div>
                                                <Label htmlFor="instructions">
                                                    Instrukcje przygotowania
                                                </Label>
                                                <Textarea
                                                    id="instructions"
                                                    name="instructions"
                                                    value={editedRecipe.instructions || ""}
                                                    onChange={handleInputChange}
                                                    rows={8}
                                                />
                                            </div>
                                        </>
                                    ) : (
                                        <>
                                            <h3 className="text-lg font-semibold">
                                                Instrukcje przygotowania
                                            </h3>
                                            <p className="whitespace-pre-line">{recipe?.instructions}</p>
                                        </>
                                    )}
                                </div>

                                {/* Wartości odżywcze */}
                                <div className="bg-white p-6 rounded-lg shadow-sm">
                                    <h3 className="text-lg font-semibold mb-3">
                                        Wartości odżywcze
                                    </h3>

                                    {editMode ? (
                                        <NutritionalValues
                                            values={editedRecipe.nutritionalValues || {
                                                calories: 0,
                                                protein: 0,
                                                fat: 0,
                                                carbs: 0
                                            }}
                                            editMode={true}
                                            onChange={(name, value) => {
                                                handleNutritionalValueChange({
                                                    target: {name, value: value.toString()}
                                                } as React.ChangeEvent<HTMLInputElement>);
                                            }}
                                        />
                                    ) : (
                                        <NutritionalValues
                                            values={recipe?.nutritionalValues || {
                                                calories: 0,
                                                protein: 0,
                                                fat: 0,
                                                carbs: 0
                                            }}
                                            size="lg"
                                        />
                                    )}
                                </div>

                                <div className="bg-white p-6 rounded-lg shadow-sm">
                                    <div className="flex justify-between items-center mb-3">
                                        <h3 className="text-lg font-semibold">Zdjęcia</h3>
                                    </div>
                                    <RecipeImageGallery
                                        images={recipe?.photos || []}
                                        editable={!editMode}
                                        onRemove={handleRemoveImage}
                                    />
                                </div>

                                {/* Metadane */}
                                <div className="bg-white p-6 rounded-lg shadow-sm">
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">
                                        Informacje dodatkowe
                                    </h3>
                                    <div className="space-y-1 text-sm">
                                        <div className="flex justify-between">
                                            <span className="text-gray-500">ID:</span>
                                            <span className="font-mono">{recipe?.id}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-500">Data utworzenia:</span>
                                            <span>
                                                {recipe?.createdAt ? formatTimestamp(recipe.createdAt) : "-"}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </ScrollArea>

                    <DialogFooter className="p-4 border-t bg-white">
                        {editMode ? (
                            <div className="flex gap-2 w-full justify-end">
                                <Button
                                    variant="outline"
                                    onClick={() => {
                                        setEditMode(false);
                                        setEditedRecipe(recipe || {});
                                    }}
                                >
                                    Anuluj
                                </Button>
                                <Button
                                    variant="default"
                                    onClick={handleSave}
                                    disabled={saving}
                                    className="flex items-center gap-2"
                                >
                                    {saving ? <LoadingSpinner size="sm"/> : <Save size={16}/>}
                                    <span>Zapisz zmiany</span>
                                </Button>
                            </div>
                        ) : (
                            <Button variant="outline" onClick={onClose}>
                                Zamknij
                            </Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Osobny dialog do dodawania zdjęcia */}
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