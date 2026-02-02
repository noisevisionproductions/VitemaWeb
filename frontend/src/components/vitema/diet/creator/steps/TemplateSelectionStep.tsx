import React, {useState} from 'react';
import {Search, Sparkles, Plus, Clock, Calendar, Trash2} from 'lucide-react';
import {DietTemplate} from "../../../../../types/DietTemplate";
import {useDietTemplates} from "../../../../../hooks/diet/templates/useDietTemplates";
import LoadingSpinner from '../../../../shared/common/LoadingSpinner';
import {User} from '../../../../../types/user';
import {toast} from "../../../../../utils/toast";

interface TemplateSelectionStepProps {
    onTemplateSelect: (template: DietTemplate | null) => void;
    onContinueWithoutTemplate: () => void;
    selectedUser: User | null;
    isLoading: boolean
}

const TemplateSelectionStep: React.FC<TemplateSelectionStepProps> = ({
                                                                         onTemplateSelect,
                                                                         onContinueWithoutTemplate,
                                                                         selectedUser,
                                                                         isLoading
                                                                     }) => {
    const {templates, loading, searchTemplates, deleteTemplate} = useDietTemplates();
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedTemplate, setSelectedTemplate] = useState<DietTemplate | null>(null);
    const [isDeleting, setIsDeleting] = useState<string | null>(null);

    const handleSearch = (query: string) => {
        setSearchQuery(query);
        searchTemplates(query).catch(console.error);
    };

    const handleTemplateClick = (template: DietTemplate) => {
        setSelectedTemplate(template);
    };

    const handleDeleteTemplate = async (e: React.MouseEvent, template: DietTemplate) => {
        e.stopPropagation();

        if (window.confirm(`Czy na pewno chcesz usunąć szablon "${template.name}"? Tej operacji nie można cofnąć.`)) {
            try {
                setIsDeleting(template.id);

                if (selectedTemplate?.id === template.id) {
                    setSelectedTemplate(null);
                }

                await deleteTemplate(template.id);
                toast.success('Szablon został usunięty');
            } catch (error) {
                console.error('Failed to delete template:', error);
                toast.error('Nie udało się usunąć szablonu');
            } finally {
                setIsDeleting(null);
            }
        }
    };

    const handleContinue = () => {
        onTemplateSelect(selectedTemplate);
    };

    const getCategoryColor = (category: string) => {
        const colors: Record<string, string> = {
            'WEIGHT_LOSS': 'bg-red-100 text-red-800 border-red-200',
            'WEIGHT_GAIN': 'bg-green-100 text-green-800 border-green-200',
            'MAINTENANCE': 'bg-blue-100 text-blue-800 border-blue-200',
            'SPORT': 'bg-orange-100 text-orange-800 border-orange-200',
            'MEDICAL': 'bg-purple-100 text-purple-800 border-purple-200',
            'VEGETARIAN': 'bg-emerald-100 text-emerald-800 border-emerald-200',
            'VEGAN': 'bg-teal-100 text-teal-800 border-teal-200',
            'CUSTOM': 'bg-gray-100 text-gray-800 border-gray-200'
        };
        return colors[category] || colors['CUSTOM'];
    };

    return (
        <div className="space-y-6">
            {isLoading && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 flex items-center gap-3">
                        <LoadingSpinner size="md"/>
                        <span className="text-gray-700">Ładowanie szablonu...</span>
                    </div>
                </div>
            )}

            {selectedUser && (
                <div className="mb-6 p-4 bg-primary-light/10 rounded-lg border border-primary-light/20">
                    <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-primary-light rounded-full flex items-center justify-center">
                <span className="text-primary text-sm font-semibold">
                    {selectedUser.nickname?.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() ||
                        selectedUser.email.substring(0, 2).toUpperCase()}
                </span>
                        </div>
                        <div>
                            <p className="font-medium text-gray-900">Dieta
                                dla: {selectedUser.nickname || selectedUser.nickname || selectedUser.email}</p>
                            <p className="text-sm text-gray-600">{selectedUser.email}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="text-center">
                <div
                    className="w-16 h-16 bg-primary-light/20 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Sparkles className="h-8 w-8 text-primary"/>
                </div>
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                    Wybierz szablon diety
                </h2>
                <p className="text-gray-600 max-w-2xl mx-auto">
                    Rozpocznij od gotowego szablonu diety lub utwórz nową dietę od zera.
                    {selectedUser && (
                        <span className="block mt-1 text-primary font-medium">
                            Dieta dla: {selectedUser.nickname || selectedUser.email}
                        </span>
                    )}
                </p>
            </div>

            {/* Wyszukiwanie */}
            <div className="max-w-md mx-auto">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5"/>
                    <input
                        type="text"
                        placeholder="Szukaj szablonów..."
                        value={searchQuery}
                        onChange={(e) => handleSearch(e.target.value)}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-primary focus:border-primary text-sm"
                    />
                </div>
            </div>

            {/* Opcja "bez szablonu" */}
            <div className="max-w-4xl mx-auto">
                <div
                    onClick={onContinueWithoutTemplate}
                    className="bg-gradient-to-r from-primary to-primary-dark text-white rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-200 mb-6"
                >
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <div className="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center">
                                <Plus className="h-6 w-6 text-white"/>
                            </div>
                            <div>
                                <h3 className="text-lg font-semibold text-white mb-1">
                                    Utwórz nową dietę od zera
                                </h3>
                                <p className="text-primary-light text-sm">
                                    Zaplanuj posiłki indywidualnie dla tego klienta
                                </p>
                            </div>
                        </div>
                        <div className="text-white/80">
                            →
                        </div>
                    </div>
                </div>

                {/* Lista szablonów */}
                {loading ? (
                    <div className="flex justify-center py-12">
                        <LoadingSpinner size="lg"/>
                    </div>
                ) : templates.length === 0 ? (
                    <div className="text-center py-12">
                        <div
                            className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Search className="h-8 w-8 text-gray-400"/>
                        </div>
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            {searchQuery ? 'Brak wyników' : 'Brak szablonów'}
                        </h3>
                        <p className="text-gray-500">
                            {searchQuery
                                ? 'Nie znaleziono szablonów pasujących do wyszukiwania'
                                : 'Nie masz jeszcze żadnych szablonów diet'
                            }
                        </p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        <h3 className="text-lg font-medium text-gray-900 mb-4">
                            Dostępne szablony ({templates.length})
                        </h3>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {templates.map((template) => (
                                <div
                                    key={template.id}
                                    onClick={() => handleTemplateClick(template)}
                                    className={`relative border-2 rounded-xl p-4 cursor-pointer transition-all duration-200 hover:shadow-md group ${
                                        selectedTemplate?.id === template.id
                                            ? 'border-primary bg-primary-light/10'
                                            : 'border-gray-200 bg-white hover:border-gray-300'
                                    }`}
                                >
                                    <div className="flex items-start justify-between mb-3">
                                        <div
                                            className="flex-1 min-w-0 pr-8">
                                            <h4 className="font-semibold text-gray-900 truncate mb-1">
                                                {template.name}
                                            </h4>
                                            <span
                                                className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getCategoryColor(template.category)}`}>
                                                {template.categoryLabel}
                                            </span>
                                        </div>

                                        {/* 4. Sekcja przycisków (Usuwanie + Wybór) */}
                                        <div className="flex items-start gap-2 absolute top-4 right-4">
                                            <button
                                                onClick={(e) => handleDeleteTemplate(e, template)}
                                                disabled={isDeleting === template.id}
                                                className={`p-1.5 rounded-full transition-all duration-200 
                                                    ${isDeleting === template.id
                                                    ? 'bg-red-100 text-red-500'
                                                    : 'text-gray-400 hover:text-red-600 hover:bg-red-50 opacity-0 group-hover:opacity-100'
                                                }
                                                `}
                                                title="Usuń szablon"
                                            >
                                                {isDeleting === template.id ? (
                                                    <LoadingSpinner size="sm"/>
                                                ) : (
                                                    <Trash2 size={18}/>
                                                )}
                                            </button>

                                            {/* Wskaźnik wyboru */}
                                            {selectedTemplate?.id === template.id && (
                                                <div
                                                    className="w-6 h-6 bg-primary rounded-full flex items-center justify-center">
                                                    <div className="w-2 h-2 bg-white rounded-full"></div>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {template.description && (
                                        <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                                            {template.description}
                                        </p>
                                    )}

                                    <div className="grid grid-cols-2 gap-4 text-sm text-gray-600 mb-3">
                                        <div className="flex items-center gap-1">
                                            <Calendar className="h-4 w-4"/>
                                            <span>{template.duration} dni</span>
                                        </div>
                                        <div className="flex items-center gap-1">
                                            <Clock className="h-4 w-4"/>
                                            <span>{template.mealsPerDay} posiłków</span>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-between text-xs text-gray-500">
                                        <span>
                                            {template.totalMeals} posiłków • {template.totalIngredients} składników
                                        </span>
                                        {template.usageCount > 0 && (
                                            <span className="bg-gray-100 px-2 py-1 rounded-full">
                                                {template.usageCount}x użyte
                                            </span>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {/* Przycisk kontynuuj z szablonem */}
            {selectedTemplate && (
                <div className="fixed bottom-6 right-6 z-10">
                    <button
                        onClick={handleContinue}
                        disabled={isLoading}
                        className={`flex items-center gap-2 px-6 py-3 rounded-xl transition-colors shadow-lg ${
                            isLoading
                                ? 'bg-gray-400 text-gray-200 cursor-not-allowed'
                                : 'bg-primary text-white hover:bg-primary-dark'
                        }`}
                    >
                        {isLoading ? (
                            <>
                                <LoadingSpinner size="sm" className="text-white"/>
                                Ładowanie...
                            </>
                        ) : (
                            <>
                                <Sparkles className="h-5 w-5"/>
                                Użyj szablon "{selectedTemplate.name}"
                            </>
                        )}
                    </button>
                </div>
            )}
        </div>
    );
};

export default TemplateSelectionStep;
