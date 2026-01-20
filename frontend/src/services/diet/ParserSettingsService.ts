import api from "../../config/axios";

export interface ParserSettings {
    // Excel parser settings
    skipColumnsCount: number;
    maxSkipColumnsCount: number;

    // Calorie validation settings
    isCalorieValidationEnabled: boolean;
    targetCalories: number;
}

export class ParserSettingsService {

    private static SKIP_COLUMNS_KEY = 'excelParserSkipColumnsCount';
    private static CALORIE_ENABLED_KEY = 'calorieValidationEnabled';
    private static CALORIE_TARGET_KEY = 'targetCalories';

    static async getAllSettings(): Promise<ParserSettings> {
        const localSettings = this.getLocalSettings();

        try {
            const response = await api.get('/diets/parser-settings');

            if (response.data) {
                return {
                    ...localSettings,
                    skipColumnsCount: response.data.skipColumnsCount || localSettings.skipColumnsCount,
                    maxSkipColumnsCount: response.data.maxSkipColumnsCount || 3,
                };
            }

            return localSettings;
        } catch (error) {
            console.error('Error fetching parser settings:', error);
            // W przypadku błędu zwróć tylko ustawienia lokalne
            return localSettings;
        }
    }

    static getLocalSettings(): ParserSettings {
        // Obsługa skipColumnsCount
        const skipColumnsCount = this.getLocalSkipColumnsCount();

        // Obsługa walidacji kalorii
        let isCalorieValidationEnabled = false;
        try {
            isCalorieValidationEnabled = localStorage.getItem(this.CALORIE_ENABLED_KEY) === 'true';
        } catch (e) {
            console.error('Error reading calorie validation enabled:', e);
        }

        // Obsługa targetCalories
        let targetCalories = 2000;
        try {
            const stored = localStorage.getItem(this.CALORIE_TARGET_KEY);
            if (stored) {
                const parsed = parseInt(stored, 10);
                if (!isNaN(parsed)) {
                    targetCalories = parsed;
                }
            }
        } catch (e) {
            console.error('Error reading target calories:', e);
        }

        return {
            skipColumnsCount,
            maxSkipColumnsCount: 3,
            isCalorieValidationEnabled,
            targetCalories
        };
    }

    static async saveAllSettings(settings: Partial<ParserSettings>): Promise<boolean> {
        this.saveToLocalStorage(settings);

        if (settings.skipColumnsCount !== undefined) {
            try {
                await api.put('/diets/parser-settings', {
                    skipColumnsCount: settings.skipColumnsCount
                });
            } catch (error) {
                console.error('Error saving parser settings to API:', error);
            }
        }

        return true;
    }

    static saveToLocalStorage(settings: Partial<ParserSettings>): void {
        try {
            if (settings.skipColumnsCount !== undefined) {
                localStorage.setItem(this.SKIP_COLUMNS_KEY, settings.skipColumnsCount.toString());
            }

            if (settings.isCalorieValidationEnabled !== undefined) {
                localStorage.setItem(
                    this.CALORIE_ENABLED_KEY,
                    settings.isCalorieValidationEnabled.toString()
                );
            }

            if (settings.targetCalories !== undefined && !isNaN(settings.targetCalories)) {
                localStorage.setItem(
                    this.CALORIE_TARGET_KEY,
                    settings.targetCalories.toString()
                );
            }
        } catch (e) {
            console.error('Error saving settings to localStorage:', e);
        }
    }

    // Pomocnicze metody
    static getLocalSkipColumnsCount(): number {
        try {
            const value = localStorage.getItem(this.SKIP_COLUMNS_KEY);
            return value ? parseInt(value, 10) : 1;
        } catch (error) {
            return 1;
        }
    }
}