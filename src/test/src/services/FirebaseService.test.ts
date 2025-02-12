import { describe, it, expect, vi, beforeEach } from 'vitest';
import { FirebaseService} from "../../../services/FirebaseService";
import {
    collection,
    doc,
    getDocs,
    query,
    Timestamp,
    where,
    writeBatch
} from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage} from "../../../config/firebase";

vi.mock('firebase/firestore', async () => {
    const actual = await vi.importActual('firebase/firestore');
    return {
        ...actual,
        addDoc: vi.fn(),
        collection: vi.fn(),
        doc: vi.fn(),
        getDocs: vi.fn(),
        query: vi.fn(),
        updateDoc: vi.fn(),
        writeBatch: vi.fn(),
        where: vi.fn(),
    }
});

vi.mock('firebase/storage', async () => {
    const actual = await vi.importActual('firebase/storage');
    return {
        ...actual,
        ref: vi.fn(),
        uploadBytes: vi.fn(),
        getDownloadURL: vi.fn(),
    }
});

vi.mock('../../config/firebase', () => ({
    db: {},
    storage: {},
}));

describe('FirebaseService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('uploadExcelFile', () => {
        it('should upload file and return download URL', async () => {
            const mockFile = new File(['test'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
            const mockUserId = 'user123';
            const mockDownloadURL = 'https://example.com/file.xlsx';

            (ref as any).mockReturnValue('mockStorageRef');
            (uploadBytes as any).mockResolvedValue({});
            (getDownloadURL as any).mockResolvedValue(mockDownloadURL);

            const result = await FirebaseService.uploadExcelFile(mockFile, mockUserId);

            expect(ref).toHaveBeenCalledWith(storage, `diets/${mockUserId}/${mockFile.name}`);
            expect(uploadBytes).toHaveBeenCalledWith('mockStorageRef', mockFile);
            expect(result).toBe(mockDownloadURL);
        });
    });

    describe('getShoppingList', () => {
        it('should return shopping list for version 2', async () => {
            const mockDietId = 'diet123';
            const mockShoppingListData = {
                dietId: mockDietId,
                items: ['item1', 'item2'],
                version: 2,
                startDate: Timestamp.now(),
                endDate: Timestamp.now(),
                userId: 'user123'
            };

            (collection as any).mockReturnValue('mockCollection');
            (query as any).mockReturnValue('mockQuery');
            (getDocs as any).mockResolvedValue({
                empty: false,
                docs: [{
                    id: 'shoppingListId',
                    data: () => mockShoppingListData
                }]
            });

            const result = await FirebaseService.getShoppingList(mockDietId);

            expect(collection).toHaveBeenCalledWith(db, 'shopping_lists');
            expect(query).toHaveBeenCalledWith(
                'mockCollection',
                where('dietId', '==', mockDietId)
            );
            expect(result).toMatchObject({
                ...mockShoppingListData,
                id: 'shoppingListId'
            });
        });

        it('should return null when no shopping list found', async () => {
            const mockDietId = 'diet123';

            (collection as any).mockReturnValue('mockCollection');
            (query as any).mockReturnValue('mockQuery');
            (getDocs as any).mockResolvedValue({ empty: true, docs: [] });

            const result = await FirebaseService.getShoppingList(mockDietId);

            expect(result).toBeNull();
        });
    });

    describe('deleteDietWithRelatedData', () => {
        it('should delete diet and related data', async () => {
            const mockDietId = 'diet123';
            const mockBatch = {
                delete: vi.fn(),
                commit: vi.fn()
            };

            (writeBatch as any).mockReturnValue(mockBatch);
            (collection as any).mockReturnValue('mockCollection');
            (query as any).mockReturnValue('mockQuery');
            (getDocs as any).mockResolvedValue({
                docs: [{ ref: 'mockShoppingListRef' }, { ref: 'mockAnotherRef' }]
            });
            (doc as any).mockReturnValue('mockDietRef');

            await FirebaseService.deleteDietWithRelatedData(mockDietId);

            expect(writeBatch).toHaveBeenCalled();
            expect(mockBatch.delete).toHaveBeenCalledWith('mockShoppingListRef');
            expect(mockBatch.delete).toHaveBeenCalledWith('mockAnotherRef');
            expect(mockBatch.delete).toHaveBeenCalledWith('mockDietRef');
            expect(mockBatch.commit).toHaveBeenCalled();
        });
    });
});