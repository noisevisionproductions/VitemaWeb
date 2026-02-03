import React from 'react';
import FileUploadZone from "./FileUploadZone";
import {FileSpreadsheet} from "lucide-react";

interface FileUploadSectionProps {
    file: File | null;
    onFileSelect: (file: File | null) => void;
    disabled?: boolean;
    sectionRef?: React.RefObject<HTMLDivElement>;
}

const FileUploadSection: React.FC<FileUploadSectionProps> = ({
                                                                 file,
                                                                 onFileSelect,
                                                                 disabled = false,
                                                                 sectionRef
                                                             }) => {
    return (
        <div ref={sectionRef} className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-md space-y-8 transition-colors">
            <div className="flex items-center gap-3 mb-4">
                <FileSpreadsheet className="h-5 w-5 text-primary dark:text-primary-light"/>
                <h3 className="font-medium text-lg text-gray-900 dark:text-gray-100">
                    Upload pliku Excel
                </h3>
            </div>
            <FileUploadZone
                file={file}
                onFileSelect={onFileSelect}
                disabled={disabled}
            />
        </div>
    );
};

export default FileUploadSection;