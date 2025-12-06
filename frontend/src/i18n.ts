import i18n from "i18next";
import LanguageDetector from 'i18next-browser-languagedetector';
import {initReactI18next} from "react-i18next";
import Backend from 'i18next-http-backend';

i18n
    .use(Backend)
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        fallbackLng: 'pl',

        ns: ['landing-page', 'privacy-policy'],
        defaultNS: 'landing-page',

        backend: {
            loadPath: '/locales/{{lng}}/{{ns}}.json'
        },

        detection: {
            order: ['localStorage', 'navigator'],
            caches: ['localStorage']
        },
        interpolation: {
            escapeValue: false
        },
        react: {
            useSuspense: true
        }
    })
    .catch(console.error);

export default i18n;