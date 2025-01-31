import React from 'react';
import {AlertCircle, AlertTriangle, CheckCircle} from 'lucide-react';

export type ValidationSeverity = 'error' | 'warning' | 'success';

interface ValidationMessageProps {
    message: string;
    severity: ValidationSeverity;
}

const ValidationMessage: React.FC<ValidationMessageProps> = ({message, severity}) => {
    const getIcon = () => {
        switch (severity) {
            case 'error':
                return <AlertCircle className="w-5 h-5 text-red-500"/>;
            case 'warning':
                return <AlertTriangle className="w-5 h-5 text-yellow-500"/>;
            case 'success':
                return <CheckCircle className="w-5 h-5 text-green-500"/>;
        }
    };

    const getColorClass = () => {
        switch (severity) {
            case 'error':
                return 'bg-red-50 text-red-700 border-red-200';
            case 'warning':
                return 'bg-yellow-50 text-yellow-700 border-yellow-200';
            case 'success':
                return 'bg-green-50 text-green-700 border-green-200';
        }
    };

    return (
        <div className={`flex items-center gap-2 p-3 rounded-md border ${getColorClass()}`}>
            {getIcon()}
            <span className="text-sm">
                {message}
            </span>
        </div>
    );
};

export default ValidationMessage;