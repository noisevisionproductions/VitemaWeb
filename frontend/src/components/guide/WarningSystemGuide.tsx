import React from "react";
import {Card, CardContent, CardHeader, CardTitle} from "../ui/card";
import {AlertCircle, AlertTriangle, Calendar, Clock, Info} from "lucide-react";

interface GuideCardProps {
    title: string;
    icon: React.ReactNode;
    children: React.ReactNode;
    color: "red" | "amber" | "blue" | "gray";
}

const GuideCard: React.FC<GuideCardProps> = ({title, icon, children, color}) => {
    const colors = {
        red: "bg-red-50 border-red-100",
        amber: "bg-amber-50 border-amber-100",
        blue: "bg-blue-50 border-blue-100",
        gray: "bg-slate-50 border-slate-100"
    };

    const titleColors = {
        red: "text-red-700",
        amber: "text-amber-700",
        blue: "text-blue-700",
        gray: "text-slate-700"
    };

    return (
        <div className={`rounded-lg border p-4 ${colors[color]}`}>
            <div className="flex items-center gap-2 mb-3">
                {icon}
                <h3 className={`font-semibold ${titleColors[color]}`}>{title}</h3>
            </div>
            <div className="space-y-2">
                {children}
            </div>
        </div>
    );
};

const WarningLevelCard: React.FC<{
    level: "critical" | "warning" | "normal",
    title: string,
    description: string,
    timeFrame: string,
    action: string
}> = ({level, title, description, timeFrame, action}) => {
    const getLevelStyles = () => {
        switch (level) {
            case "critical":
                return {
                    bg: "bg-red-50",
                    border: "border-red-200",
                    icon: <AlertCircle className="w-5 h-5 text-red-600" />,
                    textColor: "text-red-800"
                };
            case "warning":
                return {
                    bg: "bg-amber-50",
                    border: "border-amber-200",
                    icon: <AlertTriangle className="w-5 h-5 text-amber-600" />,
                    textColor: "text-amber-800"
                };
            case "normal":
            default:
                return {
                    bg: "bg-gray-50",
                    border: "border-gray-200",
                    icon: <Info className="w-5 h-5 text-gray-600" />,
                    textColor: "text-gray-800"
                };
        }
    };

    const styles = getLevelStyles();

    return (
        <div className={`${styles.bg} ${styles.border} border rounded-lg p-4`}>
            <div className="flex items-center gap-2 mb-2">
                {styles.icon}
                <h3 className={`font-semibold ${styles.textColor}`}>{title}</h3>
            </div>
            <p className="text-sm mb-3">{description}</p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-gray-500" />
                    <span><strong>Czas:</strong> {timeFrame}</span>
                </div>
                <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-gray-500" />
                    <span><strong>Zalecane działanie:</strong> {action}</span>
                </div>
            </div>
        </div>
    );
};

const WarningSystemGuide: React.FC = () => {
    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle>System Ostrzeżeń dla Diet</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                {/* Opis systemu */}
                <section>
                    <h2 className="text-xl font-semibold mb-4">Jak działa system ostrzeżeń?</h2>
                    <p className="mb-4">
                        System ostrzeżeń dla diet automatycznie monitoruje terminy zakończenia diet i powiadamia
                        o dietach wymagających uwagi. Dzięki niemu nigdy nie przeoczysz terminu zakończenia diety klienta,
                        co pozwala na lepsze planowanie i zarządzanie dietami.
                    </p>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                        <GuideCard
                            title="Wykrywanie terminów"
                            icon={<Calendar className="w-5 h-5 text-blue-600" />}
                            color="blue"
                        >
                            <p className="text-sm">
                                System automatycznie oblicza liczbę dni pozostałych do końca diety na podstawie
                                daty ostatniego dnia diety i aktualnej daty.
                            </p>
                        </GuideCard>

                        <GuideCard
                            title="Ostrzeżenia wizualne"
                            icon={<AlertTriangle className="w-5 h-5 text-amber-600" />}
                            color="amber"
                        >
                            <p className="text-sm">
                                Diety kończące się wkrótce są wyraźnie oznaczone kolorowym podświetleniem i ikonami
                                ostrzegawczymi, dzięki czemu łatwo je zauważyć.
                            </p>
                        </GuideCard>

                        <GuideCard
                            title="Filtrowanie i sortowanie"
                            icon={<Clock className="w-5 h-5 text-red-600" />}
                            color="red"
                        >
                            <p className="text-sm">
                                Możesz łatwo filtrować diety wymagające uwagi i sortować je według pilności,
                                by szybko zobaczyć, które diety wymagają natychmiastowego działania.
                            </p>
                        </GuideCard>
                    </div>
                </section>

                {/* Poziomy ostrzeżeń */}
                <section className="pt-4">
                    <h2 className="text-xl font-semibold mb-4">Poziomy ostrzeżeń</h2>
                    <div className="space-y-4">
                        <WarningLevelCard
                            level="critical"
                            title="Ostrzeżenie krytyczne"
                            description="Dieta kończy się w ciągu najbliższych 24 godzin lub jutro. Wymaga natychmiastowej uwagi."
                            timeFrame="0-1 dzień"
                            action="Natychmiast skontaktuj się z klientem"
                        />

                        <WarningLevelCard
                            level="warning"
                            title="Ostrzeżenie standardowe"
                            description="Dieta kończy się w ciągu najbliższych 2-3 dni. Należy zaplanować kontakt z klientem."
                            timeFrame="2-3 dni"
                            action="Zaplanuj kontakt z klientem"
                        />

                        <WarningLevelCard
                            level="normal"
                            title="Brak ostrzeżeń"
                            description="Dieta ma więcej niż 3 dni do zakończenia lub jest już zakończona."
                            timeFrame=">3 dni lub zakończona"
                            action="Nie wymaga natychmiastowych działań"
                        />
                    </div>
                </section>

                {/* Instrukcje korzystania */}
                <section className="pt-4">
                    <h2 className="text-xl font-semibold mb-4">Jak korzystać z systemu ostrzeżeń</h2>

                    <div className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="border rounded-lg p-4 bg-white">
                                <h3 className="font-semibold mb-2 flex items-center gap-2">
                                    <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">1</div>
                                    Sprawdzanie ostrzeżeń
                                </h3>
                                <p className="text-sm mb-2">
                                    Na liście diet, zwróć uwagę na kolorowe oznaczenia:
                                </p>
                                <ul className="list-disc pl-5 text-sm space-y-1">
                                    <li><span className="text-red-600 font-medium">Czerwone</span> - diety kończące się w ciągu 24h</li>
                                    <li><span className="text-amber-600 font-medium">Pomarańczowe</span> - diety kończące się w ciągu 2-3 dni</li>
                                    <li>Brak koloru - diety z dłuższym terminem lub zakończone</li>
                                </ul>
                            </div>

                            <div className="border rounded-lg p-4 bg-white">
                                <h3 className="font-semibold mb-2 flex items-center gap-2">
                                    <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">2</div>
                                    Filtrowanie diet wymagających uwagi
                                </h3>
                                <p className="text-sm mb-2">
                                    Kliknij przycisk "Ostrzeżenia" w górnej części strony, aby zobaczyć tylko diety wymagające uwagi.
                                </p>
                                <p className="text-sm">
                                    System automatycznie wyświetli diety z ostrzeżeniami i posortuje je według pilności.
                                </p>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="border rounded-lg p-4 bg-white">
                                <h3 className="font-semibold mb-2 flex items-center gap-2">
                                    <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">3</div>
                                    Szczegóły diety
                                </h3>
                                <p className="text-sm mb-2">
                                    Po kliknięciu w dietę, w widoku szczegółowym zobaczysz dokładną informację
                                    o czasie pozostałym do końca diety oraz sugerowane działania.
                                </p>
                                <p className="text-sm">
                                    Zakończone dni są odpowiednio oznaczone przekreśleniem.
                                </p>
                            </div>

                            <div className="border rounded-lg p-4 bg-white">
                                <h3 className="font-semibold mb-2 flex items-center gap-2">
                                    <div className="flex justify-center items-center w-6 h-6 rounded-full bg-blue-100 text-blue-700 font-bold text-sm">4</div>
                                    Kontakt z klientem
                                </h3>
                                <p className="text-sm">
                                    Na podstawie ostrzeżeń, skontaktuj się z klientem, aby:
                                </p>
                                <ul className="list-disc pl-5 text-sm space-y-1 mt-2">
                                    <li>Zaproponować przedłużenie diety</li>
                                    <li>Przygotować nową dietę</li>
                                    <li>Zebrać informacje zwrotne o obecnej diecie</li>
                                    <li>Zaplanować dalsze kroki</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Wskazówki */}
                <section className="pt-4">
                    <h2 className="text-xl font-semibold mb-4">Wskazówki i dobre praktyki</h2>

                    <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                        <h3 className="font-semibold mb-3 flex items-center gap-2">
                            <Info className="w-5 h-5 text-blue-600" />
                            Optymalne wykorzystanie systemu ostrzeżeń
                        </h3>

                        <ul className="space-y-2">
                            <li className="flex items-start gap-2">
                                <div className="min-w-5 mt-0.5">✓</div>
                                <p className="text-sm">
                                    <strong>Regularnie sprawdzaj ostrzeżenia</strong> - zaplanuj czas w ciągu dnia, aby przejrzeć diety z ostrzeżeniami.
                                </p>
                            </li>
                            <li className="flex items-start gap-2">
                                <div className="min-w-5 mt-0.5">✓</div>
                                <p className="text-sm">
                                    <strong>Kontaktuj się z wyprzedzeniem</strong> - nie czekaj do ostatniego dnia diety, aby skontaktować się z klientem.
                                </p>
                            </li>
                            <li className="flex items-start gap-2">
                                <div className="min-w-5 mt-0.5">✓</div>
                                <p className="text-sm">
                                    <strong>Reaguj priorytetowo</strong> - najpierw zajmij się dietami oznaczonymi czerwonym kolorem.
                                </p>
                            </li>
                            <li className="flex items-start gap-2">
                                <div className="min-w-5 mt-0.5">✓</div>
                                <p className="text-sm">
                                    <strong>Notuj informacje zwrotne</strong> - zapisuj uwagi klientów dotyczące diet kończących się, aby lepiej dostosować przyszłe diety.
                                </p>
                            </li>
                            <li className="flex items-start gap-2">
                                <div className="min-w-5 mt-0.5">✓</div>
                                <p className="text-sm">
                                    <strong>Przygotuj propozycje przedłużenia</strong> - miej gotowe opcje kontynuacji dla klientów zbliżających się do końca diety.
                                </p>
                            </li>
                        </ul>
                    </div>
                </section>
            </CardContent>
        </Card>
    );
};

export default WarningSystemGuide;