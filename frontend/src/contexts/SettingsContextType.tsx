import React, {createContext, useContext, useState, useEffect, ReactNode} from 'react';
import {ParserSettingsService} from "../services/diet/ParserSettingsService";
import {useAuth} from './AuthContext'; // Dodajemy kontekst autoryzacji

interface SettingsContextType {
    // Ustawienia walidacji kalorii
    isCalorieValidationEnabled: boolean;
    setIsCalorieValidationEnabled: (value: boolean) => void;
    targetCalories: number;
    setTargetCalories: (value: number) => void;

    // Ustawienia parsera Excel
    skipColumnsCount: number;
    setSkipColumnsCount: (value: number) => void;
    maxSkipColumnsCount: number;

    // Metody
    saveSettings: () => Promise<void>;
}

const SettingsContext = createContext<SettingsContextType | null>(null);

export const useSettings = () => {
    const context = useContext(SettingsContext);
    if (!context) {
        throw new Error('useSettings must be used within a SettingsProvider');
    }
    return context;
};

interface SettingsProviderProps {
    children: ReactNode;
}

export const SettingsProvider: React.FC<SettingsProviderProps> = ({children}) => {
    const auth = useAuth();
    const isUserLoggedIn = !!auth.currentUser;

    // Pobierz lokalne ustawienia raz przy inicjalizacji
    const localSettings = ParserSettingsService.getLocalSettings();

    // Inicjalizuj stany z ustawień lokalnych
    const [isCalorieValidationEnabled, setIsCalorieValidationEnabled] = useState(
        localSettings.isCalorieValidationEnabled
    );
    const [targetCalories, setTargetCalories] = useState(
        localSettings.targetCalories
    );
    const [skipColumnsCount, setSkipColumnsCount] = useState(
        localSettings.skipColumnsCount
    );
    const [maxSkipColumnsCount, setMaxSkipColumnsCount] = useState(
        localSettings.maxSkipColumnsCount
    );

    // Flagi kontrolne
    const [settingsLoaded, setSettingsLoaded] = useState(false);
    const [initialValuesSet, setInitialValuesSet] = useState(false);
    const [userInteracted, setUserInteracted] = useState(false);

    useEffect(() => {
        const loadSettings = async () => {
            if (!isUserLoggedIn) {
                console.log('Użytkownik nie jest zalogowany - pomijam pobieranie ustawień z API');
                setSettingsLoaded(true);
                return;
            }

            try {
                const settings = await ParserSettingsService.getAllSettings();

                setSkipColumnsCount(settings.skipColumnsCount);
                setMaxSkipColumnsCount(settings.maxSkipColumnsCount);
                setIsCalorieValidationEnabled(settings.isCalorieValidationEnabled);
                setTargetCalories(settings.targetCalories);

                setInitialValuesSet(true);
                setSettingsLoaded(true);
            } catch (error) {
                console.error('Błąd podczas ładowania ustawień:', error);
                setSettingsLoaded(true);
            }
        };

        if (!settingsLoaded) {
            loadSettings().catch(console.error);
        }
    }, [isUserLoggedIn, settingsLoaded]);

    const saveSettings = async () => {
        if (!isUserLoggedIn) {
            ParserSettingsService.saveToLocalStorage({
                skipColumnsCount,
                isCalorieValidationEnabled,
                targetCalories
            });
            return Promise.resolve();
        }

        try {
            await ParserSettingsService.saveAllSettings({
                skipColumnsCount,
                isCalorieValidationEnabled,
                targetCalories
            });
            return Promise.resolve();
        } catch (error) {
            console.error('Błąd podczas zapisywania ustawień:', error);
            return Promise.reject(error);
        }
    };

    const handleSetIsCalorieValidationEnabled = (value: boolean) => {
        setUserInteracted(true);
        setIsCalorieValidationEnabled(value);
    };

    const handleSetTargetCalories = (value: number) => {
        setUserInteracted(true);
        setTargetCalories(value);
    };

    const handleSetSkipColumnsCount = (value: number) => {
        setUserInteracted(true);
        setSkipColumnsCount(value);
    };

    // Automatyczne zapisywanie tylko po interakcji użytkownika i załadowaniu początkowych wartości
    useEffect(() => {
        if (settingsLoaded && userInteracted && initialValuesSet) {
            if (isUserLoggedIn) {
                ParserSettingsService.saveAllSettings({
                    isCalorieValidationEnabled,
                    targetCalories
                }).catch(console.error);
            } else {
                ParserSettingsService.saveToLocalStorage({
                    isCalorieValidationEnabled,
                    targetCalories
                });
            }
        }
    }, [isCalorieValidationEnabled, targetCalories, settingsLoaded, userInteracted, initialValuesSet, isUserLoggedIn]);

    useEffect(() => {
        if (settingsLoaded && userInteracted && initialValuesSet) {
            if (isUserLoggedIn) {
                ParserSettingsService.saveAllSettings({
                    skipColumnsCount
                }).catch(console.error);
            } else {
                ParserSettingsService.saveToLocalStorage({
                    skipColumnsCount
                });
            }
        }
    }, [skipColumnsCount, settingsLoaded, userInteracted, initialValuesSet, isUserLoggedIn]);

    const value = {
        isCalorieValidationEnabled,
        setIsCalorieValidationEnabled: handleSetIsCalorieValidationEnabled,
        targetCalories,
        setTargetCalories: handleSetTargetCalories,
        skipColumnsCount,
        setSkipColumnsCount: handleSetSkipColumnsCount,
        maxSkipColumnsCount,
        saveSettings
    };

    return (
        <SettingsContext.Provider value={value}>
            {children}
        </SettingsContext.Provider>
    );
};