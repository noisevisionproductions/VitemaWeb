import React, {useState} from 'react';
import {Trash2, X} from 'lucide-react';

interface ImageZoomProps {
    src: string;
    alt: string;
    className?: string;
    previewSize?: 'sm' | 'md' | 'lg';
    onRemove?: () => void;
    showRemoveButton?: boolean;
}

const ImageZoom: React.FC<ImageZoomProps> = ({
                                                 src,
                                                 alt,
                                                 className = '',
                                                 previewSize = 'md',
                                                 onRemove,
                                                 showRemoveButton = false
                                             }) => {
    const [isZoomed, setIsZoomed] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [hasError, setHasError] = useState(false);

    const handleImageLoad = () => {
        setIsLoading(false);
    };

    const handleImageError = () => {
        setIsLoading(false);
        setHasError(true);
    };

    const toggleZoom = () => {
        setIsZoomed(!isZoomed);
    };

    const handleRemove = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onRemove) {
            onRemove();
        }
    };

    const sizeClasses = {
        sm: 'w-16 h-16',
        md: 'w-24 h-24',
        lg: 'w-32 h-32'
    };

    const previewClass = sizeClasses[previewSize];

    return (
        <>
            <div
                className={`relative ${previewClass} flex-shrink-0 rounded-lg overflow-hidden border border-gray-200 hover:border-gray-300 transition-colors group ${className}`}
            >
                {isLoading && (
                    <div className="absolute inset-0 flex items-center justify-center bg-gray-100">
                        <div
                            className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"/>
                    </div>
                )}

                {hasError && (
                    <div className="absolute inset-0 flex items-center justify-center bg-gray-100">
                        <span className="text-gray-400 text-xs text-center px-1">Nie udało się załadować zdjęcia</span>
                    </div>
                )}

                <img
                    src={src}
                    alt={alt}
                    className={`w-full h-full object-cover transition-opacity duration-200 cursor-pointer ${isLoading || hasError ? 'opacity-0' : 'opacity-100'}`}
                    onLoad={handleImageLoad}
                    onError={handleImageError}
                    onClick={toggleZoom}
                />

                {/* Remove button */}
                {showRemoveButton && onRemove && (
                    <button
                        onClick={handleRemove}
                        className="absolute top-1 right-1 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-200 hover:bg-red-600 focus:opacity-100 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-1 z-10"
                        title="Usuń zdjęcie"
                        aria-label="Usuń zdjęcie"
                    >
                        <Trash2 size={12}/>
                    </button>
                )}

                {/* Zoom indicator */}
                <div
                    className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200 flex items-center justify-center cursor-pointer"
                    onClick={toggleZoom}
                >
                    <div
                        className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 bg-black bg-opacity-50 rounded-full p-1 pointer-events-none">
                        <span className="text-white text-xs">🔍</span>
                    </div>
                </div>
            </div>
            {/* Zoomed view */}
            {isZoomed && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-80 p-4"
                    onClick={toggleZoom}
                >
                    <div
                        className="relative max-w-4xl max-h-full flex items-center justify-center"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <button
                            className="absolute top-2 right-2 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 transition-colors z-10"
                            onClick={toggleZoom}
                        >
                            <X size={24}/>
                        </button>
                        <img
                            src={src}
                            alt={alt}
                            className="max-w-full max-h-[90vh] object-contain rounded shadow-lg"
                        />
                    </div>
                </div>
            )}
        </>
    );
};

export default ImageZoom;