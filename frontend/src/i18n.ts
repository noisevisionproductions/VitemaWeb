import i18n from "i18next";
import LanguageDetector from 'i18next-browser-languagedetector';
import {initReactI18next} from "react-i18next";

import translationEN from './locales/en/landing-page.json'
import translationPL from './locales/pl/landing-page.json'

const resources = {
    en: {
        translation: translationEN
    },
    pl: {
        translation: translationPL
    }
};

i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources,
        fallbackLng: 'pl',
        load: 'languageOnly',
        detection: {
            order: ['localStorage', 'navigator'],
            caches: ['localStorage']
        },
        interpolation: {
            escapeValue: false
        }
    })
    .catch(console.error);

export default i18n;
