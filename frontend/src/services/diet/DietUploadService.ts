import api from "../../config/axios";
import {DietExcelTemplate, ParsedDietData} from "../../types";
import axios, {AxiosError} from 'axios';
import {ParsedProduct} from "../../types/product";
import {toast} from "../../utils/toast";
import {DietRecipeService} from "./DietRecipeService";

interface ValidationResponse {
    valid: boolean;
    validationResults: Array<{
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    }>;
    additionalData: {
        totalMeals?: number;
        calorieAnalysis?: {
            averageCalories: number;
            dailyCalories: number[];
            hasDailyVariation: boolean;
            isWithinMargin?: boolean;
        };
        [key: string]: any;
    };
}


export class DietUploadService {

    static async uploadDiet(
        file: File,
        userId: string,
        parsedData: ParsedDietData,
    ): Promise<string> {
        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('userId', userId);

            const uploadResponse = await api.post('/diets/manager/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            if (!uploadResponse.data.fileUrl) {
                return Promise.reject('Failed to upload file');
            }

            let processedData = JSON.parse(JSON.stringify(parsedData));

            // Przetwarzanie listy zakupów
            if (Array.isArray(processedData.shoppingList)) {
                if (processedData.shoppingList.length > 0 && typeof processedData.shoppingList[0] === 'object') {
                    processedData.shoppingList = processedData.shoppingList.map((item: {
                        [x: string]: { original: any; };
                        original?: any;
                    }) => {
                        if (item.original) {
                            return item.original;
                        }

                        const key = Object.keys(item)[0];
                        if (key && item[key] && item[key].original) {
                            return item[key].original;
                        }

                        return key;
                    });
                }
            } else {
                processedData.shoppingList = [];
            }

            // Przetwarzanie skategoryzowanych produktów
            if (processedData.categorizedProducts && typeof processedData.categorizedProducts === 'object') {
                const converted: Record<string, string[]> = {};

                Object.entries(processedData.categorizedProducts).forEach(([categoryId, products]) => {
                    if (Array.isArray(products)) {
                        converted[categoryId] = products.map(product => {
                            if (typeof product === 'string') {
                                return product;
                            } else if (product && product.original) {
                                return product.original;
                            }
                            return JSON.stringify(product);
                        });
                    }
                });

                processedData.categorizedProducts = converted;
            }

            // Usuwanie tymczasowych ID przepisów
            processedData.days = processedData.days.map((day: { meals: any[]; }) => ({
                ...day,
                meals: day.meals.map(meal => {
                    if (meal.recipeId && meal.recipeId.startsWith('temp-recipe-')) {
                        const {recipeId, ...mealWithoutTempId} = meal;
                        return mealWithoutTempId;
                    }
                    return meal;
                })
            }));

            for (let dayIndex = 0; dayIndex < processedData.days.length; dayIndex++) {
                for (let mealIndex = 0; mealIndex < processedData.days[dayIndex].meals.length; mealIndex++) {
                    const meal = processedData.days[dayIndex].meals[mealIndex];
                    if (meal.recipeId && meal.recipeId.startsWith('temp-recipe-')) {
                        delete meal.recipeId;
                    }
                }
            }

            const saveResponse = await api.post('/diets/manager/save', {
                parsedData: processedData,
                userId,
                fileInfo: {
                    fileName: file.name,
                    fileUrl: uploadResponse.data.fileUrl
                }
            });

            if (!saveResponse.data.dietId) {
                return Promise.reject(saveResponse.data.message || 'Failed to save diet');
            }

            return saveResponse.data.dietId;
        } catch (error: any) {
            console.error('Error uploading diet:', error);

            if (axios.isAxiosError(error)) {
                const axiosError = error as AxiosError;
                const errorMessage = axiosError.message ||
                    'Wystąpił nieznany błąd';
                toast.error(errorMessage);
            } else {
                toast.error('Wystąpił błąd podczas zapisywania diety');
            }

            throw error;
        }
    }

    static async updateProduct(oldProduct: ParsedProduct, editedProduct: ParsedProduct): Promise<ParsedProduct> {
        try {
            const response = await api.put('/diets/categorization/product', {
                oldProduct,
                newProduct: {
                    ...editedProduct,
                    original: oldProduct.original
                }
            });

            if (response.data.success) {
                return response.data.product;
            } else {
                toast.error(response.data.message || 'Nie udało się zaktualizować produktu');
                return oldProduct;
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.error('Axios error updating product:', error.response?.data || error.message);
                toast.error(error.response?.data?.message || 'Błąd sieci podczas aktualizacji produktu');
            } else if (error instanceof Error) {
                console.error('Error updating product:', error.message);
                toast.error(error.message);
            } else {
                console.error('Unexpected error updating product:', error);
                toast.error('Wystąpił nieoczekiwany błąd podczas aktualizacji produktu');
            }

            return oldProduct;
        }
    }

    static async previewDiet(
        file: File,
        template: DietExcelTemplate,
        skipColumnsCount?: number,
        extraParams?: Record<string, any>
    ): Promise<ParsedDietData> {
        try {
            const formData = this.prepareDietTemplateFormData(file, template, skipColumnsCount);

            if (extraParams) {
                Object.entries(extraParams).forEach(([key, value]) => {
                    formData.append(key, String(value));
                });
            }

            const response = await api.post('/diets/upload/preview', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            const sanitizedData = this.sanitizeParsedDietData(response.data, template);

            // Dodanie analizy kalorii do danych, jeśli jest dostępna
            if (response.data.additionalData?.calorieAnalysis) {
                sanitizedData.calorieAnalysis = response.data.additionalData.calorieAnalysis;
            }

            return await this.enrichDietWithRecipes(sanitizedData);
        } catch (error) {
            if (error instanceof AxiosError) {
                if (error.response?.status === 400) {
                    const errorMessage = error.response.data?.message ||
                        error.response.data?.error ||
                        'Nieprawidłowe dane w formularzu';
                    throw new Error(errorMessage);
                }
            }
            throw error;
        }
    }

    static async validateDietTemplateWithUser(
        file: File,
        template: DietExcelTemplate,
        userId?: string,
        skipColumnsCount?: number,
        extraParams?: Record<string, any>
    ): Promise<ValidationResponse> {
        try {
            const formData = this.prepareDietTemplateFormData(file, template, skipColumnsCount);

            if (userId) {
                formData.append('userId', userId);
            }

            if (extraParams) {
                Object.entries(extraParams).forEach(([key, value]) => {
                    formData.append(key, String(value));
                });
            }

            const response = await api.post('/diets/upload/validate-template-with-user', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            return response.data;
        } catch (error) {
            console.error('Błąd podczas walidacji szablonu diety:', error);
            return {
                valid: false,
                validationResults: [
                    {
                        isValid: false,
                        message: error instanceof Error ? error.message : 'Wystąpił błąd podczas walidacji',
                        severity: 'error'
                    }
                ],
                additionalData: {}
            };
        }
    }

    private static prepareDietTemplateFormData(
        file: File,
        template: DietExcelTemplate,
        skipColumnsCounts?: number
    ): FormData {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('mealsPerDay', template.mealsPerDay.toString());
        formData.append('startDate', template.startDate.toDate().toISOString().split('T')[0]);
        formData.append('duration', template.duration.toString());

        Object.entries(template.mealTimes).forEach(([key, value]) => {
            formData.append(`mealTimes[${key}]`, value);
        });

        template.mealTypes.forEach((type) => {
            formData.append('mealTypes', type);
        });

        if (skipColumnsCounts !== undefined) {
            formData.append('skipColumnsCount', skipColumnsCounts.toString());
        }

        return formData;
    }

    /**
     * Wzbogaca podgląd diety o dane przepisów
     */
    static async enrichDietWithRecipes(parsedData: ParsedDietData): Promise<ParsedDietData> {
        try {
            const allMeals = parsedData.days.flatMap(day => day.meals);

            const recipesMap = await DietRecipeService.findRecipesForMeals(allMeals);

            const updatedDays = parsedData.days.map(day => ({
                ...day,
                meals: DietRecipeService.enrichMealsWithRecipeData(day.meals, recipesMap)
            }));

            return {
                ...parsedData,
                days: updatedDays
            };
        } catch (error) {
            console.error("Błąd podczas wzbogacania diety o dane przepisów:", error);
            return parsedData;
        }
    }

    private static sanitizeParsedDietData(data: any, template: DietExcelTemplate): ParsedDietData {
        return {
            ...data,
            shoppingList: Array.isArray(data.shoppingList) ? data.shoppingList : [],
            categorizedProducts: data.categorizedProducts || {},
            mealTimes: data.mealTimes || template.mealTimes,
            mealsPerDay: data.mealsPerDay || template.mealsPerDay,
            startDate: data.startDate || template.startDate,
            duration: data.duration || template.duration,
            mealTypes: data.mealTypes || template.mealTypes
        };
    }
}