import {useState, useEffect} from 'react';
import {X} from 'lucide-react';

const CookieConsent = () => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        const consentAccepted = localStorage.getItem('cookieConsentAccepted');
        if (!consentAccepted) {
            setIsVisible(true);
        }
    }, []);

    const handleAccept = () => {
        localStorage.setItem('cookieConsentAccepted', 'true');
        setIsVisible(false);
    };

    const handleClose = () => {
        setIsVisible(false);
    };

    if (!isVisible) return null;

    return (
        <div className="fixed bottom-0 left-0 right-0 bg-white z-50 shadow-lg border-t border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between">
                    <div className="flex-1 pr-4">
                        <h3 className="text-base font-medium text-primary mb-1">Ta strona używa plików cookie</h3>
                        <p className="text-sm text-text-secondary mb-4 sm:mb-0">
                            Używamy plików cookie, aby zapewnić najlepsze doświadczenia na naszej stronie.
                            Kontynuując korzystanie z tej witryny, wyrażasz zgodę na używanie przez nas plików cookie
                            zgodnie z naszą <a href="/privacy-policy" className="text-primary hover:underline">Polityką
                            Prywatności</a>.
                        </p>
                    </div>
                    <div className="flex space-x-3 items-center">
                        <button
                            onClick={handleAccept}
                            className="text-white bg-primary hover:bg-primary-dark transition duration-200 py-2 px-4 rounded-md text-sm font-medium"
                        >
                            Akceptuję
                        </button>
                        <button
                            onClick={handleClose}
                            className="text-text-primary"
                            aria-label="Zamknij"
                        >
                            <X className="h-5 w-5"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CookieConsent;