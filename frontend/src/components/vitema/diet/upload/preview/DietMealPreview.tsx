import React, {useState} from "react";
import {ParsedMeal} from "../../../../../types";
import {getMealTypeLabel} from "../../../../../utils/diet/mealTypeUtils";
import {Clock, Image, Plus} from "lucide-react";
import ImageUploadDialog from "../../../../shared/common/image/ImageUploadDialog";
import {Button} from "../../../../shared/ui/Button";
import api from "../../../../../config/axios";
import {toast} from "../../../../../utils/toast";
import ImageGallery from "../../../../shared/common/image/ImageGallery";

interface DietMealPreviewProps {
    meal: ParsedMeal;
    mealIndex: number;
    onImageAdd?: (mealIndex: number, imageUrl: string) => void;
}

const DietMealPreview: React.FC<DietMealPreviewProps> = ({meal, mealIndex, onImageAdd}) => {
    const [showImageUpload, setShowImageUpload] = useState(false);

    const handleImageUploadSuccess = async (imageUrl: string) => {
        try {
            if (imageUrl.startsWith('data:image/')) {
                const response = await api.post('/recipes/base64-image', {
                    imageData: imageUrl
                });

                if (response.data && response.data.imageUrl) {
                    imageUrl = response.data.imageUrl;
                } else {
                    console.error("Brak URL obrazu w odpowiedzi");
                    toast.error("Nie udało się przetworzyć obrazu");
                    return;
                }
            }

            if (onImageAdd) {
                onImageAdd(mealIndex, imageUrl);
            }
            setShowImageUpload(false);
        } catch (error) {
            console.error("Błąd podczas przetwarzania obrazu:", error);
            toast.error("Nie udało się przesłać obrazu");
        }
    };

    return (
        <div className="bg-gray-50 p-4 rounded-lg border border-gray-100">
            <div className="flex justify-between items-center">
                <div className="font-medium text-blue-700">
                    {getMealTypeLabel(meal.mealType)}
                </div>
                <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 text-gray-600 bg-blue-50 px-2 py-1 rounded-md">
                        <Clock className="h-4 w-4"/>
                        <span>{meal.time || "Brak godziny"}</span>
                    </div>
                    <Button
                        variant="outline"
                        size="sm"
                        className="flex items-center gap-1"
                        onClick={() => setShowImageUpload(true)}
                    >
                        <Plus className="h-3 w-3"/>
                        <Image className="h-4 w-4"/>
                    </Button>
                </div>
            </div>

            {meal.photos && meal.photos.length > 0 && (
                <div className="mt-3">
                    <ImageGallery
                        images={meal.photos}
                        imageSize="lg"
                        className="py-1"
                        emptyMessage="Brak zdjęć posiłku"
                        itemAlt={`${meal.name}`}
                    />
                </div>
            )}

            <div className="mt-2">
                <div className="flex items-start gap-1">
                    <div className="font-medium text-gray-900 text-lg">{meal.name}</div>
                    {meal.recipeId && !meal.recipeId.startsWith('temp-recipe-') && (
                        <div className="ml-1 px-1.5 py-0.5 bg-blue-100 text-blue-800 text-xs rounded">
                            Powiązany przepis
                        </div>
                    )}
                </div>

                {meal.instructions && (
                    <div className="text-sm text-gray-600 mt-2 whitespace-pre-line">
                        {meal.instructions}
                    </div>
                )}

                {meal.nutritionalValues && (
                    <div className="mt-3 grid grid-cols-4 gap-2 text-xs">
                        <div className="bg-green-50 p-2 rounded text-center">
                            <div className="font-bold text-green-700">{meal.nutritionalValues.calories}</div>
                            <div className="text-gray-600">kcal</div>
                        </div>
                        <div className="bg-blue-50 p-2 rounded text-center">
                            <div className="font-bold text-blue-700">{meal.nutritionalValues.protein}g</div>
                            <div className="text-gray-600">białko</div>
                        </div>
                        <div className="bg-red-50 p-2 rounded text-center">
                            <div className="font-bold text-red-700">{meal.nutritionalValues.fat}g</div>
                            <div className="text-gray-600">tłuszcz</div>
                        </div>
                        <div className="bg-yellow-50 p-2 rounded text-center">
                            <div className="font-bold text-yellow-700">{meal.nutritionalValues.carbs}g</div>
                            <div className="text-gray-600">węglowodany</div>
                        </div>
                    </div>
                )}

                {/*{meal.ingredients && meal.ingredients.length > 0 && (
                    <div className="mt-3 bg-gray-100 p-2 rounded">
                        <div className="text-sm font-medium mb-1">Składniki:</div>
                        <div className="grid grid-cols-2 gap-x-2 text-sm">
                            {meal.ingredients.map((ingredient, idx) => (
                                <div key={idx} className="flex items-start">
                                    <span className="text-green-600 mr-1">•</span>
                                    <span>{(ingredient.name || ingredient.original || '')}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}*/}
            </div>

            {showImageUpload && (
                <ImageUploadDialog
                    isOpen={showImageUpload}
                    onClose={() => setShowImageUpload(false)}
                    title={`Dodaj zdjęcie do posiłku - ${meal.name}`}
                    description="Wybierz zdjęcie, aby dodać je do tego posiłku. Zdjęcie zostanie zapisane po kliknięciu 'Zapisz dietę'."
                    onSuccess={handleImageUploadSuccess}
                    localMode={true}
                    recipeId={meal.recipeId}
                />
            )}
        </div>
    );
};

export default DietMealPreview;