// src/components/common/Toast.tsx
import React, {useState, useEffect} from 'react';
import {X} from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

interface ToastProps {
    message: string;
    type?: ToastType;
    duration?: number;
    onClose: () => void;
    visible: boolean;
}

const Toast: React.FC<ToastProps> = ({
                                         message,
                                         type = 'info',
                                         duration = 5000,
                                         onClose,
                                         visible
                                     }) => {
    const [isExiting, setIsExiting] = useState(false);

    // Kolory w zależności od typu powiadomienia
    const getTypeStyles = () => {
        switch (type) {
            case 'success':
                return 'bg-green-50 border-green-500 text-green-800';
            case 'error':
                return 'bg-red-50 border-red-500 text-red-800';
            case 'warning':
                return 'bg-amber-50 border-amber-500 text-amber-800';
            case 'info':
            default:
                return 'bg-blue-50 border-blue-500 text-blue-800';
        }
    };

    // Ikona w zależności od typu powiadomienia
    const getIconByType = () => {
        switch (type) {
            case 'success':
                return (
                    <svg className="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd"
                              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                              clipRule="evenodd"/>
                    </svg>
                );
            case 'error':
                return (
                    <svg className="w-5 h-5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd"
                              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                              clipRule="evenodd"/>
                    </svg>
                );
            case 'warning':
                return (
                    <svg className="w-5 h-5 text-amber-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd"
                              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                              clipRule="evenodd"/>
                    </svg>
                );
            case 'info':
            default:
                return (
                    <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd"
                              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                              clipRule="evenodd"/>
                    </svg>
                );
        }
    };

    // Automatyczne zamknięcie po określonym czasie
    useEffect(() => {
        if (!visible) return;

        let timer: NodeJS.Timeout;
        if (duration !== Infinity) {
            timer = setTimeout(() => {
                setIsExiting(true);
                setTimeout(() => {
                    setIsExiting(false);
                    onClose();
                }, 300);
            }, duration);
        }

        return () => {
            if (timer) clearTimeout(timer);
        };
    }, [visible, duration, onClose]);

    if (!visible) return null;

    return (
        <div
            className={`w-full max-w-md transition-all duration-300 ${
                isExiting ? 'opacity-0 translate-y-[-10px]' : 'opacity-100 translate-y-0'
            }`}
        >
            <div className={`w-full shadow-lg rounded-lg pointer-events-auto border-l-4 ${getTypeStyles()}`}>
                <div className="p-4 flex items-start">
                    <div className="flex-shrink-0 mr-3">
                        {getIconByType()}
                    </div>
                    <div className="flex-1 pt-0.5">
                        <p className="text-sm font-medium">
                            {message}
                        </p>
                    </div>
                    <div className="flex-shrink-0 flex">
                        <button
                            onClick={() => {
                                setIsExiting(true);
                                setTimeout(() => {
                                    setIsExiting(false);
                                    onClose();
                                }, 300);
                            }}
                            className="bg-transparent text-gray-500 hover:text-gray-700 focus:outline-none"
                        >
                            <X size={18}/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Toast;