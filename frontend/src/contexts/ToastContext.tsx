import React, {createContext, useContext, useState, useCallback, ReactNode, useEffect} from 'react';
import Toast, {ToastType} from "../components/common/Toast";
import { setToastAPI } from '../utils/toast';

interface ToastItem {
    id: string;
    visible: boolean;
    message: string;
    type: ToastType;
    duration: number;
}

interface ToastContextType {
    showToast: (message: string, type?: ToastType, duration?: number) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

interface ToastProviderProps {
    children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({children}) => {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const lastToasts = React.useRef<Record<string, number>>({});

    const showToast = useCallback((message: string, type: ToastType = 'info', duration: number = 5000) => {
        const toastKey = `${type}-${message}`;
        const now = Date.now();
        const lastShown = lastToasts.current[toastKey] || 0;

        if (now - lastShown < 3000) {
            return;
        }

        lastToasts.current[toastKey] = now;
        const id = `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

        setToasts(prev => [...prev, {
            id,
            visible: true,
            message,
            type,
            duration,
        }]);

        setTimeout(() => {
            if (lastToasts.current[toastKey] === now) {
                delete lastToasts.current[toastKey];
            }
        }, 3000);
    }, []);

    useEffect(() => {
        setToastAPI(showToast);
        return () => setToastAPI(null as any);
    }, [showToast]);

    const handleClose = useCallback((id: string) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    }, []);

    return (
        <ToastContext.Provider value={{showToast}}>
            {children}
            <div className="toast-container fixed top-4 left-1/2 transform -translate-x-1/2 z-50 space-y-2">
                {toasts.map((toast) => (
                    <Toast
                        key={toast.id}
                        visible={toast.visible}
                        message={toast.message}
                        type={toast.type}
                        duration={toast.duration}
                        onClose={() => handleClose(toast.id)}
                    />
                ))}
            </div>
        </ToastContext.Provider>
    );
};

export const useToast = (): ToastContextType => {
    const context = useContext(ToastContext);
    if (context === undefined) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};