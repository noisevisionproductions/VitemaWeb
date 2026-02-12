import React, {useState} from "react";
import {Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle} from "../../../shared/ui/Sheet";
import LoadingSpinner from "../../../shared/common/LoadingSpinner";
import {formatTimestamp, timestampToDate} from "../../../../utils/dateFormatters";
import {Diet} from "../../../../types";
import {useRecipes} from "../../../../hooks/useRecipes";
import {getMealTypeLabel} from "../../../../utils/diet/mealTypeUtils";
import {
    AlertTriangle,
    CalendarDays,
    CheckCircle2,
    Clock,
    FileText,
    Flame,
    Info,
    ShoppingBasket,
    Utensils,
    XCircle
} from "lucide-react";
import DietShoppingList from "../shopping/DietShoppingList";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "../../../shared/ui/Tabs";
import {
    getDaysRemainingToDietEnd,
    getDietWarningStatus,
    getWarningStatusText,
    isDietEnded
} from "../../../../utils/diet/dietWarningUtils";
import {checkFutureDiets} from "../../../../utils/diet/dietContinuityUtils";

interface DietViewProps {
    diet: Diet;
    allDiets?: Diet[];
    onClose: () => void;
}

const DietView: React.FC<DietViewProps> = ({diet, allDiets, onClose}) => {
    const {recipes, isLoadingRecipes} = useRecipes(diet.days);
    const [activeTab, setActiveTab] = useState("menu");

    // --- Logic for warnings ---
    const warningStatus = getDietWarningStatus(diet);
    const isEnded = isDietEnded(diet);
    const continuityStatus = checkFutureDiets(diet, allDiets || []);
    const WARNING_THRESHOLD = 3;
    const daysRemaining = getDaysRemainingToDietEnd(diet);
    const showWarning = isEnded || daysRemaining <= WARNING_THRESHOLD || warningStatus === 'critical';

    // --- Metadata & Warnings Section ---
    const renderInfoTab = () => (
        <div className="space-y-6">
            {/* Basic Info Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                    <div className="flex items-center gap-2 text-blue-700 mb-1">
                        <CalendarDays className="h-4 w-4"/>
                        <span className="text-sm font-semibold">Czas trwania</span>
                    </div>
                    <p className="text-2xl font-bold text-blue-900">
                        {diet.metadata?.totalDays || diet.days?.length || 0} <span
                        className="text-base font-normal text-blue-700">dni</span>
                    </p>
                </div>

                <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                    <div className="flex items-center gap-2 text-gray-600 mb-1">
                        <FileText className="h-4 w-4"/>
                        <span className="text-sm font-semibold">Plik źródłowy</span>
                    </div>
                    <p className="text-sm font-medium text-gray-900 truncate" title={diet.metadata?.fileName}>
                        {diet.metadata?.fileName || "Brak nazwy pliku"}
                    </p>
                    <p className="text-xs text-gray-500 mt-1">
                        Utworzono: {diet.createdAt ? formatTimestamp(diet.createdAt) : "-"}
                    </p>
                </div>
            </div>

            {/* Warnings / Continuity Status */}
            {showWarning && (
                <div
                    className={`p-4 rounded-xl border ${isEnded ? 'bg-gray-100 border-gray-200' : 'bg-amber-50 border-amber-100'}`}>
                    <div className="flex items-start gap-3">
                        {isEnded ? <XCircle className="h-5 w-5 text-gray-500 mt-0.5"/> :
                            <AlertTriangle className="h-5 w-5 text-amber-600 mt-0.5"/>}
                        <div>
                            <h4 className="font-semibold text-gray-900">
                                {isEnded ? "Dieta zakończona" : "Wymaga uwagi"}
                            </h4>
                            <p className="text-sm text-gray-600 mt-1">
                                {isEnded
                                    ? "Ten plan dietetyczny dobiegł końca."
                                    : daysRemaining <= WARNING_THRESHOLD
                                        ? `Dieta kończy się za ${daysRemaining} dni.`
                                        : getWarningStatusText(diet)}
                            </p>

                            {/* Continuity Info */}
                            <div className="mt-3 pt-3 border-t border-gray-200/50 flex items-center gap-2 text-sm">
                                {continuityStatus.hasFutureDiet ? (
                                    <>
                                        <CheckCircle2 className="h-4 w-4 text-green-600"/>
                                        <span className="text-green-700">
                                            Następna dieta zaplanowana (start {continuityStatus.gapDays <= 0 ? "bezpośrednio" : `za ${continuityStatus.gapDays} dni`}).
                                        </span>
                                    </>
                                ) : (
                                    <>
                                        <AlertTriangle className="h-4 w-4 text-amber-600"/>
                                        <span className="text-amber-700">
                                            Brak zaplanowanej przyszłej diety dla tego klienta.
                                        </span>
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );

    // --- Single Meal Render ---
    const renderMealCard = (meal: any, index: number) => {
        const recipe = recipes[meal.recipeId];
        if (!recipe) return null;

        return (
            <div key={index} className="bg-white border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start mb-2">
                    <div className="flex items-center gap-2">
                        <span className="px-2 py-1 bg-primary/10 text-primary text-xs font-medium rounded-md">
                            {getMealTypeLabel(meal.mealType)}
                        </span>
                        <div className="flex items-center text-xs text-gray-500">
                            <Clock className="h-3 w-3 mr-1"/>
                            {meal.time}
                        </div>
                    </div>
                </div>

                <h4 className="font-semibold text-gray-900 mb-2">{recipe.name}</h4>

                {/* Macroingredients */}
                <div
                    className="flex flex-wrap gap-3 text-xs text-gray-600 bg-gray-50 p-2 rounded border border-gray-100">
                    <div className="flex items-center gap-1 font-medium text-orange-600">
                        <Flame className="h-3 w-3"/>
                        {recipe.nutritionalValues?.calories || 0} kcal
                    </div>
                    <div>B: <span className="font-medium text-gray-900">{recipe.nutritionalValues?.protein || 0}g</span>
                    </div>
                    <div>T: <span className="font-medium text-gray-900">{recipe.nutritionalValues?.fat || 0}g</span>
                    </div>
                    <div>W: <span className="font-medium text-gray-900">{recipe.nutritionalValues?.carbs || 0}g</span>
                    </div>
                </div>

                {/* Instructions */}
                {recipe.instructions && (
                    <p className="mt-3 text-sm text-gray-600 line-clamp-2 hover:line-clamp-none transition-all cursor-pointer">
                        {recipe.instructions}
                    </p>
                )}
            </div>
        );
    };

    // --- Rendering Day ---
    const renderDay = (day: any, index: number) => {
        const dayDate = timestampToDate(day.date);

        const safeDate = dayDate || new Date();

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const compareDate = new Date(safeDate);
        compareDate.setHours(0, 0, 0, 0);

        const isPastDay = compareDate < today;

        return (
            <div key={index} className="mb-8">
                <div className="flex items-center gap-3 mb-4 sticky top-0 bg-white/95 backdrop-blur py-2 z-10 border-b">
                    <div
                        className="bg-gray-900 text-white w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm">
                        {index + 1}
                    </div>
                    <div>
                        <h3 className="font-bold text-gray-900">
                            {dayDate?.toLocaleDateString('pl-PL', {weekday: 'long', day: 'numeric', month: 'long'})}
                        </h3>
                        {isPastDay && <span className="text-xs text-gray-500 font-medium">Dzień zakończony</span>}
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pl-2 md:pl-0">
                    {day.meals?.map((meal: any, mealIndex: number) => renderMealCard(meal, mealIndex))}
                </div>
            </div>
        );
    };

    return (
        <Sheet open={true} onOpenChange={onClose}>
            <SheetContent className="w-full sm:max-w-2xl md:max-w-3xl lg:max-w-4xl overflow-hidden flex flex-col p-0">
                {/* Header */}
                <SheetHeader className="px-6 py-4 border-b bg-white z-20">
                    <div className="flex justify-between items-center">
                        <SheetTitle className="text-xl">Szczegóły Diety</SheetTitle>
                        <SheetClose className="rounded-full hover:bg-gray-100 p-2 transition-colors"/>
                    </div>
                </SheetHeader>

                {/* Scrollable content */}
                <div className="flex-1 overflow-y-auto bg-white">
                    <div className="px-6 py-6">
                        {/* Tabs */}
                        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
                            <TabsList className="grid w-full grid-cols-3 mb-8 bg-gray-100 p-1 rounded-xl">
                                <TabsTrigger value="menu" className="flex items-center gap-2">
                                    <Utensils className="h-4 w-4"/>
                                    Jadłospis
                                </TabsTrigger>
                                <TabsTrigger value="shopping" className="flex items-center gap-2">
                                    <ShoppingBasket className="h-4 w-4"/>
                                    Lista Zakupów
                                </TabsTrigger>
                                <TabsTrigger value="info" className="flex items-center gap-2">
                                    <Info className="h-4 w-4"/>
                                    Informacje
                                </TabsTrigger>
                            </TabsList>

                            {/* Tab: MENU */}
                            <TabsContent value="menu"
                                         className="space-y-6 animate-in slide-in-from-bottom-2 duration-300">
                                {isLoadingRecipes ? (
                                    <div className="py-20 flex justify-center"><LoadingSpinner/></div>
                                ) : (
                                    diet.days && diet.days.length > 0 ? (
                                        [...diet.days]
                                            .sort((a, b) => {
                                                const dateA = timestampToDate(a.date)?.getTime() || 0;
                                                const dateB = timestampToDate(b.date)?.getTime() || 0;
                                                return dateA - dateB;
                                            })
                                            .map((day, idx) => renderDay(day, idx))
                                    ) : (
                                        <div className="text-center py-10 text-gray-500">Brak posiłków w tej
                                            diecie.</div>
                                    )
                                )}
                            </TabsContent>

                            {/* Tab: SHOPPING LIST */}
                            <TabsContent value="shopping" className="animate-in slide-in-from-bottom-2 duration-300">
                                <div className="max-w-2xl mx-auto">
                                    <DietShoppingList dietId={diet.id}/>
                                </div>
                            </TabsContent>

                            {/* Tab: INFORMATION */}
                            <TabsContent value="info" className="animate-in slide-in-from-bottom-2 duration-300">
                                {renderInfoTab()}
                            </TabsContent>
                        </Tabs>
                    </div>
                </div>
            </SheetContent>
        </Sheet>
    );
};

export default DietView;