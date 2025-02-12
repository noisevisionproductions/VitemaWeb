import React from "react";
import {Timestamp} from "firebase/firestore";
import {dateToString} from "../../../../../utils/dateFormatters";

interface DietStartDateSectionProps {
    currentDate: Timestamp;
    onDateChange: (dateStr: string) => void;
}

const DietStartDateSection: React.FC<DietStartDateSectionProps> = ({
                                                                       currentDate,
                                                                       onDateChange
                                                                   }) => (
    <div className="bg-white p-4 rounded-lg">
        <h3 className="text-lg font-medium mb-4">
            Data rozpoczęcia diety
        </h3>
        <div className="bg-blue-50 p-4 rounded-lg">
            <div className="flex items-center gap-4">
                <input
                    type="date"
                    value={dateToString(currentDate)}
                    onChange={(e) => onDateChange(e.target.value)}
                    className="border rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                <span className="text-sm text-gray-600">
                    Zmiana daty rozpoczęcia automatycznie zaktualizuje wszystkie kolejne dni dla całej diety jak i listy zakupów przypisanej do niej
                </span>
            </div>
        </div>
    </div>
);

export default DietStartDateSection;