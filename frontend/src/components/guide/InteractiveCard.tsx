import React from "react";
import { ChevronRight } from "lucide-react";

interface InteractiveCardProps {
    title: string;
    children: React.ReactNode;
    icon: React.ReactNode;
    onClick: () => void;
    isActive?: boolean;
}

const InteractiveCard: React.FC<InteractiveCardProps> = ({
                                                             title,
                                                             children,
                                                             icon,
                                                             onClick,
                                                             isActive = false
                                                         }) => (
    <div
        className={`border rounded-lg p-5 transition-all duration-200 cursor-pointer hover:shadow-md hover:border-blue-300 ${
            isActive ? 'border-blue-500 bg-blue-50 shadow-sm' : 'border-slate-200 bg-white'
        }`}
        onClick={onClick}
    >
        <div className="flex justify-between items-center mb-3">
            <div className="flex items-center gap-2">
                <div className={`p-2 rounded-full ${isActive ? 'bg-blue-100' : 'bg-slate-100'}`}>
                    {icon}
                </div>
                <h3 className={`font-medium ${isActive ? 'text-blue-700' : 'text-slate-700'}`}>
                    {title}
                </h3>
            </div>
            <ChevronRight className={`w-4 h-4 transition-transform ${isActive ? 'rotate-90 text-blue-500' : 'text-slate-400'}`} />
        </div>
        <div className={`overflow-hidden transition-all ${isActive ? 'max-h-96' : 'max-h-0'}`}>
            {children}
        </div>
    </div>
);

export default InteractiveCard;