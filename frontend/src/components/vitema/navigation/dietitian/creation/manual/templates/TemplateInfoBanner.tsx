import React from 'react';
import { Sparkles, Edit, X } from 'lucide-react';
import { DietTemplate} from "../../../../../../../types/DietTemplate";

interface TemplateInfoBannerProps {
    template: DietTemplate;
    onRemoveTemplate: () => void;
    onEditTemplate?: () => void;
}

const TemplateInfoBanner: React.FC<TemplateInfoBannerProps> = ({
                                                                   template,
                                                                   onRemoveTemplate,
                                                                   onEditTemplate
                                                               }) => {
    return (
        <div className="bg-gradient-to-r from-primary-light/20 to-secondary-light/20 border border-primary-light/30 rounded-xl p-4 mb-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-primary-light/30 rounded-lg flex items-center justify-center">
                        <Sparkles className="h-5 w-5 text-primary" />
                    </div>
                    <div>
                        <h4 className="font-medium text-gray-900">
                            Używasz szablonu: "{template.name}"
                        </h4>
                        <p className="text-sm text-gray-600">
                            {template.categoryLabel} • {template.duration} dni • {template.mealsPerDay} posiłków dziennie
                        </p>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    {onEditTemplate && (
                        <button
                            onClick={onEditTemplate}
                            className="p-2 text-gray-600 hover:text-gray-800 hover:bg-white/50 rounded-lg transition-colors"
                            title="Edytuj szablon"
                        >
                            <Edit className="h-4 w-4" />
                        </button>
                    )}
                    <button
                        onClick={onRemoveTemplate}
                        className="p-2 text-gray-600 hover:text-red-600 hover:bg-white/50 rounded-lg transition-colors"
                        title="Usuń szablon i kontynuuj bez niego"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default TemplateInfoBanner;