import React, {forwardRef, useCallback, useEffect, useImperativeHandle, useState} from "react";
import {Recipe} from "../../../types";
import {RecipeService} from "../../../services/RecipeService";
import {toast} from "../../../utils/toast";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import RecipeCard from "./RecipeCard";
import debounce from 'lodash/debounce';
import ConfirmationDialog from "../../shared/common/ConfirmationDialog";
import RecipeListHeader from "./components/RecipeListHeader";
import RecipeEmptyState from "./components/RecipeEmptyState";

export type OwnershipFilter = 'all' | 'private' | 'public';

interface RecipesListProps {
    onRecipeSelect: (recipeId: string) => void;
    onCreateNew?: () => void;
    initialSearchQuery?: string;
    initialFilterWithImages?: boolean;
    initialFilterWithoutImages?: boolean;
    initialOwnershipFilter?: OwnershipFilter;
    initialSortBy?: 'newest' | 'oldest' | 'name' | 'calories';
    onRecipesCountUpdate?: (count: number, isSearching: boolean) => void;
}

export interface RecipesListRef {
    refreshRecipes: () => void;
    updateRecipe: (updatedRecipe: Recipe) => void;
}

const RecipesList = forwardRef<RecipesListRef, RecipesListProps>(({
                                                                      onRecipeSelect,
                                                                      onCreateNew,
                                                                      initialSearchQuery = '',
                                                                      initialFilterWithImages = false,
                                                                      initialFilterWithoutImages = false,
                                                                      initialOwnershipFilter = 'all',
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
    const [ownershipFilter, setOwnershipFilter] = useState<OwnershipFilter>(initialOwnershipFilter);
    const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'name' | 'calories'>(initialSortBy);
    const [isSearching, setIsSearching] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [recipeToDelete, setRecipeToDelete] = useState<Recipe | null>(null);
    const [isDeletingRecipe, setIsDeletingRecipe] = useState(false);
    const [totalElements, setTotalElements] = useState(0);

    const [currentPage, setCurrentPage] = useState(0);
    const [hasMorePages, setHasMorePages] = useState(true);
    const pageSize = 30;

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
        setOwnershipFilter(initialOwnershipFilter);
    }, [initialOwnershipFilter]);

    useEffect(() => {
        setSortBy(initialSortBy);
    }, [initialSortBy]);

    useEffect(() => {
        if (onRecipesCountUpdate) {
            // When searching, use filtered count; otherwise use totalElements from API
            const count = searchQuery.trim() ? filteredRecipes.length : totalElements;
            onRecipesCountUpdate(count, isSearching);
        }
    }, [filteredRecipes.length, totalElements, isSearching, searchQuery, onRecipesCountUpdate]);

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

    const loadSavedPages = async (targetPage: number) => {
        if (targetPage <= 0) return;

        for (let page = 1; page <= targetPage; page++) {
            await loadMoreRecipes(page);
        }
    };

    useEffect(() => {
        fetchInitialRecipes().catch(console.error);
    }, []);

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
    }, [recipes, filterWithImages, filterWithoutImages, ownershipFilter, sortBy]);

    const applyFilters = () => {
        let results = [...recipes];

        if (ownershipFilter === 'private') {
            results = results.filter(recipe => !recipe.isPublic);
        } else if (ownershipFilter === 'public') {
            results = results.filter(recipe => recipe.isPublic);
        }

        if (filterWithImages) {
            results = results.filter(recipe => recipe.photos && recipe.photos.length > 0);
        }
        if (filterWithoutImages) {
            results = results.filter(recipe => !recipe.photos || recipe.photos.length === 0);
        }

        results = [...results].sort((a, b) => {
            if (a.isMine && !b.isMine) return -1;
            if (!a.isMine && b.isMine) return 1;

            switch (sortBy) {
                case 'newest':
                    return (b.createdAt?.seconds || 0) - (a.createdAt?.seconds || 0);
                case 'calories':
                    return (b.nutritionalValues?.calories || 0) - (a.nutritionalValues?.calories || 0);
                case 'name':
                    return a.name.localeCompare(b.name);
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
            setTotalElements(response.totalElements || 0);
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
            setTotalElements(response.totalElements || 0);
            setHasMorePages(response.page < response.totalPages - 1);
        } catch (error) {
            console.error('Błąd podczas ładowania kolejnych przepisów:', error);
            toast.error('Nie udało się załadować więcej przepisów');
        } finally {
            setLoadingMore(false);
        }
    };

    // Infinite scroll handler
    const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
        const target = e.currentTarget;
        const scrollPercentage = (target.scrollTop + target.clientHeight) / target.scrollHeight;

        // Load more when scrolled to 80% of the container
        if (scrollPercentage > 0.8 && hasMorePages && !loadingMore && !searchQuery.trim()) {
            loadMoreRecipes().catch(console.error);
        }
    }, [hasMorePages, loadingMore, searchQuery]);

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

            setRecipes(prev => prev.filter(r => r.id !== recipeToDelete.id));
            setFilteredRecipes(prev => prev.filter(r => r.id !== recipeToDelete.id));

            toast.success(`Przepis "${recipeToDelete.name}" został usunięty`);
        } catch (error: any) {
            console.error('Błąd podczas usuwania przepisu:', error);

            if (error.response?.status === 403) {
                toast.error('Nie masz uprawnień do usunięcia tego przepisu (np. jest on publiczny).');
            } else {
                toast.error('Nie udało się usunąć przepisu. Spróbuj ponownie później.');
            }
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

    return (
        <div className="h-full flex flex-col">
            {/* Header with count and create button */}
            <RecipeListHeader
                count={filteredRecipes.length}
                onCreateNew={onCreateNew}
            />

            {/* Recipes list with infinite scroll */}
            <div className="flex-grow overflow-auto relative" onScroll={handleScroll}>
                {filteredRecipes.length === 0 ? (
                    <RecipeEmptyState onCreateNew={onCreateNew}/>
                ) : (
                    <>
                        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 pb-16">
                            {filteredRecipes.map(recipe => (
                                <RecipeCard
                                    key={recipe.id}
                                    recipe={recipe}
                                    onClick={onRecipeSelect}
                                    onDelete={recipe.isMine ? handleDeleteRecipe : undefined}
                                />
                            ))}
                        </div>

                        {/* Loading indicator for infinite scroll */}
                        {loadingMore && (
                            <div className="flex justify-center items-center py-8">
                                <LoadingSpinner size="sm"/>
                                <span className="ml-2 text-sm text-slate-500">Ładowanie kolejnych przepisów...</span>
                            </div>
                        )}
                    </>
                )}
            </div>

            {/* Delete confirmation dialog */}
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