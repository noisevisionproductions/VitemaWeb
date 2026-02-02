import React, {useCallback, useMemo, useRef, useState} from "react";
import {MainNav} from "../../../../types/navigation";
import {DayData, ManualDietData, MealType, ParsedDietData, ParsedMeal} from "../../../../types";
import {ParsedProduct} from "../../../../types/product";
import {toast} from "../../../../utils/toast";
import SectionHeader from "../../../shared/common/SectionHeader";
import {FloatingActionButton, FloatingActionButtonGroup} from "../../../shared/common/FloatingActionButton";
import {ArrowLeft, ArrowRight, Save, AlertTriangle, X} from "lucide-react";
import {Timestamp} from "firebase/firestore";
import {DEFAULT_DIET_CONFIG} from "../../../../types/dietDefaults";
import MealPlanningStep from "./steps/Planning/MealPlanningStep";
import ConfigurationStep from "./steps/ConfigurationStep";
import {ManualDietRequest, DietCreatorService} from "../../../../services/diet/creator/DietCreatorService";
import {User} from "../../../../types/user";
import DietPreview from "../upload/preview/DietPreview";
import {useCategorization} from "../../../../hooks/shopping/useCategorization";
import {DietCategorizationService} from "../../../../services/diet/DietCategorizationService";
import CategorySection from "./steps/Categorization/CategorySection";
import DietCreatorGuide from "./components/DietCreatorGuide";
import {CreateDietTemplateRequest, DietTemplate} from "../../../../types/DietTemplate";
import CreateTemplateDialog from "../templates/CreateTemplateDialog";
import {useTemplateLoader} from "../../../../hooks/diet/templates/useTemplateLoader";
import TemplateSelectionStep from "./steps/TemplateSelectionStep";

interface DietCreatorProps {
    onTabChange: (tab: MainNav) => void;
    onBackToSelection: () => void;
}

type Step = 'templateSelection' | 'configuration' | 'planning' | 'categorization' | 'preview';

const DietCreator: React.FC<DietCreatorProps> = ({
                                                     onTabChange
                                                 }) => {
    const [currentStep, setCurrentStep] = useState<Step>('configuration');
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [dietData, setDietData] = useState<ManualDietData>({
        userId: '',
        ...DEFAULT_DIET_CONFIG,
        days: []
    });
    const [parsedPreviewData, setParsedPreviewData] = useState<ParsedDietData | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    // Stany do obsługi szablonów i potwierdzeń
    const [showSaveAsTemplate, setShowSaveAsTemplate] = useState(false);
    const [showBackConfirmation, setShowBackConfirmation] = useState(false);

    const [templateData, setTemplateData] = useState<CreateDietTemplateRequest | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<DietTemplate | null>(null);
    const {loadTemplateIntoDiet, loading: templateLoading} = useTemplateLoader();

    const shoppingListRef = useRef<string[]>([]);

    const updateDietData = useCallback((updates: Partial<ManualDietData>) => {
        setDietData(prev => ({...prev, ...updates}));
    }, []);

    const hasPlannedMeals = useMemo(() => {
        return dietData.days.some(day =>
            day.meals.some(meal => meal.name && meal.name.trim() !== '')
        );
    }, [dietData.days]);

    const currentShoppingListItems = useMemo(() => {
        const items: string[] = [];
        dietData.days.forEach(day => {
            day.meals.forEach(meal => {
                if (meal.ingredients) {
                    meal.ingredients.forEach(ingredient => {
                        const ingredientString = `${ingredient.name} ${ingredient.quantity} ${ingredient.unit}`;
                        items.push(ingredientString);
                    });
                }
            });
        });
        const itemsString = JSON.stringify(items);
        const currentString = JSON.stringify(shoppingListRef.current);
        if (itemsString !== currentString) {
            shoppingListRef.current = items;
        }
        return items;
    }, [dietData.days]);

    const categorizationShoppingList = currentStep === 'categorization' ? shoppingListRef.current : [];

    const {
        categorizedProducts,
        uncategorizedProducts,
        handleProductDrop,
        handleProductRemove,
        handleProductEdit
    } = useCategorization(categorizationShoppingList);

    const updateMeal = useCallback((dayIndex: number, mealIndex: number, meal: ParsedMeal) => {
        setDietData(prev => {
            const newDays = [...prev.days];
            newDays[dayIndex] = {
                ...newDays[dayIndex],
                meals: newDays[dayIndex].meals.map((m, i) => i === mealIndex ? meal : m)
            };
            return {...prev, days: newDays};
        });
    }, []);

    const addIngredientToMeal = useCallback((dayIndex: number, mealIndex: number, ingredients: ParsedProduct) => {
        setDietData(prev => {
            const newDays = [...prev.days];
            const meal = newDays[dayIndex].meals[mealIndex];
            newDays[dayIndex].meals[mealIndex] = {
                ...meal,
                ingredients: [...(meal.ingredients || []), ingredients]
            };
            return {...prev, days: newDays};
        });
    }, []);

    const removeIngredientFromMeal = useCallback((dayIndex: number, mealIndex: number, ingredientIndex: number) => {
        setDietData(prev => {
            const newDays = [...prev.days];
            const meal = newDays[dayIndex].meals[mealIndex];
            newDays[dayIndex].meals[mealIndex] = {
                ...meal,
                ingredients: meal.ingredients?.filter((_, i) => i !== ingredientIndex) || []
            };
            return {...prev, days: newDays};
        });
    }, []);

    const initializeDays = useCallback(() => {
        const days: DayData[] = [];
        const startDate = new Date(dietData.startDate);

        for (let i = 0; i < dietData.duration; i++) {
            const currentDate = new Date(startDate);
            currentDate.setDate(startDate.getDate() + i);

            const meals: ParsedMeal[] = [];
            for (let j = 0; j < dietData.mealsPerDay; j++) {
                meals.push({
                    name: '',
                    instructions: '',
                    ingredients: [],
                    mealType: dietData.mealTypes[j] || MealType.BREAKFAST,
                    time: dietData.mealTimes[`meal_${j}`] || '12:00',
                    photos: []
                });
            }

            days.push({
                date: Timestamp.fromDate(currentDate),
                meals
            });
        }

        updateDietData({days});
    }, [dietData.startDate, dietData.duration, dietData.mealsPerDay, dietData.mealTimes, dietData.mealTypes, updateDietData]);

    const convertToPreviewData = useCallback((): ParsedDietData => {
        const simplifiedCategorizedProducts: Record<string, string[]> = {};
        Object.entries(categorizedProducts).forEach(([categoryId, products]) => {
            simplifiedCategorizedProducts[categoryId] = products.map(product =>
                product.original || `${product.name} ${product.quantity} ${product.unit}`
            );
        });

        return {
            days: dietData.days,
            categorizedProducts: simplifiedCategorizedProducts,
            shoppingList: shoppingListRef.current,
            mealTimes: dietData.mealTimes,
            mealsPerDay: dietData.mealsPerDay,
            startDate: Timestamp.fromDate(new Date(dietData.startDate)),
            duration: dietData.duration,
            mealTypes: dietData.mealTypes
        };
    }, [dietData, categorizedProducts]);

    const handleNext = useCallback(async () => {
        if (currentStep === 'configuration') {
            if (!selectedUser) {
                toast.error('Musisz wybrać klienta');
                return;
            }
            if (!dietData.startDate) {
                toast.error('Musisz wybrać datę rozpoczęcia diety');
                return;
            }
            setCurrentStep('templateSelection');
            return;
        }

        if (currentStep === 'templateSelection') {
            if (!selectedTemplate) {
                initializeDays();
            }
            setCurrentStep('planning');
            return;
        }

        if (currentStep === 'planning') {
            const emptyMeals = dietData.days.some(day =>
                day.meals.some(meal => !meal.name || meal.name.trim() === '')
            );

            if (emptyMeals) {
                toast.error('Wszystkie posiłki muszą mieć nazwę');
                return;
            }

            if (currentShoppingListItems.length === 0) {
                toast.warning('Brak składników do kategoryzacji. Przechodzimy do podglądu.');
                const previewData: ParsedDietData = {
                    days: dietData.days,
                    categorizedProducts: {},
                    shoppingList: [],
                    mealTimes: dietData.mealTimes,
                    mealsPerDay: dietData.mealsPerDay,
                    startDate: Timestamp.fromDate(new Date(dietData.startDate)),
                    duration: dietData.duration,
                    mealTypes: dietData.mealTypes
                };
                setParsedPreviewData(previewData);
                setCurrentStep('preview');
                return;
            }
            setCurrentStep('categorization');
        }
    }, [currentStep, dietData, selectedUser, initializeDays, currentShoppingListItems]);

    const handlePrevious = useCallback(() => {
        if (currentStep === 'templateSelection') {
            setCurrentStep('configuration');
        } else if (currentStep === 'planning') {
            // Jeśli jesteśmy w planowaniu i są wprowadzone dane, pytamy o potwierdzenie
            if (hasPlannedMeals) {
                setShowBackConfirmation(true);
            } else {
                setCurrentStep('templateSelection');
                setParsedPreviewData(null);
            }
        }
    }, [currentStep, selectedTemplate, hasPlannedMeals]);

    const handleDiscardAndBack = () => {
        setShowBackConfirmation(false);
        setCurrentStep('templateSelection');
        setParsedPreviewData(null);
    };

    const handleSaveFromConfirmation = () => {
        setShowBackConfirmation(false);
        handleSaveAsTemplate();
    };

    const handleCategorizationComplete = async () => {
        if (uncategorizedProducts.length > 0) {
            toast.error('Musisz skategoryzować wszystkie produkty');
            return;
        }
        try {
            setIsProcessing(true);
            await DietCategorizationService.updateCategories(categorizedProducts);
            const previewData = convertToPreviewData();
            setParsedPreviewData(previewData);
            setCurrentStep('preview');
            toast.success('Kategoryzacja została zapisana');
        } catch (error) {
            console.error('Błąd podczas zapisywania kategoryzacji:', error);
            toast.error('Wystąpił błąd podczas zapisywania kategoryzacji');
        } finally {
            setIsProcessing(false);
        }
    };

    const handleSaveAsTemplate = useCallback(() => {
        const templateData: CreateDietTemplateRequest = {
            name: `Szablon diety - ${new Date().toLocaleDateString('pl-PL')}`,
            description: '',
            category: 'CUSTOM',
            duration: dietData.duration,
            mealsPerDay: dietData.mealsPerDay,
            mealTimes: dietData.mealTimes,
            mealTypes: dietData.mealTypes.map(type => type.toString()),
            dietData: {
                userId: dietData.userId,
                days: dietData.days.map(day => ({
                    date: day.date,
                    meals: day.meals
                })),
                mealsPerDay: dietData.mealsPerDay,
                startDate: dietData.startDate,
                duration: dietData.duration,
                mealTimes: dietData.mealTimes,
                mealTypes: dietData.mealTypes.map(type => type.toString())
            }
        };
        setTemplateData(templateData);
        setShowSaveAsTemplate(true);
    }, [dietData]);

    const handleTemplateSelect = useCallback(async (template: DietTemplate | null) => {
        setSelectedTemplate(template);
        if (template && selectedUser) {
            try {
                setIsProcessing(true);
                const loadedDietData = await loadTemplateIntoDiet(
                    template,
                    selectedUser.id,
                    dietData.startDate
                );
                setDietData(loadedDietData);
                setCurrentStep('planning');
            } catch (error) {
                console.error('Error loading template:', error);
                toast.error('Wystąpił błąd podczas ładowania szablonu');
            } finally {
                setIsProcessing(false);
            }
        } else {
            setCurrentStep('planning');
        }
    }, [selectedUser, dietData.startDate, loadTemplateIntoDiet]);

    const handleContinueWithoutTemplate = useCallback(() => {
        setSelectedTemplate(null);
        if (dietData.days.length === 0) {
            initializeDays();
        }
        setCurrentStep('planning');
    }, [dietData.days.length, initializeDays]);

    const handleSave = useCallback(async () => {
        if (isProcessing) return;
        setIsProcessing(true);
        try {
            const request: ManualDietRequest = {
                userId: dietData.userId,
                days: dietData.days.map(day => ({
                    date: day.date,
                    meals: day.meals
                })),
                mealsPerDay: dietData.mealsPerDay,
                startDate: dietData.startDate,
                duration: dietData.duration,
                mealTimes: dietData.mealTimes,
                mealTypes: dietData.mealTypes.map(type => type.toString())
            };
            await DietCreatorService.saveManualDiet(request);
            toast.success('Dieta została pomyślnie zapisana');
            onTabChange('diets');
        } catch (error) {
            console.error('Błąd podczas zapisywania:', error);
            toast.error('Wystąpił błąd podczas zapisywania diety');
        } finally {
            setIsProcessing(false);
        }
    }, [dietData, isProcessing, onTabChange]);

    const handlePreviewCancel = useCallback(() => {
        if (shoppingListRef.current.length > 0) {
            setCurrentStep('categorization');
        } else {
            setCurrentStep('planning');
        }
        setParsedPreviewData(null);
    }, []);

    const getStepTitle = () => {
        switch (currentStep) {
            case "templateSelection":
                return 'Wybór szablonu diety';
            case "configuration":
                return 'Konfiguracja diety';
            case "planning":
                return selectedTemplate
                    ? `Planowanie posiłków - ${selectedTemplate.name}`
                    : 'Planowanie posiłków';
            case "categorization":
                return 'Kategoryzacja składników';
            case "preview":
                return 'Podgląd diety przed zapisem';
            default:
                return 'Kreator diety';
        }
    };

    const getStepDescription = () => {
        switch (currentStep) {
            case 'configuration':
                return 'Wybierz klienta i ustaw podstawowe parametry diety';
            case 'templateSelection':
                return 'Wybierz szablon diety dopasowany do Twoich wymagań lub kontynuuj bez szablonu';
            case 'planning':
                return selectedTemplate
                    ? 'Zmodyfikuj posiłki z szablonu według potrzeb'
                    : 'Zaplanuj posiłki dla każdego dnia';
            case 'categorization':
                return 'Przypisz składniki do odpowiednich kategorii';
            case 'preview':
                return 'Sprawdź dietę przed zapisem';
            default:
                return '';
        }
    };

    if (currentStep === 'preview' && parsedPreviewData) {
        return (
            <DietPreview
                parsedData={parsedPreviewData}
                onConfirm={handleSave}
                onCancel={handlePreviewCancel}
                selectedUserEmail={selectedUser?.email || ''}
                fileName="Dieta ręczna"
                skipCategorization={true}
            />
        );
    }

    return (
        <div className="space-y-6 pb-16 relative">

            <SectionHeader
                title={getStepTitle()}
                description={getStepDescription()}
            />

            {/* Guide */}
            <DietCreatorGuide className="mb-6"/>

            {/* Progress bar */}
            <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                    className="bg-primary h-2 rounded-full transition-all duration-300"
                    style={{
                        width: currentStep === 'configuration' ? '20%' :
                            currentStep === 'templateSelection' ? '40%' :
                                currentStep === 'planning' ? '60%' :
                                    currentStep === 'categorization' ? '80%' : '100%'
                    }}
                />
            </div>

            {/* Step content */}
            <div className="min-h-[500px]">
                {currentStep === 'templateSelection' && (
                    <TemplateSelectionStep
                        onTemplateSelect={handleTemplateSelect}
                        onContinueWithoutTemplate={handleContinueWithoutTemplate}
                        selectedUser={selectedUser}
                        isLoading={templateLoading || isProcessing}
                    />
                )}

                {currentStep === 'configuration' && (
                    <ConfigurationStep
                        dietData={dietData}
                        onUpdate={updateDietData}
                        selectedUser={selectedUser}
                        onUserSelect={setSelectedUser}
                    />
                )}

                {currentStep === 'planning' && (
                    <div className="relative">
                        <MealPlanningStep
                            dietData={dietData}
                            selectedTemplate={selectedTemplate}
                            selectedUser={selectedUser}
                            onRemoveTemplate={() => {
                                setSelectedTemplate(null);
                            }}
                            onUpdateMeal={updateMeal}
                            onAddIngredient={addIngredientToMeal}
                            onRemoveIngredient={removeIngredientFromMeal}
                        />
                    </div>
                )}

                {currentStep === 'categorization' && (
                    <CategorySection
                        uncategorizedProducts={uncategorizedProducts}
                        categorizedProducts={categorizedProducts}
                        onProductDrop={handleProductDrop}
                        onProductRemove={handleProductRemove}
                        onProductEdit={handleProductEdit}
                        onComplete={handleCategorizationComplete}
                        onCancel={handlePreviewCancel}
                        selectedUserEmail={selectedUser?.email || ''}
                        showBackButton={true}
                        onBack={handlePrevious}
                    />
                )}
            </div>

            {/* Navigation buttons */}
            {currentStep !== 'categorization' && currentStep !== 'preview' && (
                <div className="fixed bottom-6 right-6 flex gap-3 z-10">
                    <FloatingActionButtonGroup position="bottom-right">
                        {currentStep !== 'configuration' && currentStep !== 'templateSelection' && (
                            <FloatingActionButton
                                label="Poprzedni krok"
                                onClick={handlePrevious}
                                variant="secondary"
                                icon={<ArrowLeft className="h-5 w-5"/>}
                            />
                        )}

                        {currentStep === 'planning' && (
                            <FloatingActionButton
                                label="Zapisz jako szablon"
                                onClick={handleSaveAsTemplate}
                                variant="secondary"
                                icon={<Save className="h-5 w-5"/>}
                                className="border-emerald-200 text-emerald-700 hover:bg-emerald-50 hover:border-emerald-300"
                            />
                        )}

                        {currentStep !== 'templateSelection' && (
                            <FloatingActionButton
                                label="Następny krok"
                                onClick={handleNext}
                                variant="primary"
                                icon={<ArrowRight className="h-5 w-5"/>}
                                isLoading={isProcessing && currentStep === 'planning'}
                                loadingLabel="Przygotowywanie..."
                            />
                        )}
                    </FloatingActionButtonGroup>
                </div>
            )}

            {/* Dialog zapisu szablonu */}
            {showSaveAsTemplate && templateData && (
                <CreateTemplateDialog
                    isOpen={showSaveAsTemplate}
                    onClose={() => {
                        setShowSaveAsTemplate(false);
                        setTemplateData(null);
                    }}
                    onSuccess={() => {
                        setShowSaveAsTemplate(false);
                        setTemplateData(null);
                        toast.success('Szablon został zapisany i można go użyć w przyszłości');
                    }}
                    initialData={templateData}
                />
            )}

            {/* Dialog potwierdzenia powrotu (utrata danych) */}
            {showBackConfirmation && (
                <div className="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog"
                     aria-modal="true">
                    <div
                        className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                        <div
                            className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
                            aria-hidden="true"
                            onClick={() => setShowBackConfirmation(false)}
                        ></div>

                        <span className="hidden sm:inline-block sm:align-middle sm:h-screen"
                              aria-hidden="true">&#8203;</span>

                        <div
                            className="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
                            <div className="absolute top-0 right-0 pt-4 pr-4">
                                <button
                                    type="button"
                                    className="bg-white rounded-md text-gray-400 hover:text-gray-500 focus:outline-none"
                                    onClick={() => setShowBackConfirmation(false)}
                                >
                                    <span className="sr-only">Zamknij</span>
                                    <X className="h-6 w-6" aria-hidden="true"/>
                                </button>
                            </div>

                            <div className="sm:flex sm:items-start">
                                <div
                                    className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-amber-100 sm:mx-0 sm:h-10 sm:w-10">
                                    <AlertTriangle className="h-6 w-6 text-amber-600" aria-hidden="true"/>
                                </div>
                                <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                                    <h3 className="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                                        Masz niezapisane zmiany
                                    </h3>
                                    <div className="mt-2">
                                        <p className="text-sm text-gray-500">
                                            Powrót do poprzedniego ekranu może spowodować utratę wprowadzonych posiłków.
                                            Czy chcesz najpierw zapisać obecny stan jako szablon, aby móc do niego
                                            wrócić?
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse gap-2">
                                <button
                                    type="button"
                                    className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-emerald-600 text-base font-medium text-white hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 sm:w-auto sm:text-sm"
                                    onClick={handleSaveFromConfirmation}
                                >
                                    Zapisz jako szablon
                                </button>
                                <button
                                    type="button"
                                    className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:w-auto sm:text-sm"
                                    onClick={handleDiscardAndBack}
                                >
                                    Wróć bez zapisywania
                                </button>
                                <button
                                    type="button"
                                    className="mt-3 w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-500 hover:text-gray-700 focus:outline-none sm:mt-0 sm:w-auto sm:text-sm"
                                    onClick={() => setShowBackConfirmation(false)}
                                >
                                    Anuluj
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DietCreator;