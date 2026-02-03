import React, {useRef, useState} from "react";
import RecipesList, {OwnershipFilter, RecipesListRef} from "./RecipesList";
import RecipeModal from "./RecipeModal";
import SectionHeader from "../../shared/common/SectionHeader";
import {Button} from "../../shared/ui/Button";
import {FileImage, FileX, RefreshCcw, Search, Filter, ChevronDown, Globe, Lock} from "lucide-react";
import {Input} from "../../shared/ui/Input";
import {Badge} from "../../shared/ui/Badge";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../../shared/ui/DropdownMenu";
import LoadingSpinner from "../../shared/common/LoadingSpinner";
import {Recipe} from "../../../types";

const RecipesPage: React.FC = () => {
    const [selectedRecipeId, setSelectedRecipeId] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isCreateMode, setIsCreateMode] = useState(false);
    const recipesListRef = useRef<RecipesListRef | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterWithImages, setFilterWithImages] = useState(false);
    const [filterWithoutImages, setFilterWithoutImages] = useState(false);
    const [ownershipFilter, setOwnershipFilter] = useState<OwnershipFilter>('all');
    const [sortBy, setSortBy] = useState<'newest' | 'oldest' | 'name' | 'calories'>('newest');
    const [totalRecipesCount, setTotalRecipesCount] = useState(0);
    const [isSearching, setIsSearching] = useState(false);

    const handleRecipeSelect = (recipeId: string) => {
        setSelectedRecipeId(recipeId);
        setIsCreateMode(false);
        setIsModalOpen(true);
    };

    const handleCreateNew = () => {
        setSelectedRecipeId(null);
        setIsCreateMode(true);
        setIsModalOpen(true);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        setIsCreateMode(false);
    };

    const handleRecipeUpdate = (updatedRecipe: Recipe) => {
        if (recipesListRef.current) {
            recipesListRef.current.updateRecipe(updatedRecipe);
        }
        if (isCreateMode) {
            // Refresh the list after creating a new recipe
            recipesListRef.current?.refreshRecipes();
        }
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

    const refreshRecipes = () => {
        if (recipesListRef.current) {
            recipesListRef.current.refreshRecipes();
        }
    };

    const handleRecipesCountUpdate = (count: number, searching: boolean) => {
        setTotalRecipesCount(count);
        setIsSearching(searching);
    };

    const getOwnershipFilterLabel = () => {
        switch (ownershipFilter) {
            case 'private':
                return 'Moje';
            case 'public':
                return 'Publiczne';
            default:
                return 'Wszystkie';
        }
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

    const truncateText = (text: string, maxLength: number) => {
        if (text.length <= maxLength) return text;
        return text.slice(0, maxLength) + '...';
    };

    const headerRightContent = (
        <div className="flex flex-col items-end gap-2">
            {/* Informacja o liczbie znalezionych przepisów */}
            <div className="text-sm text-slate-500 px-1 flex items-center justify-end gap-3">
            <span>
                Znaleziono {totalRecipesCount} {totalRecipesCount === 1 ? 'przepis' : (totalRecipesCount < 5 ? 'przepisy' : 'przepisów')}
            </span>
                {isSearching && (
                    <span className="flex items-center text-slate-500">
                    <LoadingSpinner size="sm"/> Wyszukiwanie...
                </span>
                )}
            </div>
        </div>
    );

    return (
        <div className="h-full flex flex-col overflow-hidden">
            {/* Nagłówek */}
            <SectionHeader
                title="Przepisy"
                description="Zarządzaj przepisami, które będą używane w dietach dla klientów"
                rightContent={headerRightContent}
            />

            {/* Filters - Always Visible */}
            <div className="bg-white rounded-lg shadow-sm border border-slate-200 mb-4">
                <div className="p-4 flex flex-col gap-4">
                    {/* First Row: Search */}
                    <div className="relative flex-grow">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
                                size={18}/>
                        <Input
                            type="text"
                            placeholder="Szukaj przepisów (nazwa, ID, opis)..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-10"
                        />
                    </div>

                    {/* Second Row: Filters and Sort */}
                    <div className="flex gap-2 flex-wrap">
                        {/* Ownership Filter */}
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    className="flex items-center gap-1"
                                >
                                    {ownershipFilter === 'private' ? <Lock size={16}/> : ownershipFilter === 'public' ?
                                        <Globe size={16}/> : <Filter size={16}/>}
                                    <span>{getOwnershipFilterLabel()}</span>
                                    <ChevronDown size={14}/>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="start">
                                <DropdownMenuLabel>Właściciel</DropdownMenuLabel>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem onClick={() => setOwnershipFilter('all')}>
                                    Wszystkie
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setOwnershipFilter('private')}>
                                    <Lock size={14} className="mr-2"/> Moje
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setOwnershipFilter('public')}>
                                    <Globe size={14} className="mr-2"/> Publiczne
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>

                        {/* Sort */}
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
                            <DropdownMenuContent align="start">
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

                    {/* Active Filters */}
                    {(filterWithImages || filterWithoutImages || searchQuery || sortBy !== 'newest' || ownershipFilter !== 'all') && (
                        <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-100">
                            <div className="text-sm text-slate-500 mr-1">Aktywne filtry:</div>
                            {searchQuery && (
                                <Badge variant="outline"
                                       className="flex items-center gap-1 bg-blue-50 text-blue-700 border-blue-200">
                                    Wyszukiwanie: {truncateText(searchQuery, 15)}
                                </Badge>
                            )}
                            {ownershipFilter === 'private' && (
                                <Badge variant="outline" className="bg-indigo-50 text-indigo-700 border-indigo-200">
                                    Moje przepisy
                                </Badge>
                            )}
                            {ownershipFilter === 'public' && (
                                <Badge variant="outline" className="bg-sky-50 text-sky-700 border-sky-200">
                                    Publiczne przepisy
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
            </div>

            {/* Lista przepisów */}
            <div className="flex-grow overflow-hidden">
                <RecipesList
                    onRecipeSelect={handleRecipeSelect}
                    onCreateNew={handleCreateNew}
                    ref={recipesListRef}
                    initialSearchQuery={searchQuery}
                    initialFilterWithImages={filterWithImages}
                    initialFilterWithoutImages={filterWithoutImages}
                    initialOwnershipFilter={ownershipFilter}
                    initialSortBy={sortBy}
                    onRecipesCountUpdate={handleRecipesCountUpdate}
                />
            </div>

            {/* Modal ze szczegółami przepisu */}
            <RecipeModal
                recipeId={selectedRecipeId}
                isOpen={isModalOpen}
                onClose={handleModalClose}
                onRecipeUpdate={handleRecipeUpdate}
                isCreateMode={isCreateMode}
            />
        </div>
    );
};

export default RecipesPage;