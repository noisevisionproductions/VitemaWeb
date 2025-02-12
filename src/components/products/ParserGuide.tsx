import React, {useState} from "react";
import {ChevronDown, ChevronUp, Info} from "lucide-react";

const ParserGuide: React.FC = () => {
    const [isExpanded, setIsExpanded] = useState(false);

    const examples = [
        {
            type: 'Podstawowe',
            cases: [
                '100 g mąki',
                '2 jajka',
                '250 ml mleka'
            ]
        },
        {
            type: 'Ułamki i zakresy',
            cases: [
                '1/2 szklanki cukru',
                '1 1/2 łyżki oleju',
                '2-3 ziemniaki'
            ]
        },
        {
            type: 'Przedrostki',
            cases: [
                'pół kilograma mięsa',
                'ćwierć szklanki wody',
                'półtorej łyżki sosu'
            ]
        },
        {
            type: 'Miary kuchenne',
            cases: [
                '2 łyżki masła',
                '3 szklanki mąki',
                '1 łyżeczka soli'
            ]
        }
    ];

    const limitations = [
        'Nie rozpoznaje opisowych ilości (np. "szczypta soli", "do smaku")',
        'Może mieć problem z bardzo złożonymi nazwami produktów',
        "Zakresy ilości są upraszczane do średniej (np. '2-3 jabłka' jest interpretowane jako 2.5 jabłka)",
        'Nie rozpoznaje alternatyw (np. "2 łyżki sosu sojowego lub rybnego")',
        'Najlepiej nie dodawać kropek na końcu. Kropkami jedynie rozdzielać wartości np. 1.5 L, a przecinkami oddzielać produkty'
    ];

    const categorizationFeatures = [
        {
            title: 'Automatyczne łączenie',
            description: 'System automatycznie łączy te same produkty, sumując ich ilości (np. "100 g mąki" + "200 g mąki" = "300 g mąki")',
            example: 'Wszystkie wystąpienia tego samego produktu są sumowane w liście zakupów'
        },
        {
            title: 'Inteligentne rozpoznawanie',
            description: 'System rozpoznaje podobne produkty nawet jeśli są zapisane w różny sposób',
            example: 'Automatycznie sugeruje kategorie na podstawie wcześniejszych wyborów'
        },
        {
            title: 'Konwersja jednostek',
            description: 'System automatycznie przelicza i normalizuje różne jednostki miary',
            example: 'Przelicza jednostki tego samego typu (np. kilogramy na gramy)'
        },
        {
            title: 'Statystyki użycia',
            description: 'System śledzi, jak często produkty są przypisywane do poszczególnych kategorii',
            example: 'Liczby przy kategoriach pokazują aktualną liczbę produktów oraz historyczne użycie'
        }
    ];

    return (
        <div className="bg-blue-50 rounded-lg p-4 mb-4">
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between text-blue-700 font-medium"
            >
                <div className="flex items-center gap-2">
                    <Info className="h-5 w-5"/>
                    <span>
                        Przewodnik po formacie produktów i kategoryzacji
                    </span>
                </div>
                {isExpanded ? (
                    <ChevronUp className="h-5 w-5"/>
                ) : (
                    <ChevronDown className="h-5 w-5"/>
                )}
            </button>

            {isExpanded && (
                <div className="mt-4 space-y-6">
                    {/* Przykłady */}
                    <div>
                        <h4 className="font-medium mb-2">
                            Funkcja kategoryzacji powinna rozpoznawać:
                        </h4>
                        <div className="grid grid-cols-2 gap-4">
                            {examples.map((group, idx) => (
                                <div key={idx} className="bg-white p-3 rounded-lg">
                                    <h5 className="font-medium text-sm text-blue-700 mb-2">
                                        {group.type}:
                                    </h5>
                                    <ul className="text-sm space-y-1">
                                        {group.cases.map((example, i) => (
                                            <li key={i} className="text-gray-600">
                                                • {example}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Funkcje kategoryzacji */}
                    <div>
                        <h4 className="font-medium mb-2 text-green-700">
                            Funkcje kategoryzacji:
                        </h4>
                        <div className="grid grid-cols-2 gap-4">
                            {categorizationFeatures.map((feature, idx) => (
                                <div key={idx} className="bg-green-50 p-3 rounded-lg">
                                    <h5 className="font-medium text-sm text-green-700 mb-1">
                                        {feature.title}
                                    </h5>
                                    <p className="text-sm text-green-600 mb-1">
                                        {feature.description}
                                    </p>
                                    <p className="text-xs text-green-500 italic">
                                        Przykład: {feature.example}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Ograniczenia */}
                    <div>
                        <h4 className="font-medium mb-2 text-yellow-700">
                            Ograniczenia kategoryzacji:
                        </h4>
                        <ul className="bg-yellow-50 p-3 rounded-lg space-y-1">
                            {limitations.map((limitation, idx) => (
                                <li key={idx} className="text-sm text-yellow-700">
                                    • {limitation}
                                </li>
                            ))}
                        </ul>
                    </div>

                    <div className="text-sm text-gray-500 italic">
                        Wskazówka: W przypadku problemów z kategoryzacja, możesz ręcznie edytować produkty po ich
                        dodaniu
                        do kategorii. Oraz oczywiście zgłoś.
                    </div>
                </div>
            )}
        </div>
    )
}

export default ParserGuide;