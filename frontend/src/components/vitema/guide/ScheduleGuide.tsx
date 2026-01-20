import React from "react";
import { AlertCircle, Clock, Info } from "lucide-react";
import GuideSection from "./GuideSection";
import InfoCard from "./InfoCard";

const ScheduleGuide: React.FC = () => {
    return (
        <GuideSection
            title="Harmonogram posiłków"
            icon={<Clock className="w-5 h-5 text-purple-600" />}
        >
            <div className="space-y-4">
                <p className="text-slate-700 text-sm sm:text-base">
                    Przykładowe rozłożenie posiłków w ciągu dnia:
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="p-1 sm:p-2 rounded-full bg-blue-100">
                                <Info className="w-4 h-4 text-green-600" />
                            </div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                3 posiłki
                            </h3>
                        </div>
                        <div>
                            <ul className="space-y-1 sm:space-y-2 text-slate-700 mb-2 sm:mb-3 text-sm sm:text-base">
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
                        </div>
                    </div>

                    <div className="border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-3">
                            <div className="p-2 rounded-full bg-blue-100">
                                <Info className="w-4 h-4 text-purple-600" />
                            </div>
                            <h3 className="font-medium text-blue-700">
                                5 posiłków
                            </h3>
                        </div>
                        <div>
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
                        </div>
                    </div>
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