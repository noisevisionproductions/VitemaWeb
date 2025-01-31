import React, {useEffect, useState, useRef} from 'react';
import ValidationMessage from './ValidationMessage';

interface ExcelStructureValidatorProps {
    file: File;
    onValidationChange: (isValid: boolean) => void;
}

interface ErrorObject {
    row: number;
    errors: string[];
}

type ValidationSeverity = "error" | "warning" | "success";

type ValidationResult = {
    isValid: boolean;
    message: string;
    severity: ValidationSeverity;
};

const ExcelStructureValidator: React.FC<ExcelStructureValidatorProps> = ({
                                                                             file,
                                                                             onValidationChange
                                                                         }) => {
    const [validations, setValidations] = useState<ValidationResult[]>([]);
    const workerRef = useRef<Worker | null>(null);

    useEffect(() => {
        const worker = new Worker(
            new URL('../../../../workers/excelValidationWorker.ts', import.meta.url),
            {type: 'module'}
        );

        workerRef.current = worker;

        worker.onmessage = (e: MessageEvent) => {
            const {type, data} = e.data;

            switch (type) {
                case 'progress':
                    setValidations([{
                        isValid: true,
                        message: `Sprawdzanie... (${data.progress}%)`,
                        severity: "success"
                    }]);
                    break;

                case 'result':
                    if (data.isValid) {
                        setValidations([{
                            isValid: true,
                            message: `Struktura pliku jest poprawna. Znaleziono ${data.totalRows} posiłków.`,
                            severity: "success"
                        }]);
                    } else {
                        setValidations(
                            data.errors.map(({row, errors}: ErrorObject) => ({
                                isValid: false,
                                message: `Wiersz ${row}: ${errors.join(', ')}`,
                                severity: "error"
                            }))
                        );
                    }
                    onValidationChange(data.isValid);
                    break;

                case 'error':
                    setValidations([{
                        isValid: false,
                        message: `Błąd podczas przetwarzania pliku: ${data}`,
                        severity: "error"
                    }]);
                    onValidationChange(false);
                    break;
            }
        };

        // Rozpoczęcie walidacji
        if (file) {
            worker.postMessage({file});
        }

        // Cleanup
        return () => {
            worker.terminate();
            workerRef.current = null;
        };
    }, [file, onValidationChange]);

    return (
        <div className="space-y-2">
            {validations.map((validation, index) => (
                <ValidationMessage
                    key={index}
                    message={validation.message}
                    severity={validation.severity}
                />
            ))}
        </div>
    );
};

export default React.memo(ExcelStructureValidator);