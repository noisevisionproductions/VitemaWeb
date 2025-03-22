import React, {useRef, useState} from "react";
import RecipesList from "./RecipesList";
import RecipeModal from "./RecipeModal";

const RecipesPage: React.FC = () => {
    const [selectedRecipeId, setSelectedRecipeId] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const recipesListRef = useRef<{ refreshRecipes: () => void } | null>(null);

    const handleRecipeSelect = (recipeId: string) => {
        setSelectedRecipeId(recipeId);
        setIsModalOpen(true);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        if (recipesListRef.current) {
            recipesListRef.current.refreshRecipes();
        }
    };

    const handleRecipeUpdate = () => {
        if (recipesListRef.current) {
            recipesListRef.current.refreshRecipes();
        }
    };

    return (
        <div className="space-y-6 pb-8  h-full flex flex-col">
            {/* Nagłówek */}
            <div>
                <h1 className="text-2xl font-bold mb-4">Przepisy kulinarne</h1>
                <p className="text-slate-500 text-sm mt-1"> Zarządzaj przepisami, które będą używane w dietach dla
                    klientów
                </p>
            </div>

            <div className="flex-grow h-full overflow-hidden">
                <RecipesList
                    onRecipeSelect={handleRecipeSelect}
                    ref={recipesListRef}
                />
            </div>

            {/* Modal ze szczegółami przepisu */}
            <RecipeModal
                recipeId={selectedRecipeId}
                isOpen={isModalOpen}
                onClose={handleModalClose}
                onRecipeUpdate={handleRecipeUpdate}
            />
        </div>
    );
};

export default RecipesPage;