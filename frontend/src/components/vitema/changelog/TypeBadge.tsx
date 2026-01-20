import React from "react";
import {Bug, Lightbulb, Sparkles} from "lucide-react";

interface TypeBadgeProps {
    type: 'feature' | 'fix' | 'improvement';
    showIcon?: boolean;
    size?: 'sm' | 'md' | 'lg';
}

const TypeBadge: React.FC<TypeBadgeProps> = ({ type, showIcon = true, size = 'md' }) => {
    const getTypeIcon = () => {
        switch (type) {
            case "feature":
                return <Sparkles className={size === 'sm' ? "w-3 h-3" : "w-4 h-4"} />;
            case "fix":
                return <Bug className={size === 'sm' ? "w-3 h-3" : "w-4 h-4"} />;
            case "improvement":
                return <Lightbulb className={size === 'sm' ? "w-3 h-3" : "w-4 h-4"} />;
        }
    };

    const getTypeLabel = () => {
        switch (type) {
            case 'feature':
                return 'Nowa funkcja';
            case "fix":
                return 'Poprawka';
            case "improvement":
                return 'Ulepszenie';
        }
    };

    const getStyles = () => {
        const baseStyles = "inline-flex items-center gap-1.5 font-medium rounded-full";

        const sizeStyles = {
            sm: "px-2 py-0.5 text-xs",
            md: "px-3 py-1 text-sm",
            lg: "px-4 py-1.5 text-base"
        };

        const colorStyles = {
            feature: "bg-blue-100 text-blue-800",
            fix: "bg-red-100 text-red-800",
            improvement: "bg-yellow-100 text-yellow-800"
        };

        return `${baseStyles} ${sizeStyles[size]} ${colorStyles[type]}`;
    };

    return (
        <span className={getStyles()}>
            {showIcon && getTypeIcon()}
            {getTypeLabel()}
        </span>
    );
};

export default TypeBadge;