import {useEffect} from 'react';

/**
 * Hook do ustawiania tytułu strony
 * @param title - tytuł, który zostanie wyświetlony w przeglądarce
 * @param prefix - opcjonalny prefix (np. "Panel Dietetyka | ")
 */
const usePageTitle = (title: string, prefix?: string) => {
    useEffect(() => {
        const fullTitle = prefix ? `${title} - ${prefix}` : title;

        document.title = `${fullTitle} | Nutrilog`;

        return () => {
            document.title = 'Nutrilog';
        };
    }, [title, prefix]);
};

export default usePageTitle;