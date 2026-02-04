import React from "react";
import {Recipe} from "../../../types";
import {ChefHat, Trash2, Globe, Lock} from "lucide-react";
import {formatTimestamp} from "../../../utils/dateFormatters";

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
        <div
            className="relative hover:shadow-lg hover:scale-[1.02] transition-all duration-200 cursor-pointer rounded-xl border border-slate-200 overflow-hidden group bg-white"
            onClick={() => onClick(recipe.id)}
        >
            {/* Public/Private Badge */}
            <div className="absolute top-2 left-2 z-20">
                {recipe.isPublic === true ? (
                    <div
                        className="flex items-center gap-1 bg-sky-500/90 backdrop-blur-sm text-white px-2 py-1 rounded-full text-xs font-medium shadow-lg">
                        <Globe size={12}/>
                        <span>Publiczny</span>
                    </div>
                ) : (
                    <div
                        className="flex items-center gap-1 bg-slate-600/90 backdrop-blur-sm text-white px-2 py-1 rounded-full text-xs font-medium shadow-lg">
                        <Lock size={12}/>
                        <span>Prywatny</span>
                    </div>
                )}
            </div>

            {/* Delete Button */}
            {onDelete && (
                <button
                    onClick={handleDeleteClick}
                    className="absolute top-2 right-2 bg-white/90 backdrop-blur-sm rounded-full p-1.5 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-50 z-20"
                    title="Usuń przepis"
                >
                    <Trash2 className="text-red-500" size={14}/>
                </button>
            )}

            {/* Image or Placeholder */}
            <div className="relative h-36 overflow-hidden">
                {hasImage ? (
                    <img
                        src={recipe.photos[0]}
                        alt={recipe.name}
                        className="h-full w-full object-cover group-hover:scale-110 transition-all duration-500"
                    />
                ) : (
                    <div
                        className="h-full w-full bg-gradient-to-br from-emerald-50 via-teal-50 to-cyan-50 flex items-center justify-center">
                        <ChefHat className="text-emerald-300" size={48} strokeWidth={1.5}/>
                    </div>
                )}
                {/* Overlay gradient for better text visibility */}
                <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent"/>
            </div>

            {/* Content */}
            <div className="p-3">
                {/* Title */}
                <h3 className="font-semibold text-base line-clamp-1 text-slate-900 mb-2 group-hover:text-primary-dark transition-colors">
                    {recipe.name}
                </h3>

                {/* Nutritional Values - Compact Pills */}
                {recipe.nutritionalValues && (
                    <div className="flex flex-wrap gap-1.5 mb-2">
                        <span
                            className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
                            {recipe.nutritionalValues.calories || 0} kcal
                        </span>
                        <span
                            className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                            P: {recipe.nutritionalValues.protein || 0}g
                        </span>
                        <span
                            className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700 border border-red-200">
                            F: {recipe.nutritionalValues.fat || 0}g
                        </span>
                        <span
                            className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200">
                            C: {recipe.nutritionalValues.carbs || 0}g
                        </span>
                    </div>
                )}

                {/* Metadata */}
                <div className="flex items-center justify-between text-xs text-slate-500">
                    <span className="truncate">{formatTimestamp(recipe.createdAt)}</span>
                    <span className="text-slate-400 font-mono text-[10px] ml-2">#{recipe.id.slice(-6)}</span>
                </div>
            </div>
        </div>
    );
};

export default RecipeCard;