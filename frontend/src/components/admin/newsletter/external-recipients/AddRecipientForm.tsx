import React, {useState} from "react";
import {ExternalRecipientFormData} from "../../../../types/sendGrid";
import {X} from "lucide-react";

interface AddRecipientFormProps {
    onSubmit: (data: ExternalRecipientFormData) => Promise<void>;
    onCancel: () => void;
    categories: string[];
}

const AddRecipientForm: React.FC<AddRecipientFormProps> = ({
                                                               onSubmit,
                                                               onCancel,
                                                               categories
                                                           }) => {
    const [formData, setFormData] = useState<ExternalRecipientFormData>({
        email: '',
        name: '',
        category: categories.length > 0 ? categories[0] : '',
        tags: [],
        notes: ''
    });
    const [newTag, setNewTag] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errors, setErrors] = useState<{ [key: string]: string }>({});

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const {name, value} = e.target;
        setFormData({...formData, [name]: value});

        if (errors[name]) {
            setErrors({...errors, [name]: ''});
        }
    };

    const handleAddTag = () => {
        if (newTag.trim() && !formData.tags?.includes(newTag.trim())) {
            setFormData({
                ...formData,
                tags: [...(formData.tags || []), newTag.trim()]
            });
            setNewTag('');
        }
    };

    const handleRemoveTag = (tag: string) => {
        setFormData({
            ...formData,
            tags: formData.tags?.filter(t => t !== tag) || []
        });
    };

    const validateForm = (): boolean => {
        const newErrors: { [key: string]: string } = {};

        if (!formData.email) {
            newErrors.email = 'Email jest wymagany';
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            newErrors.email = 'Podaj prawidłowy adres email';
        }

        if (!formData.category) {
            newErrors.category = 'Kategoria jest wymagana';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            setIsSubmitting(true);
            await onSubmit(formData);
        } catch (error) {
            console.error('Error adding recipient:', error);
            setIsSubmitting(false);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleAddTag();
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                    Email<span className="text-red-500">*</span>
                </label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className={`mt-1 block w-full rounded-md border ${
                        errors.email ? 'border-red-300' : 'border-gray-300'
                    } px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-primary sm:text-sm`}
                    placeholder="przyklad@domena.pl"
                    disabled={isSubmitting}
                />
                {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email}</p>}
            </div>

            <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                    Imię i nazwisko
                </label>
                <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name || ''}
                    onChange={handleChange}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-primary sm:text-sm"
                    placeholder="Jan Kowalski"
                    disabled={isSubmitting}
                />
            </div>

            <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700">
                    Kategoria<span className="text-red-500">*</span>
                </label>
                <select
                    id="category"
                    name="category"
                    value={formData.category}
                    onChange={handleChange}
                    className={`mt-1 block w-full rounded-md border ${
                        errors.category ? 'border-red-300' : 'border-gray-300'
                    } px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-primary sm:text-sm`}
                    disabled={isSubmitting}
                >
                    {categories.length > 0 ? (
                        categories.map(category => (
                            <option key={category} value={category}>{category}</option>
                        ))
                    ) : (
                        <option value="">Brak dostępnych kategorii</option>
                    )}
                </select>
                {errors.category && <p className="mt-1 text-xs text-red-600">{errors.category}</p>}
            </div>

            <div>
                <label htmlFor="tags" className="block text-sm font-medium text-gray-700">
                    Tagi
                </label>
                <div className="mt-1 flex rounded-md shadow-sm">
                    <input
                        type="text"
                        id="tags"
                        value={newTag}
                        onChange={(e) => setNewTag(e.target.value)}
                        onKeyDown={handleKeyDown}
                        className="block w-full rounded-l-md border border-gray-300 px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-primary sm:text-sm"
                        placeholder="Dodaj tag i naciśnij Enter"
                        disabled={isSubmitting}
                    />
                    <button
                        type="button"
                        onClick={handleAddTag}
                        className="inline-flex items-center rounded-r-md border border-l-0 border-gray-300 bg-gray-50 px-3 py-2 text-gray-500 hover:bg-gray-100"
                        disabled={isSubmitting}
                    >
                        Dodaj
                    </button>
                </div>

                {formData.tags && formData.tags.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-2">
                        {formData.tags.map(tag => (
                            <span
                                key={tag}
                                className="inline-flex items-center rounded-full bg-primary-light bg-opacity-10 px-2.5 py-0.5 text-xs font-medium text-primary-dark"
                            >
                                {tag}
                                <button
                                    type="button"
                                    onClick={() => handleRemoveTag(tag)}
                                    className="ml-1 inline-flex h-4 w-4 flex-shrink-0 items-center justify-center rounded-full text-primary-dark hover:bg-primary-light hover:text-white focus:outline-none"
                                >
                                    <X size={12}/>
                                </button>
                            </span>
                        ))}
                    </div>
                )}
            </div>

            <div>
                <label htmlFor="notes" className="block text-sm font-medium text-gray-700">
                    Notatki
                </label>
                <textarea
                    id="notes"
                    name="notes"
                    value={formData.notes || ''}
                    onChange={handleChange}
                    rows={3}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-primary sm:text-sm"
                    placeholder="Dodatkowe informacje o odbiorcy..."
                    disabled={isSubmitting}
                />
            </div>

            <div className="mt-5 flex justify-end space-x-3">
                <button
                    type="button"
                    onClick={onCancel}
                    className="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none"
                    disabled={isSubmitting}
                >
                    Anuluj
                </button>
                <button
                    type="submit"
                    className="inline-flex justify-center rounded-md border border-transparent bg-primary px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-dark focus:outline-none disabled:opacity-50"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? 'Dodawanie...' : 'Dodaj odbiorcę'}
                </button>
            </div>
        </form>
    );
};

export default AddRecipientForm;