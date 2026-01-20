import React from 'react';
import { Upload, Loader } from 'lucide-react';
import { FloatingActionButton, FloatingActionButtonGroup} from "../../../../shared/common/FloatingActionButton";

interface UploadActionSectionProps {
    onUpload: () => void;
    isDisabled: boolean;
    isProcessing: boolean;
}

const UploadActionSection: React.FC<UploadActionSectionProps> = ({
                                                                     onUpload,
                                                                     isDisabled,
                                                                     isProcessing
                                                                 }) => {
    return (
        <FloatingActionButtonGroup position="bottom-right">
            <FloatingActionButton
                icon={isProcessing ? undefined : <Upload className="h-5 w-5" />}
                loadingIcon={<Loader className="h-5 w-5 animate-spin" />}
                label="Wyślij"
                loadingLabel="Przetwarzanie..."
                onClick={onUpload}
                disabled={isDisabled}
                isLoading={isProcessing}
                variant="primary"
            />
        </FloatingActionButtonGroup>
    );
};

export default UploadActionSection;