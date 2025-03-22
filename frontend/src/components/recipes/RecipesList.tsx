import {useEffect, useState, forwardRef, useImperativeHandle} from "react";
import {Recipe} from "../../types";
import {RecipeService} from "../../services/RecipeService";
import {toast} from "../../utils/toast";
import LoadingSpinner from "../common/LoadingSpinner";
import {Button} from "../ui/button";
import {FileImage, FileText, FileX, RefreshCcw, Search, Filter, ChevronDown} from "lucide-react";
import {Input} from "../ui/Input";
import {Badge} from "../ui/badge";
import RecipeCard from "./RecipeCard";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../ui/dropdown-menu";

interface RecipesListProps {
    onRecipeSelect: (recipeId: string) => void;
}

export interface RecipesListRef {
    refreshRecipes: () => void;
}

const RecipesList = forwardRef<RecipesListRef, RecipesListProps>(({onRecipeSelect}, ref) => {
    const [recipes, setRecipes] = useState<Recipe[]>([]);
    const [filteredRecipes, setFilteredRecipes] = useState<Recipe[]>([]);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterWithImages, setFilterWithImages] = useState(false);
    const [filterWithoutImages, setFilterWithoutImages] = useState(false);
    const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'name' | 'calories'>('newest');

    const [currentPage, setCurrentPage] = useState(0);
    const [hasMorePages, setHasMorePages] = useState(true);
    const pageSize = 50;

    useImperativeHandle(ref, () => ({
        refreshRecipes: () => {
            const savedSearchQuery = searchQuery;
            const savedFilterWithImages = filterWithImages;
            const savedFilterWithoutImages = filterWithoutImages;
            const savedSortBy = sortBy;

            fetchInitialRecipes()
                .then(() => {
                    setSearchQuery(savedSearchQuery);
                    setFilterWithImages(savedFilterWithImages);
                    setFilterWithoutImages(savedFilterWithoutImages);
                    setSortBy(savedSortBy);
                })
                .catch(console.error);
        }
    }));

    useEffect(() => {
        fetchInitialRecipes().catch(console.error);
    }, []);

    useEffect(() => {
        let results = [...recipes];

        // Filtrowanie
        if (searchQuery) {
            const query = searchQuery.toLowerCase();
            results = results.filter(recipe =>
                recipe.name.toLowerCase().includes(query) ||
                recipe.instructions.toLowerCase().includes(query)
            );
        }

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
    }, [recipes, searchQuery, filterWithImages, filterWithoutImages, sortBy]);

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

    const loadMoreRecipes = async () => {
        if (loadingMore || !hasMorePages) return;

        try {
            setLoadingMore(true);
            const nextPage = currentPage + 1;

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

    const refreshRecipes = () => {
        const savedSearchQuery = searchQuery;
        const savedFilterWithImages = filterWithImages;
        const savedFilterWithoutImages = filterWithoutImages;
        const savedSortBy = sortBy;

        fetchInitialRecipes()
            .then(() => {
                setSearchQuery(savedSearchQuery);
                setFilterWithImages(savedFilterWithImages);
                setFilterWithoutImages(savedFilterWithoutImages);
                setSortBy(savedSortBy);
            })
            .catch(console.error);
    };

    const toggleFilterWithImages = () => {
        setFilterWithImages(!filterWithImages);
        if (!filterWithImages) {
            setFilterWithoutImages(false);
        }
    };

    const toggleFilterWithoutImages = () => {
        setFilterWithoutImages(!filterWithoutImages);
        if (!filterWithoutImages) {
            setFilterWithImages(false);
        }
    };

    const truncateText = (text: string, maxLength: number) => {
        if (text.length <= maxLength) return text;
        return text.slice(0, maxLength) + '...';
    };

    const getSortLabel = () => {
        switch (sortBy) {
            case 'newest':
                return 'Najnowsze';
            case 'oldest':
                return 'Najstarsze';
            case 'name':
                return 'Nazwa A-Z';
            case 'calories':
                return 'Kaloryczność';
            default:
                return 'Sortuj';
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
            {/* Nagłówek z wyszukiwaniem i filtrami */}
            <div className="bg-white p-5 rounded-lg shadow-sm border border-slate-200 mb-5">
                <div className="flex flex-col sm:flex-row gap-4">
                    <div className="relative flex-grow">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18}/>
                        <Input
                            type="text"
                            placeholder="Szukaj przepisów..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-10"
                        />
                    </div>
                    <div className="flex gap-2 flex-wrap sm:flex-nowrap">
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    className="flex items-center gap-1"
                                >
                                    <Filter size={16}/>
                                    <span>{getSortLabel()}</span>
                                    <ChevronDown size={14}/>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuLabel>Sortowanie</DropdownMenuLabel>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem onClick={() => setSortBy('newest')}>
                                    Najnowsze
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setSortBy('oldest')}>
                                    Najstarsze
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setSortBy('name')}>
                                    Nazwa A-Z
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setSortBy('calories')}>
                                    Najwięcej kalorii
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>

                        <Button
                            variant="outline"
                            size="sm"
                            onClick={refreshRecipes}
                            className="flex items-center gap-2"
                            title="Odśwież listę przepisów"
                        >
                            <RefreshCcw size={16}/>
                            <span className="sm:inline hidden">Odśwież</span>
                        </Button>
                        <Button
                            variant={filterWithImages ? "default" : "outline"}
                            size="sm"
                            onClick={toggleFilterWithImages}
                            className="flex items-center gap-2"
                            title="Pokaż tylko przepisy ze zdjęciami"
                        >
                            <FileImage size={16}/>
                            <span className="sm:inline hidden">Ze zdjęciami</span>
                        </Button>
                        <Button
                            variant={filterWithoutImages ? "default" : "outline"}
                            size="sm"
                            onClick={toggleFilterWithoutImages}
                            className="flex items-center gap-2"
                            title="Pokaż tylko przepisy bez zdjęć"
                        >
                            <FileX size={16}/>
                            <span className="sm:inline hidden">Bez zdjęć</span>
                        </Button>
                    </div>
                </div>
                {(filterWithImages || filterWithoutImages || searchQuery || sortBy !== 'newest') && (
                    <div className="flex flex-wrap gap-2 mt-4 pt-4 border-t border-slate-100">
                        <div className="text-sm text-slate-500 mr-1">Aktywne filtry:</div>
                        {searchQuery && (
                            <Badge variant="outline"
                                   className="flex items-center gap-1 bg-blue-50 text-blue-700 border-blue-200">
                                Wyszukiwanie: {truncateText(searchQuery, 15)}
                            </Badge>
                        )}
                        {filterWithImages && (
                            <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
                                Tylko ze zdjęciami
                            </Badge>
                        )}
                        {filterWithoutImages && (
                            <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-200">
                                Tylko bez zdjęć
                            </Badge>
                        )}
                        {sortBy !== 'newest' && (
                            <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-200">
                                Sortowanie: {getSortLabel()}
                            </Badge>
                        )}
                    </div>
                )}
            </div>

            {/* Status liczby wyników */}
            <div className="mb-5 text-sm text-slate-500 px-1">
                Znaleziono {filteredRecipes.length} {filteredRecipes.length === 1 ? 'przepis' : (filteredRecipes.length < 5 ? 'przepisy' : 'przepisów')}
            </div>

            {/* Lista przepisów */}
            <div className="flex-grow overflow-auto pb-4">
                {filteredRecipes.length === 0 ? (
                    <div className="text-center py-16 bg-white rounded-lg shadow-sm border border-slate-200">
                        <FileText className="h-12 w-12 text-slate-300 mx-auto mb-4"/>
                        <h3 className="text-lg font-medium text-slate-700">Brak przepisów do wyświetlenia</h3>
                        <p className="text-slate-500 max-w-md mx-auto mt-2">
                            Nie znaleziono żadnych przepisów spełniających kryteria. Spróbuj zmienić filtry lub odśwież
                            listę.
                        </p>
                    </div>
                ) : (
                    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        {filteredRecipes.map(recipe => (
                            <RecipeCard
                                key={recipe.id}
                                recipe={recipe}
                                onClick={onRecipeSelect}
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Przycisk "Załaduj więcej" */}
            {showLoadMoreButton && (
                <div className="mt-5 mb-2 text-center">
                    <Button
                        variant="outline"
                        onClick={loadMoreRecipes}
                        disabled={loadingMore}
                        className="px-6"
                    >
                        {loadingMore ? (
                            <>
                                <LoadingSpinner size="sm"/>
                                <span className="ml-2">Ładowanie...</span>
                            </>
                        ) : (
                            'Załaduj starsze przepisy'
                        )}
                    </Button>
                </div>
            )}
        </div>
    );
});

export default RecipesList;