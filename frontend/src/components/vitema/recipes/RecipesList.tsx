import React, {forwardRef, useCallback, useEffect, useImperativeHandle, useState} from "react";
import {Recipe} from "../../../types";
import {RecipeService} from "../../../services/RecipeService";
import {toast} from "../../../utils/toast";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import {FloatingActionButton, FloatingActionButtonGroup} from "../../shared/common/FloatingActionButton";
import RecipeCard from "./RecipeCard";
import debounce from 'lodash/debounce';
import ConfirmationDialog from "../../shared/common/ConfirmationDialog";

interface RecipesListProps {
    onRecipeSelect: (recipeId: string) => void;
    initialSearchQuery?: string;
    initialFilterWithImages?: boolean;
    initialFilterWithoutImages?: boolean;
    initialSortBy?: 'newest' | 'oldest' | 'name' | 'calories';
    onRecipesCountUpdate?: (count: number, isSearching: boolean) => void;
}

export interface RecipesListRef {
    refreshRecipes: () => void;
    updateRecipe: (updatedRecipe: Recipe) => void;
}

const RecipesList = forwardRef<RecipesListRef, RecipesListProps>(({
                                                                      onRecipeSelect,
                                                                      initialSearchQuery = '',
                                                                      initialFilterWithImages = false,
                                                                      initialFilterWithoutImages = false,
                                                                      initialSortBy = 'newest',
                                                                      onRecipesCountUpdate
                                                                  }, ref) => {
    const [recipes, setRecipes] = useState<Recipe[]>([]);
    const [filteredRecipes, setFilteredRecipes] = useState<Recipe[]>([]);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [searchQuery, setSearchQuery] = useState(initialSearchQuery);
    const [filterWithImages, setFilterWithImages] = useState(initialFilterWithImages);
    const [filterWithoutImages, setFilterWithoutImages] = useState(initialFilterWithoutImages);
    const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'name' | 'calories'>(initialSortBy);
    const [isSearching, setIsSearching] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [recipeToDelete, setRecipeToDelete] = useState<Recipe | null>(null);
    const [isDeletingRecipe, setIsDeletingRecipe] = useState(false);

    const [currentPage, setCurrentPage] = useState(0);
    const [hasMorePages, setHasMorePages] = useState(true);
    const pageSize = 50;

    // Effect to handle changes from parent component
    useEffect(() => {
        setSearchQuery(initialSearchQuery);
    }, [initialSearchQuery]);

    useEffect(() => {
        setFilterWithImages(initialFilterWithImages);
    }, [initialFilterWithImages]);

    useEffect(() => {
        setFilterWithoutImages(initialFilterWithoutImages);
    }, [initialFilterWithoutImages]);

    useEffect(() => {
        setSortBy(initialSortBy);
    }, [initialSortBy]);

    useEffect(() => {
        if (onRecipesCountUpdate) {
            onRecipesCountUpdate(filteredRecipes.length, isSearching);
        }
    }, [filteredRecipes.length, isSearching, onRecipesCountUpdate]);

    // Metoda do aktualizacji pojedynczego przepisu bez ponownego ładowania całej listy
    const updateRecipe = useCallback((updatedRecipe: Recipe) => {
        setRecipes(currentRecipes =>
            currentRecipes.map(recipe =>
                recipe.id === updatedRecipe.id ? updatedRecipe : recipe
            )
        );
    }, []);

    useImperativeHandle(ref, () => ({
        refreshRecipes: async () => {
            try {
                await fetchInitialRecipes();
                await loadSavedPages(currentPage);
                setSearchQuery(searchQuery);
                setFilterWithImages(filterWithImages);
                setFilterWithoutImages(filterWithoutImages);
                setSortBy(sortBy);
            } catch (message) {
                return console.error(message);
            }
        },
        updateRecipe
    }));

    // Funkcja do ładowania kilku stron do określonego indeksu strony
    const loadSavedPages = async (targetPage: number) => {
        if (targetPage <= 0) return;

        for (let page = 1; page <= targetPage; page++) {
            await loadMoreRecipes(page);
        }
    };

    useEffect(() => {
        fetchInitialRecipes().catch(console.error);
    }, []);

    // Debounce funkcja wyszukiwania, aby nie wywoływać API za często
    const debouncedSearch = debounce(async (query: string) => {
        if (query.trim() && (query.trim().length >= 3 || /^\d+$/.test(query.trim()))) {
            try {
                setIsSearching(true);
                const results = await RecipeService.searchRecipes(query.trim());
                setFilteredRecipes(results);
            } catch (error) {
                console.error('Błąd podczas wyszukiwania przepisów:', error);
            } finally {
                setIsSearching(false);
            }
        } else {
            applyFilters();
        }
    }, 500);

    useEffect(() => {
        if (searchQuery.trim()) {
            const searchPromise = debouncedSearch(searchQuery);

            // Obsługa Promise, jeśli jest zwracany
            if (searchPromise) {
                searchPromise.catch(error => {
                    console.error('Nieoczekiwany błąd w debouncedSearch:', error);
                });
            }
        } else {
            applyFilters();
        }

        return () => {
            debouncedSearch.cancel();
        };
    }, [searchQuery]);

    useEffect(() => {
        if (!searchQuery.trim()) {
            applyFilters();
        }
    }, [recipes, filterWithImages, filterWithoutImages, sortBy]);

    const applyFilters = () => {
        let results = [...recipes];

        // Filtrowanie
        if (filterWithImages) {
            results = results.filter(recipe => recipe.photos && recipe.photos.length > 0);
        }

        if (filterWithoutImages) {
            results = results.filter(recipe => !recipe.photos || recipe.photos.length === 0);
        }

        // Sortowanie
        results = [...results].sort((a, b) => {
            switch (sortBy) {
                case 'newest':
                    return new Date(b.createdAt.seconds * 1000).getTime() - new Date(a.createdAt.seconds * 1000).getTime();
                case 'oldest':
                    return new Date(a.createdAt.seconds * 1000).getTime() - new Date(b.createdAt.seconds * 1000).getTime();
                case 'name':
                    return a.name.localeCompare(b.name);
                case 'calories':
                    const aCalories = a.nutritionalValues?.calories || 0;
                    const bCalories = b.nutritionalValues?.calories || 0;
                    return bCalories - aCalories;
                default:
                    return 0;
            }
        });

        setFilteredRecipes(results);
    };

    const fetchInitialRecipes = async () => {
        try {
            setLoading(true);
            setRecipes([]);
            setCurrentPage(0);

            const response = await RecipeService.getRecipesPage(0, pageSize);
            setRecipes(response.content || []);
            setHasMorePages(response.page < response.totalPages - 1);
        } catch (error) {
            console.error('Błąd podczas pobierania przepisów:', error);
            toast.error('Nie udało się pobrać listy przepisów');
        } finally {
            setLoading(false);
        }
    };

    const loadMoreRecipes = async (pageToLoad?: number) => {
        if (loadingMore) return;

        const nextPage = pageToLoad !== undefined ? pageToLoad : currentPage + 1;
        if (!hasMorePages && nextPage > currentPage) return;

        try {
            setLoadingMore(true);

            const response = await RecipeService.getRecipesPage(nextPage, pageSize);

            setRecipes(prevRecipes => {
                const existingIds = new Set(prevRecipes.map(recipe => recipe.id));
                const newUniqueRecipes = response.content.filter(recipe => !existingIds.has(recipe.id));
                return [...prevRecipes, ...newUniqueRecipes];
            });

            setCurrentPage(nextPage);
            setHasMorePages(response.page < response.totalPages - 1);
        } catch (error) {
            console.error('Błąd podczas ładowania kolejnych przepisów:', error);
            toast.error('Nie udało się załadować więcej przepisów');
        } finally {
            setLoadingMore(false);
        }
    };

    const handleDeleteRecipe = (recipe: Recipe, e: React.MouseEvent) => {
        e.stopPropagation();
        setRecipeToDelete(recipe);
        setDeleteDialogOpen(true);
    };

    const handleCloseDeleteDialog = () => {
        if (!isDeletingRecipe) {
            setDeleteDialogOpen(false);
            setRecipeToDelete(null);
        }
    };

    const confirmDeleteRecipe = async () => {
        if (!recipeToDelete) return;

        try {
            setIsDeletingRecipe(true);
            await RecipeService.deleteRecipe(recipeToDelete.id);

            // Aktualizacja lokalnych list przepisów
            const newRecipes = recipes.filter(r => r.id !== recipeToDelete.id);
            setRecipes(newRecipes);

            // Aktualizacja filtrowanej listy
            const newFilteredRecipes = filteredRecipes.filter(r => r.id !== recipeToDelete.id);
            setFilteredRecipes(newFilteredRecipes);

            toast.success(`Przepis "${recipeToDelete.name}" został usunięty`);
        } catch (error) {
            console.error('Błąd podczas usuwania przepisu:', error);
            toast.error('Nie udało się usunąć przepisu');
        } finally {
            setIsDeletingRecipe(false);
            setRecipeToDelete(null);
            setDeleteDialogOpen(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner/>
            </div>
        );
    }

    // Sprawdzamy, czy powinniśmy pokazać przycisk "Załaduj więcej"
    const showLoadMoreButton = hasMorePages && !(searchQuery || filterWithImages || filterWithoutImages);

    return (
        <div className="h-full flex flex-col">
            {/* Lista przepisów */}
            <div className="flex-grow overflow-auto relative">
                {filteredRecipes.length === 0 ? (
                    <div className="text-center py-16 bg-white rounded-lg shadow-sm border border-slate-200">
                        <h3 className="text-lg font-medium text-slate-700">Brak przepisów do wyświetlenia</h3>
                        <p className="text-slate-500 max-w-md mx-auto mt-2">
                            Nie znaleziono żadnych przepisów spełniających kryteria. Spróbuj zmienić filtry lub odśwież
                            listę.
                        </p>
                    </div>
                ) : (
                    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 pb-16">
                        {filteredRecipes.map(recipe => (
                            <RecipeCard
                                key={recipe.id}
                                recipe={recipe}
                                onClick={onRecipeSelect}
                                onDelete={handleDeleteRecipe}
                            />
                        ))}
                    </div>
                )}

                {/* Przycisk "Załaduj więcej" */}
                {showLoadMoreButton && (
                    <FloatingActionButtonGroup position="bottom-right">
                        <FloatingActionButton
                            label="Załaduj więcej"
                            onClick={() => loadMoreRecipes()}
                            disabled={loadingMore}
                            isLoading={loadingMore}
                            loadingLabel="Ładowanie..."
                            loadingIcon={<LoadingSpinner size="sm"/>}
                            variant="primary"
                        />
                    </FloatingActionButtonGroup>
                )}
            </div>

            {/* Dialog potwierdzający usunięcie przepisu */}
            <ConfirmationDialog
                isOpen={deleteDialogOpen}
                onClose={handleCloseDeleteDialog}
                onConfirm={confirmDeleteRecipe}
                title="Czy na pewno chcesz usunąć ten przepis?"
                description={`Przepis "${recipeToDelete?.name || ''}" zostanie trwale usunięty. Tej operacji nie można cofnąć.`}
                confirmLabel="Usuń"
                cancelLabel="Anuluj"
                variant="destructive"
                isLoading={isDeletingRecipe}
            />
        </div>
    );
});

export default RecipesList;