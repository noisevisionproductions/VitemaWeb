import React, {useState} from 'react';
import {toast} from "../../utils/toast";
import {RecipeService} from "../../services/RecipeService";
import BaseImageUpload from "../common/BaseImageUpload";

interface RecipeImageUploadProps {
    recipeId: string;
    onSuccess: (imageUrl: string) => void;
    onCancel: () => void;
}

const RecipeImageUpload: React.FC<RecipeImageUploadProps> = ({
                                                                 recipeId,
                                                                 onSuccess,
                                                                 onCancel
                                                             }) => {
    const [uploading, setUploading] = useState(false);
    const [progress, setProgress] = useState(0);

    const handleFileSelect = async (file: File) => {
        try {
            setUploading(true);

            const progressInterval = setInterval(() => {
                setProgress(prev => {
                    if (prev >= 90) {
                        clearInterval(progressInterval);
                        return 90;
                    }
                    return prev + 10;
                });
            }, 300);

            const imageUrl = await RecipeService.uploadRecipeImage(recipeId, file);

            clearInterval(progressInterval);
            setProgress(100);

            setTimeout(() => {
                const img = new Image();
                img.onload = () => {
                    onSuccess(imageUrl);
                    setUploading(false);
                    setProgress(0);
                };
                img.onerror = () => {
                    console.warn("Obraz nie jest jeszcze dostępny, ale kontynuujemy proces");
                    onSuccess(imageUrl);
                    setUploading(false);
                    setProgress(0);
                    toast.info("Zdjęcie zostało dodane, ale może być widoczne z opóźnieniem");
                };
                img.src = imageUrl;
            }, 500);

        } catch (error) {
            console.error('Błąd podczas przesyłania:', error);
            toast.error('Wystąpił błąd podczas przesyłania pliku');
            setUploading(false);
            setProgress(0);
        }
    };

    return (
        <BaseImageUpload
            onFileSelect={handleFileSelect}
            onCancel={onCancel}
            actionInProgress={uploading}
            progressValue={progress}
            actionButtonText="Prześlij"
        />
    );
};

export default RecipeImageUpload;