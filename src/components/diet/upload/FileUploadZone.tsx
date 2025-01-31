import React, {useState} from "react";
import {Upload} from "lucide-react";

interface FileUploadZoneProps {
    file: File | null;
    onFileSelect: (file: File | null) => void;
    disabled?: boolean;
}

const FileUploadZone: React.FC<FileUploadZoneProps> = ({
                                                           file,
                                                           onFileSelect,
                                                           disabled = false,
                                                       }) => {
    const [isDragging, setIsDragging] = useState<boolean>(false);
    const fileInputRef = React.useRef<HTMLInputElement>(null);

    const handleDragOver = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = (): void => {
        setIsDragging(false);
    };

    const handleDrop = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setIsDragging(false);

        const files = e.dataTransfer.files;
        if (files.length) {
            handleFile(files[0]);
        }
    };

    const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>): void => {
        if (e.target.files?.length) {
            handleFile(e.target.files[0]);
        }
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleFile = (file: File): void => {
        const validTypes = [
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'application/vnd.ms-excel'
        ];

        if (validTypes.includes(file.type)) {
            onFileSelect(file);
        } else {
            alert('Proszę wybrać plik Excel');
        }
    };

    React.useEffect(() => {
        if (disabled) {
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    }, [disabled]);

    return (
        <div className="max-w-2xl mx-auto">
            <div
                className={`border-2 border-dashed rounded-lg p-8 text-center
                ${isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
                ${file ? 'bg-green-50' : ''}
                ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
            >
                <Upload className="w-12 h-12 mx-auto mb-4 text-gray-400"/>

                <p className="mb-4 text-gray-600">
                    {file
                        ? `Wybrany plik: ${file.name}`
                        : 'Przeciągnij i upuść plik Excel lub kliknij, aby wybrać'}
                </p>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept=".xlsx,.xls"
                    className="hidden"
                    id="fileInput"
                    onChange={handleFileInput}
                />

                <label
                    htmlFor="fileInput"
                    className={`inline-flex items-center px-4 py-2 ${
                        disabled
                            ? 'bg-gray-400 cursor-not-allowed'
                            : 'bg-blue-500 hover:bg-blue-600 cursor-pointer'
                    } text-white rounded-lg`}
                >
                    Wybierz plik
                </label>
            </div>
        </div>
    );
};

export default FileUploadZone;