import React, {useState} from "react";
import {useDietTemplates} from "../../../hooks/diet/templates/useDietTemplates";
import {DietTemplateCategory} from "../../../types/DietTemplate";
import SectionHeader from "../../common/SectionHeader";
import {BarChart3, Filter, Plus, Search} from "lucide-react";
import LoadingSpinner from "../../common/LoadingSpinner";
import CreateTemplateDialog from "./CreateTemplateDialog";
import DietTemplateStats from "./DietTemplateStats";
import DietTemplateFilters from "./DietTemplateFilters";
import DietTemplateCard from "./DietTemplateCard";

const DietTemplatesManager: React.FC = () => {
    const {
        templates,
        loading,
        error,
        loadTemplatesByCategory,
        searchTemplates,
        deleteTemplate,
        incrementUsage
    } = useDietTemplates();

    const [searchQuery, setSearchQuery] = useState('');
    const [selectedCategory, setSelectedCategory] = useState<DietTemplateCategory | 'ALL'>('ALL');
    const [showFilters, setShowFilters] = useState(false);
    const [showStats, setShowStats] = useState(false);
    const [showCreateDialog, setShowCreateDialog] = useState(false);

    const handleSearch = (query: string) => {
        setSearchQuery(query);
        searchTemplates(query).catch(console.error);
    };

    const handleCategoryChange = (category: DietTemplateCategory | 'ALL') => {
        setSelectedCategory(category);
        if (category === 'ALL') {
            searchTemplates(searchQuery).catch(console.error);
        } else {
            loadTemplatesByCategory(category).catch(console.error);
        }
    };

    const handleUseTemplate = async (templateId: string) => {
        await incrementUsage(templateId);

        // TODO
        console.log('Using template:', templateId);
    };

    return (
        <div className="space-y-6">
            <SectionHeader
                title="Szablony Diet"
                description="Zarządzaj szablonami diet do wielokrotnego użytku"
            />

            {/* Header z akcjami */}
            <div className="bg-white rounded-xl shadow-sm border p-6">
                <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
                    {/* Wyszukiwanie */}
                    <div className="flex-1 max-w-md">
                        <div className="relative">
                            <Search
                                className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4"/>
                            <input
                                type="text"
                                placeholder="Szukaj szablonów..."
                                value={searchQuery}
                                onChange={(e) => handleSearch(e.target.value)}
                                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                            />
                        </div>
                    </div>

                    {/* Przyciski akcji */}
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setShowFilters(!showFilters)}
                            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                                showFilters
                                    ? 'bg-primary text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            <Filter className="h-4 w-4"/>
                            Filtry
                        </button>

                        <button
                            onClick={() => setShowStats(!showStats)}
                            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                                showStats
                                    ? 'bg-primary text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            <BarChart3 className="h-4 w-4"/>
                            Statystyki
                        </button>

                        <button
                            onClick={() => setShowCreateDialog(true)}
                            className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                        >
                            <Plus className="h-4 w-4"/>
                            Nowy szablon
                        </button>
                    </div>
                </div>

                {/* Filtry */}
                {showFilters && (
                    <div className="mt-4 pt-4 border-t border-gray-200">
                        <DietTemplateFilters
                            selectedCategory={selectedCategory}
                            onCategoryChange={handleCategoryChange}
                        />
                    </div>
                )}

                {/* Statystyki */}
                {showStats && (
                    <div className="mt-4 pt-4 border-t border-gray-200">
                        <DietTemplateStats/>
                    </div>
                )}
            </div>

            {/* Zawartość */}
            {loading ? (
                <div className="flex justify-center py-12">
                    <LoadingSpinner size="lg"/>
                </div>
            ) : error ? (
                <div className="text-center py-12">
                    <p className="text-red-600">{error}</p>
                </div>
            ) : templates.length === 0 ? (
                <div className="text-center py-12 bg-white rounded-xl shadow-sm border">
                    <div className="max-w-md mx-auto">
                        <div
                            className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Plus className="h-8 w-8 text-gray-400"/>
                        </div>
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            Brak szablonów diet
                        </h3>
                        <p className="text-gray-500 mb-4">
                            {searchQuery
                                ? 'Nie znaleziono szablonów pasujących do wyszukiwania'
                                : 'Utwórz pierwszy szablon diety, aby móc go wielokrotnie używać'
                            }
                        </p>
                        <button
                            onClick={() => setShowCreateDialog(true)}
                            className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                        >
                            <Plus className="h-4 w-4"/>
                            Utwórz pierwszy szablon
                        </button>
                    </div>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {templates.map((template) => (
                        <DietTemplateCard
                            key={template.id}
                            template={template}
                            onUse={() => handleUseTemplate(template.id)}
                            onDelete={() => deleteTemplate(template.id)}
                        />
                    ))}
                </div>
            )}

            {/* Dialog tworzenia szablonu */}
            {showCreateDialog && (
                <CreateTemplateDialog
                    isOpen={showCreateDialog}
                    onClose={() => setShowCreateDialog(false)}
                    onSuccess={() => {
                        setShowCreateDialog(false);
                        // Odśwież listę
                        if (selectedCategory === 'ALL') {
                            searchTemplates(searchQuery).catch(console.error);
                        } else {
                            loadTemplatesByCategory(selectedCategory).catch(console.error);
                        }
                    }}
                />
            )}
        </div>
    );
};

export default DietTemplatesManager;