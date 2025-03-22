import React from "react";
import { Input } from "../ui/Input";
import { Label } from "../ui/Label";

export interface NutritionalValuesData {
    calories: number;
    protein: number;
    fat: number;
    carbs: number;
}

interface NutritionalValuesProps {
    values: NutritionalValuesData;
    editMode?: boolean;
    onChange?: (name: string, value: number) => void;
    size?: "sm" | "md" | "lg";
}

const NutritionalValues: React.FC<NutritionalValuesProps> = ({
                                                                 values,
                                                                 editMode = false,
                                                                 onChange,
                                                                 size = "md"
                                                             }) => {
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        const numValue = parseFloat(value) || 0;
        if (onChange) {
            onChange(name, numValue);
        }
    };

    // Klasy dla różnych rozmiarów
    const getTextSizeClasses = () => {
        switch (size) {
            case "sm":
                return {
                    value: "text-sm font-bold",
                    label: "text-xs"
                };
            case "lg":
                return {
                    value: "text-xl font-bold",
                    label: "text-sm"
                };
            default:
                return {
                    value: "text-lg font-bold",
                    label: "text-sm"
                };
        }
    };

    const textSizes = getTextSizeClasses();
    const paddingClass = size === "sm" ? "p-2" : "p-3";

    if (editMode) {
        return (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div>
                    <Label htmlFor="calories">Kalorie (kcal)</Label>
                    <Input
                        id="calories"
                        name="calories"
                        type="number"
                        value={values.calories || 0}
                        onChange={handleInputChange}
                    />
                </div>
                <div>
                    <Label htmlFor="protein">Białko (g)</Label>
                    <Input
                        id="protein"
                        name="protein"
                        type="number"
                        value={values.protein || 0}
                        onChange={handleInputChange}
                    />
                </div>
                <div>
                    <Label htmlFor="fat">Tłuszcze (g)</Label>
                    <Input
                        id="fat"
                        name="fat"
                        type="number"
                        value={values.fat || 0}
                        onChange={handleInputChange}
                    />
                </div>
                <div>
                    <Label htmlFor="carbs">Węglowodany (g)</Label>
                    <Input
                        id="carbs"
                        name="carbs"
                        type="number"
                        value={values.carbs || 0}
                        onChange={handleInputChange}
                    />
                </div>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className={`bg-green-50 ${paddingClass} rounded text-center`}>
                <div className={`${textSizes.value} text-green-700`}>
                    {values.calories || 0}
                </div>
                <div className={`${textSizes.label} text-gray-600`}>kcal</div>
            </div>
            <div className={`bg-blue-50 ${paddingClass} rounded text-center`}>
                <div className={`${textSizes.value} text-blue-700`}>
                    {values.protein || 0}g
                </div>
                <div className={`${textSizes.label} text-gray-600`}>białko</div>
            </div>
            <div className={`bg-red-50 ${paddingClass} rounded text-center`}>
                <div className={`${textSizes.value} text-red-700`}>
                    {values.fat || 0}g
                </div>
                <div className={`${textSizes.label} text-gray-600`}>tłuszcz</div>
            </div>
            <div className={`bg-yellow-50 ${paddingClass} rounded text-center`}>
                <div className={`${textSizes.value} text-yellow-700`}>
                    {values.carbs || 0}g
                </div>
                <div className={`${textSizes.label} text-gray-600`}>węglowodany</div>
            </div>
        </div>
    );
};

export default NutritionalValues;