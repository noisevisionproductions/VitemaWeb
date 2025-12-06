import React from 'react';
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "../../../../shared/ui/Dialog";
import {NewsletterSubscriber} from "../../../../../types/nutrilog/newsletter";
import {formatPostgresTimestamp} from "../../../../../utils/dateFormatters";

interface SurveyDetailsModalProps {
    subscriber: NewsletterSubscriber;
    isOpen: boolean;
    onClose: () => void;
}

interface SurveyQuestion {
    id: string;
    question: string;
    type: 'radio' | 'checkbox' | 'text';
}

const SurveyDetailsModal: React.FC<SurveyDetailsModalProps> = ({
                                                                   subscriber,
                                                                   isOpen,
                                                                   onClose
                                                               }) => {
    // Definicja pytań ankiety-musi odpowiadać strukturze w dietitiansurvey.tsx
    const questions: SurveyQuestion[] = [
        {
            id: 'dietSoftwareExperience',
            question: 'Czy korzystałeś/aś wcześniej z oprogramowania do układania diet?',
            type: 'radio'
        },
        {
            id: 'currentTools',
            question: 'Z jakich narzędzi aktualnie korzystasz przy układaniu diet?',
            type: 'checkbox'
        },
        {
            id: 'clientsPerMonth',
            question: 'Ilu klientów przeciętnie prowadzisz miesięcznie?',
            type: 'radio',
        },
        {
            id: 'softwareKeyFeatures',
            question: 'Które funkcje w aplikacji do układania diet są dla Ciebie najważniejsze?',
            type: 'checkbox'
        },
        {
            id: 'additionalInfo',
            question: 'Czy masz jakieś pytania, dodatkowe informacje lub wymagania?',
            type: 'text'
        }
    ];

    const getSurveyAnswers = () => {
        try {
            if (subscriber.metadata?.surveyAnswers) {
                return JSON.parse(subscriber.metadata.surveyAnswers);
            }
        } catch (e) {
            console.error('Error parsing survey answers:', e);
        }
        return {};
    };

    const answers = getSurveyAnswers();
    const surveyCompletedDate = subscriber.metadata?.surveyCompletedAt
        ? new Date(subscriber.metadata.surveyCompletedAt).toLocaleDateString('pl-PL', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
        : formatPostgresTimestamp(subscriber.verifiedAt);

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-3xl bg-white">
                <DialogHeader className="border-b border-gray-100 pb-4">
                    <DialogTitle className="text-xl font-semibold text-primary">Szczegóły Ankiety</DialogTitle>
                    <DialogDescription className="text-text-secondary">
                        Odpowiedzi dietetyka na pytania ankiety
                    </DialogDescription>
                </DialogHeader>

                <div className="mt-4 overflow-y-auto max-h-[70vh]">
                    <div className="bg-surface p-4 rounded-md mb-4 shadow-sm">
                        <h3 className="font-medium text-text-primary">Informacje o dietetyku</h3>
                        <div className="grid md:grid-cols-2 gap-2 mt-2">
                            <div>
                                <p className="text-sm text-text-secondary">Email:</p>
                                <p className="font-medium">{subscriber.email}</p>
                            </div>
                            <div>
                                <p className="text-sm text-text-secondary">Data wypełnienia ankiety:</p>
                                <p className="font-medium">{surveyCompletedDate}</p>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-6">
                        {questions.map((question) => (
                            <div key={question.id} className="border-b border-gray-200 pb-4 last:border-b-0">
                                <h4 className="font-medium mb-2 text-text-primary">{question.question}</h4>

                                {question.type === 'text' ? (
                                    <p className="bg-surface p-3 rounded-md whitespace-pre-wrap text-text-primary">
                                        {answers[question.id] || 'Brak odpowiedzi'}
                                    </p>
                                ) : question.type === 'radio' ? (
                                    <p className="text-text-primary">{answers[question.id] || 'Brak odpowiedzi'}</p>
                                ) : (
                                    <div>
                                        {Array.isArray(answers[question.id]) && answers[question.id].length > 0 ? (
                                            <ul className="list-disc list-inside space-y-1">
                                                {answers[question.id].map((answer: string) => (
                                                    <li key={answer} className="text-text-primary">{answer}</li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <p className="text-text-secondary italic">Brak odpowiedzi</p>
                                        )}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default SurveyDetailsModal;