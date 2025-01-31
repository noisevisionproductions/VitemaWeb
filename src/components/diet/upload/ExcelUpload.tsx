import React, {useMemo, useState} from "react";
import {User} from "../../../types/user";
import {toast} from "sonner";
import UserSelector from "./UserSelector";
import FileUploadZone from "./FileUploadZone";
import {ExcelParserService} from "../../../services/ExcelParserService";
import {FirebaseService} from "../../../services/FirebaseService";
import {DietTemplate, MealType, ParsedDietData} from "../../../types/diet";
import DietTemplateConfig from "./DietTemplateConfig";
import DietPreview from "./DietPreview";
import {Timestamp} from "firebase/firestore";
import ValidationSection from "./validation/ValidationSection";
import debounce from "lodash/debounce";

interface ValidationState {
    isExcelStructureValid: boolean | null;
    isMealsPerDayValid: boolean;
    isDateValid: boolean;
    isMealsConfigValid: boolean;
}

const ExcelUpload: React.FC = () => {
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [file, setFile] = useState<File | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [previewData, setPreviewData] = useState<ParsedDietData | null>(null);
    const [totalMeals, setTotalMeals] = useState<number>(0);

    const [validationState, setValidationState] = useState<ValidationState>({
        isExcelStructureValid: null,
        isMealsPerDayValid: true,
        isDateValid: true,
        isMealsConfigValid: true
    });

    const handleFileSelect = async (file: File | null) => {
        setFile(null);
        setTotalMeals(0);

        if (!selectedUser) {
            toast.error('Najpierw wybierz użytkownika');
            return;
        }

        if (file) {
            try {
                const parsedExcel = await ExcelParserService.parseDietExcel(file);
                setTotalMeals(parsedExcel.totalMeals);
                setFile(file);
            } catch (error) {
                console.error('Error parsing file:', error);
                toast.error('Błąd podczas przetwarzania pliku');
            }
        }
    };

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
            toast.error('Wybierz użytkownika i plik przed wysłaniem');
            return;
        }

        setIsProcessing(true);
        try {
            const parsedExcel = await ExcelParserService.parseDietExcel(file);
            const parsedData = ExcelParserService.applyTemplate(parsedExcel, template);

            const dataWithTimestamps: ParsedDietData = {
                ...parsedData,
                days: parsedData.days.map(day => ({
                    ...day,
                    date: day.date
                }))
            };

            console.log('Data before setting preview:', dataWithTimestamps);
            setPreviewData(dataWithTimestamps);
        } catch (error) {
            console.error('Error parsing diet:', error);
            toast.error('Wystąpił błąd podczas przetwarzania pliku');
        } finally {
            setIsProcessing(false);
        }
    };

    const handleConfirm = async () => {
        if (!selectedUser || !file || !previewData) return;

        setIsProcessing(true);
        try {
            const fileUrl = await FirebaseService.uploadExcelFile(file, selectedUser.id);

            const safePreviewData = {
                ...previewData,
                days: previewData.days.map(day => ({
                    ...day,
                    meals: day.meals.map(meal => ({
                        ...meal,
                        nutritionalValues: meal.nutritionalValues || {
                            calories: 0,
                            protein: 0,
                            fat: 0,
                            carbs: 0
                        }
                    }))
                }))
            };

            await FirebaseService.saveDietWithShoppingList(
                safePreviewData,
                selectedUser.id,
                {
                    fileName: file.name,
                    fileUrl
                }
            );

            toast.success('Dieta została pomyślnie zapisana');
            setFile(null);
            setPreviewData(null);
        } catch (error) {
            console.error('Error saving diet:', error);
            toast.error('Wystąpił błąd podczas zapisywania diety');
        } finally {
            setIsProcessing(false);
        }
    };

    const isValidationPassed = useMemo(() => {
        if (!file) return false;

        return Object.entries(validationState).every(([key, value]) => {
            if (key === 'isExcelStructureValid' && !file) return true;
            return value === true;
        });
    }, [validationState, file]);

    const updateValidationState = useMemo(() =>
            debounce((field: keyof ValidationState, value: boolean) => {
                setValidationState(prev => {
                    if (prev[field] === value) return prev;
                    return { ...prev, [field]: value };
                });
            }, 300),
        []
    );

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
                    onValidationChange={{
                        onExcelStructureValidation: (valid) =>
                            updateValidationState('isExcelStructureValid', valid),
                        onMealsPerDayValidation: (valid) =>
                            updateValidationState('isMealsPerDayValid', valid),
                        onDateValidation: (valid) =>
                            updateValidationState('isDateValid', valid),
                        onMealsConfigValidation: (valid) =>
                            updateValidationState('isMealsConfigValid', valid)
                    }}
                />
            )}

            <DietTemplateConfig
                template={template}
                onTemplateChange={setTemplate}
            />

            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-medium mb-4"> Wybierz użytkownika</h3>
                <UserSelector
                    selectedUser={selectedUser}
                    onUserSelect={setSelectedUser}
                />
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
                <h3 className="text-lg font-medium mb-4">Upload pliku Excel</h3>
                <FileUploadZone
                    file={file}
                    onFileSelect={handleFileSelect}
                    disabled={!selectedUser}
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