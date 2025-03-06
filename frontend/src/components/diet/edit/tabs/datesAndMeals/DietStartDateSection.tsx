import React from "react";
import {Timestamp} from "firebase/firestore";
import {toISODate} from "../../../../../utils/dateFormatters";
import {Calendar} from "react-feather";

interface DietStartDateSectionProps {
    currentDate: Timestamp;
    onDateChange: (dateStr: string) => void;
}

const DietStartDateSection: React.FC<DietStartDateSectionProps> = ({
                                                                       currentDate,
                                                                       onDateChange
                                                                   }) => {
    const dateValue = React.useMemo(() => {
        try {
            return toISODate(currentDate) || '';
        } catch (error) {
            console.error('Error formatting date:', error);
            return '';
        }
    }, [currentDate]);

    return (
        <div className="bg-white p-4 rounded-lg">
            <h3 className="text-lg flex items-center gap-2 font-medium mb-4">
                <Calendar className="w-5 h-5 text-green-600" />
                <span className="text-gray-700">Data rozpoczęcia diety</span>
            </h3>
            <div className="bg-blue-50 p-4 rounded-lg">
                <div className="flex items-center gap-4">
                    <input
                        type="date"
                        id="dietStartDate"
                        value={dateValue}
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
};

export default DietStartDateSection;