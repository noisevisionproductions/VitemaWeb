import React, {useState} from 'react';
import {formatTimestamp} from "../../utils/dateFormatters";
import {Diet} from "../../types";
import {Trash2, Eye, Edit2, Calendar, User, Clock, FileText} from 'lucide-react';
import ConfirmationDialog from "../common/ConfirmationDialog";
import {getDietWarningStatus, isDietEnded} from "../../utils/dietWarningUtils";
import DietWarningIndicator from "../common/DietWarningIndicator";

interface DietCardProps {
    diet: Diet & { userEmail?: string };
    onViewClick: () => void;
    onEditClick: () => void;
    onDeleteClick: (dietId: string) => void;
}

const DietCard: React.FC<DietCardProps> = ({
                                               diet,
                                               onViewClick,
                                               onEditClick,
                                               onDeleteClick
                                           }) => {
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

    const warningStatus = getDietWarningStatus(diet);
    const isEnded = isDietEnded(diet);

    const getDietPeriod = (days: Diet['days']) => {
        if (!days || days.length === 0) return 'Brak dni';

        const sortedDays = [...days].sort((a, b) => {
            return a.date.seconds - b.date.seconds;
        });

        const firstDay = formatTimestamp(sortedDays[0].date);
        const lastDay = formatTimestamp(sortedDays[sortedDays.length - 1].date);

        return `${firstDay} - ${lastDay}`;
    };

    const handleDelete = async () => {
        try {
            setIsDeleting(true);
            onDeleteClick(diet.id);
        } catch (error) {
            console.error('Error deleting diet:', error);
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirmation(false);
        }
    };

    const getCardStyles = () => {
        if (warningStatus === 'critical') {
            return 'border-l-4 border-l-red-500 bg-red-50';
        } else if (warningStatus === 'warning') {
            return 'border-l-4 border-l-amber-500 bg-amber-50';
        } else if (isEnded) {
            return 'border-l-4 border-l-gray-400 bg-gray-50';
        } else {
            return 'border-l-4 border-l-blue-500';
        }
    };

    const fileName = diet.metadata?.fileName
        ? (diet.metadata.fileName.length > 25
            ? diet.metadata.fileName.substring(0, 22) + '...'
            : diet.metadata.fileName)
        : 'Brak nazwy';

    return (
        <>
            <div
                className={`bg-white rounded-lg shadow-sm border border-slate-200
                hover:shadow-md transition-all duration-300 ${getCardStyles()} overflow-hidden`}
            >
                <div className="flex justify-between items-start p-4">
                    <div className="flex flex-col">
                        <h3 className="text-slate-800 font-medium flex items-center">
                            <User className="h-3.5 w-3.5 mr-2 text-slate-500"/>
                            {diet.userEmail || 'Brak emaila'}
                        </h3>
                        {(warningStatus !== 'normal' || isEnded) && (
                            <DietWarningIndicator
                                status={warningStatus}
                                diet={diet}
                                size="sm"
                            />
                        )}
                        <span className="text-xs text-slate-500 mt-1">
                            ID: {diet.id.slice(0, 8)}
                        </span>
                    </div>
                    <button
                        onClick={() => setShowDeleteConfirmation(true)}
                        className="text-slate-400 hover:text-red-500 p-1 transition-colors"
                        aria-label="Usuń dietę"
                    >
                        <Trash2 className="h-4 w-4"/>
                    </button>
                </div>

                <div className="px-4 pb-3 pt-1">
                    <div className="space-y-2 mb-3">
                        <div className="flex items-center text-xs text-slate-600">
                            <FileText className="h-4 w-4 mr-2 text-slate-600"/>
                            <span className="truncate max-w-[200px]">{fileName}</span>
                        </div>

                        <div className="flex items-center text-xs text-slate-600">
                            <Calendar className="h-3.5 w-3.5 mr-2 text-slate-500"/>
                            <span>{getDietPeriod(diet.days)}</span>
                        </div>

                        <div className="flex items-center text-xs text-slate-600">
                            <Clock className="h-3.5 w-3.5 mr-2 text-slate-500"/>
                            <span>Utworzono: {diet.createdAt ? formatTimestamp(diet.createdAt, true) : 'Brak daty'}</span>
                        </div>
                    </div>

                    <div className="flex border-t border-slate-200 pt-3 space-x-1">
                        <button
                            onClick={onViewClick}
                            className="flex-1 flex items-center justify-center text-xs rounded-md py-1.5 bg-white text-blue-600 hover:bg-blue-50 transition-colors font-medium shadow-sm"
                        >
                            <Eye className="h-3 w-3 mr-1"/>
                            Podgląd
                        </button>
                        <button
                            onClick={onEditClick}
                            className="flex-1 flex items-center justify-center text-xs rounded-md py-1.5 bg-white text-emerald-600 hover:bg-emerald-50 transition-colors font-medium shadow-sm"
                        >
                            <Edit2 className="h-3 w-3 mr-1"/>
                            Edytuj
                        </button>
                    </div>
                </div>
            </div>

            <ConfirmationDialog
                isOpen={showDeleteConfirmation}
                onClose={() => setShowDeleteConfirmation(false)}
                onConfirm={handleDelete}
                title="Potwierdzenie usunięcia"
                description="Czy na pewno chcesz usunąć tę dietę? Ta operacja jest nieodwracalna i spowoduje usunięcie wszystkich powiązanych danych, w tym listy zakupów i referencji do przepisów."
                confirmLabel="Usuń dietę"
                variant="destructive"
                isLoading={isDeleting}
            />
        </>
    );
};

export default DietCard;