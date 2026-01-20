import React from "react";
import { AlertCircle, CheckCircle2, ListChecks } from "lucide-react";
import GuideSection from "./GuideSection";

const RulesGuide: React.FC = () => {
    return (
        <GuideSection
            title="Zasady układania diet"
            icon={<ListChecks className="w-5 h-5 text-green-600" />}
        >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                    <div className="flex items-center gap-2 mb-2 sm:mb-3">
                        <div className="p-1 sm:p-2 rounded-full bg-blue-100">
                            <CheckCircle2 className="w-4 h-4 text-green-600" />
                        </div>
                        <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                            Podstawowe zasady
                        </h3>
                    </div>
                    <div>
                        <ul className="list-disc pl-4 sm:pl-6 space-y-1 sm:space-y-2 text-slate-700 mb-2 sm:mb-3 text-sm sm:text-base">
                            <li>Każdy dzień może mieć od 3 do 5 posiłków</li>
                            <li>Posiłki muszą być rozłożone równomiernie w ciągu dnia</li>
                            <li>Należy uwzględnić wszystkie główne grupy pokarmowe</li>
                            <li>Dieta powinna być zróżnicowana</li>
                        </ul>
                    </div>
                </div>

                <div className="border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm">
                    <div className="flex items-center gap-2 mb-3">
                        <div className="p-2 rounded-full bg-blue-100">
                            <AlertCircle className="w-4 h-4 text-amber-600" />
                        </div>
                        <h3 className="font-medium text-blue-700">
                            Weryfikacja danych
                        </h3>
                    </div>
                    <div>
                        <ul className="list-disc pl-6 space-y-2 text-slate-700 mb-3">
                            <li>Wszystkie nazwy posiłków muszą być unikalne</li>
                            <li>Każdy posiłek musi mieć opis przygotowania</li>
                            <li>Lista składników jest obowiązkowa oraz jest traktowana również jako lista zakupów</li>
                            <li>Wartości odżywcze są opcjonalne</li>
                        </ul>
                    </div>
                </div>
            </div>
        </GuideSection>
    );
};

export default RulesGuide;