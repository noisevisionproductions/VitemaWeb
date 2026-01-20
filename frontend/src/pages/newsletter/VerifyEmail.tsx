import {useEffect, useState} from 'react';
import {useSearchParams, Link} from 'react-router-dom';
import LandingLayout from "../../components/landing/layout/LandingLayout";
import {PublicNewsletterService} from "../../services/newsletter";
import DietitianSurvey from "./DietitianSurvey";
import {Timestamp} from "firebase/firestore";
import {formatTimestamp} from "../../utils/dateFormatters";

interface VerifyEmailResponse {
    message?: string;
    subscriberId?: string;
    subscriberRole?: string;
    email?: string;
    verifiedAt?: Timestamp | any;
}

const VerifyEmail = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');

    const [isVerifying, setIsVerifying] = useState(true);
    const [isSuccess, setIsSuccess] = useState(false);
    const [message, setMessage] = useState('');
    const [showSurvey, setShowSurvey] = useState(false);
    const [subscriberId, setSubscriberId] = useState('');
    const [verificationDetails, setVerificationDetails] = useState<{
        email: string;
        verifiedAt: Timestamp | null;
    } | null>(null);

    useEffect(() => {
        const verifyEmail = async () => {
            if (!token) {
                setIsVerifying(false);
                setMessage('Nieprawidłowy token weryfikacyjny');
                return;
            }

            try {
                function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
                    const timeout = new Promise<never>((_, reject) => {
                        setTimeout(() => reject(new Error('Timeout')), timeoutMs);
                    });
                    return Promise.race([promise, timeout]);
                }

                const result = await withTimeout<VerifyEmailResponse>(PublicNewsletterService.verifyEmail(token), 20000);

                setIsSuccess(true);
                setMessage(result.message || 'Adres email został pomyślnie zweryfikowany');

                if (result.subscriberId && result.subscriberRole) {
                    setSubscriberId(result.subscriberId);

                    if (result.email) {
                        setVerificationDetails({
                            email: result.email,
                            verifiedAt: result.verifiedAt || Timestamp.now()
                        });
                    }

                    if (result.subscriberRole === 'DIETITIAN') {
                        setShowSurvey(true);
                    }
                }
            } catch (error: any) {
                console.error('Verification error:', error);
                setIsSuccess(false);

                if (error.message === 'Timeout') {
                    setMessage('Weryfikacja trwała zbyt długo. Odśwież stronę lub spróbuj ponownie później.');
                } else {
                    setMessage(error.response?.data?.message || 'Wystąpił błąd podczas weryfikacji adresu email');
                }
            } finally {
                setIsVerifying(false);
            }
        };

        verifyEmail().catch(console.error);
    }, [token]);

    const handleSurveyComplete = () => {
        setShowSurvey(false);
    };

    return (
        <LandingLayout>
            <div className="min-h-[70vh] flex items-center justify-center py-12">
                {showSurvey ? (
                    <div className="w-full py-8 px-4">
                        {/* Informacja o weryfikacji nad ankietą */}
                        <div className="max-w-3xl mx-auto bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg"
                                         viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                        <path fillRule="evenodd"
                                              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                              clipRule="evenodd"/>
                                    </svg>
                                </div>
                                <div className="ml-3">
                                    <h3 className="text-sm font-medium text-green-800">Weryfikacja zakończona
                                        pomyślnie!</h3>
                                    {verificationDetails && (
                                        <div className="mt-1 text-sm text-green-700">
                                            <p>Email <strong>{verificationDetails.email}</strong> został
                                                zweryfikowany {verificationDetails.verifiedAt ? formatTimestamp(verificationDetails.verifiedAt) : 'teraz'}.</p>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        <DietitianSurvey
                            subscriberId={subscriberId}
                            onComplete={handleSurveyComplete}
                        />
                    </div>
                ) : (
                    <div className="max-w-md w-full p-8 bg-white rounded-lg shadow-md">
                        {isVerifying ? (
                            <div className="text-center">
                                <div
                                    className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
                                <p className="mt-4 text-lg">Weryfikujemy Twój adres email...</p>
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
                                        <h2 className="mt-4 text-2xl font-semibold">Weryfikacja zakończona
                                            pomyślnie!</h2>
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
                                        <h2 className="mt-4 text-2xl font-semibold">Błąd weryfikacji</h2>
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
                )}
            </div>
        </LandingLayout>
    );
};

export default VerifyEmail;