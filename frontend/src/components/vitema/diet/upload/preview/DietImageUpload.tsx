import React, {useState} from 'react';
import {toast} from "../../../../../utils/toast";
import BaseImageUpload from "../../../../shared/common/image/BaseImageUpload";

interface DietImageUploadProps {
    onSuccess: (imageDataUrl: string) => void;
    onCancel: () => void;
}

const DietImageUpload: React.FC<DietImageUploadProps> = ({
                                                             onSuccess,
                                                             onCancel
                                                         }) => {
    const [processing, setProcessing] = useState(false);
    const [progress, setProgress] = useState(0);

    const handleFileSelect = async (file: File) => {
        try {
            setProcessing(true);

            // Symulacja postępu
            const progressInterval = setInterval(() => {
                setProgress(prev => {
                    if (prev >= 90) {
                        clearInterval(progressInterval);
                        return 90;
                    }
                    return prev + 10;
                });
            }, 100);

            const reader = new FileReader();
            return new Promise<void>((resolve, reject) => {
                reader.onloadend = () => {
                    clearInterval(progressInterval);
                    setProgress(100);

                    setTimeout(() => {
                        if (typeof reader.result === 'string') {
                            onSuccess(reader.result);
                        } else {
                            toast.error('Nie udało się przetworzyć obrazu');
                        }
                        setProcessing(false);
                        setProgress(0);
                        resolve();
                    }, 300);
                };

                reader.onerror = () => {
                    clearInterval(progressInterval);
                    toast.error('Wystąpił błąd podczas przetwarzania pliku');
                    setProcessing(false);
                    setProgress(0);
                    reject(new Error('Błąd przetwarzania pliku'));
                };

                reader.readAsDataURL(file);
            });
        } catch (error) {
            console.error('Błąd podczas przetwarzania:', error);
            toast.error('Wystąpił błąd podczas przetwarzania pliku');
            setProcessing(false);
            setProgress(0);
        }
    };

    return (
        <BaseImageUpload
            onFileSelect={handleFileSelect}
            onCancel={onCancel}
            actionInProgress={processing}
            progressValue={progress}
            actionButtonText="Dodaj"
        />
    );
};

export default DietImageUpload;