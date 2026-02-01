import React, {useState} from 'react';
import {ArrowPathIcon} from "@heroicons/react/24/outline";
import {cn} from "../../../utils/cs";

interface RefreshButtonProps {
    onRefresh: () => Promise<void>;
    isLoading?: boolean;
    className?: string;
    title?: string;
}

const RefreshButton: React.FC<RefreshButtonProps> = ({
                                                         onRefresh,
                                                         isLoading: externalLoading,
                                                         className,
                                                         title = "Odśwież dane"
                                                     }) => {
    const [localLoading, setLocalLoading] = useState(false);

    const isLoading = externalLoading || localLoading;

    const handleRefresh = async () => {
        if (isLoading) return;

        setLocalLoading(true);
        try {
            await onRefresh();
        } finally {
            setTimeout(() => setLocalLoading(false), 600);
        }
    };

    return (
        <button
            onClick={handleRefresh}
            disabled={isLoading}
            className={cn(
                "p-2 text-gray-400 hover:text-primary transition-colors rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary/20",
                isLoading && "cursor-not-allowed",
                className
            )}
            title={title}
        >
            <ArrowPathIcon
                className={cn(
                    "h-5 w-5",
                    isLoading && "animate-spin text-primary"
                )}
            />
        </button>
    );
};

export default RefreshButton;