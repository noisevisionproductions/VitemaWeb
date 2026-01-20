import React from "react";
import { AlertCircle, AlertTriangle, Calendar, CheckCircle2, Clock, Info } from "lucide-react";
import GuideSection from "./GuideSection";
import InfoCard from "./InfoCard";

const WarningSystemGuide: React.FC = () => {
    return (
        <GuideSection
            title="System ostrzeżeń dla diet"
            icon={<AlertTriangle className="w-5 h-5 text-amber-600" />}
        >
            <div className="space-y-6">
                {/* Opis systemu */}
                <p className="text-slate-700 text-sm sm:text-base">
                    System ostrzeżeń dla diet automatycznie monitoruje terminy zakończenia diet i powiadamia
                    o dietach wymagających uwagi. Dzięki niemu nigdy nie przeoczysz terminu zakończenia diety klienta,
                    co pozwala na lepsze planowanie i zarządzanie dietami.
                </p>

                {/* Funkcje systemu */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 sm:gap-4">
                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="p-1 sm:p-2 rounded-full bg-blue-100">
                                <Calendar className="w-4 h-4 text-blue-600" />
                            </div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Wykrywanie terminów
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700">
                                System automatycznie oblicza liczbę dni pozostałych do końca diety i sprawdza,
                                czy istnieje już zaplanowana kontynuacja diety.
                            </p>
                        </div>
                    </div>

                    <div className="border rounded-lg p-3 sm:p-5 border-green-500 bg-green-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="p-1 sm:p-2 rounded-full bg-green-100">
                                <CheckCircle2 className="w-4 h-4 text-green-600" />
                            </div>
                            <h3 className="font-medium text-green-700 text-sm sm:text-base">
                                Ciągłość diet
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700">
                                System wykrywa, czy dla danego klienta istnieje już przypisana kolejna dieta,
                                która rozpocznie się po zakończeniu obecnej.
                            </p>
                        </div>
                    </div>

                    <div className="border rounded-lg p-3 sm:p-5 border-amber-500 bg-amber-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="p-1 sm:p-2 rounded-full bg-amber-100">
                                <AlertTriangle className="w-4 h-4 text-amber-600" />
                            </div>
                            <h3 className="font-medium text-amber-700 text-sm sm:text-base">
                                Powiadomienia
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700">
                                Ostrzeżenia są wyświetlane tylko dla diet kończących się w ciągu 3 dni i różnią się
                                w zależności od tego, czy jest już zaplanowana kontynuacja.
                            </p>
                        </div>
                    </div>
                </div>

                {/* Poziomy ostrzeżeń */}
                <h3 className="text-lg font-semibold mt-6 mb-4 text-slate-800">Poziomy ostrzeżeń</h3>

                <div className="space-y-4">
                    <InfoCard
                        title="Ostrzeżenie krytyczne (bez kontynuacji)"
                        color="yellow"
                        icon={<AlertCircle className="w-5 h-5 text-red-600" />}
                    >
                        <p className="text-sm text-slate-700 mb-3">
                            Dieta kończy się w ciągu najbliższych 24 godzin lub jutro, a klient nie ma zaplanowanej kolejnej diety.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                            <div className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-gray-500" />
                                <span><strong>Czas:</strong> 0-1 dzień</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-gray-500" />
                                <span><strong>Zalecane działanie:</strong> Natychmiast skontaktuj się z klientem</span>
                            </div>
                        </div>
                    </InfoCard>

                    <InfoCard
                        title="Ostrzeżenie z kontynuacją diety"
                        color="yellow"
                        icon={<AlertTriangle className="w-5 h-5 text-amber-600" />}
                    >
                        <p className="text-sm text-slate-700 mb-3">
                            Dieta kończy się w ciągu najbliższych 24 godzin lub jutro, ale klient ma już zaplanowaną kolejną dietę.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                            <div className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-gray-500" />
                                <span><strong>Czas:</strong> 0-1 dzień</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-gray-500" />
                                <span><strong>Zalecane działanie:</strong> Monitoruj przejście między dietami</span>
                            </div>
                        </div>
                    </InfoCard>

                    <InfoCard
                        title="Ostrzeżenie standardowe"
                        color="yellow"
                        icon={<AlertTriangle className="w-5 h-5 text-amber-600" />}
                    >
                        <p className="text-sm text-slate-700 mb-3">
                            Dieta kończy się w ciągu najbliższych 2-3 dni, a klient nie ma zaplanowanej kolejnej diety.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                            <div className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-gray-500" />
                                <span><strong>Czas:</strong> 2-3 dni</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-gray-500" />
                                <span><strong>Zalecane działanie:</strong> Zaplanuj kontakt z klientem</span>
                            </div>
                        </div>
                    </InfoCard>

                    <InfoCard
                        title="Kontynuacja diety"
                        color="green"
                        icon={<CheckCircle2 className="w-5 h-5 text-green-600" />}
                    >
                        <p className="text-sm text-slate-700 mb-3">
                            Dieta kończy się wkrótce, ale klient ma już zaplanowaną kolejną dietę, która zacznie się po zakończeniu obecnej.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                            <div className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-gray-500" />
                                <span><strong>Czas:</strong> 2-3 dni</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-gray-500" />
                                <span><strong>Zalecane działanie:</strong> Monitoruj przejście między dietami</span>
                            </div>
                        </div>
                    </InfoCard>

                    <InfoCard
                        title="Brak ostrzeżeń"
                        color="blue"
                        icon={<Info className="w-5 h-5 text-blue-600" />}
                    >
                        <p className="text-sm text-slate-700 mb-3">
                            Dieta ma więcej niż 3 dni do zakończenia lub jest już zakończona.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                            <div className="flex items-center gap-2">
                                <Clock className="w-4 h-4 text-gray-500" />
                                <span><strong>Czas:</strong> &gt;3 dni lub zakończona</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Calendar className="w-4 h-4 text-gray-500" />
                                <span><strong>Zalecane działanie:</strong> Nie wymaga natychmiastowych działań</span>
                            </div>
                        </div>
                    </InfoCard>
                </div>

                {/* Instrukcje korzystania */}
                <h3 className="text-lg font-semibold mt-6 mb-4 text-slate-800">Jak korzystać z systemu ostrzeżeń</h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">1</div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Sprawdzanie ostrzeżeń
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700 mb-2">
                                Na liście diet, zwróć uwagę na kolorowe oznaczenia:
                            </p>
                            <ul className="list-disc pl-5 text-sm space-y-1">
                                <li><span className="text-red-600 font-medium">Czerwone</span> - kończące się bez kontynuacji (0-1 dzień)</li>
                                <li><span className="text-amber-600 font-medium">Pomarańczowe</span> - kończące się wkrótce (2-3 dni)</li>
                                <li><span className="text-green-600 font-medium">Zielone</span> - kończące się z zaplanowaną kontynuacją</li>
                                <li><span className="text-blue-600 font-medium">Niebieskie</span> - informacyjne (np. dla diet zakończonych)</li>
                            </ul>
                        </div>
                    </div>

                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">2</div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Filtrowanie diet
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700 mb-2">
                                Korzystaj z dostępnych filtrów:
                            </p>
                            <ul className="list-disc pl-5 text-sm space-y-1">
                                <li><span className="font-medium">Ostrzeżenia</span> - pokaże diety kończące się w ciągu 3 dni</li>
                                <li><span className="font-medium">Bez kontynuacji</span> - pokaże diety, dla których nie ma zaplanowanej kontynuacji</li>
                            </ul>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4 mt-3">
                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">3</div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Szczegóły diety
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700 mb-2">
                                W widoku szczegółowym diety zobaczysz:
                            </p>
                            <ul className="list-disc pl-5 text-sm space-y-1">
                                <li>Dokładny czas pozostały do końca diety</li>
                                <li>Informację o kolejnej diecie (jeśli istnieje)</li>
                                <li>Datę rozpoczęcia kolejnej diety</li>
                                <li>Liczbę dni przerwy między dietami (jeśli występuje)</li>
                            </ul>
                        </div>
                    </div>

                    <div className="border rounded-lg p-3 sm:p-5 border-blue-500 bg-blue-50 shadow-sm">
                        <div className="flex items-center gap-2 mb-2 sm:mb-3">
                            <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">4</div>
                            <h3 className="font-medium text-blue-700 text-sm sm:text-base">
                                Powiadomienia email
                            </h3>
                        </div>
                        <div>
                            <p className="text-sm text-slate-700 mb-2">
                                System codziennie wysyła powiadomienia email o:
                            </p>
                            <ul className="list-disc pl-5 text-sm space-y-1">
                                <li>Dietach kończących się w ciągu najbliższych 3 dni</li>
                                <li>Dietach bez zaplanowanej kontynuacji</li>
                                <li>Szczegółach dotyczących klientów, z którymi należy się skontaktować</li>
                            </ul>
                        </div>
                    </div>
                </div>

                {/* Wskazówki */}
                <InfoCard
                    title="Optymalne wykorzystanie systemu ostrzeżeń"
                    color="blue"
                    icon={<Info className="w-5 h-5 text-blue-600" />}
                >
                    <ul className="space-y-2 pl-2">
                        <li className="flex items-start gap-2">
                            <div className="min-w-5 mt-0.5">✓</div>
                            <p className="text-sm">
                                <strong>Planuj z wyprzedzeniem</strong> - twórz i przypisuj kontynuacje diet na co najmniej 3 dni przed końcem obecnej diety.
                            </p>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="min-w-5 mt-0.5">✓</div>
                            <p className="text-sm">
                                <strong>Sprawdzaj powiadomienia email</strong> - codziennie rano otrzymasz podsumowanie diet wymagających uwagi.
                            </p>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="min-w-5 mt-0.5">✓</div>
                            <p className="text-sm">
                                <strong>Priorytetyzuj kontakty</strong> - najpierw skontaktuj się z klientami, których diety kończą się najwcześniej i nie mają kontynuacji.
                            </p>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="min-w-5 mt-0.5">✓</div>
                            <p className="text-sm">
                                <strong>Monitoruj przejścia</strong> - nawet gdy klient ma zaplanowaną kontynuację, warto monitorować płynne przejście między dietami.
                            </p>
                        </li>
                        <li className="flex items-start gap-2">
                            <div className="min-w-5 mt-0.5">✓</div>
                            <p className="text-sm">
                                <strong>Dbaj o ciągłość</strong> - unikaj przerw między dietami, najlepiej planując diety "na zakładkę", aby rozpoczynały się w dniu zakończenia poprzedniej.
                            </p>
                        </li>
                    </ul>
                </InfoCard>
            </div>
        </GuideSection>
    );
};

export default WarningSystemGuide;