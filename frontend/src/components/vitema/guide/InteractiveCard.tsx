import React from "react";

interface InteractiveCardProps {
    title: string;
    children: React.ReactNode;
    icon: React.ReactNode;
}

const InteractiveCard: React.FC<InteractiveCardProps> = ({
                                                             title,
                                                             children,
                                                             icon
                                                         }) => (
    <div className="border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm">
        <div className="flex items-center gap-2 mb-3">
            <div className="p-2 rounded-full bg-blue-100">
                {icon}
            </div>
            <h3 className="font-medium text-blue-700">
                {title}
            </h3>
        </div>
        <div>
            {children}
        </div>
    </div>
);

export default InteractiveCard;