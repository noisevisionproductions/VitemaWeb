import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ProductCategorizationService} from "../../../services/categorization/ProductCategorizationService";
import { getDocs, collection, query, where, writeBatch } from 'firebase/firestore'
import {db} from "../../../config/firebase";

vi.mock('firebase/firestore', async () => {
    const actual = await vi.importActual('firebase/firestore');
    return {
        ...actual,
        getDocs: vi.fn(),
        collection: vi.fn(),
        query: vi.fn(),
        where: vi.fn(),
        writeBatch: vi.fn(),
        doc: vi.fn(),
        increment: vi.fn(),
        arrayUnion: vi.fn(),
    }
});

describe('ProductCategorizationService', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('getDefaultCategories', () => {
        it('should return default categories', () => {
            const categories = ProductCategorizationService.getDefaultCategories()
            expect(categories).toBeDefined()
            expect(Array.isArray(categories)).toBe(true)
            expect(categories.length).toBeGreaterThan(0)
        })
    })

    describe('getProductCategories', () => {
        it('should fetch product categories from Firestore', async () => {
            const mockDocs = [
                {
                    id: '1',
                    data: () => ({
                        productName: 'makaron',
                        categoryId: 'grains',
                        usageCount: 1
                    })
                }
            ]

            vi.mocked(getDocs).mockResolvedValueOnce({
                docs: mockDocs,
                empty: false
            } as any)

            const result = await ProductCategorizationService.getProductCategories()

            expect(collection).toHaveBeenCalledWith(db, 'product_categorizations')
            expect(result).toHaveLength(1)
            expect(result[0]).toMatchObject({
                id: '1',
                productName: 'makaron',
                categoryId: 'grains'
            })
        })
    })

    describe('getSuggestions', () => {
        it('should return category suggestion for existing product', async () => {
            const mockDocs = [
                {
                    data: () => ({
                        categoryId: 'grains',
                        usageCount: 5
                    })
                },
                {
                    data: () => ({
                        categoryId: 'other',
                        usageCount: 2
                    })
                }
            ]

            vi.mocked(getDocs).mockResolvedValueOnce({
                docs: mockDocs,
                empty: false
            } as any)

            const result = await ProductCategorizationService.getSuggestions('makaron')

            expect(query).toHaveBeenCalled()
            expect(where).toHaveBeenCalledWith('variations', 'array-contains', 'makaron')
            expect(result).toBe('grains')
        })

        it('should return null for non-existing product', async () => {
            vi.mocked(getDocs).mockResolvedValueOnce({
                docs: [],
                empty: true
            } as any)

            const result = await ProductCategorizationService.getSuggestions('nieistniejący produkt')
            expect(result).toBeNull()
        })
    })

    describe('saveCategorization', () => {
        it('should save new categorizations to Firestore', async () => {
            const mockBatch = {
                set: vi.fn(),
                update: vi.fn(),
                commit: vi.fn()
            }

            vi.mocked(writeBatch).mockReturnValue(mockBatch as any)
            vi.mocked(getDocs).mockResolvedValue({ empty: true, docs: [] } as any)

            const categorizedProducts = {
                'grains': ['makaron', 'ryż'],
                'dairy': ['mleko']
            }

            await ProductCategorizationService.saveCategorization(categorizedProducts)

            expect(writeBatch).toHaveBeenCalled()
            expect(mockBatch.set).toHaveBeenCalledTimes(3) // 3 produkty
            expect(mockBatch.commit).toHaveBeenCalled()
        })
    })
})