import React, {useState} from "react";
import {PublicNewsletterService} from "../../services/newsletter";

interface SurveyQuestion {
    id: string;
    question: string;
    type: 'radio' | 'checkbox' | 'text';
    options?: string[];
    required?: boolean;
}

interface SurveyProps {
    subscriberId: string;
    onComplete: () => void;
}

const DietitianSurvey: React.FC<SurveyProps> = ({
                                                    subscriberId,
                                                    onComplete
                                                }) => {
    const [currentStep, setCurrentStep] = useState(0);
    const [answers, setAnswers] = useState<Record<string, any>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validationError, setValidationError] = useState<string | null>(null);

    const questions: SurveyQuestion[] = [
        {
            id: 'dietExperience',
            question: 'Jak obecnie rozwiązujesz kwestię diety u swoich podopiecznych?',
            type: 'radio',
            options: [
                'Układam jadłospisy samodzielnie',
                'Wysyłam tylko ogólne zalecenia / makro',
                'Współpracuję z dietetykiem',
                'Nie oferuję wsparcia żywieniowego'
            ],
            required: true
        },
        {
            id: 'currentTools',
            question: 'Z jakich narzędzi korzystasz do prowadzenia podopiecznych?',
            type: 'checkbox',
            options: [
                'Excel / Google Sheets',
                'Aplikacje treningowe',
                'PDF / Word / Canva',
                'Email / WhatsApp',
                'Papier i długopis',
                'Dedykowane programy dietetyczne'
            ],
            required: true
        },
        {
            id: 'clientsPerMonth',
            question: 'Ilu podopiecznych prowadzisz miesięcznie?',
            type: 'radio',
            options: [
                '1-5',
                '6-15',
                '16-40',
                '40+',
            ],
            required: true
        },
        {
            id: 'biggestPain',
            question: 'Co jest Twoim największym problemem w obecnym modelu pracy?',
            type: 'radio',
            options: [
                'Strata czasu na układanie/kopiowanie diet',
                'Brak trzymania diety przez podopiecznych',
                'Chaos w komunikacji (SMS/Mail/Messenger)',
                'Trudność w rozliczaniu płatności',
                'Brak profesjonalnego wizerunku (wysyłanie PDF)',
                'Inny'
            ],
            required: true
        },
        {
            id: 'featuresPriority',
            question: 'Co przekonałoby Cię do zmiany narzędzia na Vitema?',
            type: 'checkbox',
            options: [
                'Gotowa baza szablonów diet (redukcja/masa)',
                'Aplikacja mobilna dla klienta z listą zakupów',
                'Szybka edycja diety na telefonie',
                'Możliwość dodania własnego logo',
                'Automatyczne wyliczanie makro'
            ],
            required: true
        },
        {
            id: 'additionalInfo',
            question: 'Czy masz jakieś pytania lub sugestie do twórców? (opcjonalne)',
            type: 'text',
            required: false
        }
    ];

    const handleInputChange = (questionId: string, value: any) => {
        if (questions[currentStep].type === 'checkbox') {
            setAnswers(prev => {
                const currentValues = prev[questionId] || [];

                if (currentValues.includes(value)) {
                    return {
                        ...prev,
                        [questionId]: currentValues.filter((v: string) => v !== value)
                    };
                } else {
                    return {
                        ...prev,
                        [questionId]: [...currentValues, value]
                    };
                }
            });
        } else {
            setAnswers(prev => ({
                ...prev,
                [questionId]: value
            }));
        }
    };

    const validateCurrentStep = (): boolean => {
        const currentQuestion = questions[currentStep];

        if (!currentQuestion.required) {
            return true;
        }

        const answer = answers[currentQuestion.id];

        if (!answer) {
            setValidationError('To pytanie wymaga odpowiedzi.');
            return false;
        }

        if (currentQuestion.type === 'checkbox' && Array.isArray(answer) && answer.length === 0) {
            setValidationError('Wybierz przynajmniej jedną opcję.');
            return false;
        }

        return true;
    };

    const handleNext = () => {
        if (!validateCurrentStep()) {
            return;
        }

        if (currentStep < questions.length - 1) {
            setCurrentStep(prev => prev + 1);
            setValidationError(null);
        } else {
            handleSubmit().catch(console.error);
        }
    };

    const handlePrevious = () => {
        if (currentStep > 0) {
            setCurrentStep(prev => prev - 1);
            setValidationError(null);
        }
    };

    const handleSubmit = async () => {
        try {
            setIsSubmitting(true);
            await PublicNewsletterService.saveSubscriberMetadata(subscriberId, {
                surveyCompleted: 'true',
                surveyAnswers: JSON.stringify(answers),
                surveyCompletedAt: new Date().toISOString()
            });
            onComplete();
        } catch (error) {
            console.error('Błąd podczas zapisywania wyników ankiety:', error);
            onComplete();
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSkip = () => {
        PublicNewsletterService.saveSubscriberMetadata(subscriberId, {
            surveySkipped: 'true',
            surveySkippedAt: new Date().toISOString()
        }).catch(console.error);
        onComplete();
    };

    const currentQuestion = questions[currentStep];
    const progress = ((currentStep + 1) / questions.length) * 100;

    return (
        <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-6 overflow-y-auto max-h-[80vh]">
            <div className="mb-6 sticky top-0 bg-white pt-2 pb-4 z-10">
                <h2 className="text-2xl font-semibold text-text-primary mb-2">Pomóż nam ulepszyć Vitema</h2>
                <p className="text-text-secondary">
                    Dziękujemy za potwierdzenie adresu email. Twoje odpowiedzi na poniższe pytania pomogą nam lepiej
                    zrozumieć potrzeby trenerów.
                </p>

                <div className="mt-4 mb-2 bg-gray-200 rounded-full h-2.5">
                    <div
                        className="bg-primary h-2.5 rounded-full transition-all duration-300 ease-in-out"
                        style={{width: `${progress}%`}}
                    ></div>
                </div>
                <p className="text-sm text-text-secondary">Pytanie {currentStep + 1} z {questions.length}</p>
            </div>

            <div className="mb-8">
                <h3 className="text-lg font-medium mb-4 break-words">
                    {currentQuestion.question}
                    {currentQuestion.required && <span className="text-red-500 ml-1">*</span>}
                </h3>

                {currentQuestion.type === 'radio' && (
                    <div className="space-y-3">
                        {currentQuestion.options?.map((option) => (
                            <label key={option} className="flex items-start cursor-pointer group">
                                <input
                                    type="radio"
                                    name={currentQuestion.id}
                                    value={option}
                                    checked={answers[currentQuestion.id] === option}
                                    onChange={() => handleInputChange(currentQuestion.id, option)}
                                    className="mt-1 h-4 w-4 text-primary focus:ring-primary-light border-gray-300"
                                />
                                <span className="ml-2 group-hover:text-primary-dark transition-colors">{option}</span>
                            </label>
                        ))}
                    </div>
                )}

                {currentQuestion.type === 'checkbox' && (
                    <div className="space-y-3">
                        {currentQuestion.options?.map((option) => (
                            <label key={option} className="flex items-start cursor-pointer group">
                                <input
                                    type="checkbox"
                                    value={option}
                                    checked={(answers[currentQuestion.id] || []).includes(option)}
                                    onChange={() => handleInputChange(currentQuestion.id, option)}
                                    className="mt-1 h-4 w-4 text-primary focus:ring-primary-light rounded border-gray-300"
                                />
                                <span className="ml-2 group-hover:text-primary-dark transition-colors">{option}</span>
                            </label>
                        ))}
                    </div>
                )}

                {currentQuestion.type === 'text' && (
                    <textarea
                        rows={4}
                        className="w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring focus:ring-primary-light focus:ring-opacity-50"
                        value={answers[currentQuestion.id] || ''}
                        onChange={(e) => handleInputChange(currentQuestion.id, e.target.value)}
                        placeholder="Wpisz swoją odpowiedź..."
                    ></textarea>
                )}

                {validationError && (
                    <p className="text-red-500 mt-2 text-sm">{validationError}</p>
                )}
            </div>

            <div className="flex justify-between sticky bottom-0 bg-white pt-4">
                <div>
                    {currentStep > 0 && (
                        <button
                            onClick={handlePrevious}
                            className="px-4 py-2 text-text-secondary bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                        >
                            Wstecz
                        </button>
                    )}
                </div>

                <div>
                    <button
                        onClick={handleSkip}
                        className="px-4 py-2 mr-3 text-text-secondary hover:underline transition-colors"
                    >
                        Pomiń ankietę
                    </button>

                    <button
                        onClick={handleNext}
                        disabled={isSubmitting}
                        className="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark disabled:opacity-50 transition-colors"
                    >
                        {isSubmitting ? (
                            <span className="flex items-center">
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                                     xmlns="http://www.w3.org/2000/svg"
                                     fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                            strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor"
                                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Wysyłanie...
                            </span>
                        ) : currentStep === questions.length - 1 ? "Zakończ" : "Dalej"}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DietitianSurvey;