export const handleFirebaseError = (error: any): string => {
    if (error?.code === 'auth/invalid-api-key') {
        return 'Wystąpił problem z autoryzacją. Spróbuj wyczyścić pliki cookie przeglądarki i odświeżyć stronę.';
    }
    return 'Wystąpił nieoczekiwany błąd. Spróbuj ponownie później.';
};