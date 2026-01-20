import React from "react";
import {useForm} from 'react-hook-form';
import {Bug, Lightbulb, Sparkles} from "lucide-react";

interface ChangelogFormData {
    description: string;
    type: 'feature' | 'fix' | 'improvement';
}

interface ChangelogFormProps {
    onSubmit: (data: ChangelogFormData) => Promise<void>;
    isSubmitting: boolean;
}

const ChangelogForm: React.FC<ChangelogFormProps> = ({onSubmit, isSubmitting}) => {
    const {register, handleSubmit, reset, formState: {errors}} = useForm<ChangelogFormData>({
        defaultValues: {
            type: 'feature'
        }
    });

    const onFormSubmit = async (data: ChangelogFormData) => {
        await onSubmit(data);
        reset();
    };

    return (
        <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    Typ zmiany
                </label>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <label
                        className="relative border rounded-lg p-3 flex items-center space-x-3 cursor-pointer hover:bg-gray-50">
                        <input
                            type="radio"
                            value="feature"
                            {...register('type')}
                            className="h-4 w-4 text-blue-600 border-gray-300"
                        />
                        <span className="flex items-center">
                            <Sparkles className="w-5 h-5 text-blue-500 mr-2"/>
                            <span>Nowa funkcja</span>
                        </span>
                    </label>
                    <label
                        className="relative border rounded-lg p-3 flex items-center space-x-3 cursor-pointer hover:bg-gray-50">
                        <input
                            type="radio"
                            value="fix"
                            {...register('type')}
                            className="h-4 w-4 text-red-600 border-gray-300"
                        />
                        <span className="flex items-center">
                            <Bug className="w-5 h-5 text-red-500 mr-2"/>
                            <span>Poprawka</span>
                        </span>
                    </label>
                    <label
                        className="relative border rounded-lg p-3 flex items-center space-x-3 cursor-pointer hover:bg-gray-50">
                        <input
                            type="radio"
                            value="improvement"
                            {...register('type')}
                            className="h-4 w-4 text-yellow-600 border-gray-300"
                        />
                        <span className="flex items-center">
                            <Lightbulb className="w-5 h-5 text-yellow-500 mr-2"/>
                            <span>Ulepszenie</span>
                        </span>
                    </label>
                </div>
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700">
                    Opis zmiany
                </label>
                <textarea
                    {...register('description', {required: 'Opis jest wymagany'})}
                    rows={6}
                    className="mt-1 px-3 py-2 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    placeholder="Szczegółowy opis wprowadzonej zmiany..."
                />
                {errors.description && (
                    <p className="mt-1 text-sm text-red-600">
                        {errors.description.message}
                    </p>
                )}
            </div>

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
                {isSubmitting ? 'Dodawanie...' : 'Dodaj wpis'}
            </button>
        </form>
    );
};

export default ChangelogForm;