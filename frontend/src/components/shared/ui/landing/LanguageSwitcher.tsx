import React from "react";
import {useTranslation} from "react-i18next";

interface LanguageSwitcherProps {
    className?: string;
    isMobile?: boolean;
}

const LanguageSwitcher: React.FC<LanguageSwitcherProps> = ({
                                                               className = "",
                                                               isMobile = false
                                                           }) => {
    const {i18n} = useTranslation();

    const isPl = i18n.language.startsWith('pl');

    const toggleLanguage = () => {
        const newLang = isPl ? 'en' : 'pl';
        i18n.changeLanguage(newLang).catch(console.error);
    };

    if (isMobile) {
        return (
            <div className="flex items-center gap-4 py-2 px-1">
                <button
                    onClick={() => i18n.changeLanguage('pl')}
                    className={`text-sm font-medium transition-colors ${i18n.language === 'pl' ? 'text-primary' : 'text-text-secondary'}`}
                >
                    Polski
                </button>
                <div className="h-4 w-[1px] bg-gray-300"></div>
                <button
                    onClick={() => i18n.changeLanguage('en')}
                    className={`text-sm font-medium transition-colors ${i18n.language === 'en' ? 'text-primary' : 'text-text-secondary'}`}
                >
                    English
                </button>
            </div>
        );
    }

    return (
        <button
            onClick={toggleLanguage}
            className={`flex items-center gap-1 font-medium text-sm tracking-wide transition-opacity hover:opacity-80 ${className}`}
            aria-label="Switch language"
        >
            <span className={i18n.language === 'pl' ? 'font-bold' : 'font-normal opacity-70'}>PL</span>
            <span className="opacity-40">|</span>
            <span className={i18n.language !== 'pl' ? 'font-bold' : 'font-normal opacity-70'}>EN</span>
        </button>
    );
};

export default LanguageSwitcher;