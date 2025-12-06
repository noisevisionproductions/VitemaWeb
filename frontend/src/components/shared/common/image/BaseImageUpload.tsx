import React, {useState, useRef} from 'react';
import {toast} from 'sonner';
import {Upload, X, Image as ImageIcon, AlertCircle} from 'lucide-react';
import {Button} from "../../ui/Button";
import {Card, CardContent} from "../../ui/Card";
import LoadingSpinner from "../LoadingSpinner";

interface BaseImageUploadProps {
    onFileSelect: (file: File) => Promise<void>;
    onCancel: () => void;
    actionInProgress: boolean;
    progressValue: number;
    actionButtonText?: string;
}

const BaseImageUpload: React.FC<BaseImageUploadProps> = ({
                                                             onFileSelect,
                                                             onCancel,
                                                             actionInProgress,
                                                             progressValue,
                                                             actionButtonText = "Prześlij"
                                                         }) => {
    const [file, setFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    const [previewError, setPreviewError] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const selectedFile = e.target.files[0];

            if (!selectedFile.type.startsWith('image/')) {
                toast.error('Wybierz plik graficzny (JPG, PNG, itp.)');
                return;
            }

            const MAX_SIZE = 5 * 1024 * 1024; // 5MB
            if (selectedFile.size > MAX_SIZE) {
                toast.error('Plik jest zbyt duży. Maksymalny rozmiar to 5MB');
                return;
            }

            setFile(selectedFile);
            setPreviewError(false);

            try {
                const objectUrl = URL.createObjectURL(selectedFile);
                setPreview(objectUrl);

                return () => URL.revokeObjectURL(objectUrl);
            } catch (error) {
                console.error("Błąd podczas tworzenia podglądu:", error);
                setPreviewError(true);
                toast.error('Nie udało się wczytać podglądu obrazu');
            }
        }
    };

    const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        e.currentTarget.classList.add('bg-gray-50', 'border-primary');
    };

    const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        e.currentTarget.classList.remove('bg-gray-50', 'border-primary');

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            const droppedFile = e.dataTransfer.files[0];

            if (!droppedFile.type.startsWith('image/')) {
                toast.error('Wybierz plik graficzny (JPG, PNG, itp.)');
                return;
            }

            const MAX_SIZE = 5 * 1024 * 1024; // 5MB
            if (droppedFile.size > MAX_SIZE) {
                toast.error('Plik jest zbyt duży. Maksymalny rozmiar to 5MB');
                return;
            }

            setFile(droppedFile);
            setPreviewError(false);

            try {
                const objectUrl = URL.createObjectURL(droppedFile);
                setPreview(objectUrl);
            } catch (error) {
                console.error("Błąd podczas tworzenia podglądu:", error);
                setPreviewError(true);
                toast.error('Nie udało się wczytać podglądu obrazu');
            }
        }
    };

    const handleAction = async () => {
        if (!file) {
            toast.error('Najpierw wybierz plik');
            return;
        }

        try {
            await onFileSelect(file);
        } catch (error) {
            console.error('Błąd podczas przetwarzania pliku:', error);
        }
    };

    const clearFile = () => {
        setFile(null);
        setPreview(null);
        setPreviewError(false);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    return (
        <Card>
            <CardContent className="p-4">
                {!file ? (
                    <div
                        className="border-2 border-dashed border-gray-300 rounded-md p-8 text-center cursor-pointer hover:bg-gray-50 transition-colors"
                        onClick={() => fileInputRef.current?.click()}
                        onDragOver={handleDragOver}
                        onDrop={handleDrop}
                    >
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            accept="image/*"
                            className="hidden"
                        />

                        <div className="flex flex-col items-center justify-center space-y-2">
                            <ImageIcon size={40} className="text-gray-400"/>
                            <div className="text-sm font-medium">
                                Przeciągnij i upuść plik lub kliknij, aby wybrać
                            </div>
                            <div className="text-xs text-gray-500">
                                (JPG, PNG, GIF - max. 5MB)
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="space-y-4">
                        <div className="relative rounded-md overflow-hidden bg-gray-100">
                            <div className="max-h-[300px] overflow-auto">
                                {previewError ? (
                                    <div className="flex flex-col items-center justify-center p-4 min-h-[200px]">
                                        <AlertCircle size={32} className="text-red-500 mb-2"/>
                                        <p className="text-sm text-gray-700 text-center font-medium">
                                            Nie udało się wczytać podglądu
                                        </p>
                                        <p className="text-xs text-gray-500 text-center mt-1">
                                            Możesz kontynuować przesyłanie lub wybrać inny plik
                                        </p>
                                    </div>
                                ) : (
                                    <img
                                        src={preview || ''}
                                        alt="Podgląd"
                                        className="w-full object-contain"
                                        onError={() => setPreviewError(true)}
                                    />
                                )}
                            </div>

                            {!actionInProgress && (
                                <Button
                                    variant="destructive"
                                    size="icon"
                                    className="absolute top-2 right-2 h-8 w-8 z-10"
                                    onClick={clearFile}
                                >
                                    <X size={16}/>
                                </Button>
                            )}
                        </div>

                        {actionInProgress && (
                            <div className="space-y-2">
                                <div className="h-2 w-full bg-gray-200 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-primary rounded-full transition-all duration-300"
                                        style={{width: `${progressValue}%`}}
                                    ></div>
                                </div>
                                <div className="text-center text-sm text-gray-500">
                                    {actionButtonText === "Prześlij" ? "Przesyłanie" : "Przetwarzanie"}: {progressValue}%
                                </div>
                            </div>
                        )}

                        <div className="flex justify-between mt-4">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={onCancel}
                                disabled={actionInProgress}
                            >
                                Anuluj
                            </Button>

                            <Button
                                variant="default"
                                size="sm"
                                onClick={handleAction}
                                disabled={actionInProgress}
                                className="flex items-center gap-2"
                            >
                                {actionInProgress ? (
                                    <LoadingSpinner size="sm"/>
                                ) : (
                                    <Upload size={16}/>
                                )}
                                <span>{actionButtonText}</span>
                            </Button>
                        </div>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};

export default BaseImageUpload;