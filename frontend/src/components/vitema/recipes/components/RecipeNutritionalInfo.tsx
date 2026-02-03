import React from 'react';
import NutritionalValues, {NutritionalValuesData} from "../../../shared/common/NutritionalValues";

interface RecipeNutritionalInfoProps {
    values: NutritionalValuesData;
    editMode: boolean;
    onChange: (name: string, value: number) => void;
}

const RecipeNutritionalInfo: React.FC<RecipeNutritionalInfoProps> = ({
                                                                          values,
                                                                          editMode,
                                                                          onChange
                                                                      }) => {
    return (
        <div className="bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-lg font-semibold mb-3">Wartości odżywcze</h3>

            {editMode ? (
                <NutritionalValues
                    values={values}
                    editMode={true}
                    onChange={onChange}
                />
            ) : (
                <NutritionalValues
                    values={values}
                    size="lg"
                />
            )}
        </div>
    );
};

export default RecipeNutritionalInfo;
