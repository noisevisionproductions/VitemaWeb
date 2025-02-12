import {
    addDoc,
    collection,
    doc,
    getDocs,
    query,
    Timestamp,
    updateDoc,
    where,
    writeBatch
} from 'firebase/firestore';
import {getDownloadURL, ref, uploadBytes} from 'firebase/storage';
import {db, storage} from '../config/firebase';
import {CategorizedShoppingListItem, ParsedDietData, ShoppingList, ShoppingListV1, ShoppingListV2} from "../types";
import {ProductParsingService} from "./categorization/ProductParsingService";

export class FirebaseService {

    static async uploadExcelFile(file: File, userId: string) {
        const storageRef = ref(storage, `diets/${userId}/${file.name}`);
        await uploadBytes(storageRef, file);
        return await getDownloadURL(storageRef);
    }

    static async deleteDietWithRelatedData(dietId: string) {
        const batch = writeBatch(db);

        // Usuń listę zakupów
        const shoppingListQuery = query(
            collection(db, 'shopping_lists'),
            where('dietId', '==', dietId)
        );
        const shoppingListSnapshot = await getDocs(shoppingListQuery);
        shoppingListSnapshot.docs.forEach(doc => {
            batch.delete(doc.ref);
        });

        // Usuń referencje przepisów
        const recipeRefsQuery = query(
            collection(db, 'recipe_references'),
            where('dietId', '==', dietId)
        );
        const recipeRefsSnapshot = await getDocs(recipeRefsQuery);
        recipeRefsSnapshot.docs.forEach(doc => {
            batch.delete(doc.ref);
        });

        // Usuń dietę
        batch.delete(doc(db, 'diets', dietId));

        await batch.commit();
    }

    static async saveDietWithShoppingList(
        parsedData: ParsedDietData,
        userId: string,
        fileInfo: {
            fileName: string,
            fileUrl: string
        }
    ) {
        try {
            // 1. Najpierw tworzymy nowy dokument diety
            const dietsRef = collection(db, 'diets');
            const dietDocRef = await addDoc(dietsRef, {
                userId,
                createdAt: Timestamp.fromDate(new Date()),
                updatedAt: Timestamp.fromDate(new Date()),
                days: [],
                metadata: {
                    totalDays: parsedData.days.length,
                    fileName: fileInfo.fileName,
                    fileUrl: fileInfo.fileUrl
                }
            });

            // 2. Zapisujemy przepisy i zbieramy ich ID
            const savedRecipeIds: { [key: string]: string } = {};

            for (let dayIndex = 0; dayIndex < parsedData.days.length; dayIndex++) {
                const day = parsedData.days[dayIndex];

                for (const meal of day.meals) {
                    const recipe = {
                        name: meal.name,
                        instructions: meal.instructions,
                        nutritionalValues: meal.nutritionalValues,
                        createdAt: Timestamp.fromDate(new Date()),
                        photos: [],
                        parentRecipeId: null
                    };

                    const recipesRef = collection(db, 'recipes');
                    const recipeDocRef = await addDoc(recipesRef, recipe);

                    // Dodajemy referencję przepisu
                    await addDoc(collection(db, 'recipe_references'), {
                        recipeId: recipeDocRef.id,
                        dietId: dietDocRef.id,
                        userId,
                        mealType: meal.mealType,
                        addedAt: Timestamp.fromDate(new Date())
                    });

                    savedRecipeIds[`${dayIndex}_${meal.mealType}`] = recipeDocRef.id;
                }
            }

            // 3. Aktualizujemy dni w diecie
            const updatedDays = parsedData.days.map((day, dayIndex) => ({
                date: day.date,
                meals: day.meals.map(meal => ({
                    recipeId: savedRecipeIds[`${dayIndex}_${meal.mealType}`],
                    mealType: meal.mealType,
                    time: meal.time
                }))
            }));

            // Aktualizujemy dokument diety z dniami
            await updateDoc(dietDocRef, {days: updatedDays});

            const categorizedItems: Record<string, CategorizedShoppingListItem[]> = {};

            if (parsedData.categorizedProducts) {
                for (const [categoryId, products] of Object.entries(parsedData.categorizedProducts)) {
                    categorizedItems[categoryId] = products.map(productString => {
                        const parseResult = ProductParsingService.parseProduct(productString);
                        if (parseResult.success && parseResult.product){
                            return {
                                name: parseResult.product.name,
                                quantity: parseResult.product.quantity,
                                unit: parseResult.product.unit,
                                original: productString
                            };
                        }
                        return {
                            name: productString,
                            quantity: 1,
                            unit: 'szt',
                            original: productString
                        };
                    });
                }
            } else  {
                categorizedItems['other'] = parsedData.shoppingList.map(productString => {
                    const parseResult = ProductParsingService.parseProduct(productString);
                    if (parseResult.success && parseResult.product) {
                        return {
                            name: parseResult.product.name,
                            quantity: parseResult.product.quantity,
                            unit: parseResult.product.unit,
                            original: productString
                        };
                    }
                    return {
                        name: productString,
                        quantity: 1,
                        unit: 'szt',
                        original: productString
                    };
                });
            }

            // 4. Zapisujemy listę zakupów
            await addDoc(collection(db, 'shopping_lists'), {
                dietId: dietDocRef.id,
                userId,
                items: categorizedItems,
                createdAt: Timestamp.fromDate(new Date()),
                startDate: parsedData.days[0].date,
                endDate: parsedData.days[parsedData.days.length - 1].date,
                version: 3
            });

            return dietDocRef.id;
        } catch (error) {
            console.error('Error saving diet:', error);
            throw error;
        }
    }

    static async getShoppingList(dietId: string): Promise<ShoppingList | null> {
        const q = query(
            collection(db, 'shopping_lists'),
            where('dietId', '==', dietId)
        );

        const snapshot = await getDocs(q);
        if (snapshot.empty) return null;

        const doc = snapshot.docs[0];
        const data = doc.data();

        if (data.version === 2) {
            return {
                id: doc.id,
                ...data
            } as ShoppingListV2;
        } else {
            return {
                id: doc.id,
                ...data,
                version: 1
            } as ShoppingListV1;
        }
    }

    /*
        private static organizeShoppingList(
            parsedData: ParsedDietData,
            savedRecipeIds: { [key: string]: string }
        ): ShoppingListItem[] {
            const itemsMap = new Map<string, ShoppingListItem>();

            parsedData.days.forEach((day, dayIndex) => {
                day.meals.forEach((meal) => {
                    const recipeId = savedRecipeIds[`${dayIndex}_${meal.mealType}`];

                    meal.ingredients.forEach((ingredient) => {
                        const normalizedIngredient = ingredient.toLowerCase().trim();

                        if (!itemsMap.has(normalizedIngredient)) {
                            itemsMap.set(normalizedIngredient, {
                                name: ingredient,
                                recipes: [],
                                contexts: []
                            });
                        }

                        const productContext: ShoppingListProductContext = {
                            productId: `${dayIndex}_${recipeId}_${normalizedIngredient}`,
                            name: ingredient,
                            recipeId,
                            dayIndex,
                            mealType: meal.mealType
                        }

                        itemsMap.get(normalizedIngredient)!.contexts.push(productContext);

                        const recipeReference: ShoppingListRecipeReference = {
                            recipeId,
                            recipeName: meal.name,
                            dayIndex,
                            mealType: meal.mealType,
                            mealTime: meal.time
                        };

                        itemsMap.get(normalizedIngredient)!.recipes.push(recipeReference);
                    });
                });
            });

            return Array.from(itemsMap.values());
        }*/
}