import {read, utils} from 'xlsx';

interface ValidationMessage {
    type: 'progress' | 'preliminary-validation' | 'error';
    data: any;
}

interface PreliminaryValidation {
    isValid: boolean;
    totalRows: number;
    hasShoppingList: boolean;
    errors: Array<{
        row: number;
        errors: string[];
    }>;
    structure: {
        meals: Array<{
            name: string;
            preparation: string;
            ingredients: string;
            nutritionalValues: string;
        }>;
    };
}

self.onmessage = async (e: MessageEvent) => {
    const {file} = e.data;

    try {
        const arrayBuffer = await file.arrayBuffer();
        const workbook = read(arrayBuffer, {
            type: 'array',
            cellFormula: false,
            cellHTML: false,
            cellText: false
        });

        const worksheet = workbook.Sheets[workbook.SheetNames[0]];
        const errors: string | any[] = [];
        let validRows = 0;
        let hasShoppingList = false;
        const meals = [];

        const isRowEmpty = (rowIndex: number) => {
            for (let col = 0; col <= 3; col++) {
                const cell = worksheet[utils.encode_cell({r: rowIndex, c: col})];
                if (cell && cell.v !== undefined && cell.v.toString().trim() !== '') {
                    return false;
                }
            }
            return true;
        };

        const hasShoppingListInRow = (rowIndex: number) => {
            if (rowIndex === 0) return false;

            const cell = worksheet[utils.encode_cell({r: rowIndex, c: 3})];
            return cell && cell.v !== undefined && cell.v.toString().trim() !== '';
        };

        const range = utils.decode_range(worksheet['!ref'] || 'A1');
        let lastRow = 0;

        for (let row = 1; row <= range.e.r; row++) {
            if (!isRowEmpty(row)) {
                lastRow = row;
                validRows++;
            }

            if (hasShoppingListInRow(row)) {
                hasShoppingList = true;
            }

            if (row % 1000 === 0) {
                self.postMessage({
                    type: 'progress',
                    data: {
                        progress: Math.round((row / range.e.r) * 100)
                    }
                } as ValidationMessage);
            }
        }

        for (let R = 1; R <= lastRow; R++) {
            const getValue = (C: number) => {
                const cell = worksheet[utils.encode_cell({r: R, c: C})];
                return cell ? cell.v?.toString().trim() : '';
            };

            const meal = {
                name: getValue(1),
                preparation: getValue(2),
                ingredients: getValue(3),
                nutritionalValues: getValue(4)
            };

            if (meal.name || meal.preparation) {
                meals.push(meal);
            }
        }

        self.postMessage({
            type: 'preliminary-validation',
            data: {
                isValid: errors.length === 0,
                totalRows: validRows,
                hasShoppingList,
                errors,
                structure: {
                    meals
                }
            } as PreliminaryValidation
        } as ValidationMessage);

    } catch (error) {
        let errorMessage = `Nieznany błąd podczas przetwarzania pliku, ${error}`;

        if (error instanceof Error) {
            errorMessage = error.message;
        } else if (typeof error === 'string') {
            errorMessage = error;
        } else if (error && typeof error === 'object' && 'message' in error) {
            errorMessage = error.message as string;
        }

        self.postMessage({
            type: 'error',
            data: errorMessage
        } as ValidationMessage);
    }
};