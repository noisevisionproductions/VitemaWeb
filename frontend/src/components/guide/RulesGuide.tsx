import React from "react";
import { AlertCircle, CheckCircle2, ListChecks } from "lucide-react";
import GuideSection from "./GuideSection";
import InteractiveCard from "./InteractiveCard";

interface RulesGuideProps {
    activeCard: string;
    toggleCard: (cardId: string) => void;
}

const RulesGuide: React.FC<RulesGuideProps> = ({ activeCard, toggleCard }) => {
    return (
        <GuideSection
            title="Zasady układania diet"
            icon={<ListChecks className="w-5 h-5 text-green-600" />}
        >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InteractiveCard
                    title="Podstawowe zasady"
                    icon={<CheckCircle2 className="w-4 h-4 text-green-600" />}
                    onClick={() => toggleCard('basic-rules')}
                    isActive={activeCard === 'basic-rules'}
                >
                    <ul className="list-disc pl-6 space-y-2 text-slate-700 mb-3">
                        <li>Każdy dzień może mieć od 3 do 5 posiłków</li>
                        <li>Posiłki muszą być rozłożone równomiernie w ciągu dnia</li>
                        <li>Należy uwzględnić wszystkie główne grupy pokarmowe</li>
                        <li>Dieta powinna być zróżnicowana</li>
                    </ul>
                </InteractiveCard>

                <InteractiveCard
                    title="Weryfikacja danych"
                    icon={<AlertCircle className="w-4 h-4 text-amber-600" />}
                    onClick={() => toggleCard('data-verification')}
                    isActive={activeCard === 'data-verification'}
                >
                    <ul className="list-disc pl-6 space-y-2 text-slate-700 mb-3">
                        <li>Wszystkie nazwy posiłków muszą być unikalne</li>
                        <li>Każdy posiłek musi mieć opis przygotowania</li>
                        <li>Lista składników jest obowiązkowa oraz jest traktowana również jako lista zakupów</li>
                        <li>Wartości odżywcze są opcjonalne</li>
                    </ul>
                </InteractiveCard>
            </div>
        </GuideSection>
    );
};

export default RulesGuide;