import { describe, it, expect, vi } from 'vitest';
import { ExcelParserService} from "../../../services/ExcelParserService";
import { MealType} from "../../../types";
import * as XLSX from 'xlsx';

vi.mock('xlsx', () => ({
    read: vi.fn(),
    utils: {
        sheet_to_json: vi.fn()
    }
}));

describe('ExcelParserService', () => {
    describe('parseNutritionalValues', () => {
        it('should parse valid nutritional values', () => {
            const method = (ExcelParserService as any).parseNutritionalValues;
            const result = method('100,20,10,30');

            expect(result).toEqual({
                calories: 100,
                protein: 20,
                fat: 10,
                carbs: 30
            });
        });

        it('should return undefined for invalid input', () => {
            const method = (ExcelParserService as any).parseNutritionalValues;
            expect(method(undefined)).toBeUndefined();
            expect(method('invalid')).toBeUndefined();
            expect(method('1,2,3')).toBeUndefined();
        });
    });

    describe('extractShoppingItems', () => {
        it('should extract unique shopping items from multiple rows', () => {
            const method = (ExcelParserService as any).extractShoppingItems;
            const mockJsonData = [
                ['', 'Meal', 'Instructions', 'Ingredient1, Ingredient2, Ingredient3'],
                ['', 'Another Meal', 'Instructions', 'Ingredient2, Ingredient4']
            ];

            const result = method(mockJsonData);

            expect(result.sort()).toEqual([
                'Ingredient2',
                'Ingredient4'
            ].sort());
        });

        it('should handle empty or invalid rows', () => {
            const method = (ExcelParserService as any).extractShoppingItems;
            const mockJsonData = [
                ['', '', '', ''],
                ['', 'Meal', 'Instructions', 'Ingredient1, Ingredient2']
            ];

            const result = method(mockJsonData);

            expect(result.sort()).toEqual([
                'Ingredient1',
                'Ingredient2'
            ].sort());
        });
    });

    describe('parseDietExcel', () => {
        it('should parse excel file successfully', async () => {
            const mockWorkbook = {
                Sheets: {
                    'Sheet1': {}
                },
                SheetNames: ['Sheet1']
            };

            const mockJsonData = [
                ['Header1', 'Header2', 'Header3', 'Header4', 'Header5'],
                ['', 'Meal 1', 'Prepare 1', 'Ingredient1, Ingredient2', '100,20,10,30'],
                ['', 'Meal 2', 'Prepare 2', 'Ingredient3, Ingredient4', '200,30,15,40']
            ];

            (XLSX.read as any).mockReturnValue(mockWorkbook);
            (XLSX.utils.sheet_to_json as any).mockReturnValue(mockJsonData);

            const mockFile = {
                arrayBuffer: vi.fn().mockResolvedValue(new ArrayBuffer(8))
            } as any;

            const result = await ExcelParserService.parseDietExcel(mockFile);

            expect(result.meals).toHaveLength(2);
            expect(result.totalMeals).toBe(2);

            expect(result.shoppingList.sort()).toEqual([
                'Ingredient1',
                'Ingredient2',
                'Ingredient3',
                'Ingredient4'
            ].sort());

            // Sprawdzenie szczegółów posiłku
            expect(result.meals[0]).toMatchObject({
                name: 'Meal 1',
                instructions: 'Prepare 1',
                mealType: MealType.BREAKFAST,
                time: '',
                nutritionalValues: {
                    calories: 100,
                    protein: 20,
                    fat: 10,
                    carbs: 30
                }
            });
        });
    });
});