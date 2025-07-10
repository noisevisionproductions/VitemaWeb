import React, {useState} from "react";
import {DietTemplate} from "../../../types/DietTemplate";
import { formatDistanceToNow } from "date-fns";
import { pl } from 'date-fns/locale';
import {Calendar, Clock, Edit, Eye, Play, Trash2, Users} from "lucide-react";
import ConfirmationDialog from "../../common/ConfirmationDialog";

interface DietTemplateCardProps {
    template: DietTemplate;
    onUse: () => void;
    onDelete: () => void;
}

const DietTemplateCard: React.FC<DietTemplateCardProps> = ({
                                                               template,
                                                               onUse,
                                                               onDelete
                                                           }) => {
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const getCategoryColor = (category: string) => {
        const colors: Record<string, string> = {
            'WEIGHT_LOSS': 'bg-red-100 text-red-800',
            'WEIGHT_GAIN': 'bg-green-100 text-green-800',
            'MAINTENANCE': 'bg-blue-100 text-blue-800',
            'SPORT': 'bg-orange-100 text-orange-800',
            'MEDICAL': 'bg-purple-100 text-purple-800',
            'VEGETARIAN': 'bg-emerald-100 text-emerald-800',
            'VEGAN': 'bg-teal-100 text-teal-800',
            'CUSTOM': 'bg-gray-100 text-gray-800'
        };
        return colors[category] || colors['CUSTOM'];
    };

    const formatDate = (dateString: string) => {
        try {
            return formatDistanceToNow(new Date(dateString), {
                addSuffix: true,
                locale: pl
            });
        } catch {
            return 'nieznana data';
        }
    };

    return (
        <div
            className="bg-white rounded-xl shadow-sm border hover:shadow-md transition-all duration-200 overflow-hidden">
            {/* Header */}
            <div className="p-6 pb-4">
                <div className="flex items-start justify-between mb-3">
                    <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-semibold text-gray-900 truncate mb-1">
                            {template.name}
                        </h3>
                        <span
                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getCategoryColor(template.category)}`}>
                            {template.categoryLabel}
                        </span>
                    </div>
                    {template.usageCount > 0 && (
                        <div className="flex items-center gap-1 text-sm text-gray-500 ml-3">
                            <Users className="h-4 w-4"/>
                            {template.usageCount}x
                        </div>
                    )}
                </div>

                {template.description && (
                    <p className="text-sm text-gray-600 line-clamp-2 mb-3">
                        {template.description}
                    </p>
                )}

                {/* Statystyki */}
                <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="flex items-center gap-2 text-gray-600">
                        <Calendar className="h-4 w-4"/>
                        <span>{template.duration} dni</span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                        <Clock className="h-4 w-4"/>
                        <span>{template.mealsPerDay} posiłków/dzień</span>
                    </div>
                </div>

                {/* Dodatkowe info */}
                <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
                    <div className="text-xs text-gray-500">
                        <span>Utworzono {formatDate(template.createdAt)}</span>
                        {template.lastUsed && (
                            <span className="block">Ostatnio używano {formatDate(template.lastUsed)}</span>
                        )}
                    </div>
                    <div className="flex items-center gap-1">
                        {template.hasPhotos && (
                            <span
                                className="inline-flex items-center gap-1 text-xs text-blue-600 bg-blue-50 px-2 py-1 rounded-full">
                                📷 Zdjęcia
                            </span>
                        )}
                        <span className="text-xs text-gray-500">
                            {template.totalMeals} posiłków
                        </span>
                    </div>
                </div>
            </div>

            {/* Actions */}
            <div className="px-6 py-4 bg-gray-50 border-t border-gray-100">
                <div className="flex items-center gap-2">
                    <button
                        onClick={onUse}
                        className="flex-1 flex items-center justify-center gap-2 px-3 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors text-sm font-medium"
                    >
                        <Play className="h-4 w-4"/>
                        Użyj szablonu
                    </button>

                    <button
                        onClick={() => {/* TODO: Podgląd */
                        }}
                        className="flex items-center justify-center gap-2 px-3 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                        title="Podgląd"
                    >
                        <Eye className="h-4 w-4"/>
                    </button>

                    <button
                        onClick={() => {/* TODO: Edycja */
                        }}
                        className="flex items-center justify-center gap-2 px-3 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                        title="Edytuj"
                    >
                        <Edit className="h-4 w-4"/>
                    </button>

                    <button
                        onClick={() => setShowDeleteConfirm(true)}
                        className="flex items-center justify-center gap-2 px-3 py-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-lg transition-colors"
                        title="Usuń"
                    >
                        <Trash2 className="h-4 w-4"/>
                    </button>
                </div>
            </div>

            {/* Confirmation dialog */}
            <ConfirmationDialog
                isOpen={showDeleteConfirm}
                onClose={() => setShowDeleteConfirm(false)}
                onConfirm={() => {
                    onDelete();
                    setShowDeleteConfirm(false);
                }}
                title="Usuń szablon diety"
                description={`Czy na pewno chcesz usunąć szablon "${template.name}"? Ta akcja jest nieodwracalna.`}
                confirmLabel="Usuń"
                cancelLabel="Anuluj"
                variant="destructive"
            />
        </div>
    );
};

export default DietTemplateCard;