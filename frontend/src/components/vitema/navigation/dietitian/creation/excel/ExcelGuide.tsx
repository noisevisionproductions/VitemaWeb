import React from "react";
import { CheckCircle2, FileSpreadsheet, Info } from "lucide-react";
import GuideSection from "../../../../guide/GuideSection";
import ExcelExample from "../../../../guide/ExcelExample";

const ExcelGuide: React.FC = () => {
    return (
        <GuideSection
            title="Struktura pliku Excel"
            icon={<FileSpreadsheet className="w-5 h-5 text-blue-600" />}
        >
            <div className="space-y-4">
                <div className="space-y-3">
                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="p-1 sm:p-2 rounded-full bg-blue-100">
                                <CheckCircle2 className="w-4 h-4 text-green-600" />
                            </div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Wymagane kolumny
                            </h3>
                        </div>
                        <div>
                            <ul className="list-disc pl-4 sm:pl-6 space-y-2 sm:space-y-3 text-slate-700 mb-2 sm:mb-4 text-sm sm:text-base">
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
                        </div>
                    </div>

                    <div className="border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-3">
                            <div className="p-2 rounded-full bg-blue-100">
                                <Info className="w-4 h-4 text-blue-600" />
                            </div>
                            <h3 className="font-medium text-blue-700">
                                Ważne zasady
                            </h3>
                        </div>
                        <div>
                            <ul className="list-disc pl-6 mt-2 space-y-2 text-slate-700 mb-3">
                                <li>Pierwszy wiersz (1) to nagłówki, np. "Nazwa posiłku" w kolumnie B. Nagłówki są pomijane, ale muszą być podane raz w pierwszym wierszu w każdym pliku.</li>
                                <li>Każdy wiersz to jeden posiłek</li>
                                <li>Wartości odżywcze póki co są opcjonalne i nie do końca skonfigurowane.</li>
                                <li>Puste wiersze są pomijane</li>
                            </ul>
                        </div>
                    </div>

                    <div className="border rounded-lg p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-3">
                            <div className="p-2 rounded-full bg-blue-100">
                                <FileSpreadsheet className="w-4 h-4 text-blue-600" />
                            </div>
                            <h3 className="font-medium text-blue-700">
                                Przykład poprawnego pliku
                            </h3>
                        </div>
                        <div>
                            <div className="mb-4">
                                <ExcelExample />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </GuideSection>
    );
};

export default ExcelGuide;