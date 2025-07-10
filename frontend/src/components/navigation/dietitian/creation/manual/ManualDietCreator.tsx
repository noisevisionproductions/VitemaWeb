import React, {useCallback, useMemo, useRef, useState} from "react";
import {MainNav} from "../../../../../types/navigation";
import {DayData, ManualDietData, MealType, ParsedDietData, ParsedMeal} from "../../../../../types";
import {ParsedProduct} from "../../../../../types/product";
import {toast} from "../../../../../utils/toast";
import SectionHeader from "../../../../common/SectionHeader";
import {FloatingActionButton, FloatingActionButtonGroup} from "../../../../common/FloatingActionButton";
import {ArrowLeft, ArrowRight, BookAudio} from "lucide-react";
import {Timestamp} from "firebase/firestore";
import {DEFAULT_DIET_CONFIG} from "../../../../../types/dietDefaults";
import MealPlanningStep from "./steps/MealPlanningStep";
import DietConfigurationStep from "./steps/DietConfigurationStep";
import {ManualDietRequest, ManualDietService} from "../../../../../services/diet/manual/ManualDietService";
import {User} from "../../../../../types/user";
import DietPreview from "../../../../diet/upload/preview/DietPreview";
import {useCategorization} from "../../../../../hooks/shopping/useCategorization";
import {DietCategorizationService} from "../../../../../services/diet/DietCategorizationService";
import CategorySection from "../../../../diet/upload/preview/CategorySection";
import ManualDietGuide from "./components/ManualDietGuide";
import {CreateDietTemplateRequest, DietTemplate} from "../../../../../types/DietTemplate";
import CreateTemplateDialog from "../../../../diet/templates/CreateTemplateDialog";
import {useTemplateLoader} from "../../../../../hooks/diet/templates/useTemplateLoader";
import TemplateSelectionStep from "./templates/TemplateSelectionStep";

interface ManualDietCreatorProps {
    onTabChange: (tab: MainNav) => void;
    onBackToSelection: () => void;
}

type Step = 'templateSelection' | 'configuration' | 'planning' | 'categorization' | 'preview';

const ManualDietCreator: React.FC<ManualDietCreatorProps> = ({
                                                                 onTabChange,
                                                                 onBackToSelection
                                                             }) => {
    const [currentStep, setCurrentStep] = useState<Step>('templateSelection');
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [dietData, setDietData] = useState<ManualDietData>({
        userId: '',
        ...DEFAULT_DIET_CONFIG,
        days: []
    });
    const [parsedPreviewData, setParsedPreviewData] = useState<ParsedDietData | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [showSaveAsTemplate, setShowSaveAsTemplate] = useState(false);
    const [templateData, setTemplateData] = useState<CreateDietTemplateRequest | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<DietTemplate | null>(null);
    const {loadTemplateIntoDiet, loading: templateLoading} = useTemplateLoader();

    const shoppingListRef = useRef<string[]>([]);

    const updateDietData = useCallback((updates: Partial<ManualDietData>) => {
        setDietData(prev => ({...prev, ...updates}));
    }, []);

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
        if (currentStep === 'templateSelection') {
            return;
        } else if (currentStep === 'configuration') {
            if (!dietData.userId || !selectedUser) {
                toast.error('Wybierz użytkownika przed przejściem dalej');
                return;
            }
            initializeDays();
            setCurrentStep('planning');
        } else if (currentStep === 'planning') {
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
        if (currentStep === 'configuration') {
            setCurrentStep('templateSelection');
        } else if (currentStep === 'planning') {
            if (selectedTemplate) {
                setCurrentStep('templateSelection');
            } else {
                setCurrentStep('configuration');
            }
            setParsedPreviewData(null);
        }
    }, [currentStep]);

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
                setCurrentStep('configuration');
            } finally {
                setIsProcessing(false);
            }
        } else {
            setCurrentStep('configuration');
        }
    }, [selectedUser, dietData.startDate, loadTemplateIntoDiet]);

    const handleContinueWithoutTemplate = useCallback(() => {
        setSelectedTemplate(null);
        setCurrentStep('configuration');
    }, []);

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

            await ManualDietService.saveManualDiet(request);
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
            case 'templateSelection':
                return 'Wybierz gotowy szablon diety lub utwórz nową od zera';
            case 'configuration':
                return 'Ustaw podstawowe parametry diety';
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

            {currentStep === 'configuration' && (
                <div className="flex items-center space-x-4 mb-6">
                    <button
                        onClick={onBackToSelection}
                        className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                    >
                        <ArrowLeft className="h-4 w-4 mr-2"/>
                        Powrót do wyboru metody
                    </button>
                </div>
            )}

            <SectionHeader
                title={getStepTitle()}
                description={getStepDescription()}
            />

            {/* Guide */}
            <ManualDietGuide className="mb-6"/>

            {/* Progress bar */}
            <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                    className="bg-primary h-2 rounded-full transition-all duration-300"
                    style={{
                        width: currentStep === 'templateSelection' ? '20%' :
                            currentStep === 'configuration' ? '40%' :
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
                    <DietConfigurationStep
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
                            onRemoveTemplate={() => {
                                setSelectedTemplate(null);
                            }}
                            onUpdateMeal={updateMeal}
                            onAddIngredient={addIngredientToMeal}
                            onRemoveIngredient={removeIngredientFromMeal}
                        />

                        {/* Przycisk zapisywania szablonu */}
                        <div className="mt-6 flex justify-center">
                            <button
                                onClick={handleSaveAsTemplate}
                                className="flex items-center gap-2 px-4 py-2 bg-secondary text-white rounded-lg hover:bg-secondary-dark transition-colors"
                            >
                                <BookAudio className="h-4 w-4"/>
                                Zapisz jako szablon
                            </button>
                        </div>
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
            {currentStep !== 'categorization' && currentStep !== 'preview' && currentStep !== 'templateSelection' && (
                <div className="fixed bottom-6 right-6 flex gap-3 z-10">
                    <FloatingActionButtonGroup position="bottom-right">
                        {currentStep !== 'configuration' && (
                            <FloatingActionButton
                                label="Poprzedni krok"
                                onClick={handlePrevious}
                                variant="secondary"
                                icon={<ArrowLeft className="h-5 w-5"/>}
                            />
                        )}

                        <FloatingActionButton
                            label="Następny krok"
                            onClick={handleNext}
                            variant="primary"
                            icon={<ArrowRight className="h-5 w-5"/>}
                            isLoading={isProcessing && currentStep === 'planning'}
                            loadingLabel="Przygotowywanie..."
                        />
                    </FloatingActionButtonGroup>
                </div>
            )}

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
        </div>
    );
};

export default ManualDietCreator;