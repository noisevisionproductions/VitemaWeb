import { toast as sonnerToast } from 'sonner';
import { ToastType } from '../components/common/Toast';

const ToastContextAPI = {
    showToast: null as null | ((message: string, type: ToastType, duration?: number) => void)
};

export const setToastAPI = (showToast: (message: string, type: ToastType, duration?: number) => void) => {
    ToastContextAPI.showToast = showToast;
};

export const toast = {
    success: (message: string, duration?: number) => {
        if (ToastContextAPI.showToast) {
            ToastContextAPI.showToast(message, 'success', duration);
        } else {
            sonnerToast.success(message);
        }
    },
    error: (message: string, duration?: number) => {
        if (ToastContextAPI.showToast) {
            ToastContextAPI.showToast(message, 'error', duration);
        } else {
            sonnerToast.error(message);
        }
    },
    warning: (message: string, duration?: number) => {
        if (ToastContextAPI.showToast) {
            ToastContextAPI.showToast(message, 'warning', duration);
        } else {
            sonnerToast.warning(message);
        }
    },
    info: (message: string, duration?: number) => {
        if (ToastContextAPI.showToast) {
            ToastContextAPI.showToast(message, 'info', duration);
        } else {
            sonnerToast.info(message);
        }
    }
};