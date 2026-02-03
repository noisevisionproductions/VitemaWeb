import React from 'react';

interface User {
    displayName?: string | null;
    email?: string | null;
}

interface SectionHeaderProps {
    title: string;
    description?: string;
    welcomeUser?: User | null;
    welcomeMessage?: string;
    rightContent?: React.ReactNode;
}

/**
 * Komponent nagłówka sekcji, używany na stronach i w panelach aplikacji
 * @param title - Główny tytuł sekcji
 * @param description - Opcjonalny opis sekcji
 * @param welcomeUser - Opcjonalny obiekt użytkownika, jeśli chcemy wyświetlić powitanie
 * @param welcomeMessage - Opcjonalna wiadomość powitalna (domyślnie: "Witaj, {nazwa}! ")
 * @param rightContent - Opcjonalna zawartość do wyświetlenia po prawej stronie
 */
const SectionHeader: React.FC<SectionHeaderProps> = ({
                                                         title,
                                                         description,
                                                         welcomeUser,
                                                         welcomeMessage = "Witaj, ",
                                                         rightContent
                                                     }) => {
    const getUserName = () => {
        if (!welcomeUser) return '';
        return welcomeUser.displayName || welcomeUser.email || '';
    };

    return (
        <div className="flex justify-between items-start mb-6">
            <div>
                <h1 className="text-2xl font-bold mb-1 text-gray-900 dark:text-gray-100">{title}</h1>
                {(description || welcomeUser) && (
                    <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
                        {welcomeUser && (
                            <>{welcomeMessage}{getUserName()}! </>
                        )}
                        {description}
                    </p>
                )}
            </div>
            {rightContent && (
                <div className="flex items-center">
                    {rightContent}
                </div>
            )}
        </div>
    );
};

export default SectionHeader;