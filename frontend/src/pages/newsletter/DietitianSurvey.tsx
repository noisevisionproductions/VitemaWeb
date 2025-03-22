import React, {useState} from "react";
import {PublicNewsletterService} from "../../services/newsletter";

interface SurveyQuestion {
    id: string;
    question: string;
    type: 'radio' | 'checkbox' | 'text';
    options?: string[];
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

    const questions: SurveyQuestion[] = [
        {
            id: 'dietGoals',
            question: 'Jakie cele żywieniowe najczęściej realizujesz ze swoimi klientami?',
            type: 'checkbox',
            options: [
                'Redukcja masy ciała',
                'Zwiększenie masy mięśniowej',
                'Poprawa zdrowia',
                'Leczenie chorób dietozależnych',
                'Dieta podczas ciąży',
                'Dieta sportowa',
                'Inne'
            ]
        },
        {
            id: 'clientsPerMonth',
            question: 'Ilu klientów przeciętnie prowadzisz miesięcznie?',
            type: 'radio',
            options: [
                '1-5',
                '6-10',
                '11-20',
                '21-50',
                'Powyżej 50'
            ]
        },
        {
            id: 'toolsUsed',
            question: 'Jakich narzędzi obecnie używasz do układania diet?',
            type: 'checkbox',
            options: [
                'Excel/Arkusze kalkulacyjne',
                'Specjalistyczne oprogramowanie (jakie?)',
                'Gotowe szablony PDF',
                'Własne rozwiązania',
                'Inne'
            ]
        },
        {
            id: 'painPoints',
            question: 'Co jest dla Ciebie największym wyzwaniem w układaniu diet?',
            type: 'text'
        },
        {
            id: 'desiredFeatures',
            question: 'Jakie funkcje chciał(a)byś widzieć w aplikacji do układania diet?',
            type: 'checkbox',
            options: [
                'Automatyczne obliczanie makroskładników',
                'Baza produktów z wartościami odżywczymi',
                'Szablony diet',
                'Komunikacja z klientami',
                'Raportowanie postępów klientów',
                'Generowanie raportów i faktur',
                'Integracja z kalendarzem',
                'Mobilny dostęp dla klientów',
                'Inne'
            ]
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

    const handleNext = () => {
        if (currentStep < questions.length - 1) {
            setCurrentStep(prev => prev + 1);
        } else {
            handleSubmit().catch(console.error);
        }
    };

    const handlePrevious = () => {
        if (currentStep > 0) {
            setCurrentStep(prev => prev - 1);
        }
    };

    const handleSubmit = async () => {
        try {
            await PublicNewsletterService.saveSubscriberMetadata(subscriberId, {
                surveyCompleted: 'true',
                surveyAnswers: JSON.stringify(answers)
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
        onComplete();
    };

    const currentQuestion = questions[currentStep];
    const progress = ((currentStep + 1) / questions.length) * 100;

    return (
        <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-6">
            <div className="mb-6">
                <h2 className="text-2xl font-semibold text-text-primary mb-2">Pomóż nam ulepszyć NutriLog</h2>
                <p className="text-text-secondary">
                    Dziękujemy za potwierdzenie adresu email. Twoje odpowiedzi na poniższe pytania pomogą nam lepiej
                    zrozumieć potrzeby dietetyków.
                </p>
            </div>

            <div className="mb-4 bg-gray-200 rounded-full h-2.5">
                <div
                    className="bg-primary h-2.5 rounded-full transition-all duration-300 ease-in-out"
                    style={{width: `${progress}%`}}
                ></div>
            </div>
            <p className="text-sm text-text-secondary mb-6">Pytanie {currentStep + 1} z {questions.length}</p>

            <div className="mb-8">
                <h3 className="text-lg font-medium mb-4">{currentQuestion.question}</h3>

                {currentQuestion.type === 'radio' && (
                    <div className="space-y-3">
                        {currentQuestion.options?.map((option) => (
                            <label key={option} className="flex items-start">
                                <input
                                    type="radio"
                                    name={currentQuestion.id}
                                    value={option}
                                    checked={answers[currentQuestion.id] === option}
                                    onChange={() => handleInputChange(currentQuestion.id, option)}
                                    className="mt-1 h-4 w-4 text-primary focus:ring-primary-light border-gray-300"
                                />
                                <span className="ml-2">{option}</span>
                            </label>
                        ))}
                    </div>
                )}

                {currentQuestion.type === 'checkbox' && (
                    <div className="space-y-3">
                        {currentQuestion.options?.map((option) => (
                            <label key={option} className="flex items-start">
                                <input
                                    type="checkbox"
                                    value={option}
                                    checked={(answers[currentQuestion.id] || []).includes(option)}
                                    onChange={() => handleInputChange(currentQuestion.id, option)}
                                    className="mt-1 h-4 w-4 text-primary focus:ring-primary-light rounded border-gray-300"
                                />
                                <span className="ml-2">{option}</span>
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
            </div>

            <div className="flex justify-between">
                <div>
                    {currentStep > 0 && (
                        <button
                            onClick={handlePrevious}
                            className="px-4 py-2 text-text-secondary bg-gray-100 rounded-md hover:bg-gray-200"
                        >
                            Wstecz
                        </button>
                    )}
                </div>

                <div>
                    <button
                        onClick={handleSkip}
                        className="px-4 py-2 mr-3 text-text-secondary hover:underline"
                    >
                        Pomiń ankietę
                    </button>

                    <button
                        onClick={handleNext}
                        disabled={isSubmitting}
                        className="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark disabled:opacity-50"
                    >
                        {isSubmitting ? (
                            <span className="flex items-center">
                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg"
                     fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
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