import React from 'react';
import {ParsedProduct} from '../../../../../../../../types/product';
import InlineIngredientSearch from '../../../../components/InlineIngredientSearch';
import IngredientsList from '../../../../components/IngredientsList';

interface MealEditorIngredientsProps {
    ingredients: ParsedProduct[];
    onAddIngredient: (ingredient: ParsedProduct) => void;
    onRemoveIngredient: (index: number) => void;
    trainerId?: string;
}

const MealEditorIngredients: React.FC<MealEditorIngredientsProps> = ({
                                                                          ingredients,
                                                                          onAddIngredient,
                                                                          onRemoveIngredient,
                                                                          trainerId
                                                                      }) => {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
                Składniki
            </label>

            <div className="mb-3">
                <InlineIngredientSearch
                    onSelect={onAddIngredient}
                    placeholder="Dodaj składnik, np. 'mleko 200ml'..."
                    trainerId={trainerId}
                />
            </div>

            <IngredientsList
                ingredients={ingredients}
                onRemove={onRemoveIngredient}
            />
        </div>
    );
};

export default MealEditorIngredients;
