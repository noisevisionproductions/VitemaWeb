import {read, utils, WorkBook} from 'xlsx';
import {Timestamp} from "firebase/firestore";
import {DietTemplate, MealType, ParsedDietData, ParsedMeal} from "../types";
import {ParsedProduct} from 'src/types/product';
import {ProductParsingService} from "./categorization/ProductParsingService";
import {createSafeProduct} from "../utils/productUtils";

export interface ParsedDay {
    date: Timestamp;
    meals: ParsedMeal[];
}

interface ParsedExcelResult {
    meals: ParsedMeal[];
    totalMeals: number;
    shoppingList: {
        original: string;
        parsed: ParsedProduct;
    }[];
}

export class ExcelParserService {

    private static parseNutritionalValues(value: string | undefined): ParsedMeal['nutritionalValues'] | undefined {
        if (!value) return undefined;

        const values = value.split(',').map(v => parseFloat(v));
        if (values.length !== 4 || values.some(isNaN)) return undefined;

        return {
            calories: values[0] || 0,
            protein: values[1] || 0,
            fat: values[2] || 0,
            carbs: values[3] || 0
        };
    }

    private static extractShoppingItems(jsonData: string[][]): {
        original: string;
        parsed: ParsedProduct;
    }[] {
        const uniqueItems = new Map<string, {
            original: string;
            parsed: ParsedProduct;
            occurrences: number;
        }>();

        for (const row of jsonData.slice(1)) {
            if (!row[3]?.trim()) continue;

            const items = row[3]
                .split(',')
                .map(item => item.trim())
                .filter(item => item.length > 0);

            for (const item of items) {
                const parseResult = ProductParsingService.parseProduct(item);
                const parsedProduct = parseResult.success && parseResult.product
                    ? parseResult.product
                    : createSafeProduct(item);

                const key = parsedProduct.name.toLowerCase();

                if (uniqueItems.has(key)) {
                    const existingItem = uniqueItems.get(key)!;
                    // Sumujemy, tylko jeśli jednostki są takie same
                    if (existingItem.parsed.unit === parsedProduct.unit) {
                        existingItem.parsed.quantity += parsedProduct.quantity;
                        existingItem.occurrences += 1;
                    } else {
                        // Jeśli jednostki są różne, dodaj jako nowy produkt
                        const newKey = `${key}_${parsedProduct.unit}`;
                        uniqueItems.set(newKey, {
                            original: item,
                            parsed: parsedProduct,
                            occurrences: 1
                        });
                    }
                } else {
                    uniqueItems.set(key, {
                        original: item,
                        parsed: parsedProduct,
                        occurrences: 1
                    });
                }
            }
        }

        // Konwertuj wartości mapy na końcową listę produktów
        return Array.from(uniqueItems.values()).map(({ original, parsed }) => ({
            original,
            parsed: {
                ...parsed,
                // Zaokrąglamy quantity do 2 miejsc po przecinku
                quantity: Math.round(parsed.quantity * 100) / 100
            }
        }));
    }

    static async parseDietExcel(file: File): Promise<ParsedExcelResult> {
        try {
            const data = await file.arrayBuffer();
            const workbook: WorkBook = read(data);
            const worksheet = workbook.Sheets[workbook.SheetNames[0]];
            const jsonData = utils.sheet_to_json<string[]>(worksheet, {
                header: 1,
                raw: false,
                defval: ''
            }) as string[][];

            const shoppingList = this.extractShoppingItems(jsonData);

            const meals: ParsedMeal[] = [];
            for (let i = 1; i < jsonData.length; i++) {
                const row = jsonData[i];
                if (!row[1]) continue;

                const ingredients = row[3]
                    .split(',')
                    .map(item => item.trim())
                    .filter(item => item.length > 0)
                    .map(item => {
                        const parseResult = ProductParsingService.parseProduct(item);
                        if (parseResult.success && parseResult.product) {
                            return {
                                name: parseResult.product.name,
                                quantity: parseResult.product.quantity,
                                unit: parseResult.product.unit,
                                original: parseResult.product.original,
                                hasCustomUnit: parseResult.product.hasCustomUnit || false
                            } as ParsedProduct;
                        }
                        // jeśli parsowanie się nie udało, zwracamy domyślny obiekt
                        return {
                            name: item,
                            quantity: 1,
                            unit: 'szt',
                            original: item,
                            hasCustomUnit: false
                        } as ParsedProduct;
                    });

                meals.push({
                    name: row[1].trim(),             // Kolumna B: Nazwa
                    instructions: row[2].trim(),      // Kolumna C: Sposób przygotowania
                    ingredients: ingredients, // Kolumna D: Lista składników
                    nutritionalValues: this.parseNutritionalValues(row[4]), // Kolumna E: Wartości odżywcze (opcjonalne)
                    mealType: MealType.BREAKFAST,    // Tymczasowo, zostanie zaktualizowane przez szablon
                    time: ''                         // Tymczasowo, zostanie zaktualizowane przez szablon
                });
            }

            return {
                meals,
                totalMeals: meals.length,
                shoppingList
            };
        } catch (error) {
            console.error('Error parsing Excel file:', error);
            throw new Error('Błąd podczas parsowania pliku Excel');
        }
    }

    static applyTemplate(parsedExcel: ParsedExcelResult, template: DietTemplate): ParsedDietData {
        const days: ParsedDay[] = [];
        const startDate = template.startDate.toDate();
        let mealIndex = 0;

        // Sumujemy ilości tych samych produktów
        const aggregatedProducts = new Map<string, ParsedProduct>();

        parsedExcel.shoppingList.forEach(({ parsed }) => {
            const key = `${parsed.name}_${parsed.unit}`;
            if (aggregatedProducts.has(key)) {
                const existing = aggregatedProducts.get(key)!;
                existing.quantity += parsed.quantity;
            } else {
                aggregatedProducts.set(key, { ...parsed });
            }
        });

        for (let i = 0; i < template.duration; i++) {
            const currentDate = new Date(startDate);
            currentDate.setDate(startDate.getDate() + i);

            const dayMeals = template.mealTypes.map((mealType, typeIndex) => {
                const meal = parsedExcel.meals[mealIndex % parsedExcel.meals.length];
                mealIndex++;

                return {
                    ...meal,
                    mealType,
                    time: template.mealTimes[`meal_${typeIndex}`]
                };
            });

            days.push({
                date: Timestamp.fromDate(currentDate),
                meals: dayMeals
            });
        }

        // Konwertujemy zagregowane produkty z powrotem na listę
        const formattedShoppingList = Array.from(aggregatedProducts.values()).map(product =>
            `${product.quantity} ${product.unit} ${product.name}`
        );

        return {
            days,
            shoppingList: formattedShoppingList
        };
    }
}