import React from 'react';
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "../../../ui/dialog";
import {NewsletterSubscriber} from "../../../../types/newsletter";
import {formatTimestamp} from "../../../../utils/dateFormatters";

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
    // Definicja pytań ankiety-musi odpowiadać strukturze w DietitianSurvey.tsx
    const questions: SurveyQuestion[] = [
        {
            id: 'dietGoals',
            question: 'Jakie cele żywieniowe najczęściej realizujesz ze swoimi klientami?',
            type: 'checkbox'
        },
        {
            id: 'clientsPerMonth',
            question: 'Ilu klientów przeciętnie prowadzisz miesięcznie?',
            type: 'radio'
        },
        {
            id: 'toolsUsed',
            question: 'Jakich narzędzi obecnie używasz do układania diet?',
            type: 'checkbox'
        },
        {
            id: 'painPoints',
            question: 'Co jest dla Ciebie największym wyzwaniem w układaniu diet?',
            type: 'text'
        },
        {
            id: 'desiredFeatures',
            question: 'Jakie funkcje chciał(a)byś widzieć w aplikacji do układania diet?',
            type: 'checkbox'
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
                                <p className="font-medium">{formatTimestamp(subscriber.verifiedAt)}</p>
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