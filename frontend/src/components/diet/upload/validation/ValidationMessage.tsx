import React from 'react';
import {AlertCircle, AlertTriangle, CheckCircle} from 'lucide-react';

export type ValidationSeverity = 'error' | 'warning' | 'success' | 'ERROR' | 'WARNING' | 'SUCCESS';

interface ValidationMessageProps {
    message: string;
    severity: ValidationSeverity;
}

const ValidationMessage: React.FC<ValidationMessageProps> = ({message, severity}) => {
    // Normalize severity to lowercase for consistent handling
    const normalizedSeverity = severity.toLowerCase() as 'error' | 'warning' | 'success';

    const getIcon = () => {
        switch (normalizedSeverity) {
            case 'error':
                return <AlertCircle className="w-5 h-5 text-red-500"/>;
            case 'warning':
                return <AlertTriangle className="w-5 h-5 text-yellow-500"/>;
            case 'success':
                return <CheckCircle className="w-5 h-5 text-green-500"/>;
        }
    };

    const getStyleClasses = () => {
        const baseClasses = "flex items-center gap-2 p-3 rounded-md border ";

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

    return (
        <div className={getStyleClasses()}>
            {getIcon()}
            <span className="text-sm">
                {message}
            </span>
        </div>
    );
};

export default ValidationMessage;