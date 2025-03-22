import React, {useMemo, useState, useCallback, useRef} from "react";
import {User} from "../../../types/user";
import {toast} from "../../../utils/toast";
import UserSelector from "./UserSelector";
import FileUploadZone from "./FileUploadZone";
import {DietUploadService} from "../../../services/diet/DietUploadService";
import DietTemplateConfig from "./DietTemplateConfig";
import DietPreview from "./preview/DietPreview";
import {Timestamp} from "firebase/firestore";
import ValidationSection from "./validation/ValidationSection";
import {DietTemplate, MealType, ParsedDietData} from "../../../types";
import {MainNav} from "../../../types/navigation";
import {ChevronDown, ChevronUp, UserCircle} from "lucide-react";
import {AxiosError} from 'axios';
import {ValidationErrorType} from "./validation/ValidationMessage";

interface ValidationState {
    isExcelStructureValid: boolean;
    isMealsPerDayValid: boolean;
    isDateValid: boolean;
    isMealsConfigValid: boolean;
}

interface ExcelUploadProps {
    onTabChange: (tab: MainNav) => void;
}

const ExcelUpload: React.FC<ExcelUploadProps> = ({onTabChange}) => {
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [file, setFile] = useState<File | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [previewData, setPreviewData] = useState<ParsedDietData | null>(null);
    const [totalMeals, setTotalMeals] = useState<number>(0);
    const [isUserSelectorExpanded, setIsUserSelectorExpanded] = useState(true);

    const [validationState, setValidationState] = useState<ValidationState>({
        isExcelStructureValid: false,
        isMealsPerDayValid: false,
        isDateValid: false,
        isMealsConfigValid: false
    });

    const handleFileSelect = useCallback(async (newFile: File | null) => {
        if (!newFile) {
            setFile(null);
            setTotalMeals(0);
            setValidationState({
                isExcelStructureValid: false,
                isMealsPerDayValid: false,
                isDateValid: false,
                isMealsConfigValid: false
            });
            return;
        }

        if (!selectedUser) {
            toast.error('Najpierw wybierz użytkownika');
            return;
        }

        setFile(newFile);
    }, [selectedUser]);

    const [template, setTemplate] = useState<DietTemplate>({
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
            const validationResponse = await DietUploadService.validateDietTemplateWithUser(file, template, selectedUser?.id);

            if (!validationResponse.valid) {
                const errorMessages = validationResponse.validationResults
                    .filter(result => !result.isValid)
                    .map(result => result.message)
                    .join(', ');
                toast.error(`Błąd walidacji: ${errorMessages}`);
                return;
            }

            const previewData = await DietUploadService.previewDiet(
                file,
                template
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
                mealTypes: [...template.mealTypes]
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
                mealTypes: previewData.mealTypes || template.mealTypes
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

    const handleTemplateChange = useCallback((newTemplate: DietTemplate) => {
        setTemplate(newTemplate);
    }, []);

    const fileUploadRef = useRef<HTMLDivElement>(null);
    const userSelectorRef = useRef<HTMLDivElement>(null);
    const templateConfigRef = useRef<HTMLDivElement>(null);
    const mealsPerDayRef = useRef<HTMLDivElement>(null);
    const dateConfigRef = useRef<HTMLDivElement>(null);
    const mealsConfigRef = useRef<HTMLDivElement>(null);

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
            default:
                return null;
        }
    };

    const isValidationPassed = useMemo(() => {
        return file &&
            selectedUser &&
            validationState.isExcelStructureValid &&
            validationState.isMealsPerDayValid &&
            validationState.isDateValid &&
            validationState.isMealsConfigValid;
    }, [file, selectedUser, validationState]);

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

    if (previewData) {
        return (
            <DietPreview
                parsedData={previewData}
                onConfirm={handleConfirm}
                onCancel={() => setPreviewData(null)}
                selectedUserEmail={selectedUser?.email || ''}
            />
        );
    }

    return (
        <div className="space-y-8">
            {file && (
                <ValidationSection
                    file={file}
                    template={template}
                    totalMeals={totalMeals}
                    userId={selectedUser?.id}
                    onValidationChange={{
                        onExcelStructureValidation: updateExcelStructureValidation,
                        onMealsPerDayValidation: updateMealsPerDayValidation,
                        onDateValidation: updateDateValidation,
                        onMealsConfigValidation: updateMealsConfigValidation
                    }}
                    onNavigate={navigateToSection}
                />
            )}

            <div ref={userSelectorRef} className="bg-white p-6 rounded-lg shadow-sm">
                <div
                    className={`flex items-center justify-between ${selectedUser ? 'bg-blue-50 p-3 rounded-lg transition-colors' : ''} cursor-pointer`}
                    onClick={() => setIsUserSelectorExpanded(!isUserSelectorExpanded)}
                >
                    <div className="flex items-center">
                        <h3 className="text-lg font-medium">
                            Wybierz użytkownika
                        </h3>
                        {selectedUser && (
                            <div className="flex items-center ml-3 font-medium text-blue-600">
                                <UserCircle className="h-5 w-5 mr-1"/>
                                <span>{selectedUser.email}</span>
                            </div>
                        )}
                    </div>
                    <button
                        type="button"
                        className="text-gray-500 hover:text-gray-700 focus:outline-none"
                        aria-label={isUserSelectorExpanded ? "Zwiń listę użytkowników" : "Rozwiń listę użytkowników"}
                    >
                        {isUserSelectorExpanded ? (
                            <ChevronUp className="h-5 w-5"/>
                        ) : (
                            <ChevronDown className="h-5 w-5"/>
                        )}
                    </button>
                </div>

                {isUserSelectorExpanded ? (
                    <div className="mt-4">
                        <UserSelector
                            selectedUser={selectedUser}
                            onUserSelect={(user) => {
                                setSelectedUser(user);
                                setIsUserSelectorExpanded(false);
                            }}
                        />
                    </div>
                ) : selectedUser && (
                    <div className="mt-2 text-sm text-gray-500 pl-3">
                        Kliknij powyżej, aby zmienić użytkownika
                    </div>
                )}
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-medium mb-4">Upload pliku Excel</h3>
                <FileUploadZone
                    file={file}
                    onFileSelect={handleFileSelect}
                    disabled={!selectedUser}
                />
            </div>

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

            <div className="flex justify-end">
                <button
                    onClick={handleUpload}
                    disabled={!selectedUser || !file || !isValidationPassed || isProcessing}
                    className={`px-4 py-2 rounded-lg ${
                        !selectedUser || !file || !isValidationPassed || isProcessing
                            ? 'bg-gray-300 cursor-not-allowed'
                            : 'bg-blue-500 hover:bg-blue-600 text-white'
                    }`}
                >
                    {isProcessing ? 'Przetwarzanie...' : 'Wyślij'}
                </button>
            </div>
        </div>
    );
};

export default ExcelUpload;