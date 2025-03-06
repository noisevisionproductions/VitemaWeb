import React, {useState} from "react";
import {Upload} from "lucide-react";
import {toast} from "sonner";

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
        if (!disabled) {
            setIsDragging(true);
        }
    };

    const handleDragLeave = (): void => {
        setIsDragging(false);
    };

    const handleDrop = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setIsDragging(false);

        if (disabled) {
            return;
        }

        const files = e.dataTransfer.files;
        if (files.length) {
            handleFile(files[0]);
        }
    };

    const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>): void => {
        if (disabled) {
            e.preventDefault();
            return;
        }

        if (e.target.files?.length) {
            handleFile(e.target.files[0]);
        }
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleFile = (file: File): void => {
        if (disabled) {
            return;
        }

        const validTypes = [
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'application/vnd.ms-excel',
            '.xlsx',
            '.xls'
        ];

        const fileType = file.type || file.name.split('.').pop()?.toLowerCase();

        if (validTypes.includes(fileType || '')) {
            onFileSelect(file);
        } else {
            toast.error('Proszę wybrać plik Excel (.xlsx lub .xls)');
        }
    };

    const handleClick = (e: React.MouseEvent) => {
        if (disabled) {
            e.preventDefault();
            toast.error('Najpierw wybierz użytkownika');
            return;
        }
    };

    return (
        <div className="max-w-2xl mx-auto">
            <div
                className={`border-2 border-dashed rounded-lg p-8 text-center
                ${isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
                ${file ? 'bg-green-50' : ''}
                ${disabled ? 'opacity-50 pointer-events-none' : ''}`}
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
                    disabled={disabled}
                />

                <label
                    htmlFor="fileInput"
                    className={`inline-flex items-center px-4 py-2 ${
                        disabled
                            ? 'bg-gray-400 cursor-not-allowed'
                            : 'bg-blue-500 hover:bg-blue-600 cursor-pointer'
                    } text-white rounded-lg`}
                    onClick={handleClick}
                >
                    Wybierz plik
                </label>
            </div>
        </div>
    );
};

export default FileUploadZone;