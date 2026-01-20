import React from "react";
import {DietTemplateCategory} from "../../../../types/DietTemplate";
import {DietTemplateService} from "../../../../services/diet/manual/DietTemplateService";

interface DietTemplateFiltersProps {
    selectedCategory: DietTemplateCategory | 'ALL';
    onCategoryChange: (category: DietTemplateCategory | 'ALL') => void;
}

const DietTemplateFilters: React.FC<DietTemplateFiltersProps> = ({
                                                                     selectedCategory,
                                                                     onCategoryChange
                                                                 }) => {
    const categories = [
        { value: 'ALL' as const, label: 'Wszystkie' },
        ...DietTemplateService.getAllCategories()
    ];

    return (
        <div className="space-y-4">
            <h4 className="text-sm font-medium text-gray-900">Filtruj według kategorii</h4>

            <div className="flex flex-wrap gap-2">
                {categories.map((category) => (
                    <button
                        key={category.value}
                        onClick={() => onCategoryChange(category.value)}
                        className={`px-3 py-1.5 text-sm rounded-full transition-colors ${
                            selectedCategory === category.value
                                ? 'bg-primary text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                        {category.label}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default DietTemplateFilters;