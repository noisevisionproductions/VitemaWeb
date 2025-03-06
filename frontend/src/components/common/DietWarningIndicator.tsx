import React from "react";
import {getWarningStatusText, WarningStatus} from "../../utils/dietWarningUtils";
import {Diet} from "../../types";
import {AlertCircle, AlertTriangle} from "lucide-react";

interface DietWarningIndicatorProps {
    status: WarningStatus;
    diet: Diet;
    showText?: boolean;
    size?: 'sm' | 'md' | 'lg';
}

const DietWarningIndicator: React.FC<DietWarningIndicatorProps> = ({
                                                                       status,
                                                                       diet,
                                                                       showText = true,
                                                                       size = "md"
                                                                   }) => {
    if (status == 'normal') return null;

    const iconSizes = {
        sm: 14,
        md: 16,
        lg: 20
    };

    const iconSize = iconSizes[size];
    const statusText = getWarningStatusText(diet);

    return (
        <div className={`diet-warning-indicator ${status} flex  items-center gap-1.5`}>
            {status === 'critical' ? (
                <AlertCircle
                    size={iconSize}
                    className="text-red-600"
                    aria-label="Krytyczne ostrzeżenie"
                />
            ) : (
                <AlertTriangle
                    size={iconSize}
                    className="text-amber-500"
                    aria-label="Ostrzeżenie"
                />
            )}

            {showText && statusText && (
                <span className={`text-xs font-medium ${status === 'critical' ? 'text-red-600' : 'text-amber-500'}`}>
                    {statusText}
                </span>
            )}
        </div>
    );
};

export default DietWarningIndicator;