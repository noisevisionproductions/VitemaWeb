import React from "react";

interface GuideSectionProps {
    title: string;
    children: React.ReactNode;
    icon?: React.ReactNode;
}

const GuideSection: React.FC<GuideSectionProps> = ({
                                                       title,
                                                       children,
                                                       icon
                                                   }) => (
    <div className="space-y-4 mb-6">
        <h3 className="text-xl font-semibold flex items-center gap-2 text-slate-800">
            {icon}
            {title}
        </h3>
        <div className="pl-1">
            {children}
        </div>
    </div>
);

export default GuideSection;