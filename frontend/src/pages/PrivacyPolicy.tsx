import React from 'react';

interface PrivacyPolicySection {
    id: string;
    title: string;
    content: React.ReactNode;
}

const PrivacyPolicy: React.FC = () => {
    // Data ostatniej aktualizacji
    const lastUpdate = "19 styczeń 2026 r.";

    // Adres email do kontaktu
    const contactEmail = "kontakt@vitema.pl";

    // Definicja sekcji polityki prywatności
    const privacyPolicySections: PrivacyPolicySection[] = [
        {
            id: "introduction",
            title: "1. Wprowadzenie",
            content: (
                <>
                    <p className="mb-4">
                        Vitema ("my", "nas", "nasz") szanuje Twoją prywatność i zobowiązuje się do jej ochrony.
                        Niniejsza Polityka Prywatności wyjaśnia, w jaki sposób gromadzimy, wykorzystujemy i chronimy
                        Twoje dane osobowe podczas korzystania z naszej aplikacji internetowej, aplikacji mobilnej i
                        usług.
                    </p>
                    <p className="mb-4">
                        Vitema to platforma przeznaczona dla dietetyków i ich klientów, umożliwiająca zarządzanie
                        dietami i monitorowanie postępów. Rozumiemy, że informacje dotyczące zdrowia i diety są
                        wyjątkowo wrażliwe, dlatego przykładamy szczególną wagę do ich ochrony.
                    </p>
                    <p className="mb-4">
                        Prosimy o dokładne zapoznanie się z niniejszą Polityką Prywatności, aby zrozumieć, jak
                        przetwarzamy Twoje dane. Korzystając z naszych usług, wyrażasz zgodę na praktyki opisane w tym
                        dokumencie.
                    </p>
                </>
            )
        },
        {
            id: "data-collection",
            title: "2. Gromadzenie danych",
            content: (
                <>
                    <p className="mb-4">
                        Gromadzimy dane osobowe, które są niezbędne do świadczenia naszych usług i zapewnienia ich
                        prawidłowego funkcjonowania. W zależności od tego, czy jesteś dietetykiem, czy klientem
                        dietetyka, możemy gromadzić różne rodzaje danych:
                    </p>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Dane dietetyków:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Dane identyfikacyjne i kontaktowe (imię, nazwisko, adres email, numer telefonu)</li>
                        <li>Dane do rozliczeń i płatności związane z subskrypcją</li>
                        <li>Informacje o preferencjach dotyczących usług</li>
                    </ul>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Dane klientów dietetyków:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Dane identyfikacyjne i kontaktowe (imię, nazwisko, adres email)</li>
                        <li>Informacje zdrowotne i dietetyczne niezbędne do opracowania indywidualnego planu
                            żywieniowego (waga, wzrost, choroby, alergie, nietolerancje pokarmowe, preferencje
                            żywieniowe)
                        </li>
                        <li>Dane dotyczące postępów i realizacji planu dietetycznego</li>
                        <li>Inne dane uznane przez dietetyka za istotne w procesie układania i monitorowania diety</li>
                    </ul>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Dane techniczne i użytkowania:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Informacje o urządzeniu i przeglądarce</li>
                        <li>Adres IP i dane geolokalizacyjne</li>
                        <li>Informacje o sesji i aktywności w aplikacji</li>
                        <li>Dane diagnostyczne aplikacji (logi, raporty o błędach)</li>
                        <li>Pliki cookies i podobne technologie śledzące</li>
                    </ul>

                    <p className="mb-4">
                        Dane są gromadzone bezpośrednio od użytkowników podczas rejestracji, uzupełniania profilu,
                        korzystania z aplikacji oraz poprzez automatyczne mechanizmy monitorujące działanie naszych
                        usług.
                    </p>
                </>
            )
        },
        {
            id: "data-processing-purpose",
            title: "3. Cel przetwarzania danych",
            content: (
                <>
                    <p className="mb-4">
                        Przetwarzamy Twoje dane osobowe w następujących celach:
                    </p>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Podstawowe cele przetwarzania:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Świadczenie usług Vitema dla dietetyków i ich klientów</li>
                        <li>Umożliwienie dietetykom tworzenia, zarządzania i monitorowania planów dietetycznych dla
                            klientów
                        </li>
                        <li>Synchronizacja danych między aplikacją webową a mobilną</li>
                        <li>Realizacja płatności i zarządzanie subskrypcjami</li>
                        <li>Uwierzytelnianie użytkowników i zabezpieczenie dostępu do kont</li>
                        <li>Komunikacja z użytkownikami w sprawach dotyczących usługi</li>
                    </ul>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Cele dodatkowe:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Analiza i poprawa jakości naszych usług</li>
                        <li>Wykrywanie i zapobieganie błędom oraz problemom technicznym</li>
                        <li>Wysyłanie informacji o aktualizacjach, nowych funkcjach i ofertach (w przypadku wyrażenia
                            zgody)
                        </li>
                        <li>Przeprowadzanie ankiet i badań użyteczności</li>
                        <li>Przestrzeganie zobowiązań prawnych i regulacyjnych</li>
                    </ul>

                    <p className="mb-4">
                        W przypadku dietetyków, dane są wykorzystywane również do zarządzania subskrypcją i dostępem do
                        różnych funkcji platformy, zgodnie z wybranym planem.
                    </p>

                    <p className="mb-4">
                        W przypadku klientów dietetyków, ich dane są przetwarzane przede wszystkim w celu umożliwienia
                        dietetykom skutecznego świadczenia usług dietetycznych, w tym tworzenia spersonalizowanych
                        planów żywieniowych, monitorowania postępów i dostosowywania zaleceń.
                    </p>
                </>
            )
        },
        {
            id: "newsletter",
            title: "4. Newsletter",
            content: (
                <>
                    <p className="mb-4">
                        Jeśli wyrazisz na to zgodę, będziemy używać Twojego adresu email do wysyłania newslettera
                        zawierającego:
                    </p>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Informacje o nowych funkcjach i aktualizacjach platformy Vitema</li>
                        <li>Porady dotyczące efektywnego wykorzystania naszych narzędzi</li>
                        <li>Materiały edukacyjne związane z dietetyką i zarządzaniem praktyką dietetyczną</li>
                        <li>Promocje i oferty specjalne dla użytkowników Vitema</li>
                        <li>Zaproszenia do udziału w badaniach i ankietach</li>
                    </ul>

                    <p className="mb-4">
                        Częstotliwość wysyłki newslettera nie przekroczy dwóch wiadomości w miesiącu. Staramy się, aby
                        treści były wartościowe i odpowiadały Twoim potrzebom jako użytkownika naszej platformy.
                    </p>

                    <p className="mb-4">
                        W każdej chwili możesz zrezygnować z otrzymywania naszego newslettera poprzez:
                    </p>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Kliknięcie linku "Wypisz się" znajdującego się w stopce każdego newslettera</li>
                        <li>Odwiedzenie strony <a href="/unsubscribe" className="text-primary hover:underline">Rezygnacja
                            z newslettera</a></li>
                        <li>Kontakt z nami pod adresem <a href={`mailto:${contactEmail}`}
                                                          className="text-primary hover:underline">{contactEmail}</a>
                        </li>
                    </ul>

                    <p className="mb-4">
                        Po rezygnacji z newslettera Twój adres email zostanie usunięty z naszej listy mailingowej w
                        ciągu 7 dni roboczych. Pamiętaj, że rezygnacja z newslettera nie wpływa na Twoje konto w
                        serwisie Vitema ani na żadne inne aspekty korzystania z naszych usług.
                    </p>
                </>
            )
        },
        {
            id: "data-sharing",
            title: "5. Udostępnianie danych",
            content: (
                <>
                    <p className="mb-4">
                        Nie sprzedajemy, nie wymieniamy ani nie przekazujemy Twoich danych osobowych stronom trzecim bez
                        Twojej zgody, z wyjątkiem przypadków opisanych poniżej.
                    </p>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Udostępnianie w ramach świadczenia usług:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Dane klientów są udostępniane przypisanym im dietetykom w celu świadczenia usług
                            dietetycznych
                        </li>
                        <li>Korzystamy z usług Firebase (Google) do przechowywania i przetwarzania danych (Firestore,
                            Storage, Auth, Functions, Cloud, Analytics, Crashlytics)
                        </li>
                        <li>Możemy korzystać z dostawców usług płatniczych do obsługi subskrypcji</li>
                    </ul>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Inne przypadki udostępniania danych:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Gdy jest to wymagane przez prawo (np. w odpowiedzi na nakaz sądowy lub wezwanie organu
                            państwowego)
                        </li>
                        <li>W celu ochrony naszych praw, własności lub bezpieczeństwa, naszych użytkowników lub innych
                            osób
                        </li>
                        <li>W przypadku fuzji, przejęcia lub sprzedaży części lub całości naszej firmy, z zastrzeżeniem
                            że nabywca będzie zobowiązany do przestrzegania warunków niniejszej Polityki Prywatności
                        </li>
                    </ul>

                    <p className="mb-4">
                        Wszyscy dostawcy usług, z których korzystamy do przetwarzania danych, są zobowiązani do
                        zachowania poufności i bezpieczeństwa Twoich informacji oraz wykorzystywania ich wyłącznie w
                        celu świadczenia określonych usług.
                    </p>

                    <p className="mb-4">
                        W przypadku międzynarodowego transferu danych zapewniamy odpowiednie zabezpieczenia zgodnie z
                        przepisami o ochronie danych osobowych.
                    </p>
                </>
            )
        },
        {
            id: "data-security",
            title: "6. Bezpieczeństwo danych",
            content: (
                <>
                    <p className="mb-4">
                        Bezpieczeństwo Twoich danych jest dla nas priorytetem. Wdrożyliśmy odpowiednie środki techniczne
                        i organizacyjne, aby chronić Twoje dane osobowe przed nieuprawnionym dostępem, utratą,
                        modyfikacją, ujawnieniem lub zniszczeniem.
                    </p>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Nasze środki bezpieczeństwa obejmują:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Szyfrowanie danych w bazie Firebase - dane klientów dietetyków oraz dane osobowe dietetyków
                            są przechowywane w formie zaszyfrowanej
                        </li>
                        <li>Bezpieczną autoryzację i uwierzytelnianie użytkowników z wykorzystaniem Firebase
                            Authentication
                        </li>
                        <li>Regularne kopie zapasowe dla zapewnienia integralności i dostępności danych</li>
                        <li>Monitorowanie i testowanie naszych systemów pod kątem potencjalnych luk w zabezpieczeniach
                        </li>
                        <li>Dostęp do danych osobowych wyłącznie dla upoważnionych pracowników/współpracowników na
                            zasadzie "niezbędnej wiedzy"
                        </li>
                        <li>Stosowanie najlepszych praktyk branżowych w zakresie bezpieczeństwa aplikacji webowych i
                            mobilnych
                        </li>
                    </ul>

                    <p className="mb-4">
                        Mimo że stosujemy zaawansowane środki bezpieczeństwa, żadna metoda transmisji danych przez
                        Internet ani metoda elektronicznego przechowywania nie jest w 100% bezpieczna. Dlatego, choć
                        dążymy do ochrony Twoich danych osobowych, nie możemy zagwarantować ich całkowitego
                        bezpieczeństwa.
                    </p>

                    <p className="mb-4">
                        Zachęcamy również do podejmowania własnych działań na rzecz ochrony danych, takich jak
                        stosowanie silnych haseł, regularna ich zmiana oraz nieudostępnianie danych logowania osobom
                        trzecim.
                    </p>
                </>
            )
        },
        {
            id: "your-rights",
            title: "7. Twoje prawa",
            content: (
                <>
                    <p className="mb-4">
                        Zgodnie z przepisami o ochronie danych osobowych, w szczególności RODO (Rozporządzenie Ogólne o
                        Ochronie Danych), przysługują Ci następujące prawa:
                    </p>

                    <ul className="list-disc pl-6 mb-4">
                        <li><strong>Prawo dostępu</strong> - masz prawo uzyskać informację, czy przetwarzamy Twoje dane
                            osobowe, a jeśli tak, uzyskać do nich dostęp oraz otrzymać informacje o celach przetwarzania
                            i kategoriach przetwarzanych danych.
                        </li>

                        <li><strong>Prawo do sprostowania</strong> - masz prawo żądać poprawienia nieprawidłowych danych
                            osobowych lub uzupełnienia niekompletnych danych.
                        </li>

                        <li><strong>Prawo do usunięcia ("prawo do bycia zapomnianym")</strong> - w określonych
                            okolicznościach masz prawo żądać usunięcia swoich danych osobowych, na przykład gdy dane nie
                            są już niezbędne do celów, dla których zostały zebrane.
                        </li>

                        <li><strong>Prawo do ograniczenia przetwarzania</strong> - w pewnych sytuacjach masz prawo żądać
                            ograniczenia przetwarzania Twoich danych osobowych, na przykład gdy kwestionujesz
                            prawidłowość danych.
                        </li>

                        <li><strong>Prawo do przenoszenia danych</strong> - masz prawo otrzymać swoje dane osobowe w
                            ustrukturyzowanym, powszechnie używanym formacie oraz przesłać te dane innemu
                            administratorowi.
                        </li>

                        <li><strong>Prawo do sprzeciwu</strong> - masz prawo w dowolnym momencie wnieść sprzeciw wobec
                            przetwarzania Twoich danych osobowych w określonych celach, w szczególności marketingu
                            bezpośredniego.
                        </li>

                        <li><strong>Prawo do wycofania zgody</strong> - jeżeli przetwarzanie odbywa się na podstawie
                            Twojej zgody, masz prawo wycofać tę zgodę w dowolnym momencie, co nie wpływa na zgodność z
                            prawem przetwarzania dokonanego przed jej wycofaniem.
                        </li>
                    </ul>

                    <p className="mb-4">
                        Aby skorzystać z tych praw, skontaktuj się z nami poprzez adres email: <a
                        href={`mailto:${contactEmail}`} className="text-primary hover:underline">{contactEmail}</a>.
                        Dołożymy wszelkich starań, aby odpowiedzieć na Twoje żądanie bez zbędnej zwłoki, nie później niż
                        w ciągu miesiąca od jego otrzymania.
                    </p>

                    <p className="mb-4">
                        Masz również prawo do wniesienia skargi do właściwego organu nadzorczego ds. ochrony danych
                        osobowych, w szczególności w państwie członkowskim UE swojego zwykłego pobytu, miejsca pracy lub
                        miejsca popełnienia domniemanego naruszenia, jeżeli sądzisz, że przetwarzanie Twoich danych
                        osobowych narusza przepisy RODO.
                    </p>

                    <p className="mb-4">
                        W przypadku dietetyków: możesz zarządzać swoimi danymi osobowymi poprzez panel administracyjny
                        platformy. W przypadku klientów dietetyków: wiele aspektów zarządzania Twoimi danymi może być
                        realizowanych za pośrednictwem Twojego dietetyka lub poprzez aplikację mobilną.
                    </p>
                </>
            )
        },
        {
            id: "data-retention",
            title: "8. Okres przechowywania danych",
            content: (
                <>
                    <p className="mb-4">
                        Przechowujemy Twoje dane osobowe tylko tak długo, jak jest to niezbędne do celów, dla których
                        zostały zebrane, w tym do spełnienia wymogów prawnych, księgowych lub sprawozdawczych.
                    </p>

                    <p className="mb-4">
                        Stosujemy następujące okresy przechowywania danych:
                    </p>
                    <ul className="list-disc pl-6 mb-4">
                        <li><strong>Dane konta użytkownika</strong> - przechowywane przez okres posiadania aktywnego
                            konta na platformie oraz przez 30 dni po jego usunięciu (w celu umożliwienia ewentualnego
                            przywrócenia konta)
                        </li>
                        <li><strong>Dane klientów dietetyków</strong> - przechowywane zgodnie z ustaleniami między
                            dietetykiem a klientem, jednak nie dłużej niż przez okres aktywności konta dietetyka oraz 90
                            dni po jego dezaktywacji
                        </li>
                        <li><strong>Dane związane z płatnościami</strong> - przechowywane przez okres wymagany
                            przepisami podatkowymi i rachunkowymi (zwykle 5 lat od końca roku kalendarzowego)
                        </li>
                        <li><strong>Logi i dane analityczne</strong> - przechowywane przez okres do 24 miesięcy</li>
                    </ul>

                    <p className="mb-4">
                        Po upływie odpowiednich okresów przechowywania, Twoje dane osobowe zostaną bezpiecznie usunięte
                        lub zanonimizowane, aby nie można było ich powiązać z Twoją tożsamością.
                    </p>
                </>
            )
        },
        {
            id: "automated-decision-making",
            title: "9. Zautomatyzowane podejmowanie decyzji i profilowanie",
            content: (
                <>
                    <p className="mb-4">
                        Nie stosujemy zautomatyzowanego podejmowania decyzji ani profilowania, które wywoływałoby wobec
                        Ciebie skutki prawne lub w podobny sposób istotnie na Ciebie wpływało.
                    </p>

                    <p className="mb-4">
                        Możemy wykorzystywać algorytmy do analizy danych w celu personalizacji doświadczenia użytkownika
                        i poprawy naszych usług, jednak zawsze z udziałem czynnika ludzkiego przy podejmowaniu istotnych
                        decyzji.
                    </p>

                    <p className="mb-4">
                        W przypadku wprowadzenia w przyszłości mechanizmów zautomatyzowanego podejmowania decyzji lub
                        profilowania, które mogłyby mieć istotny wpływ na użytkowników, zaktualizujemy niniejszą
                        Politykę Prywatności i poinformujemy o tym zgodnie z obowiązującymi przepisami.
                    </p>
                </>
            )
        },
        {
            id: "children-privacy",
            title: "10. Prywatność dzieci",
            content: (
                <>
                    <p className="mb-4">
                        Nasza platforma i usługi nie są skierowane do osób poniżej 16 roku życia i świadomie nie
                        gromadzimy danych osobowych od takich osób.
                    </p>

                    <p className="mb-4">
                        W przypadku usług dietetycznych dla nieletnich:
                    </p>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Zakładamy, że wszelkie dane dotyczące dzieci są wprowadzane i zarządzane przez ich rodziców
                            lub opiekunów prawnych, którzy wyrazili zgodę na takie przetwarzanie
                        </li>
                        <li>Dietetycy pracujący z nieletnimi klientami powinni uzyskać odpowiednią zgodę od rodziców lub
                            opiekunów prawnych
                        </li>
                    </ul>

                    <p className="mb-4">
                        Jeśli dowiesz się, że dziecko poniżej 16 roku życia przekazało nam dane osobowe bez zgody
                        rodzica lub opiekuna prawnego, prosimy o kontakt. Jeśli dowiemy się, że nieumyślnie zebraliśmy
                        dane osobowe od dzieci bez weryfikacji zgody rodzica, podejmiemy kroki w celu usunięcia tych
                        informacji z naszych serwerów.
                    </p>
                </>
            )
        },
        {
            id: "third-party-links",
            title: "11. Linki do stron trzecich",
            content: (
                <>
                    <p className="mb-4">
                        Nasza platforma może zawierać linki do stron internetowych, aplikacji lub usług osób trzecich,
                        które nie są własnością ani nie są kontrolowane przez Vitema. Ta Polityka Prywatności dotyczy
                        wyłącznie informacji gromadzonych przez naszą platformę.
                    </p>

                    <p className="mb-4">
                        Nie ponosimy odpowiedzialności za praktyki dotyczące prywatności lub treści stron internetowych,
                        aplikacji lub usług osób trzecich. Zachęcamy do zapoznania się z politykami prywatności każdej
                        strony odwiedzanej za pośrednictwem linków z naszej platformy.
                    </p>

                    <p className="mb-4">
                        Dietetycy korzystający z naszej platformy mogą umieszczać linki do zewnętrznych zasobów
                        edukacyjnych lub innych materiałów. Vitema nie weryfikuje ani nie odpowiada za treści dostępne
                        poprzez takie linki.
                    </p>
                </>
            )
        },
        {
            id: "cookies",
            title: "12. Cookies",
            content: (
                <>
                    <p className="mb-4">
                        Nasza platforma wykorzystuje pliki cookies oraz podobne technologie, aby zapewnić Ci lepsze
                        doświadczenie użytkowania, analizować ruch na stronie oraz personalizować treści.
                    </p>

                    <h3 className="text-lg font-semibold mt-4 mb-2">Rodzaje używanych cookies:</h3>
                    <ul className="list-disc pl-6 mb-4">
                        <li><strong>Niezbędne cookies</strong> - konieczne do prawidłowego funkcjonowania strony i
                            umożliwiające korzystanie z podstawowych funkcji, takich jak logowanie i utrzymywanie sesji.
                        </li>

                        <li><strong>Funkcjonalne cookies</strong> - umożliwiające zapamiętanie Twoich preferencji i
                            ustawień, aby poprawić komfort korzystania z platformy.
                        </li>

                        <li><strong>Analityczne cookies</strong> - pomagają nam zrozumieć, w jaki sposób użytkownicy
                            korzystają z naszej platformy, co pozwala nam na ciągłe udoskonalanie jej funkcjonalności.
                        </li>

                        <li><strong>Firebase cookies</strong> - używane przez usługi Firebase (w tym Authentication,
                            Analytics i Crashlytics) do zapewnienia funkcjonalności uwierzytelniania i monitorowania
                            wydajności aplikacji.
                        </li>
                    </ul>

                    <p className="mb-4">
                        Większość przeglądarek internetowych domyślnie akceptuje pliki cookies. Możesz jednak zmienić
                        ustawienia swojej przeglądarki, aby odrzucać pliki cookies lub otrzymywać powiadomienia o ich
                        zapisywaniu. Należy pamiętać, że zablokowanie wszystkich plików cookies może wpłynąć na
                        funkcjonalność naszej platformy i ograniczyć dostęp do niektórych jej funkcji.
                    </p>

                    <p className="mb-4">
                        W przypadku aplikacji mobilnej Vitema, używamy podobnych technologii do przechowywania
                        informacji lokalnie na urządzeniu oraz identyfikatorów reklamowych zgodnie z ustawieniami
                        urządzenia.
                    </p>

                    <p className="mb-4">
                        Korzystając z naszej platformy z aktualnymi ustawieniami przeglądarki, wyrażasz zgodę na
                        używanie przez nas plików cookies zgodnie z niniejszą Polityką Prywatności.
                    </p>
                </>
            )
        },
        {
            id: "policy-changes",
            title: "13. Zmiany w Polityce Prywatności",
            content: (
                <>
                    <p className="mb-4">
                        Okresowo możemy wprowadzać zmiany w niniejszej Polityce Prywatności, aby odzwierciedlić zmiany w
                        naszych praktykach dotyczących danych, nowych funkcjach platformy lub wymogach prawnych. Data
                        ostatniej aktualizacji będzie zawsze widoczna na początku dokumentu.
                    </p>

                    <p className="mb-4">
                        W przypadku istotnych zmian w sposobie przetwarzania Twoich danych osobowych, poinformujemy Cię
                        o tym poprzez:
                    </p>
                    <ul className="list-disc pl-6 mb-4">
                        <li>Wyświetlenie powiadomienia w aplikacji webowej lub mobilnej</li>
                        <li>Wysłanie wiadomości email na adres powiązany z Twoim kontem</li>
                        <li>Publikację informacji o zmianach na naszej stronie głównej</li>
                    </ul>

                    <p className="mb-4">
                        Zalecamy regularne zapoznawanie się z niniejszą Polityką Prywatności, aby być na bieżąco z
                        informacjami dotyczącymi ochrony Twoich danych osobowych. Kontynuowanie korzystania z naszych
                        usług po wprowadzeniu zmian oznacza akceptację zaktualizowanej Polityki Prywatności.
                    </p>

                    <p className="mb-4">
                        Poprzednie wersje Polityki Prywatności będą przechowywane w naszym archiwum i udostępniane na
                        żądanie.
                    </p>
                </>
            )
        },
        {
            id: "contact",
            title: "14. Kontakt",
            content: (
                <>
                    <p className="mb-4">
                        Jeśli masz pytania dotyczące naszej Polityki Prywatności, sposobu przetwarzania Twoich danych
                        osobowych lub chcesz skorzystać z przysługujących Ci praw, zachęcamy do kontaktu z nami:
                    </p>

                    <div className="mb-4 pl-6">
                        <p><strong>Noise Vision Software</strong></p>
                        <p>Email: <a href={`mailto:${contactEmail}`}
                                     className="text-primary hover:underline">{contactEmail}</a></p>
                        <p>Telefon: +48 880 172 098</p>
                    </div>

                    <p className="mb-4">
                        Na Twoje zapytania staramy się odpowiadać w ciągu 48 godzin roboczych. W przypadku wniosków
                        dotyczących praw osób, których dane dotyczą, odpowiemy bez zbędnej zwłoki, nie później niż w
                        ciągu miesiąca od otrzymania żądania.
                    </p>

                    <p className="mb-4">
                        W sprawach dotyczących ochrony danych osobowych wyznaczyliśmy Inspektora Ochrony Danych (IOD), z
                        którym można się skontaktować pod adresem email: <a href="mailto:kontakt@vitema.pl"
                                                                            className="text-primary hover:underline">kontakt@vitema.pl</a>.
                    </p>
                </>
            )
        },
    ];

    return (
        <div className="pt-20 pb-16">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
                <h1 className="text-3xl font-bold text-text-primary mt-8 mb-6">Polityka Prywatności</h1>

                <div className="prose max-w-none">
                    <p className="mb-4">
                        Ostatnia aktualizacja: {lastUpdate}
                    </p>

                    {/* Renderowanie sekcji z tablicy */}
                    {privacyPolicySections.map((section) => (
                        <div key={section.id} id={section.id}>
                            <h2 className="text-xl font-semibold mt-8 mb-3">{section.title}</h2>
                            {section.content}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default PrivacyPolicy;