import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import Toast, { ToastType} from "../components/common/Toast";

interface ToastContextType {
    showToast: (message: string, type?: ToastType, duration?: number) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

interface ToastProviderProps {
    children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
    const [toast, setToast] = useState<{
        visible: boolean;
        message: string;
        type: ToastType;
        duration: number;
    }>({
        visible: false,
        message: '',
        type: 'info',
        duration: 5000,
    });

    // Zabezpieczenie przed wielokrotnym pokazywaniem tych samych toastów
    const lastToasts = React.useRef<Record<string, number>>({});

    const showToast = useCallback((message: string, type: ToastType = 'info', duration: number = 5000) => {
        // Sprawdzamy, czy ten sam toast nie został pokazany w ciągu ostatnich 3 sekund
        const toastKey = `${type}-${message}`;
        const now = Date.now();
        const lastShown = lastToasts.current[toastKey] || 0;

        if (now - lastShown < 3000) {
            return;
        }

        lastToasts.current[toastKey] = now;

        setToast({
            visible: true,
            message,
            type,
            duration,
        });

        // Automatycznie czyścimy referencję po 3 sekundach
        setTimeout(() => {
            if (lastToasts.current[toastKey] === now) {
                delete lastToasts.current[toastKey];
            }
        }, 3000);
    }, []);

    const handleClose = useCallback(() => {
        setToast(prev => ({ ...prev, visible: false }));
    }, []);

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}
            <Toast
                visible={toast.visible}
                message={toast.message}
                type={toast.type}
                duration={toast.duration}
                onClose={handleClose}
            />
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

export default ToastContext;