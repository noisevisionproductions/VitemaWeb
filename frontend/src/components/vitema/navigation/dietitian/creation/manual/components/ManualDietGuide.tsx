import {
    BookOpen,
    Camera,
    ChevronDown,
    ChevronUp, Clock, Copy,
    HelpCircle, Lightbulb,
    Save,
    Search,
    ShoppingCart,
    Sparkles,
    Users
} from "lucide-react";
import React, {useState} from "react";

interface ManualDietGuideProps {
    className?: string;
}

const ManualDietGuide: React.FC<ManualDietGuideProps> = ({
                                                             className = ''
                                                         }) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const [activeSection, setActiveSection] = useState<string | null>(null);

    const toggleSection = (sectionId: string) => {
        setActiveSection(activeSection === sectionId ? null : sectionId);
    };

    const GuideSection = ({
                              id,
                              title,
                              icon: Icon,
                              children,
                              color
                          }: {
        id: string;
        title: string;
        icon: React.ElementType;
        children: React.ReactNode;
        color: string;
    }) => (
        <div className="border border-gray-200 rounded-lg overflow-hidden">
            <button
                onClick={() => toggleSection(id)}
                className={`w-full px-4 py-3 text-left hover:bg-gray-50 transition-colors flex items-center justify-between ${color}`}
            >
                <div className="flex items-center gap-3">
                    <Icon className="h-5 w-5"/>
                    <span className="font-medium">{title}</span>
                </div>
                {activeSection === id ? (
                    <ChevronUp className="h-4 w-4 text-gray-400"/>
                ) : (
                    <ChevronDown className="h-4 w-4 text-gray-400"/>
                )}
            </button>
            {activeSection === id && (
                <div className="px-4 pb-4 bg-gray-50/50">
                    {children}
                </div>
            )}
        </div>
    );

    return (
        <div className={`bg-blue-50 border border-blue-200 rounded-xl ${className}`}>
            {/* Header */}

            <div
                className="p-4 cursor-pointer hover:bg-blue-100/50 transition-colors"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-blue-500 rounded-lg">
                            <HelpCircle className="h-5 w-5 text-white"/>
                        </div>
                        <div>
                            <h3 className="font-semibold text-blue-900">
                                📚 Przewodnik tworzenia diet ręcznych
                            </h3>
                            <p className="text-sm text-blue-700 mt-1">
                                Dowiedz się jak efektywnie korzystać z kreatora diet i systemu szablonów
                            </p>
                        </div>
                    </div>
                    <div className="flex items-center gap-2">
                        <span className="px-3 py-1 bg-blue-500 text-white text-xs rounded-full font-medium">
                            Przewodnik
                        </span>
                        {isExpanded ? (
                            <ChevronUp className="h-5 w-5 text-blue-600"/>
                        ) : (
                            <ChevronDown className="h-5 w-5 text-blue-600"/>
                        )}
                    </div>
                </div>
            </div>

            {/* Expanded Content */}
            {isExpanded && (
                <div className="px-4 pb-4 border-t border-blue-200">
                    <div className="mt-4 space-y-4">

                        {/* Podstawy */}
                        <GuideSection
                            id="basics"
                            title="🚀 Jak działa kreator diet?"
                            icon={Lightbulb}
                            color="text-green-700"
                        >
                            <div className="space-y-3 py-2 text-sm text-gray-700">
                                <div className="bg-white p-3 rounded-lg border-l-4 border-green-500">
                                    <p className="font-medium text-green-800 mb-2">Kreator przeprowadzi Cię przez 4
                                        proste kroki:</p>
                                    <ol className="list-decimal list-inside space-y-1 text-green-700">
                                        <li><strong>Konfiguracja</strong> - wybierz klienta i ustaw parametry diety</li>
                                        <li><strong>Planowanie</strong> - zaplanuj posiłki dla każdego dnia</li>
                                        <li><strong>Kategoryzacja</strong> - uporządkuj składniki na liście zakupów</li>
                                        <li><strong>Podgląd</strong> - sprawdź wszystko przed zapisem</li>
                                    </ol>
                                </div>
                                <p className="flex items-start gap-2">
                                    <Sparkles className="h-4 w-4 text-blue-500 mt-0.5 flex-shrink-0"/>
                                    System automatycznie zapisuje Twoje posiłki jako <strong>szablony</strong> do
                                    przyszłego użycia!
                                </p>
                            </div>
                        </GuideSection>

                        {/* Szablony */}
                        <GuideSection
                            id="templates"
                            title="🔄 Czym są szablony posiłków?"
                            icon={BookOpen}
                            color="text-purple-700"
                        >
                            <div className="space-y-3 py-2 text-sm text-gray-700">
                                <div className="bg-white p-3 rounded-lg border-l-4 border-purple-500">
                                    <p className="font-medium text-purple-800 mb-2">Szablon to "przepis wielokrotnego
                                        użytku" zawierający:</p>
                                    <ul className="list-disc list-inside space-y-1 text-purple-700">
                                        <li>Nazwę posiłku i instrukcje przygotowania</li>
                                        <li>Listę składników z dokładnymi ilościami</li>
                                        <li>Wartości odżywcze i zdjęcia</li>
                                        <li>Informacje o popularności (ile razy używany)</li>
                                    </ul>
                                </div>
                                <div className="flex items-start gap-2">
                                    <Save className="h-4 w-4 text-purple-500 mt-0.5 flex-shrink-0"/>
                                    <div>
                                        <p className="font-medium">Automatyczne zapisywanie:</p>
                                        <p className="text-gray-600">Gdy stworzysz nowy posiłek, system automatycznie
                                            zapisze go jako szablon. Następnym razem wystarczy zacząć pisać nazwę, a
                                            system podpowie gotowy przepis!</p>
                                    </div>
                                </div>
                            </div>
                        </GuideSection>

                        {/* Wyszukiwanie */}
                        <GuideSection
                            id="search"
                            title="🔍 Jak działa inteligentne wyszukiwanie?"
                            icon={Search}
                            color="text-blue-700"
                        >
                            <div className="space-y-3 py-2 text-sm text-gray-700">
                                <div className="bg-white p-3 rounded-lg border-l-4 border-blue-500">
                                    <p className="font-medium text-blue-800 mb-2">System wyszukuje w dwóch źródłach:</p>
                                    <div className="space-y-2">
                                        <div className="flex items-center gap-2 text-blue-700">
                                            <Users className="h-4 w-4 text-green-600"/>
                                            <span><strong>Szablony</strong> - Twoje wcześniej zapisane posiłki</span>
                                        </div>
                                        <div className="flex items-center gap-2 text-blue-700">
                                            <BookOpen className="h-4 w-4 text-blue-600"/>
                                            <span><strong>Przepisy</strong> - Z bazy przepisów systemu</span>
                                        </div>
                                    </div>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                    <div className="bg-green-50 p-3 rounded-lg">
                                        <p className="font-medium text-green-800 mb-1">💡 Wskazówka:</p>
                                        <p className="text-green-700 text-xs">Im częściej używasz szablonu, tym wyżej
                                            pojawia się w wynikach wyszukiwania</p>
                                    </div>
                                    <div className="bg-yellow-50 p-3 rounded-lg">
                                        <p className="font-medium text-yellow-800 mb-1">⚡ Szybkość:</p>
                                        <p className="text-yellow-700 text-xs">Wpisz minimum 2 litery, a system od razu
                                            zacznie podpowiadać</p>
                                    </div>
                                </div>
                            </div>
                        </GuideSection>

                        {/* Składniki */}
                        <GuideSection
                            id="ingredients"
                            title="🥕 Dodawanie składników"
                            icon={ShoppingCart}
                            color="text-orange-700"
                        >
                            <div className="space-y-3 py-2 text-sm text-gray-700">
                                <div className="bg-white p-3 rounded-lg border-l-4 border-orange-500">
                                    <p className="font-medium text-orange-800 mb-2">Dodawanie składników jest bardzo
                                        elastyczne:</p>
                                    <div className="space-y-2 text-orange-700">
                                        <p><strong>Sposób 1:</strong> Wpisz "mleko 200ml" - system automatycznie
                                            rozpozna nazwę i ilość</p>
                                        <p><strong>Sposób 2:</strong> Wpisz tylko "mleko" i wybierz z podpowiedzi</p>
                                        <p><strong>Sposób 3:</strong> Jeśli składnika nie ma, zostanie automatycznie
                                            utworzony</p>
                                    </div>
                                </div>
                                <div className="bg-orange-50 p-3 rounded-lg">
                                    <p className="font-medium text-orange-800 mb-1">🎯 Przykłady prawidłowego
                                        formatowania:</p>
                                    <ul className="text-xs text-orange-700 space-y-1">
                                        <li>• "banan 2 szt"</li>
                                        <li>• "mąka pszenna 300g"</li>
                                        <li>• "oliwa z oliwek 2 łyżki"</li>
                                        <li>• "ser żółty 100 gram"</li>
                                    </ul>
                                </div>
                            </div>
                        </GuideSection>

                        {/* Funkcje dodatkowe */}
                        <GuideSection
                            id="features"
                            title="⚡ Przydatne funkcje"
                            icon={Sparkles}
                            color="text-indigo-700"
                        >
                            <div className="space-y-3 py-2 text-sm text-gray-700">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                    <div className="bg-white p-3 rounded-lg border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                                            <Copy className="h-4 w-4 text-blue-500"/>
                                            <span className="font-medium text-indigo-800">Kopiowanie posiłków</span>
                                        </div>
                                        <p className="text-xs text-gray-600">Kliknij ikonę kopiowania przy posiłku, aby
                                            skopiować go do innych dni z danej pory dnia</p>
                                    </div>
                                    <div className="bg-white p-3 rounded-lg border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                                            <Camera className="h-4 w-4 text-green-500"/>
                                            <span className="font-medium text-indigo-800">Zdjęcia posiłków</span>
                                        </div>
                                        <p className="text-xs text-gray-600">Dodaj zdjęcia do posiłków - będą zapisane w
                                            szablonie na przyszłość</p>
                                    </div>
                                    <div className="bg-white p-3 rounded-lg border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                                            <Clock className="h-4 w-4 text-purple-500"/>
                                            <span className="font-medium text-indigo-800">Auto-zapisywanie</span>
                                        </div>
                                        <p className="text-xs text-gray-600">System automatycznie zapisuje postęp
                                            szablonów (nie strony!)</p>
                                    </div>
                                    <div className="bg-white p-3 rounded-lg border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                                            <ShoppingCart className="h-4 w-4 text-orange-500"/>
                                            <span className="font-medium text-indigo-800">Lista zakupów</span>
                                        </div>
                                        <p className="text-xs text-gray-600">Automatycznie generowana z wszystkich
                                            składników diety</p>
                                    </div>
                                </div>
                            </div>
                        </GuideSection>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ManualDietGuide;