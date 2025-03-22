import React, { useState } from 'react';
import { Trash2, Image as ImageIcon } from 'lucide-react';
import { Button } from "../ui/button";
import LoadingSpinner from "../common/LoadingSpinner";

interface RecipeImageGalleryProps {
    images: string[];
    editable?: boolean;
    onRemove?: (imageUrl: string) => void;
}

const RecipeImageGallery: React.FC<RecipeImageGalleryProps> = ({
                                                                   images = [],
                                                                   editable = false,
                                                                   onRemove
                                                               }) => {
    const [loadingStates, setLoadingStates] = useState<Record<string, boolean>>(
        Object.fromEntries(images.map(img => [img, true]))
    );
    const [errorStates, setErrorStates] = useState<Record<string, boolean>>(
        Object.fromEntries(images.map(img => [img, false]))
    );

    const handleImageLoad = (imageUrl: string) => {
        setLoadingStates(prev => ({ ...prev, [imageUrl]: false }));
    };

    const handleImageError = (imageUrl: string) => {
        setLoadingStates(prev => ({ ...prev, [imageUrl]: false }));
        setErrorStates(prev => ({ ...prev, [imageUrl]: true }));
        console.error(`Nie udało się załadować obrazu: ${imageUrl}`);
    };

    const handleRemoveImage = (imageUrl: string) => {
        if (onRemove) {
            onRemove(imageUrl);
        }
    };

    if (!images || images.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-6 border-2 border-dashed border-slate-200 rounded-lg">
                <ImageIcon className="h-12 w-12 text-slate-300 mb-2" />
                <h3 className="text-lg font-medium text-slate-700 mb-1">Brak zdjęć</h3>
                <p className="text-slate-500 mb-4 text-center">
                    Ten przepis nie ma jeszcze żadnych zdjęć.
                </p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {images.map((image, index) => (
                <div
                    key={`${image}-${index}`}
                    className="relative group rounded-md overflow-hidden bg-gray-100 aspect-video"
                >
                    {loadingStates[image] && (
                        <div className="absolute inset-0 flex items-center justify-center">
                            <LoadingSpinner />
                        </div>
                    )}

                    {errorStates[image] && (
                        <div className="absolute inset-0 flex flex-col items-center justify-center p-4">
                            <ImageIcon size={32} className="text-gray-400 mb-2" />
                            <p className="text-sm text-gray-500 text-center">
                                Nie udało się załadować zdjęcia
                            </p>
                        </div>
                    )}

                    <img
                        src={image}
                        alt={`Zdjęcie przepisu ${index + 1}`}
                        className={`w-full h-full object-cover transition-opacity duration-200 ${
                            loadingStates[image] || errorStates[image] ? 'opacity-0' : 'opacity-100'
                        }`}
                        onLoad={() => handleImageLoad(image)}
                        onError={() => handleImageError(image)}
                    />

                    {editable && !errorStates[image] && (
                        <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all duration-200">
                            <Button
                                variant="destructive"
                                size="icon"
                                className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                                onClick={() => handleRemoveImage(image)}
                            >
                                <Trash2 size={16} />
                            </Button>
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
};

export default RecipeImageGallery;