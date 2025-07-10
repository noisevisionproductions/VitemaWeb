import React, {useState} from 'react';
import {X} from 'lucide-react';
import {Dialog, DialogContent, DialogHeader, DialogTitle} from '../../ui/dialog';
import {CreateDietTemplateRequest} from "../../../types/DietTemplate";
import {toast} from '../../../utils/toast';
import LoadingSpinner from '../../common/LoadingSpinner';
import {DietTemplateService} from "../../../services/diet/manual/DietTemplateService";

interface CreateTemplateDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    initialData?: Partial<CreateDietTemplateRequest>;
}

const CreateTemplateDialog: React.FC<CreateTemplateDialogProps> = ({
                                                                       isOpen,
                                                                       onClose,
                                                                       onSuccess,
                                                                       initialData
                                                                   }) => {
    const [formData, setFormData] = useState<CreateDietTemplateRequest>({
        name: initialData?.name || '',
        description: initialData?.description || '',
        category: initialData?.category || 'CUSTOM',
        duration: initialData?.duration || 7,
        mealsPerDay: initialData?.mealsPerDay || 5,
        mealTimes: initialData?.mealTimes || {},
        mealTypes: initialData?.mealTypes || [],
        notes: initialData?.notes || '',
        ...initialData
    });

    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.name.trim()) {
            toast.error('Nazwa szablonu jest wymagana');
            return;
        }

        setLoading(true);
        try {
            if (formData.dietData) {
                // Tworzenie z istniejącej diety
                await DietTemplateService.createTemplateFromDiet(formData);
            } else {
                // Tworzenie nowego szablonu
                await DietTemplateService.createTemplate(formData);
            }

            toast.success('Szablon został utworzony');
            onSuccess();
        } catch (error) {
            console.error('Error creating template:', error);
            toast.error('Nie udało się utworzyć szablonu');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="bg-white max-w-lg">
                <DialogHeader>
                    <DialogTitle className="flex items-center justify-between">
                        <span>Utwórz szablon diety</span>
                        <button
                            onClick={onClose}
                            className="p-1 hover:bg-gray-100 rounded-lg transition-colors"
                        >
                            <X className="h-5 w-5"/>
                        </button>
                    </DialogTitle>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Nazwa */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Nazwa szablonu *
                        </label>
                        <input
                            type="text"
                            value={formData.name}
                            onChange={(e) => setFormData(prev => ({...prev, name: e.target.value}))}
                            placeholder="np. Dieta na masę dla mężczyzn"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                            required
                        />
                    </div>

                    {/* Opis */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Opis
                        </label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData(prev => ({...prev, description: e.target.value}))}
                            placeholder="Opisz szablon diety..."
                            rows={3}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary resize-none"
                        />
                    </div>

                    {/* Kategoria */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Kategoria
                        </label>
                        <select
                            value={formData.category}
                            onChange={(e) => setFormData(prev => ({...prev, category: e.target.value}))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                        >
                            {DietTemplateService.getAllCategories().map((cat) => (
                                <option key={cat.value} value={cat.value}>
                                    {cat.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    {!formData.dietData && (
                        <>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Czas trwania (dni)
                                    </label>
                                    <input
                                        type="number"
                                        min="1"
                                        max="365"
                                        value={formData.duration}
                                        onChange={(e) => setFormData(prev => ({
                                            ...prev,
                                            duration: parseInt(e.target.value) || 1
                                        }))}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Posiłków dziennie
                                    </label>
                                    <input
                                        type="number"
                                        min="1"
                                        max="10"
                                        value={formData.mealsPerDay}
                                        onChange={(e) => setFormData(prev => ({
                                            ...prev,
                                            mealsPerDay: parseInt(e.target.value) || 1
                                        }))}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary"
                                    />
                                </div>
                            </div>
                        </>
                    )}

                    {/* Notatki */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Notatki
                        </label>
                        <textarea
                            value={formData.notes}
                            onChange={(e) => setFormData(prev => ({...prev, notes: e.target.value}))}
                            placeholder="Dodatkowe informacje o szablonie..."
                            rows={2}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-primary focus:border-primary resize-none"
                        />
                    </div>

                    {/* Informacja o źródle */}
                    {formData.dietData && (
                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-800">
                                📋 Szablon zostanie utworzony na podstawie aktualnej diety z kreatora.
                                Zawierać będzie wszystkie posiłki, składniki i ustawienia.
                            </p>
                        </div>
                    )}

                    {/* Przyciski */}
                    <div className="flex justify-end gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                            disabled={loading}
                        >
                            Anuluj
                        </button>
                        <button
                            type="submit"
                            disabled={loading || !formData.name.trim()}
                            className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                        >
                            {loading && <LoadingSpinner size="sm"/>}
                            Utwórz szablon
                        </button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default CreateTemplateDialog;