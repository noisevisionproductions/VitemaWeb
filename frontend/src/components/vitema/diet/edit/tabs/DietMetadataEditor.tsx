import React from 'react';
import {Diet} from '../../../../../types';
import {formatTimestamp} from '../../../../../utils/dateFormatters';
import {FileText, Link as LinkIcon} from 'lucide-react';

interface DietMetadataEditorProps {
    diet: Diet;
    onUpdate: (updatedDiet: Diet) => Promise<void>;
}

const DietMetadataEditor: React.FC<DietMetadataEditorProps> = ({
                                                                   diet
                                                               }) => {
    return (
        <div className="space-y-6">
            <div className="bg-white p-4 rounded-lg">
                <div className="flex justify-between items-center mb-6">
                    <h3 className="text-lg font-medium">Metadane diety</h3>
                </div>

                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                Nazwa pliku
                            </label>
                            <div className="flex items-center gap-2 text-gray-900">
                                <FileText className="h-4 w-4 text-gray-400"/>
                                {diet.metadata.fileName}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                Liczba dni
                            </label>
                            <div className="text-gray-900">
                                {diet.metadata.totalDays}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                Link do pliku
                            </label>
                            <div className="flex items-center gap-2">
                                <LinkIcon className="h-4 w-4 text-gray-400"/>
                                <a
                                    href={diet.metadata.fileUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-blue-600 hover:underline"
                                >
                                    Pobierz plik z dietą
                                </a>
                            </div>
                        </div>
                    </div>

                    <div className="mt-6 pt-6 border-t">
                        <div className="text-sm text-gray-500">
                            <div>Utworzono: {formatTimestamp(diet.createdAt)}</div>
                            <div>Ostatnia modyfikacja: {formatTimestamp(diet.updatedAt)}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DietMetadataEditor;