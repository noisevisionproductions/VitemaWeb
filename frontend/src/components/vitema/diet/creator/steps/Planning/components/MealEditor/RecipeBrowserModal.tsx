import React, {useEffect, useState} from 'react';
import {Search, X, ChefHat, Flame} from 'lucide-react';
import {UnifiedSearchResult} from "../../../../../../../../types";
import {DietCreatorService} from "../../../../../../../../services/diet/creator/DietCreatorService";
import LoadingSpinner from "../../../../../../../shared/common/LoadingSpinner";
import {RecipeService} from "../../../../../../../../services/RecipeService";

interface RecipeBrowserModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSelect: (result: UnifiedSearchResult) => void;
    trainerId?: string;
}

const RecipeBrowserModal: React.FC<RecipeBrowserModalProps> = ({
                                                                   isOpen,
                                                                   onClose,
                                                                   onSelect,
                                                                   trainerId
                                                               }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [results, setResults] = useState<UnifiedSearchResult[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            loadRecipes(searchTerm).catch(console.error);
        }
    }, [isOpen, trainerId]);

    const loadRecipes = async (query: string) => {
        setIsLoading(true);
        try {
            if (!query || query.trim() === '') {
                const response = await RecipeService.getRecipesPage(0, 20);

                const mappedRecipes: UnifiedSearchResult[] = response.content.map((recipe: any) => ({
                    id: recipe.id,
                    name: recipe.name,
                    type: 'RECIPE',
                    photos: recipe.photos,
                    nutritionalValues: recipe.nutritionalValues,
                    unit: 'szt'
                }));

                setResults(mappedRecipes);
            } else {
                const data = await DietCreatorService.searchUnified(query, trainerId);
                const recipesOnly = data.filter((item: { type: string; }) => item.type === 'RECIPE');
                setResults(recipesOnly);
            }
        } catch (error) {
            console.error("Error loading recipes", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value;
        setSearchTerm(val);
        if (val.length > 2 || val.length === 0) {
            loadRecipes(val).catch(console.error);
        }
    };

    /**
     * Handles closing the modal and resetting the state.
     * Decision: We clear the search term so next time it opens fresh.
     */
    const handleClose = () => {
        setSearchTerm('');
        onClose();
    };

    /**
     * Handles selection and clears the search term.
     */
    const handleSelectWrapper = (result: UnifiedSearchResult) => {
        setSearchTerm('');
        onSelect(result);
    };

    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200"
            onClick={handleClose}
        >
            <div
                className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[85vh] flex flex-col overflow-hidden"
                onClick={(e) => e.stopPropagation()}
            >

                {/* Header */}
                <div className="p-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                    <div>
                        <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                            <ChefHat className="h-5 w-5 text-primary"/>
                            Baza przepisów
                        </h3>
                        <p className="text-sm text-gray-500">Wybierz gotowy posiłek z bazy</p>
                    </div>
                    <button
                        onClick={handleClose}
                        className="p-2 hover:bg-gray-200 rounded-full transition-colors text-gray-500"
                    >
                        <X className="h-5 w-5"/>
                    </button>
                </div>

                {/* Search Bar inside Modal */}
                <div className="p-4 border-b border-gray-100">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400"/>
                        <input
                            type="text"
                            placeholder="Szukaj po nazwie..."
                            value={searchTerm}
                            onChange={handleSearch}
                            className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                            autoFocus
                        />
                    </div>
                </div>

                {/* Grid Content */}
                <div className="flex-1 overflow-y-auto p-4 bg-gray-50/30">
                    {isLoading ? (
                        <div className="flex justify-center items-center h-40">
                            <LoadingSpinner size="lg"/>
                        </div>
                    ) : results.length === 0 ? (
                        <div className="text-center py-10 text-gray-500">
                            Nie znaleziono przepisów
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                            {results.map((recipe) => (
                                <button
                                    key={recipe.id}
                                    onClick={() => handleSelectWrapper(recipe)}
                                    className="group flex flex-col bg-white border border-gray-200 rounded-xl overflow-hidden hover:shadow-md hover:border-primary/50 transition-all text-left"
                                >
                                    {/* Image Area */}
                                    <div className="h-32 bg-gray-100 relative overflow-hidden">
                                        {recipe.photos && recipe.photos.length > 0 ? (
                                            <img
                                                src={recipe.photos[0]}
                                                alt={recipe.name}
                                                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                            />
                                        ) : (
                                            <div
                                                className="w-full h-full flex items-center justify-center text-gray-300">
                                                <ChefHat className="h-10 w-10"/>
                                            </div>
                                        )}
                                        {/* Overlay Badges */}
                                        <div className="absolute bottom-2 left-2 right-2 flex gap-1">
                                            {recipe.nutritionalValues?.calories && (
                                                <span
                                                    className="bg-black/60 backdrop-blur-sm text-white text-[10px] font-medium px-2 py-0.5 rounded-full flex items-center gap-1">
                                                    <Flame className="h-3 w-3"/>
                                                    {Math.round(recipe.nutritionalValues.calories)} kcal
                                                </span>
                                            )}
                                        </div>
                                    </div>

                                    {/* Content Area */}
                                    <div className="p-3">
                                        <h4 className="font-semibold text-gray-900 text-sm line-clamp-2 mb-1 group-hover:text-primary transition-colors">
                                            {recipe.name}
                                        </h4>

                                        {/* Macros Mini Bar */}
                                        {recipe.nutritionalValues && (
                                            <div className="flex items-center gap-2 text-xs text-gray-500 mt-2">
                                                <span
                                                    className="bg-red-50 text-red-700 px-1.5 py-0.5 rounded">B: {Math.round(recipe.nutritionalValues.protein || 0)}</span>
                                                <span
                                                    className="bg-yellow-50 text-yellow-700 px-1.5 py-0.5 rounded">T: {Math.round(recipe.nutritionalValues.fat || 0)}</span>
                                                <span
                                                    className="bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded">W: {Math.round(recipe.nutritionalValues.carbs || 0)}</span>
                                            </div>
                                        )}
                                    </div>
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RecipeBrowserModal;