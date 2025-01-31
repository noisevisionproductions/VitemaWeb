import { read, utils } from 'xlsx';

interface ValidationMessage {
    type: 'progress' | 'result' | 'error';
    data: any;
}

self.onmessage = async (e: MessageEvent) => {
    const { file } = e.data;

    try {
        const arrayBuffer = await file.arrayBuffer();
        const workbook = read(arrayBuffer, {
            type: 'array',
            cellFormula: false,
            cellHTML: false,
            cellText: false
        });

        const worksheet = workbook.Sheets[workbook.SheetNames[0]];
        const errors = [];
        let validRows = 0;

        const isRowEmpty = (rowIndex: number) => {
            for (let col = 0; col <= 3; col++) {
                const cell = worksheet[utils.encode_cell({r: rowIndex, c: col})];
                if (cell && cell.v !== undefined && cell.v.toString().trim() !== '') {
                    return false;
                }
            }
            return true;
        };

        const range = utils.decode_range(worksheet['!ref'] || 'A1');
        let lastRow = 0;

        for (let row = 1; row <= range.e.r; row++) {
            if (!isRowEmpty(row)) {
                lastRow = row;
                validRows++;
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

            const name = getValue(1);
            const preparation = getValue(2);
            const shoppingList = getValue(3);

            if (!name && !preparation && !shoppingList) {
                continue;
            }

            const rowErrors = [];
            if (!name) rowErrors.push('brak nazwy posiłku');
            if (!preparation) rowErrors.push('brak sposobu przygotowania');
            if (!shoppingList) rowErrors.push('brak listy zakupów');

            if (rowErrors.length > 0) {
                errors.push({
                    row: R + 1,
                    errors: rowErrors
                });
            }
        }

        self.postMessage({
            type: 'result',
            data: {
                isValid: errors.length === 0,
                totalRows: validRows,
                errors
            }
        } as ValidationMessage);

    } catch (error) {
        let errorMessage = 'Nieznany błąd podczas przetwarzania pliku';

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