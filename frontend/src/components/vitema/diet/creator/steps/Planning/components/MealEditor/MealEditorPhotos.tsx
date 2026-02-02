import React, {useState, useCallback} from 'react';
import {Camera} from 'lucide-react';
import ImageGallery from "../../../../../../../shared/common/image/ImageGallery";
import ImageUploadDialog from "../../../../../../../shared/common/image/ImageUploadDialog";
import ConfirmationDialog from '../../../../../../../shared/common/ConfirmationDialog';
import {MealSuggestionService} from '../../../../../../../../services/diet/manual/MealSuggestionService';
import {toast} from '../../../../../../../../utils/toast';

interface MealEditorPhotosProps {
    photos: string[];
    mealName: string;
    recipeId?: string;
    onPhotosUpdate: (photos: string[]) => void;
}

const MealEditorPhotos: React.FC<MealEditorPhotosProps> = ({
                                                               photos,
                                                               mealName,
                                                               recipeId,
                                                               onPhotosUpdate
                                                           }) => {
    const [showImageUpload, setShowImageUpload] = useState(false);
    const [confirmDeleteImage, setConfirmDeleteImage] = useState(false);
    const [imageToDelete, setImageToDelete] = useState<number | null>(null);

    const handleImageUploadSuccess = useCallback(async (imageUrl: string) => {
        try {
            if (imageUrl.startsWith('data:image/')) {
                imageUrl = await MealSuggestionService.uploadBase64MealImage(imageUrl, recipeId);
            }

            const updatedPhotos = [...photos, imageUrl];
            onPhotosUpdate(updatedPhotos);
            setShowImageUpload(false);
            toast.success('Zdjęcie zostało dodane');
        } catch (error) {
            console.error('Błąd podczas przesyłania zdjęcia:', error);
            toast.error('Nie udało się przesłać zdjęcia');
        }
    }, [photos, recipeId, onPhotosUpdate]);

    const handleRemoveImage = useCallback((imageIndex: number) => {
        setImageToDelete(imageIndex);
        setConfirmDeleteImage(true);
    }, []);

    const confirmRemoveImage = useCallback(() => {
        if (imageToDelete !== null) {
            const updatedPhotos = photos.filter((_, index) => index !== imageToDelete);
            onPhotosUpdate(updatedPhotos);
            toast.success('Zdjęcie zostało usunięte');
        }
        setConfirmDeleteImage(false);
        setImageToDelete(null);
    }, [photos, onPhotosUpdate, imageToDelete]);

    return (
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

            {photos && photos.length > 0 ? (
                <ImageGallery
                    images={photos}
                    imageSize="md"
                    className="py-1"
                    emptyMessage="Brak zdjęć posiłku"
                    itemAlt={mealName}
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

            {showImageUpload && (
                <ImageUploadDialog
                    isOpen={showImageUpload}
                    onClose={() => setShowImageUpload(false)}
                    title={`Dodaj zdjęcie - ${mealName || 'Posiłek'}`}
                    description="Wybierz zdjęcie posiłku. Zostanie ono zapisane razem z dietą."
                    onSuccess={handleImageUploadSuccess}
                    localMode={true}
                    recipeId={recipeId}
                />
            )}

            {confirmDeleteImage && (
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
            )}
        </div>
    );
};

export default MealEditorPhotos;
