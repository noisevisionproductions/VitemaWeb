import React from "react";
import {FileSpreadsheet, PenTool} from "lucide-react";
import SectionHeader from "../../../../shared/common/SectionHeader";

export type DietCreationMethodType = 'excel' | 'manual';

interface DietCreationMethodProps {
    onMethodSelect: (method: DietCreationMethodType) => void;
}

const DietCreationMethod: React.FC<DietCreationMethodProps> = ({onMethodSelect}) => {
    const creationMethods = [
        {
            id: 'excel' as DietCreationMethodType,
            name: 'Import z pliku Excel',
            description: 'Prześlij gotową dietę w formacie Excel. Szybka metoda, gdy ma się już przygotowane plany żywieniowe.',
            icon: FileSpreadsheet,
            benefits: [
                'Szybkie importowanie gotowych diet',
                'Automatyczne przetwarzanie list zakupów',
                'Obsługa złożonych planów żywieniowych',
                'Walidacja wartości odżywczych'
            ],
            new: false
        },
        {
            id: 'manual' as DietCreationMethodType,
            name: 'Tworzenie ręczne',
            description: 'Utwórz dietę krok po kroku używając wbudowanego kreatora. Idealne dla tworzenia spersonalizowanych planów.',
            icon: PenTool,
            benefits: [
                'Możliwość użycia gotowych szablonów diet',
                'Pełna kontrola nad każdym elementem',
                'Kreator krok po kroku',
                'Dostosowanie do indywidualnych potrzeb',
                'Podgląd na żywo podczas tworzenia'
            ],
            new: true
        }
    ];

    const handleMethodClick = (method: DietCreationMethodType) => {
        onMethodSelect(method);
    };

    return (
        <div className="space-y-6 pb-8">
            <SectionHeader
                title="Tworzenie diety"
                description="Wybierz metodę tworzenia nowej diety dla klienta"
            />

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {creationMethods.map((method) => {
                    const Icon = method.icon;

                    return (
                        <div
                            key={method.id}
                            className={`
                                relative bg-white border-2 rounded-xl p-6 cursor-pointer transition-all duration-200
                                hover:shadow-lg hover:border-primary-light group
                                ${method.new ? 'border-primary-light bg-primary-light/5' : 'border-gray-200'}
                            `}
                            onClick={() => handleMethodClick(method.id)}
                        >
                            {method.new && (
                                <div className="absolute -top-3 left-6">
                                    <span
                                        className="bg-secondary text-white text-xs font-semibold px-3 py-1 rounded-full">
                                        Nowość
                                    </span>
                                </div>
                            )}

                            <div className="flex items-start space-x-4">
                                <div className={`
                                    p-3 rounded-lg transition-colors
                                    ${method.new
                                    ? 'bg-primary-light/20 group-hover:bg-primary-light/30'
                                    : 'bg-gray-100 group-hover:bg-gray-200'
                                }
                                `}>
                                    <Icon className={`
                                        h-8 w-8 transition-colors
                                        ${method.new ? 'text-primary' : 'text-gray-600'}
                                    `}/>
                                </div>

                                <div className="flex-1 min-w-0">
                                    <h3 className={`
                                        text-xl font-semibold mb-2 transition-colors
                                        ${method.new
                                        ? 'text-primary group-hover:text-primary-dark'
                                        : 'text-gray-900 group-hover:text-primary'
                                    }
                                    `}>
                                        {method.name}
                                    </h3>

                                    <p className="text-gray-600 mb-4 leading-relaxed">
                                        {method.description}
                                    </p>

                                    <div className="space-y-2">
                                        <h4 className="text-sm font-medium text-gray-900 mb-2">
                                            Główne zalety:
                                        </h4>
                                        <ul className="space-y-1">
                                            {method.benefits.map((benefit, index) => (
                                                <li key={index} className="flex items-center text-sm text-gray-600">
                                                    <div className={`
                                                        w-1.5 h-1.5 rounded-full mr-2 flex-shrink-0
                                                        ${method.new ? 'bg-primary' : 'bg-gray-400'}
                                                    `}/>
                                                    {benefit}
                                                </li>
                                            ))}
                                        </ul>
                                    </div>

                                    <div className="mt-6 pt-4 border-t border-gray-100">
                                        <span className={`
                                            inline-flex items-center text-sm font-medium transition-colors
                                            ${method.new
                                            ? 'text-primary group-hover:text-primary-dark'
                                            : 'text-gray-700 group-hover:text-primary'
                                        }
                                        `}>
                                            Wybierz tę metodę
                                            <span
                                                className="ml-2 transform transition-transform group-hover:translate-x-1">
                                                →
                                            </span>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    )
};

export default DietCreationMethod;