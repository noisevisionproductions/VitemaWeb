import React from 'react';
import {Input} from "../../../shared/ui/Input";
import {Textarea} from "../../../shared/ui/Textarea";
import {Label} from "../../../shared/ui/Label";

interface RecipeBasicInfoProps {
    name: string;
    instructions: string;
    editMode: boolean;
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}

const RecipeBasicInfo: React.FC<RecipeBasicInfoProps> = ({
                                                              name,
                                                              instructions,
                                                              editMode,
                                                              onChange
                                                          }) => {
    if (editMode) {
        return (
            <div className="space-y-4 bg-white p-6 rounded-lg shadow-sm">
                <div>
                    <Label htmlFor="name">Nazwa przepisu</Label>
                    <Input
                        id="name"
                        name="name"
                        value={name}
                        onChange={onChange}
                        placeholder="Wpisz nazwę przepisu..."
                    />
                </div>
                <div>
                    <Label htmlFor="instructions">Instrukcje przygotowania</Label>
                    <Textarea
                        id="instructions"
                        name="instructions"
                        value={instructions}
                        onChange={onChange}
                        rows={8}
                        placeholder="Opisz krok po kroku jak przygotować przepis..."
                    />
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-4 bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-lg font-semibold">Instrukcje przygotowania</h3>
            <p className="whitespace-pre-line text-gray-700">{instructions || 'Brak instrukcji'}</p>
        </div>
    );
};

export default RecipeBasicInfo;
