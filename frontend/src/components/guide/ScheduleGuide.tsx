import React from "react";
import { AlertCircle, Clock, Info } from "lucide-react";
import GuideSection from "./GuideSection";
import InteractiveCard from "./InteractiveCard";
import InfoCard from "./InfoCard";

interface ScheduleGuideProps {
    activeCard: string;
    toggleCard: (cardId: string) => void;
}

const ScheduleGuide: React.FC<ScheduleGuideProps> = ({ activeCard, toggleCard }) => {
    return (
        <GuideSection
            title="Harmonogram posiłków"
            icon={<Clock className="w-5 h-5 text-purple-600" />}
        >
            <div className="space-y-6">
                <p className="text-slate-700">
                    Przykładowe rozłożenie posiłków w ciągu dnia:
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <InteractiveCard
                        title="3 posiłki"
                        icon={<Info className="w-4 h-4 text-green-600" />}
                        onClick={() => toggleCard('three-meals')}
                        isActive={activeCard === 'three-meals'}
                    >
                        <ul className="space-y-2 text-slate-700 mb-3">
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">Śniadanie:</span>
                                <span className="text-slate-600">8:00</span>
                            </li>
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">Obiad:</span>
                                <span className="text-slate-600">13:00</span>
                            </li>
                            <li className="flex justify-between items-center">
                                <span className="font-medium">Kolacja:</span>
                                <span className="text-slate-600">18:00</span>
                            </li>
                        </ul>
                    </InteractiveCard>

                    <InteractiveCard
                        title="5 posiłków"
                        icon={<Info className="w-4 h-4 text-purple-600" />}
                        onClick={() => toggleCard('five-meals')}
                        isActive={activeCard === 'five-meals'}
                    >
                        <ul className="space-y-2 text-slate-700 mb-3">
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">Śniadanie:</span>
                                <span className="text-slate-600">7:00</span>
                            </li>
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">II śniadanie:</span>
                                <span className="text-slate-600">10:00</span>
                            </li>
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">Obiad:</span>
                                <span className="text-slate-600">13:00</span>
                            </li>
                            <li className="flex justify-between items-center border-b pb-2">
                                <span className="font-medium">Podwieczorek:</span>
                                <span className="text-slate-600">16:00</span>
                            </li>
                            <li className="flex justify-between items-center">
                                <span className="font-medium">Kolacja:</span>
                                <span className="text-slate-600">19:00</span>
                            </li>
                        </ul>
                    </InteractiveCard>
                </div>

                <InfoCard
                    title="Ważne uwagi"
                    color="yellow"
                    icon={<AlertCircle className="w-5 h-5 text-amber-600" />}
                >
                    <ul className="list-disc pl-6 mt-2 space-y-1">
                        <li>Odstępy między posiłkami powinny wynosić 2-4 godziny</li>
                        <li>Ostatni posiłek nie później niż 2-3 godziny przed snem</li>
                        <li>Godziny można dostosować do preferencji użytkownika</li>
                        <li>Należy zachować regularne pory posiłków</li>
                    </ul>
                </InfoCard>
            </div>
        </GuideSection>
    );
};

export default ScheduleGuide;