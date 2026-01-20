import React from "react";
import {getDaysRemainingToDietEnd, getWarningStatusText, WarningStatus} from "../../../utils/diet/dietWarningUtils";
import {Diet} from "../../../types";
import {AlertCircle, AlertTriangle, Clock} from "lucide-react";
import {checkFutureDiets} from "../../../utils/diet/dietContinuityUtils";

interface DietWarningIndicatorProps {
    status: WarningStatus;
    diet: Diet;
    showText?: boolean;
    size?: 'sm' | 'md' | 'lg';
    allDiets?: Diet[]
}

const DietWarningIndicator: React.FC<DietWarningIndicatorProps> = ({
                                                                       status,
                                                                       diet,
                                                                       showText = true,
                                                                       size = "md",
                                                                       allDiets = []
                                                                   }) => {
    const daysRemaining = getDaysRemainingToDietEnd(diet);
    const continuityStatus = checkFutureDiets(diet, allDiets);
    const isEnded = daysRemaining < 0;
    const WARNING_THRESHOLD = 3;

    const containerClasses = {
        sm: 'text-xs py-0.5 px-1.5 mt-1',
        md: 'text-sm py-1 px-2 my-1',
        lg: 'text-base py-1.5 px-3 my-2'
    };

    const iconSizes = {
        sm: 14,
        md: 16,
        lg: 20
    };

    const iconSize = iconSizes[size];
    const statusText = getWarningStatusText(diet);

    // Jeśli dieta jest zakończona, pokazujemy specjalny status
    if (isEnded) {
        return (
            <div
                className={`flex items-center rounded-full bg-gray-100 text-gray-600 font-medium ${containerClasses[size]}`}>
                <Clock size={iconSize} className="mr-1.5"/>
                {showText && <span>Zakończona</span>}
            </div>
        );
    }

    if (daysRemaining > WARNING_THRESHOLD && status !== 'critical') {
        return null;
    }

    // Dla krytycznego statusu (0-1 dni do końca)
    if (status === 'critical') {
        // Jeśli istnieje następna dieta, pokazujemy warning zamiast error
        if (continuityStatus.hasFutureDiet) {
            return (
                <div
                    className={`flex items-center rounded-full bg-amber-100 text-amber-600 font-medium ${containerClasses[size]}`}>
                    <AlertTriangle size={iconSize} className="mr-1.5"/>
                    {showText && (
                        <span>
                            {statusText}
                            {continuityStatus.gapDays <= 0
                                ? ' (następna zaplanowana)'
                                : ` (następna za ${continuityStatus.gapDays} dni)`}
                        </span>
                    )}
                </div>
            );
        }

        // Brak następnej diety - critical warning
        return (
            <div
                className={`flex items-center rounded-full bg-red-100 text-red-600 font-medium ${containerClasses[size]}`}>
                <AlertCircle size={iconSize} className="mr-1.5"/>
                {showText && (
                    <span>
                        {statusText}
                        {' Brak następnej diety!'}
                    </span>
                )}
            </div>
        );
    }

    // Dla statusu ostrzeżenia (2-3 dni do końca)
    if (status === 'warning') {
        // Jeśli istnieje następna dieta, pokazujemy informację zamiast ostrzeżenia
        if (continuityStatus.hasFutureDiet) {
            return (
                <div
                    className={`flex items-center rounded-full bg-blue-100 text-blue-600 font-medium ${containerClasses[size]}`}>
                    <Clock size={iconSize} className="mr-1.5"/>
                    {showText && (
                        <span>
                            {statusText}
                            {continuityStatus.gapDays <= 0
                                ? ' (następna zaplanowana)'
                                : ` (następna za ${continuityStatus.gapDays} dni)`}
                        </span>
                    )}
                </div>
            );
        }

        // Brak następnej diety-warning
        return (
            <div
                className={`flex items-center rounded-full bg-amber-100 text-amber-600 font-medium ${containerClasses[size]}`}>
                <AlertTriangle size={iconSize} className="mr-1.5"/>
                {showText && (
                    <span>
                        {statusText}
                        {' Brak następnej diety'}
                    </span>
                )}
            </div>
        );
    }

    return null;
};

export default DietWarningIndicator;