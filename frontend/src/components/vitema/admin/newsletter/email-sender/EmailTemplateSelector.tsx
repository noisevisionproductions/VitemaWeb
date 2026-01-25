import React from 'react';
import { Check } from 'lucide-react';
import {EmailTemplate, EmailTemplateType} from "../../../../../types/email";

interface EmailTemplateSelectorProps {
    templates: EmailTemplate[];
    selectedTemplate: EmailTemplateType;
    onTemplateSelect: (templateId: EmailTemplateType) => void;
    disabled?: boolean;
    className?: string;
    isLoading?: boolean;
}

const EmailTemplateSelector: React.FC<EmailTemplateSelectorProps> = ({
                                                                         templates,
                                                                         selectedTemplate,
                                                                         onTemplateSelect,
                                                                         disabled = false,
                                                                         className = '',
                                                                         isLoading = false
                                                                     }) => {
    const getDefaultIcon = (templateId: EmailTemplateType) => {
        const iconMap: Record<string, React.ReactNode> = {
            'basic': <div className="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center">B</div>,
            'promotional': <div className="w-8 h-8 rounded-full bg-secondary text-white flex items-center justify-center">P</div>,
            'survey': <div className="w-8 h-8 rounded-full bg-nutrition-protein text-white flex items-center justify-center">A</div>,
            'announcement': <div className="w-8 h-8 rounded-full bg-status-warning text-white flex items-center justify-center">O</div>
        };

        return iconMap[templateId] || (
            <div className="w-8 h-8 rounded-full bg-gray-400 text-white flex items-center justify-center">
                {templateId.charAt(0).toUpperCase()}
            </div>
        );
    };

    if (isLoading) {
        return (
            <div className={`grid grid-cols-1 md:grid-cols-2 gap-4 ${className}`}>
                {[1, 2, 3, 4].map((index) => (
                    <div
                        key={index}
                        className="border rounded-lg p-4 animate-pulse"
                    >
                        <div className="flex items-start space-x-3">
                            <div className="w-8 h-8 rounded-full bg-gray-200"></div>
                            <div className="flex-1">
                                <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
                                <div className="h-3 bg-gray-100 rounded w-3/4"></div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className={`grid grid-cols-1 md:grid-cols-2 gap-4 ${className}`}>
            {templates.map(template => (
                <div
                    key={template.id}
                    className={`
                        relative border rounded-lg p-4 cursor-pointer transition-colors duration-200
                        ${selectedTemplate === template.id
                        ? 'border-primary bg-primary-light/10 bg-opacity-10'
                        : 'border-gray-200 hover:border-gray-300'
                    }
                        ${disabled ? 'opacity-60 cursor-not-allowed' : ''}
                    `}
                    onClick={() => !disabled && onTemplateSelect(template.id)}
                >
                    <div className="flex items-start space-x-3">
                        {template.icon || getDefaultIcon(template.id)}
                        <div className="flex-1">
                            <div className="flex items-center justify-between">
                                <h3 className="font-medium text-gray-900">{template.name}</h3>
                                {selectedTemplate === template.id && (
                                    <span className="bg-primary text-white p-1 rounded-full">
                                        <Check size={14} />
                                    </span>
                                )}
                            </div>
                            <p className="text-sm text-gray-500 mt-1">{template.description}</p>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default EmailTemplateSelector;