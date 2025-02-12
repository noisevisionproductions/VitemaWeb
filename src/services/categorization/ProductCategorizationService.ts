import {ProductCategorization, UncategorizedProduct} from "../../types/product-categories";
import {
    collection, doc,
    getDocs, query,
    Timestamp, where, writeBatch, arrayUnion, increment,
    limit, QueryConstraint, DocumentData, OrderByDirection,
    orderBy
} from "firebase/firestore";
import {db} from "../../config/firebase";
import {DEFAULT_CATEGORIES} from "../../data/productCategories";
import {ParsedProduct} from "../../types/product";
import {ProductParsingService} from "./ProductParsingService";

interface ProductCategoryData extends DocumentData {
    productName: string;
    categoryId: string;
    usageCount: number;
    variations: string[];
    lastUsed: Timestamp;
    createdAt: Timestamp;
    updatedAt: Timestamp;
}

interface BestMatch {
    categoryId: string;
    similarity: number;
}

export class ProductCategorizationService {

    private static readonly SIMILARITY_THRESHOLD = 0.8;
    private static readonly COLLECTION_NAME = 'product_categories';

    static getDefaultCategories() {
        return DEFAULT_CATEGORIES;
    }

    static async getProductCategories(): Promise<ProductCategorization[]> {
        const productCategoriesRef = collection(db, 'product_categorizations');
        const snapshot = await getDocs(productCategoriesRef);

        return snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        } as ProductCategorization));
    }

    static async getUncategorizedProducts(): Promise<UncategorizedProduct[]> {
        const uncategorizedRef = collection(db, 'uncategorized_products');
        const snapshot = await getDocs(uncategorizedRef);
        return snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        } as UncategorizedProduct));
    }

    static async saveCategorization(
        categorizedProducts: Record<string, ParsedProduct[]>
    ) {
        const batch = writeBatch(db);

        for (const [categoryId, products] of Object.entries(categorizedProducts)) {
            for (const product of products) {
                const existingQuery = query(
                    collection(db, 'product_categorizations'),
                    where('productName', '==', product.name.toLowerCase())
                );

                const existingDocs = await getDocs(existingQuery);

                if (existingDocs.empty) {
                    batch.set(doc(collection(db, 'product_categorizations')), {
                        productName: product.name.toLowerCase(),
                        categoryId,
                        usageCount: 1,
                        lastUsed: Timestamp.now(),
                        variations: [product.original],
                        quantities: [{
                            quantity: product.quantity,
                            unit: product.unit
                        }],
                        createdAt: Timestamp.now(),
                        updatedAt: Timestamp.now()
                    });
                } else {
                    const doc = existingDocs.docs[0];
                    batch.update(doc.ref, {
                        usageCount: increment(1),
                        lastUsed: Timestamp.now(),
                        updatedAt: Timestamp.now(),
                        variations: arrayUnion(product.original),
                        quantities: arrayUnion({
                            quantity: product.quantity,
                            unit: product.unit
                        })
                    });
                }
            }
        }

        await batch.commit();
    }

    static async getSuggestions(productName: string): Promise<string | null> {
        const q = query(
            collection(db, 'product_categorizations'),
            where('variations', 'array-contains', productName.toLowerCase())
        );

        try {
            const querySnapshot = await getDocs(q);

            if (!querySnapshot.empty) {
                const mostUsed = querySnapshot.docs.reduce((prev, current) =>
                    (current.data().usageCount > prev.data().usageCount) ? current : prev
                );

                return mostUsed.data().categoryId;
            }
        } catch (error) {
            console.error('Error getting suggestions:', error);
            if (error instanceof Error && error.message.includes('index')) {
                console.error('Missing index. Please create it in Firebase Console');
            }
        }

        return null;
    }

    static async suggestCategory(product: ParsedProduct): Promise<string | null> {
        const normalizeName = ProductParsingService.cleanProductName(product.name);

        // Szukanie dokładnego dopasowania
        const exactMatchConstraints: QueryConstraint[] = [
            where('productName', '==', normalizeName),
            orderBy('usageCount', 'desc' as OrderByDirection),
            limit(1)
        ];

        const exactMatchQuery = query(
            collection(db, this.COLLECTION_NAME),
            ...exactMatchConstraints
        );

        const exactMatches = await getDocs(exactMatchQuery);
        if (!exactMatches.empty) {
            return (exactMatches.docs[0].data() as ProductCategoryData).categoryId;
        }

        // Szukanie w wariacjach nazw
        const variationConstraints: QueryConstraint[] = [
            where('variations', 'array-contains', normalizeName),
            orderBy('usageCount', 'desc' as OrderByDirection),
            limit(1)
        ];

        const variationsQuery = query(
            collection(db, this.COLLECTION_NAME),
            ...variationConstraints
        );

        const variationMatches = await getDocs(variationsQuery);
        if (!variationMatches.empty) {
            return (variationMatches.docs[0].data() as ProductCategoryData).categoryId;
        }

        // Szukanie podobnych produktów
        const similarityConstraints: QueryConstraint[] = [
            orderBy('usageCount', 'desc' as OrderByDirection),
            limit(50)
        ];

        const allProductsQuery = query(
            collection(db, this.COLLECTION_NAME),
            ...similarityConstraints
        );

        const allProducts = await getDocs(allProductsQuery);
        let bestMatch: BestMatch | undefined;

        allProducts.forEach(doc => {
            const data = doc.data() as ProductCategoryData;
            const similarity = ProductParsingService.calculateSimilarity(
                normalizeName,
                data.productName
            );

            if (similarity >= this.SIMILARITY_THRESHOLD &&
                (!bestMatch || similarity > bestMatch.similarity)) {
                bestMatch = {
                    categoryId: data.categoryId,
                    similarity: similarity
                };
            }
        });

        return bestMatch?.categoryId ?? null;
    }

    static async bulkSaveProductCategorizations(
        categorizedProducts: Record<string, ParsedProduct[]>
    ): Promise<void> {
        const batch = writeBatch(db);
        const timestamp = Timestamp.now();

        for (const [categoryId, products] of Object.entries(categorizedProducts)) {
            for (const product of products) {
                const normalizedName = ProductParsingService.cleanProductName(product.name);

                if (!DEFAULT_CATEGORIES.find(cat => cat.id === categoryId)) {
                    console.warn(`Nieprawidłowa kategoria: ${categoryId} dla produktu: ${product.name}`);
                    continue;
                }

                const existingQuery = query(
                    collection(db, this.COLLECTION_NAME),
                    where('productName', '==', normalizedName)
                );

                const existingDocs = await getDocs(existingQuery);

                if (existingDocs.empty) {
                    const newDocRef = doc(collection(db, this.COLLECTION_NAME));
                    batch.set(newDocRef, {
                        productName: normalizedName,
                        categoryId,
                        usageCount: 1,
                        variations: [product.original.toLowerCase()],
                        lastUsed: timestamp,
                        createdAt: timestamp,
                        updatedAt: timestamp
                    });
                } else {
                    const docRef = existingDocs.docs[0].ref;
                    const currentData = existingDocs.docs[0].data() as ProductCategoryData;

                    batch.update(docRef, {
                        usageCount: increment(1),
                        variations: [...new Set([...currentData.variations, product.original.toLowerCase()])],
                        lastUsed: timestamp,
                        updatedAt: timestamp
                    });
                }
            }
        }

        await batch.commit();
    }

    static async getCategoryStats(): Promise<Record<string, number>> {
        const stats: Record<string, number> = {};

        DEFAULT_CATEGORIES.forEach(category => {
            stats[category.id] = 0;
        });

        const snapshot = await getDocs(collection(db, this.COLLECTION_NAME));

        snapshot.forEach(doc => {
            const data = doc.data() as ProductCategoryData;
            if (data.categoryId in stats) {
                stats[data.categoryId] += data.usageCount;
            }
        });

        return stats;
    }
}