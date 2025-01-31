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
import {ParsedDietData, ShoppingListItem} from '../types/diet';

export class FirebaseService {
    static async uploadExcelFile(file: File, userId: string) {
        const storageRef = ref(storage, `diets/${userId}/${file.name}`);
        await uploadBytes(storageRef, file);
        return await getDownloadURL(storageRef);
    }

    static async saveShoppingList(shoppingList: {
        userId: string;
        dietId: string;
        items: string[];
        createdAt: Timestamp;
        startDate: string;
        endDate: string
    }) {
        const shoppingListRef = collection(db, 'shopping_lists');
        const docRef = await addDoc(shoppingListRef, shoppingList);
        return docRef.id;
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
                        ingredients: meal.ingredients,
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

            // 4. Zapisujemy listę zakupów
            await addDoc(collection(db, 'shopping_lists'), {
                dietId: dietDocRef.id,
                userId,
                items: this.organizeShoppingList(parsedData, savedRecipeIds),
                createdAt: Timestamp.fromDate(new Date()),
                startDate: parsedData.days[0].date,
                endDate: parsedData.days[parsedData.days.length - 1].date
            });

            return dietDocRef.id;
        } catch (error) {
            console.error('Error saving diet:', error);
            throw error;
        }
    }

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
                            recipes: []
                        });
                    }

                    itemsMap.get(normalizedIngredient)!.recipes.push({
                        recipeId,
                        recipeName: meal.name,
                        dayIndex
                    });
                });
            });
        });

        return Array.from(itemsMap.values());
    }
}