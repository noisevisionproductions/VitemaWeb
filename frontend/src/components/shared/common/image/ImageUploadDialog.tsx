import React from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription} from "../../ui/Dialog";
import RecipeImageUpload from "../../../nutrilog/recipes/RecipeImageUpload";
import DietImageUpload from "../../../nutrilog/diet/upload/preview/DietImageUpload";

interface ImageUploadDialogProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    description?: string;
    recipeId?: string;
    onSuccess: (imageUrl: string) => void;
    localMode?: boolean;
}

const ImageUploadDialog: React.FC<ImageUploadDialogProps> = ({
                                                                 isOpen,
                                                                 onClose,
                                                                 title = "Dodaj zdjęcie do przepisu",
                                                                 description = "Wybierz zdjęcie, aby dodać je do przepisu. Obsługiwane formaty: JPG, PNG, GIF.",
                                                                 recipeId,
                                                                 onSuccess,
                                                                 localMode = false
                                                             }) => {
    if (!localMode && !recipeId) {
        console.error('ImageUploadDialog: Missing recipeId when not in localMode');
        return null;
    }

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
            <DialogContent className="bg-white max-w-xl">
                <DialogHeader>
                    <DialogTitle className="text-xl">{title}</DialogTitle>
                    {description && (
                        <DialogDescription className="text-gray-500">
                            {description}
                        </DialogDescription>
                    )}
                </DialogHeader>
                <div className="py-4">
                    {localMode ? (
                        <DietImageUpload
                            onSuccess={onSuccess}
                            onCancel={onClose}
                        />
                    ) : (
                        <RecipeImageUpload
                            recipeId={recipeId!}
                            onSuccess={onSuccess}
                            onCancel={onClose}
                        />
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default ImageUploadDialog;