import React from 'react';
import {AlertCircle, AlertTriangle, Calendar, CheckCircle, Clock, FileText, List} from 'lucide-react';

export type ValidationSeverity = 'error' | 'warning' | 'success' | 'ERROR' | 'WARNING' | 'SUCCESS';

export type ValidationErrorType =
    | 'diet-overlap'
    | 'excel-structure'
    | 'meals-per-day'
    | 'date'
    | 'meals-config'
    | 'unknown';

interface ValidationMessageProps {
    message: string;
    severity: ValidationSeverity;
    errorType?: ValidationErrorType;
    onNavigate?: (type: ValidationErrorType) => void;
}

const ValidationMessage: React.FC<ValidationMessageProps> = ({
                                                                 message,
                                                                 severity,
                                                                 errorType = 'unknown',
                                                                 onNavigate
                                                             }) => {
    // Normalize severity to lowercase for consistent handling
    const normalizedSeverity = severity.toLowerCase() as 'error' | 'warning' | 'success';

    const getIcon = () => {
        switch (normalizedSeverity) {
            case 'error':
                return <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0"/>;
            case 'warning':
                return <AlertTriangle className="w-5 h-5 text-yellow-500 flex-shrink-0"/>;
            case 'success':
                return <CheckCircle className="w-5 h-5 text-green-500 flex-shrink-0"/>;
        }
    };

    const getStyleClasses = () => {
        const baseClasses = "flex flex-col p-3 rounded-md border ";

        switch (normalizedSeverity) {
            case 'error':
                return baseClasses + 'bg-red-50 border-red-200 text-red-700';
            case 'warning':
                return baseClasses + 'bg-yellow-50 border-yellow-200 text-yellow-700';
            case 'success':
                return baseClasses + 'bg-green-50 border-green-200 text-green-700';
            default:
                return baseClasses + 'bg-gray-50 border-gray-200 text-gray-700';
        }
    };

    const getButtonText = () => {
        switch (errorType) {
            case 'diet-overlap':
                return 'Zmień datę rozpoczęcia';
            case 'excel-structure':
                return 'Przejdź do sekcji pliku';
            case 'meals-per-day':
                return 'Przejdź do liczby posiłków';
            case 'date':
                return 'Przejdź do daty rozpoczęcia';
            case 'meals-config':
                return 'Przejdź do konfiguracji posiłków';
            default:
                return 'Przejdź do pola';
        }
    };

    const getButtonIcon = () => {
        switch (errorType) {
            case 'diet-overlap':
            case 'date':
                return <Calendar className="h-4 w-4 mr-1" />;
            case 'excel-structure':
                return <FileText className="h-4 w-4 mr-1" />;
            case 'meals-per-day':
                return <List className="h-4 w-4 mr-1" />;
            case 'meals-config':
                return <Clock className="h-4 w-4 mr-1" />;
            default:
                return <AlertCircle className="h-4 w-4 mr-1" />;
        }
    };

    const handleNavigate = () => {
        if (onNavigate) {
            onNavigate(errorType);
        }
    };

    const shouldShowButton = normalizedSeverity === 'error' && onNavigate;

    return (
        <div className={getStyleClasses()}>
            <div className="flex items-start gap-2">
                {getIcon()}
                <span className="text-sm">
                    {message}
                </span>
            </div>

            {shouldShowButton && (
                <div className="flex flex-wrap gap-2 mt-3">
                    <button
                        onClick={handleNavigate}
                        className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-md
                                bg-blue-100 text-blue-800 hover:bg-blue-200 focus:outline-none
                                focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                        {getButtonIcon()}
                        {getButtonText()}
                    </button>
                </div>
            )}
        </div>
    );
};

export default ValidationMessage;