import {Link, useSearchParams} from "react-router-dom";
import {PublicNewsletterService} from "../../services/newsletter";
import {useState} from "react";
import LandingLayout from "../../components/landing/layout/LandingLayout";

const Unsubscribe = () => {
    const [searchParams] = useSearchParams();
    const email = searchParams.get('email');

    const [isProcessing, setIsProcessing] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [message, setMessage] = useState('');
    const [isConfirmed, setIsConfirmed] = useState(false);

    const handleUnsubscribe = async () => {
        if (!email) {
            setMessage('Nieprawidłowy adres email');
            return;
        }

        setIsProcessing(true);

        try {
            const result = await PublicNewsletterService.unsubscribe(email);
            setIsSuccess(true);
            setMessage(result.message);
        } catch (error: any) {
            setIsSuccess(false);
            setMessage(error.message || 'Wystąpił błąd podczas wypisywania z newslettera');
        } finally {
            setIsProcessing(false);
            setIsConfirmed(true);
        }
    };

    return (
        <LandingLayout>
            <div className="min-h-[70vh] flex items-center justify-center">
                <div className="max-w-lg w-full p-8 bg-white rounded-lg shadow-md">
                    {!isConfirmed ? (
                        <div className="text-center">
                            <h2 className="text-2xl font-semibold mb-4">Wypisanie z newslettera</h2>
                            <p className="mb-6">
                                Czy na pewno chcesz zrezygnować z otrzymywania informacji o Vitema?
                            </p>
                            <p className="text-sm text-text-secondary mb-6">
                                Email: <span className="font-medium">{email}</span>
                            </p>

                            <div className="flex justify-center space-x-4">
                                <button
                                    onClick={handleUnsubscribe}
                                    disabled={isProcessing}
                                    className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                                >
                                    {isProcessing ? 'Przetwarzanie...' : 'Tak, wypisz mnie'}
                                </button>
                                <Link
                                    to="/"
                                    className="px-6 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
                                >
                                    Anuluj
                                </Link>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center">
                            {isSuccess ? (
                                <>
                                    <div className="inline-block rounded-full p-3 bg-green-100 text-green-800">
                                        <svg className="h-8 w-8" xmlns="http://www.w3.org/2000/svg" fill="none"
                                             viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                                  d="M5 13l4 4L19 7"/>
                                        </svg>
                                    </div>
                                    <h2 className="mt-4 text-2xl font-semibold">Zostałeś wypisany</h2>
                                </>
                            ) : (
                                <>
                                    <div className="inline-block rounded-full p-3 bg-red-100 text-red-800">
                                        <svg className="h-8 w-8" xmlns="http://www.w3.org/2000/svg" fill="none"
                                             viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                                  d="M6 18L18 6M6 6l12 12"/>
                                        </svg>
                                    </div>
                                    <h2 className="mt-4 text-2xl font-semibold">Wystąpił błąd</h2>
                                </>
                            )}

                            <p className="mt-2 text-text-secondary">{message}</p>

                            <div className="mt-8">
                                <Link
                                    to="/"
                                    className="inline-block px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                                >
                                    Wróć do strony głównej
                                </Link>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </LandingLayout>
    );
};

export default Unsubscribe;