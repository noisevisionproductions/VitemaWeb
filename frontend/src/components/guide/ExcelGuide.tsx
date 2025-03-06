import React from "react";
import { CheckCircle2, FileSpreadsheet, Info } from "lucide-react";
import GuideSection from "./GuideSection";
import InteractiveCard from "./InteractiveCard";
import ExcelExample from "./ExcelExample";

interface ExcelGuideProps {
    activeCard: string;
    toggleCard: (cardId: string) => void;
}

const ExcelGuide: React.FC<ExcelGuideProps> = ({ activeCard, toggleCard }) => {
    return (
        <GuideSection
            title="Struktura pliku Excel"
            icon={<FileSpreadsheet className="w-5 h-5 text-blue-600" />}
        >
            <div className="space-y-6">
                <div className="space-y-4">
                    <InteractiveCard
                        title="Wymagane kolumny"
                        icon={<CheckCircle2 className="w-4 h-4 text-green-600" />}
                        onClick={() => toggleCard('columns')}
                        isActive={activeCard === 'columns'}
                    >
                        <ul className="list-disc pl-6 space-y-3 text-slate-700 mb-4">
                            <li>
                                <strong className="text-slate-900">Kolumna A (pomijana)</strong>
                                <div className="pl-1 mt-1 text-sm">Może zawierać dowolne notatki, nie jest brana pod uwagę przy przetwarzaniu</div>
                            </li>
                            <li>
                                <strong className="text-slate-900">Kolumna B (Nazwa)</strong>
                                <div className="pl-1 mt-1 text-sm">Nazwa posiłku</div>
                            </li>
                            <li>
                                <strong className="text-slate-900">Kolumna C (Sposób przygotowania)</strong>
                                <div className="pl-1 mt-1 text-sm">Dokładny opis przygotowania posiłku</div>
                            </li>
                            <li>
                                <strong className="text-slate-900">Kolumna D (Lista składników)</strong>
                                <div className="pl-1 mt-1 text-sm">
                                    Składniki oddzielone przecinkami.
                                    <span className="text-red-600 font-medium"> Z tego względu pojemności powinny być zaznaczane kropką zamiast przecinkiem, np. 1.2 litry, zamiast 1,2 litry</span>
                                </div>
                            </li>
                            <li>
                                <strong className="text-slate-900">Kolumna E (Wartości odżywcze)</strong>
                                <div className="pl-1 mt-1 text-sm">Opcjonalne, format: kalorie,białko,tłuszcze,węglowodany</div>
                            </li>
                        </ul>
                    </InteractiveCard>

                    <InteractiveCard
                        title="Ważne zasady"
                        icon={<Info className="w-4 h-4 text-blue-600" />}
                        onClick={() => toggleCard('rules')}
                        isActive={activeCard === 'rules'}
                    >
                        <ul className="list-disc pl-6 mt-2 space-y-2 text-slate-700 mb-3">
                            <li>Pierwszy wiersz (1) to nagłówki, np. "Nazwa posiłku" w kolumnie B. Nagłówki są pomijane, ale muszą być podane raz w pierwszym wierszu w każdym pliku.</li>
                            <li>Każdy wiersz to jeden posiłek</li>
                            <li>Wartości odżywcze póki co są opcjonalne i nie do końca skonfigurowane.</li>
                            <li>Puste wiersze są pomijane</li>
                        </ul>
                    </InteractiveCard>

                    <InteractiveCard
                        title="Przykład poprawnego pliku"
                        icon={<FileSpreadsheet className="w-4 h-4 text-blue-600" />}
                        onClick={() => toggleCard('example')}
                        isActive={activeCard === 'example'}
                    >
                        <div className="mb-4">
                            <ExcelExample />
                        </div>
                    </InteractiveCard>
                </div>
            </div>
        </GuideSection>
    );
};

export default ExcelGuide;