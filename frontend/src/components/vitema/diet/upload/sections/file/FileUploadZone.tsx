import React, { useState, useRef } from "react";
import { Upload, HelpCircle, FileSpreadsheet, ChevronDown, ChevronUp } from "lucide-react";
import { toast} from "../../../../../../utils/toast";
import ExcelExample from "../../../../guide/ExcelExample";

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
    const [showHelp, setShowHelp] = useState<boolean>(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

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

    const toggleHelp = () => {
        setShowHelp(!showHelp);
    };

    return (
        <div className="max-w-2xl mx-auto">
            <div
                className={`border-2 border-dashed rounded-lg p-8 text-center
                ${isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
                ${file ? 'bg-green-50 border-green-500' : ''}
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

                <div className="flex justify-center space-x-3">
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

                    <button
                        type="button"
                        onClick={toggleHelp}
                        className="inline-flex items-center px-3 py-2 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-lg transition-colors"
                    >
                        <HelpCircle className="w-4 h-4 mr-1" />
                        <span>Pokaż przykład</span>
                        {showHelp ? <ChevronUp className="w-4 h-4 ml-1" /> : <ChevronDown className="w-4 h-4 ml-1" />}
                    </button>
                </div>
            </div>

            {/* Poradnik z przykładem poprawnego pliku Excel */}
            {showHelp && (
                <div className="mt-4 border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm transition-all">
                    <div className="flex items-center gap-2 mb-3">
                        <div className="p-2 rounded-full bg-blue-100">
                            <FileSpreadsheet className="w-4 h-4 text-blue-600" />
                        </div>
                        <h3 className="font-medium text-blue-700">
                            Przykład poprawnego pliku
                        </h3>
                    </div>
                    <div>
                        <div className="mb-4">
                            <ExcelExample />
                        </div>
                        <div className="text-sm text-slate-700 mt-3">
                            <p><strong>Przypomnienie:</strong></p>
                            <ul className="list-disc pl-5 mt-1 space-y-1">
                                <li>Kolumna A może zawierać dowolne notatki (pomijana)</li>
                                <li>Kolumna B to nazwa posiłku</li>
                                <li>Kolumna C to sposób przygotowania</li>
                                <li>Kolumna D to lista składników oddzielona przecinkami</li>
                                <li>Kolumna E to wartości odżywcze (opcjonalne)</li>
                            </ul>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FileUploadZone;