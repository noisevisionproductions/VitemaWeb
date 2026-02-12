import React, {useCallback, useState} from "react";
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
import DietCreatorGuide from "./components/DietCreatorGuide";
import {CreateDietTemplateRequest, DietTemplate} from "../../../../types/DietTemplate";
import CreateTemplateDialog from "../templates/CreateTemplateDialog";
import {useTemplateLoader} from "../../../../hooks/diet/templates/useTemplateLoader";
import TemplateSelectionStep from "./steps/TemplateSelectionStep";
import {useAuth} from "../../../../contexts/AuthContext";
import type {DietDayDto, DietMealDto, DietIngredientDto} from "../../../../types";

interface DietCreatorProps {
    onTabChange: (tab: MainNav) => void;
    onBackToSelection: () => void;
}

type Step = 'templateSelection' | 'configuration' | 'planning' | 'preview';

/** Map backend draft ingredient to ParsedProduct for planner. */
function draftIngredientToParsedProduct(i: DietIngredientDto): ParsedProduct {
    return {
        name: i.name,
        quantity: i.quantity,
        unit: i.unit,
        original: i.name,
        categoryId: i.categoryId ?? undefined,
        id: i.productId ?? undefined,
    };
}

/** Map backend draft meal to ParsedMeal for planner. */
function draftMealToParsedMeal(m: DietMealDto): ParsedMeal {
    return {
        name: m.name,
        instructions: m.instructions ?? '',
        ingredients: (m.ingredients ?? []).map(draftIngredientToParsedProduct),
        nutritionalValues: m.nutritionalValues ?? undefined,
        mealType: m.mealType as MealType,
        time: m.time ?? '12:00',
        photos: [],
        originalRecipeId: m.originalRecipeId ?? undefined,
    };
}

const DietCreator: React.FC<DietCreatorProps> = ({
                                                     onTabChange
                                                 }) => {
    const {currentUser} = useAuth();
    const trainerId = currentUser?.uid ?? null;

    const [currentStep, setCurrentStep] = useState<Step>('configuration');
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [dietData, setDietData] = useState<ManualDietData>({
        userId: '',
        ...DEFAULT_DIET_CONFIG,
        days: []
    });
    const [parsedPreviewData, setParsedPreviewData] = useState<ParsedDietData | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const [showSaveAsTemplate, setShowSaveAsTemplate] = useState(false);
    const [showBackConfirmation, setShowBackConfirmation] = useState(false);
    const [initialStateSnapshot, setInitialStateSnapshot] = useState<string>('');

    const [templateData, setTemplateData] = useState<CreateDietTemplateRequest | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<DietTemplate | null>(null);
    const {loadTemplateIntoDiet, loading: templateLoading} = useTemplateLoader();

    const updateDietData = useCallback((updates: Partial<ManualDietData>) => {
        setDietData(prev => {
            if (updates.startDate && prev.days.length > 0 && updates.startDate !== prev.startDate) {
                const newStart = new Date(updates.startDate);
                const newDays = prev.days.map((day, index) => {
                    const newDate = new Date(newStart);
                    newDate.setDate(newStart.getDate() + index);
                    return {
                        ...day,
                        date: Timestamp.fromDate(newDate)
                    };
                });
                return {...prev, ...updates, days: newDays};
            }

            return {...prev, ...updates};
        });
    }, []);

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

    const updateIngredientInMeal = useCallback((dayIndex: number, mealIndex: number, ingredientIndex: number, updatedIngredient: ParsedProduct) => {
        setDietData(prev => {
            const newDays = [...prev.days];
            const meal = newDays[dayIndex].meals[mealIndex];

            const newIngredients = [...(meal.ingredients || [])];
            newIngredients[ingredientIndex] = updatedIngredient;

            newDays[dayIndex].meals[mealIndex] = {
                ...meal,
                ingredients: newIngredients
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

    const convertToPreviewData = useCallback((): ParsedDietData => {
        const aggregatedIngredients: Record<string, { name: string; quantity: number; unit: string }> = {};

        dietData.days.forEach(day => {
            day.meals.forEach(meal => {
                if (meal.ingredients) {
                    meal.ingredients.forEach(ing => {
                        const key = `${ing.name.trim().toLowerCase()}_${ing.unit.trim().toLowerCase()}`;

                        if (aggregatedIngredients[key]) {
                            aggregatedIngredients[key].quantity += (ing.quantity || 0);
                        } else {
                            aggregatedIngredients[key] = {
                                name: ing.name,
                                quantity: ing.quantity || 0,
                                unit: ing.unit
                            };
                        }
                    });
                }
            });
        });

        const shoppingListStrings: string[] = Object.values(aggregatedIngredients)
            .sort((a, b) => a.name.localeCompare(b.name))
            .map(p => `${p.name} - ${parseFloat(p.quantity.toFixed(2))} ${p.unit}`);

        const previewCategorizedProducts: Record<string, string[]> = {
            "Produkty (podgląd)": shoppingListStrings
        };

        return {
            days: dietData.days,
            categorizedProducts: previewCategorizedProducts,
            shoppingList: shoppingListStrings,
            mealTimes: dietData.mealTimes,
            mealsPerDay: dietData.mealsPerDay,
            startDate: Timestamp.fromDate(new Date(dietData.startDate)),
            duration: dietData.duration,
            mealTypes: dietData.mealTypes
        };
    }, [dietData]);

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
            if (selectedTemplate) {
                await handleTemplateSelect(selectedTemplate);
            } else {
                handleContinueWithoutTemplate();
            }
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

            // Shopping list is generated automatically on the backend - proceed directly to preview
            const previewData = convertToPreviewData();
            setParsedPreviewData(previewData);
            setCurrentStep('preview');
        }
    }, [currentStep, dietData, selectedUser, selectedTemplate, convertToPreviewData]);

    const handlePrevious = useCallback(() => {
        if (currentStep === 'templateSelection') {
            setCurrentStep('configuration');
        } else if (currentStep === 'planning') {
            const currentSnapshot = JSON.stringify(dietData);
            const hasRealChanges = currentSnapshot !== initialStateSnapshot;

            if (hasRealChanges) {
                setShowBackConfirmation(true);
            } else {
                setCurrentStep('templateSelection');
                setParsedPreviewData(null);
            }
        }
    }, [currentStep, dietData, initialStateSnapshot]);

    const handleDiscardAndBack = () => {
        setShowBackConfirmation(false);
        setCurrentStep('templateSelection');
        setParsedPreviewData(null);
    };

    const handleSaveFromConfirmation = () => {
        setShowBackConfirmation(false);
        handleSaveAsTemplate();
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

                setInitialStateSnapshot(JSON.stringify(loadedDietData));

                setCurrentStep('planning');
            } catch (error) {
                console.error('Error loading template:', error);
                toast.error('Wystąpił błąd podczas ładowania szablonu');
            } finally {
                setIsProcessing(false);
            }
        } else {
        }
    }, [selectedUser, dietData.startDate, loadTemplateIntoDiet]);

    const handleContinueWithoutTemplate = useCallback(() => {
        setSelectedTemplate(null);

        const startDate = new Date(dietData.startDate);
        const days: DayData[] = [];

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
            days.push({date: Timestamp.fromDate(currentDate), meals});
        }

        const newDietData = {...dietData, days};

        setDietData(newDietData);
        setInitialStateSnapshot(JSON.stringify(newDietData));
        setCurrentStep('planning');
    }, [dietData]);

    const handleHistoryItemSelect = useCallback(async (dietId: string) => {
        if (!selectedUser) {
            toast.error('Wybierz klienta przed załadowaniem diety z historii');
            return;
        }

        try {
            setIsProcessing(true);
            const draft = await DietCreatorService.loadDietDraft(dietId);
            if (!draft.days?.length) {
                toast.error('Ta dieta nie zawiera dni.');
                return;
            }
            const firstDay = draft.days[0];
            const mealsPerDay = firstDay.meals?.length ?? dietData.mealsPerDay;
            const duration = draft.days.length;
            const mealTimes: Record<string, string> = {};
            const mealTypes: MealType[] = [];
            firstDay.meals?.forEach((meal, idx) => {
                mealTimes[`meal_${idx}`] = meal.time ?? '12:00';
                mealTypes.push((meal.mealType as MealType) ?? MealType.LUNCH);
            });
            const startDate = dietData.startDate || new Date().toISOString().slice(0, 10);
            const start = new Date(startDate);
            const days: DayData[] = draft.days.map((day: DietDayDto, i: number) => {
                const dayDate = new Date(start);
                dayDate.setDate(start.getDate() + i);
                return {
                    date: Timestamp.fromDate(dayDate),
                    meals: (day.meals ?? []).map(draftMealToParsedMeal),
                };
            });
            const loadedData = {
                userId: selectedUser.id,
                mealsPerDay,
                startDate,
                duration,
                mealTimes: Object.keys(mealTimes).length ? mealTimes : dietData.mealTimes,
                mealTypes: mealTypes.length ? mealTypes : dietData.mealTypes,
                days,
            };

            setDietData(loadedData);
            setInitialStateSnapshot(JSON.stringify(loadedData));
            setSelectedTemplate(null);
            setCurrentStep('planning');
        } catch (error) {
            console.error('Error loading diet from history', error);
            toast.error('Nie udało się załadować diety z historii');
        } finally {
            setIsProcessing(false);
        }
    }, [selectedUser, dietData]);

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
        setCurrentStep('planning');
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
        <div className="space-y-6 pb-32 relative">
            <SectionHeader
                title={getStepTitle()}
                description={getStepDescription()}
            />

            {/* Guide */}
            <DietCreatorGuide className="mb-6"/>

            {/* Progress bar - 4 steps: configuration (25%) -> templateSelection (50%) -> planning (75%) -> preview (100%) */}
            <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                    className="bg-primary h-2 rounded-full transition-all duration-300"
                    style={{
                        width: currentStep === 'configuration' ? '25%' :
                            currentStep === 'templateSelection' ? '50%' :
                                currentStep === 'planning' ? '75%' : '100%'
                    }}
                />
            </div>

            {/* Step content */}
            <div className="min-h-[500px]">
                {currentStep === 'templateSelection' && (
                    <TemplateSelectionStep
                        onTemplateSelect={setSelectedTemplate}
                        onContinueWithoutTemplate={handleContinueWithoutTemplate}
                        onHistoryItemSelect={handleHistoryItemSelect}
                        selectedUser={selectedUser}
                        trainerId={trainerId}
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
                            onUpdateIngredient={updateIngredientInMeal}
                            trainerId={trainerId || undefined}
                        />
                    </div>
                )}
            </div>

            {/* Navigation buttons */}
            {currentStep !== 'preview' && (
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

                        {currentStep === 'planning' && (
                            <FloatingActionButton
                                label="Zapisz jako szablon"
                                onClick={handleSaveAsTemplate}
                                variant="secondary"
                                icon={<Save className="h-5 w-5"/>}
                                className="border-emerald-200 text-emerald-700 hover:bg-emerald-50 hover:border-emerald-300"
                            />
                        )}

                        <FloatingActionButton
                            label={
                                currentStep === 'templateSelection' && selectedTemplate
                                    ? `Użyj szablon "${selectedTemplate.name}"`
                                    : "Następny krok"
                            }
                            onClick={handleNext}
                            variant="primary"
                            icon={<ArrowRight className="h-5 w-5"/>}
                            isLoading={isProcessing}
                            loadingLabel="Przygotowywanie..."
                        />
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