import React from "react";

interface InfoCardProps {
    title: string;
    children: React.ReactNode;
    color?: "blue" | "green" | "purple" | "yellow";
    icon: React.ReactNode;
}

const InfoCard: React.FC<InfoCardProps> = ({
                                               title,
                                               children,
                                               color = "blue",
                                               icon
                                           }) => {
    const colors = {
        blue: "bg-blue-50 border-blue-100 text-blue-800",
        green: "bg-green-50 border-green-100 text-green-800",
        purple: "bg-purple-50 border-purple-100 text-purple-800",
        yellow: "bg-amber-50 border-amber-100 text-amber-800",
    };

    return (
        <div className={`p-4 rounded-lg border ${colors[color]}`}>
            <h4 className="font-medium mb-2 flex items-center gap-2">
                {icon}
                {title}
            </h4>
            {children}
        </div>
    );
};

export default InfoCard;