import {useState} from "react";
import {toast} from "../utils/toast";
import {NewsletterFormData} from "../types/newsletter";
import {PublicNewsletterService} from "../services/newsletter";

interface UseNewsletterSubscriptionResult {
    isSubmitting: boolean;
    isSuccess: boolean;
    subscribeToNewsletter: (data: NewsletterFormData) => Promise<void>;
    resetSuccess: () => void;
}

export const useNewsletterSubscription = (): UseNewsletterSubscriptionResult => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const subscribeToNewsletter = async (data: NewsletterFormData) => {
        setIsSubmitting(true);
        try {
            await PublicNewsletterService.subscribe(data);

            setIsSuccess(true);
            toast.success('Sprawdź swoją skrzynkę email, aby potwierdzić zapis do newslettera');

        } catch (error) {
            console.error('Błąd zapisu do newslettera:', error);
        } finally {
            setIsSubmitting(false);
        }
    };

    const resetSuccess = () => {
        setIsSuccess(false);
    };

    return {
        isSubmitting,
        isSuccess,
        subscribeToNewsletter,
        resetSuccess
    };
};