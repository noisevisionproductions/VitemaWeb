import React, { useState } from "react";
import { ParsedDay } from "../../../../../types";
import { formatTimestamp } from "../../../../../utils/dateFormatters";
import DietMealPreview from "./DietMealPreview";
import { ChevronDown, ChevronUp, CalendarDays } from "lucide-react";

interface DietDayPreviewProps {
    day: ParsedDay;
    dayIndex: number;
}

const DietDayPreview: React.FC<DietDayPreviewProps> = ({ day, dayIndex }) => {
    const [expanded, setExpanded] = useState(dayIndex === 0);

    return (
        <div className="bg-white rounded-lg shadow overflow-hidden">
            <div
                className="p-4 border-b flex justify-between items-center cursor-pointer hover:bg-gray-50"
                onClick={() => setExpanded(!expanded)}
            >
                <h3 className="text-lg font-semibold flex items-center gap-2">
                    <CalendarDays className="h-5 w-5 text-blue-600" />
                    <span>Dzień {dayIndex + 1} - {formatTimestamp(day.date)}</span>
                </h3>
                {expanded ? (
                    <ChevronUp className="h-5 w-5 text-gray-500" />
                ) : (
                    <ChevronDown className="h-5 w-5 text-gray-500" />
                )}
            </div>

            {expanded && (
                <div className="p-4">
                    <div className="space-y-4">
                        {day.meals.map((meal, mealIndex) => (
                            <DietMealPreview
                                key={mealIndex}
                                meal={meal}
                                mealIndex={mealIndex}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DietDayPreview;