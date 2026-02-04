import {ParsedMeal, Recipe} from "../../types";
import {RecipeService} from "../RecipeService";

export class DietRecipeService {

    /*
    * Wyszukuje przepisy dla posiłków na podstawie ich nazw
    * */
    static async findRecipesForMeals(meals: ParsedMeal[]): Promise<Record<string, Recipe>> {
        try {
            const mealNames = [...new Set(meals.map(meal => meal.name))];

            if (mealNames.length === 0) {
                return {};
            }

            const recipesMap: Record<string, Recipe> = {};

            await Promise.all(mealNames.map(async (name) => {
                try {
                    const searchResults = await RecipeService.searchRecipes(name);

                    if (searchResults.length > 0) {
                        recipesMap[name] = searchResults.find(recipe =>
                            recipe.name.toLowerCase() === name.toLowerCase()
                        ) || searchResults[0];
                    }
                } catch (error) {
                    console.warn(`Nie udało się znaleźć przepisu dla: ${name}`, error);
                }
            }));

            return recipesMap;
        } catch (error) {
            console.error('Błąd podczas wyszukiwania przepisów dla posiłków:', error);
            return {};
        }
    }

    /*
    * Aktualizuje posiłki z danymi z istniejących przepisów
    * */
    static enrichMealsWithRecipeData(meals: ParsedMeal[], recipesMap: Record<string, Recipe>): ParsedMeal[] {
        return meals.map(meal => {
            const matchingRecipe = recipesMap[meal.name];

            if (matchingRecipe) {
                return {
                    ...meal,
                    originalRecipeId: matchingRecipe.id,
                    photos: matchingRecipe.photos || meal.photos || [],
                    nutritionalValues: meal.nutritionalValues || matchingRecipe.nutritionalValues,
                    instructions: meal.instructions || matchingRecipe.instructions
                };
            }

            return meal;
        });
    }
}