import React from 'react';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../ui/AlertDialog"

interface ConfirmationDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    description: string;
    confirmLabel?: string;
    cancelLabel?: string;
    variant?: 'default' | 'warning' | 'destructive';
    isLoading?: boolean;
}

const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({
                                                                   isOpen,
                                                                   onClose,
                                                                   onConfirm,
                                                                   title,
                                                                   description,
                                                                   confirmLabel = 'Potwierdź',
                                                                   cancelLabel = 'Anuluj',
                                                                   variant = 'default',
                                                                   isLoading = false
                                                               }) => {
    const getVariantStyles = () => {
        switch (variant) {
            case 'warning':
                return 'bg-yellow-500 hover:bg-yellow-600';
            case 'destructive':
                return 'bg-red-500 hover:bg-red-600';
            default:
                return 'bg-blue-500 hover:bg-blue-600';
        }
    };

    return (
        <AlertDialog open={isOpen} onOpenChange={onClose}>
            <AlertDialogContent className="bg-white">
                <AlertDialogHeader>
                    <AlertDialogTitle className="text-gray-9009">
                        {title}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="text-gray-600">
                        {description}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel className="text-gray-700">
                        {cancelLabel}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        onClick={onConfirm}
                        className={`text-white ${getVariantStyles()}`}
                    >
                        {isLoading ? 'Trwa usuwanie...' : confirmLabel}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};

export default ConfirmationDialog;