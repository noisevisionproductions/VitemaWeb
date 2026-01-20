import React from 'react';
import { Flame, Heart, Wheat, Droplets } from 'lucide-react';
import { NutritionalValues } from '../../../../../../../types';

interface ColoredNutritionBadgesProps {
    nutritionalValues: NutritionalValues;
    size?: 'sm' | 'md' | 'lg';
    layout?: 'horizontal' | 'grid';
}

const ColoredNutritionBadges: React.FC<ColoredNutritionBadgesProps> = ({
                                                                           nutritionalValues,
                                                                           size = 'md',
                                                                           layout = 'horizontal'
                                                                       }) => {
    const sizeClasses = {
        sm: 'text-xs px-2 py-1',
        md: 'text-sm px-3 py-1.5',
        lg: 'text-base px-4 py-2'
    };

    const iconSizes = {
        sm: 'h-3 w-3',
        md: 'h-4 w-4',
        lg: 'h-5 w-5'
    };

    const badges = [
        {
            label: 'kcal',
            value: nutritionalValues.calories,
            color: 'bg-nutrition-calories text-white shadow-sm',
            icon: Flame
        },
        {
            label: 'białko',
            value: nutritionalValues.protein,
            color: 'bg-nutrition-protein text-white shadow-sm',
            icon: Heart
        },
        {
            label: 'tłuszcze',
            value: nutritionalValues.fat,
            color: 'bg-nutrition-fats text-white shadow-sm',
            icon: Droplets
        },
        {
            label: 'węglowodany',
            value: nutritionalValues.carbs,
            color: 'bg-nutrition-carbs text-white shadow-sm',
            icon: Wheat
        }
    ].filter(badge => badge.value !== undefined && badge.value > 0);

    if (badges.length === 0) return null;

    const containerClass = layout === 'grid'
        ? 'grid grid-cols-2 gap-2'
        : 'flex flex-wrap gap-2';

    return (
        <div className={containerClass}>
            {badges.map((badge) => {
                const Icon = badge.icon;
                return (
                    <div
                        key={badge.label}
                        className={`
                            inline-flex items-center gap-1.5 rounded-full font-medium transition-all duration-200
                            hover:scale-105 hover:shadow-md
                            ${badge.color} ${sizeClasses[size]}
                        `}
                    >
                        <Icon className={iconSizes[size]} />
                        <span className="font-bold">{badge.value}</span>
                        {badge.label === 'kcal' ? (
                            <span className="opacity-90 text-xs">kcal</span>
                        ) : (
                            <span className="opacity-90 text-xs">g</span>
                        )}
                        {badge.label !== 'kcal' && size !== 'sm' && (
                            <span className="opacity-75 text-xs hidden sm:inline">
                                {badge.label}
                            </span>
                        )}
                    </div>
                );
            })}
        </div>
    );
};

export default ColoredNutritionBadges;