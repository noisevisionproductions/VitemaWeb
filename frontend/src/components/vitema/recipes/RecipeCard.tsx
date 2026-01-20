import React from "react";
import {Recipe} from "../../../types";
import {FileX, Trash2} from "lucide-react";
import {formatTimestamp} from "../../../utils/dateFormatters";
import NutritionalValues from "../../shared/common/NutritionalValues";

interface RecipeCardProps {
    recipe: Recipe;
    onClick: (recipeId: string) => void;
    onDelete?: (recipe: Recipe, e: React.MouseEvent) => void;
}

const RecipeCard: React.FC<RecipeCardProps> = ({recipe, onClick, onDelete}) => {
    const hasImage = recipe.photos && recipe.photos.length > 0;

    const handleDeleteClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onDelete) {
            onDelete(recipe, e);
        }
    };

    return (
        <div className="p-1">
            <div
                className="hover:shadow-md hover:scale-[1.01] hover:border-primary-light transition-all duration-300 cursor-pointer rounded-lg border border-slate-200 overflow-hidden group bg-white"
                onClick={() => onClick(recipe.id)}
            >
                <button
                    onClick={handleDeleteClick}
                    className="absolute top-2 right-2 bg-white rounded-full p-1.5 shadow-md opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-50 z-10"
                    title="Usuń przepis"
                >
                    <Trash2 className="text-red-500" size={16}/>
                </button>

                {hasImage && (
                    <div className="h-32 overflow-hidden">
                        <img
                            src={recipe.photos[0]}
                            alt={recipe.name}
                            className="h-full w-full object-cover group-hover:scale-105 transition-all duration-500"
                        />
                    </div>
                )}

                <div className={`p-5 h-full flex flex-col ${!hasImage ? 'h-full' : ''}`}>
                    <div className="flex-grow">
                        <div className="flex justify-between items-start mb-3">
                            <div>
                                <h3 className="font-medium text-lg line-clamp-2 text-slate-900 group-hover:text-primary-dark transition-colors">
                                    {recipe.name}
                                </h3>
                                <div className="text-xs text-slate-400 mt-1">
                                    ID: {recipe.id}
                                </div>
                            </div>
                            {!hasImage && (
                                <div
                                    className="h-8 w-8 bg-gray-50 rounded-md flex items-center justify-center shadow-sm border border-slate-200 flex-shrink-0 ml-2">
                                    <FileX className="text-slate-500" size={16}/>
                                </div>
                            )}
                        </div>
                        <p className="text-sm text-slate-500 mb-3">
                            Utworzony: {formatTimestamp(recipe.createdAt)}
                        </p>
                        <p className="text-sm text-slate-600 line-clamp-2 mb-5">
                            {recipe.instructions}
                        </p>
                    </div>

                    {/* Wartości odżywcze */}
                    <div className="mt-auto pt-2 border-t border-slate-100">
                        {recipe.nutritionalValues && (
                            <NutritionalValues
                                values={recipe.nutritionalValues}
                                size="sm"
                            />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RecipeCard;