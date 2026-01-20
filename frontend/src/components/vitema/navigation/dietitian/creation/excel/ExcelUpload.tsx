import React, {useMemo, useState, useCallback, useRef} from "react";
import {User} from "../../../../../../types/user";
import {toast} from "../../../../../../utils/toast";
import {DietUploadService} from "../../../../../../services/diet/DietUploadService";
import DietPreview from "../../../../diet/upload/preview/DietPreview";
import {Timestamp} from "firebase/firestore";
import ValidationSection from "../../../../diet/upload/validation/ValidationSection";
import {DietExcelTemplate, MealType, ParsedDietData} from "../../../../../../types";
import {MainNav} from "../../../../../../types/navigation";
import {ValidationErrorType} from "../../../../diet/upload/validation/ValidationMessage";
import SectionHeader from "../../../../../shared/common/SectionHeader";
import {AxiosError} from "axios";
import UserSelectionSection from "../../../../diet/upload/sections/user/UserSelectionSection";
import FileUploadSection from "../../../../diet/upload/sections/file/FileUploadSection";
import UploadActionSection from "../../../../diet/upload/sections/UploadActionSection";
import DietTemplateConfig from "../../../../diet/upload/sections/DietTemplateConfig";
import ExcelParserSettings from "../../../../diet/upload/sections/ExcelParserSettings";
import {useSettings} from "../../../../../../contexts/SettingsContextType";

interface ValidationState {
    isExcelStructureValid: boolean;
    isMealsPerDayValid: boolean;
    isDateValid: boolean;
    isMealsConfigValid: boolean;
    isCalorieValid?: boolean;
}

interface ExcelUploadProps {
    onTabChange: (tab: MainNav) => void;
}

const ExcelUpload: React.FC<ExcelUploadProps> = ({onTabChange}) => {
    // Stan użytkownika i pliku
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [file, setFile] = useState<File | null>(null);

    // Stan przetwarzania i danych
    const [isProcessing, setIsProcessing] = useState(false);
    const [previewData, setPreviewData] = useState<ParsedDietData | null>(null);
    const [totalMeals, setTotalMeals] = useState<number>(0);

    const {
        skipColumnsCount,
        setSkipColumnsCount,
        isCalorieValidationEnabled,
        setIsCalorieValidationEnabled,
        targetCalories,
        setTargetCalories
    } = useSettings();

    const [calorieValidationResult, setCalorieValidationResult] = useState<{
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    } | undefined>(undefined);

    // Stan walidacji
    const [validationState, setValidationState] = useState<ValidationState>({
        isExcelStructureValid: false,
        isMealsPerDayValid: false,
        isDateValid: false,
        isMealsConfigValid: false,
        isCalorieValid: true
    });

    // Referencje do sekcji (do nawigacji)
    const fileUploadRef = useRef<HTMLDivElement>(null);
    const userSelectorRef = useRef<HTMLDivElement>(null);
    const templateConfigRef = useRef<HTMLDivElement>(null);
    const mealsPerDayRef = useRef<HTMLDivElement>(null);
    const dateConfigRef = useRef<HTMLDivElement>(null);
    const mealsConfigRef = useRef<HTMLDivElement>(null);
    const calorieValidationRef = useRef<HTMLDivElement>(null);

    // Inicjalizacja szablonu diety
    const [template, setTemplate] = useState<DietExcelTemplate>({
        mealsPerDay: 5,
        startDate: Timestamp.fromDate(new Date()),
        duration: 7,
        mealTimes: {
            meal_0: '08:00',
            meal_1: '11:00',
            meal_2: '14:00',
            meal_3: '16:30',
            meal_4: '19:00',
        },
        mealTypes: [
            MealType.BREAKFAST,
            MealType.SECOND_BREAKFAST,
            MealType.LUNCH,
            MealType.SNACK,
            MealType.DINNER
        ]
    });

    // Obsługa wyboru pliku
    const handleFileSelect = useCallback(async (newFile: File | null) => {
        if (!newFile) {
            setFile(null);
            setTotalMeals(0);
            setValidationState({
                isExcelStructureValid: false,
                isMealsPerDayValid: false,
                isDateValid: false,
                isMealsConfigValid: false,
                isCalorieValid: true
            });
            setCalorieValidationResult(undefined);
            return;
        }

        if (!selectedUser) {
            toast.error('Najpierw wybierz użytkownika');
            return;
        }

        setFile(newFile);
    }, [selectedUser]);

    // Obsługa zmiany szablonu diety
    const handleTemplateChange = useCallback((newTemplate: DietExcelTemplate) => {
        setTemplate(newTemplate);
    }, []);

    // Nawigacja do sekcji z błędem
    const navigateToSection = useCallback((errorType: ValidationErrorType) => {
        let targetRef: React.RefObject<HTMLDivElement> | null = null;

        switch (errorType) {
            case 'excel-structure':
                targetRef = fileUploadRef;
                break;
            case 'meals-per-day':
                targetRef = mealsPerDayRef;
                break;
            case 'date':
            case 'diet-overlap':
                targetRef = dateConfigRef;
                break;
            case 'meals-config':
                targetRef = mealsConfigRef;
                break;
            case 'parser-settings':
                targetRef = fileUploadRef;
                break;
            case 'calorie-validation':
                targetRef = calorieValidationRef;
                break;
            default:
                targetRef = templateConfigRef;
                break;
        }

        if (targetRef && targetRef.current) {
            targetRef.current.scrollIntoView({behavior: 'smooth'});

            // Wyróżnij sekcję przez chwilę
            targetRef.current.classList.add('ring-2', 'ring-blue-500', 'ring-opacity-50', 'transition-all');
            setTimeout(() => {
                targetRef.current?.classList.remove('ring-2', 'ring-blue-500', 'ring-opacity-50');
            }, 3000);

            // Fokusuj odpowiednie pole, jeśli możliwe
            const inputSelector = getInputSelectorForType(errorType);
            const input = inputSelector ? targetRef.current.querySelector(inputSelector) : null;

            if (input instanceof HTMLElement) {
                setTimeout(() => {
                    input.focus();
                    input.classList.add('ring-2', 'ring-blue-500', 'ring-opacity-50');
                    setTimeout(() => {
                        input.classList.remove('ring-2', 'ring-blue-500', 'ring-opacity-50');
                    }, 3000);
                }, 500);
            }
        }
    }, []);

    // Helper dla nawigacji
    const getInputSelectorForType = (errorType: ValidationErrorType): string | null => {
        switch (errorType) {
            case 'date':
            case 'diet-overlap':
                return 'input[type="date"]';
            case 'meals-per-day':
                return 'input[name="mealsPerDay"]';
            case 'meals-config':
                return 'input[type="time"]';
            case 'excel-structure':
                return 'input[type="file"]';
            case 'parser-settings':
                return 'input[id="skipColumnsCount"]';
            case 'calorie-validation':
                return 'input[id="targetCalories"]';
            default:
                return null;
        }
    };

    const validateCalories = useCallback(async () => {
        if (!file || !isCalorieValidationEnabled) {
            setCalorieValidationResult(undefined);
            setValidationState(prev => ({...prev, isCalorieValid: true}));
            return;
        }

        try {
            // Przygotowanie parametrów walidacji kalorii
            const extraParams = {
                calorieValidationEnabled: true,
                targetCalories: targetCalories,
                calorieErrorMargin: 5 // Stały margines błędu
            };

            // Walidacja z dodatkowymi parametrami
            const validationResponse = await DietUploadService.validateDietTemplateWithUser(
                file,
                template,
                selectedUser?.id,
                skipColumnsCount,
                extraParams
            );

            // Znajdź wyniki walidacji kalorii
            const calorieResult = validationResponse.validationResults.find(
                result => result.message.toLowerCase().includes('kalori')
            );

            if (calorieResult) {
                setCalorieValidationResult({
                    isValid: calorieResult.isValid,
                    message: calorieResult.message,
                    severity: calorieResult.isValid ? 'success' : 'error'
                });
                setValidationState(prev => ({...prev, isCalorieValid: calorieResult.isValid}));
            } else {
                // Jeśli nie ma wyników walidacji kalorii, ustaw domyślne wartości
                setCalorieValidationResult(undefined);
                setValidationState(prev => ({...prev, isCalorieValid: true}));
            }
        } catch (error) {
            console.error('Błąd podczas walidacji kalorii:', error);
            setCalorieValidationResult({
                isValid: false,
                message: 'Wystąpił błąd podczas walidacji kalorii',
                severity: 'error'
            });
            setValidationState(prev => ({...prev, isCalorieValid: false}));
        }
    }, [file, isCalorieValidationEnabled, targetCalories, template, selectedUser, skipColumnsCount]);

    const isValidationPassed = useMemo(() => {
        const basicValidation = file &&
            selectedUser &&
            validationState.isExcelStructureValid &&
            validationState.isMealsPerDayValid &&
            validationState.isDateValid &&
            validationState.isMealsConfigValid;

        if (isCalorieValidationEnabled) {
            return basicValidation && validationState.isCalorieValid;
        }

        return basicValidation;
    }, [file, selectedUser, validationState, isCalorieValidationEnabled]);

    // Funkcje aktualizujące stan walidacji
    const updateExcelStructureValidation = useCallback((valid: boolean) => {
        setValidationState(prev => ({...prev, isExcelStructureValid: valid}));
    }, []);

    const updateMealsPerDayValidation = useCallback((valid: boolean) => {
        setValidationState(prev => ({...prev, isMealsPerDayValid: valid}));
    }, []);

    const updateDateValidation = useCallback((valid: boolean) => {
        setValidationState(prev => ({...prev, isDateValid: valid}));
    }, []);

    const updateMealsConfigValidation = useCallback((valid: boolean) => {
        setValidationState(prev => ({...prev, isMealsConfigValid: valid}));
    }, []);

    const updateCalorieValidation = useCallback((valid: boolean) => {
        setValidationState(prev => ({...prev, isCalorieValid: valid}));
    }, []);

    // Obsługa wyniku walidacji kalorii
    const handleCalorieValidationResult = useCallback((result?: {
        isValid: boolean;
        message: string;
        severity: 'error' | 'warning' | 'success';
    }) => {
        setCalorieValidationResult(result);
    }, []);

    // Akcja wysłania diety
    const handleUpload = async () => {
        if (!selectedUser || !file) {
            toast.error('Wybierz użytkownika i plik');
            return;
        }

        if (!isValidationPassed) {
            toast.error('Popraw błędy walidacji przed wysłaniem');
            return;
        }

        if (isProcessing) {
            return;
        }

        setIsProcessing(true);

        try {
            // Przygotowanie parametrów walidacji kalorii
            const extraParams: any = {};
            if (isCalorieValidationEnabled) {
                extraParams.calorieValidationEnabled = true;
                extraParams.targetCalories = targetCalories;
                extraParams.calorieErrorMargin = 5; // Stały margines błędu
            }

            // Podgląd diety
            const previewData = await DietUploadService.previewDiet(
                file,
                template,
                skipColumnsCount,
                extraParams
            );

            if (!previewData || !previewData.days || !Array.isArray(previewData.days)) {
                toast.error('Nieprawidłowy format danych z pliku Excel');
                return;
            }

            const sanitizedPreviewData: ParsedDietData = {
                days: previewData.days.map(day => ({
                    ...day,
                    date: day.date,
                    meals: Array.isArray(day.meals)
                        ? day.meals.map(meal => ({
                            ...meal,
                            time: meal.time || template.mealTimes[`meal_${template.mealTypes.indexOf(meal.mealType)}`] || '12:00'
                        }))
                        : []
                })),
                shoppingList: Array.isArray(previewData.shoppingList) ? previewData.shoppingList : [],
                categorizedProducts: previewData.categorizedProducts || {},
                mealTimes: {...template.mealTimes},
                mealsPerDay: template.mealsPerDay,
                startDate: template.startDate,
                duration: template.duration,
                mealTypes: [...template.mealTypes],
                calorieAnalysis: previewData.calorieAnalysis
            };

            if (sanitizedPreviewData.days.length === 0) {
                toast.error('Brak danych o posiłkach w pliku');
                return;
            }

            if (sanitizedPreviewData.days.length !== template.duration) {
                toast.warning(`Liczba dni w pliku (${sanitizedPreviewData.days.length}) różni się od zadeklarowanej (${template.duration})`);
            }

            setPreviewData(sanitizedPreviewData);

        } catch (error: unknown) {
            console.error('Error parsing diet:', error);

            if (error instanceof AxiosError) {
                const errorMessage = error.response?.data?.message ||
                    error.response?.data?.error ||
                    'Wystąpił nieoczekiwany błąd';
                toast.error(errorMessage);
            } else if (error instanceof Error) {
                toast.error(error.message);
            } else {
                toast.error('Wystąpił błąd podczas przetwarzania pliku');
            }
        } finally {
            setIsProcessing(false);
        }
    };

    // Akcja potwierdzenia podglądu diety
    const handleConfirm = async () => {
        if (!selectedUser || !file || !previewData) {
            toast.error('Brakuje wymaganych danych');
            return;
        }

        setIsProcessing(true);
        try {
            const sanitizedData: ParsedDietData = {
                days: previewData.days.map(day => ({
                    ...day,
                    date: day.date
                })),
                shoppingList: Array.isArray(previewData.shoppingList) ? previewData.shoppingList : [],
                categorizedProducts: previewData.categorizedProducts || {},
                mealTimes: previewData.mealTimes || template.mealTimes,
                mealsPerDay: previewData.mealsPerDay || template.mealsPerDay,
                startDate: previewData.startDate || template.startDate,
                duration: previewData.duration || template.duration,
                mealTypes: previewData.mealTypes || template.mealTypes,
                calorieAnalysis: previewData.calorieAnalysis
            };

            await DietUploadService.uploadDiet(
                file,
                selectedUser.id,
                sanitizedData
            );

            toast.success('Dieta została pomyślnie zapisana');
            setFile(null);
            setPreviewData(null);

            onTabChange('diets');
        } catch (error) {
            console.error('Error saving diet:', error);
            toast.error(typeof error === 'string' ? error : 'Wystąpił błąd podczas zapisywania diety');
        } finally {
            setIsProcessing(false);
        }
    };

    // Jeśli mamy dane podglądu, renderujemy komponent podglądu
    if (previewData) {
        return (
            <DietPreview
                parsedData={previewData}
                onConfirm={handleConfirm}
                onCancel={() => setPreviewData(null)}
                selectedUserEmail={selectedUser?.email || ''}
                fileName={file?.name}
                skipCategorization={false}
            />
        );
    }

    return (
        <div className="space-y-6 pb-8">
            <SectionHeader
                title="Tworzenie diety"
                description="Dodaj dietę za pomocą arkusza excel"
            />

            {file && (
                <ValidationSection
                    file={file}
                    template={template}
                    totalMeals={totalMeals}
                    userId={selectedUser?.id}
                    skipColumnsCount={skipColumnsCount}
                    isCalorieValidationEnabled={isCalorieValidationEnabled}
                    targetCalories={targetCalories}
                    onValidationChange={{
                        onExcelStructureValidation: updateExcelStructureValidation,
                        onMealsPerDayValidation: updateMealsPerDayValidation,
                        onDateValidation: updateDateValidation,
                        onMealsConfigValidation: updateMealsConfigValidation,
                        onCalorieValidation: updateCalorieValidation
                    }}
                    onNavigate={navigateToSection}
                    onCalorieValidationResult={handleCalorieValidationResult}
                />
            )}

            <UserSelectionSection
                selectedUser={selectedUser}
                onUserSelect={setSelectedUser}
                sectionRef={userSelectorRef}
            />

            <ExcelParserSettings
                skipColumnsCount={skipColumnsCount}
                onSkipColumnsCountChange={setSkipColumnsCount}
                isCalorieValidationEnabled={isCalorieValidationEnabled}
                onCalorieValidationEnabledChange={setIsCalorieValidationEnabled}
                targetCalories={targetCalories}
                onTargetCaloriesChange={setTargetCalories}
                calorieValidationResult={calorieValidationResult}
                onValidate={validateCalories}
                sectionRef={calorieValidationRef}
            />

            <div ref={templateConfigRef}>
                <DietTemplateConfig
                    template={template}
                    onTemplateChange={handleTemplateChange}
                    refs={{
                        mealsPerDayRef,
                        dateConfigRef,
                        mealsConfigRef
                    }}
                />
            </div>

            <FileUploadSection
                file={file}
                onFileSelect={handleFileSelect}
                disabled={!selectedUser}
                sectionRef={fileUploadRef}
            />

            <UploadActionSection
                onUpload={handleUpload}
                isDisabled={!selectedUser || !file || !isValidationPassed}
                isProcessing={isProcessing}
            />
        </div>
    );
};

export default ExcelUpload;