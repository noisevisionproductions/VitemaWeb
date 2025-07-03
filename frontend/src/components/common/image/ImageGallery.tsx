import React from 'react';
import ImageZoom from './ImageZoom';

interface ImageGalleryProps {
    images: string[];
    imageSize?: 'sm' | 'md' | 'lg';
    className?: string;
    emptyMessage?: string;
    itemAlt?: string;
    onRemoveImage?: (index: number) => void;
    showRemoveButton?: boolean;
}

const ImageGallery: React.FC<ImageGalleryProps> = ({
                                                       images,
                                                       imageSize = 'md',
                                                       className = '',
                                                       emptyMessage = 'Brak zdjęć',
                                                       itemAlt = 'Zdjęcie',
                                                       onRemoveImage,
                                                       showRemoveButton = false
                                                   }) => {
    if (!images || images.length === 0) {
        return (
            <div className={`text-center py-3 text-gray-500 ${className}`}>
                {emptyMessage}
            </div>
        );
    }

    return (
        <div className={`overflow-x-auto ${className}`}>
            <div className="flex gap-2">
                {images.map((imageUrl, index) => (
                    <ImageZoom
                        key={index}
                        src={imageUrl}
                        alt={`${itemAlt} ${index + 1}`}
                        previewSize={imageSize}
                        onRemove={showRemoveButton && onRemoveImage ? () => onRemoveImage(index) : undefined}
                        showRemoveButton={showRemoveButton}
                    />
                ))}
            </div>
        </div>
    );
};

export default ImageGallery;