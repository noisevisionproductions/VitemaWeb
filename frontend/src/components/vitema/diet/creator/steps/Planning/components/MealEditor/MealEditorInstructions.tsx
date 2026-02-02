import React, {useRef, useEffect} from 'react';

interface MealEditorInstructionsProps {
    instructions: string;
    onChange: (instructions: string) => void;
}

const MealEditorInstructions: React.FC<MealEditorInstructionsProps> = ({
                                                                            instructions,
                                                                            onChange
                                                                        }) => {
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    // Auto-resize textarea based on content
    useEffect(() => {
        const textarea = textareaRef.current;
        if (textarea) {
            textarea.style.height = 'auto';
            textarea.style.height = `${textarea.scrollHeight}px`;
        }
    }, [instructions]);

    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
                Instrukcje przygotowania
            </label>
            <textarea
                ref={textareaRef}
                value={instructions}
                onChange={(e) => onChange(e.target.value)}
                placeholder="Opisz jak przygotować posiłek..."
                rows={4}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-primary focus:border-primary resize-none"
                style={{minHeight: '100px'}}
            />
            <p className="mt-1 text-xs text-gray-500">
                Możesz dodać szczegółowe instrukcje przygotowania posiłku
            </p>
        </div>
    );
};

export default MealEditorInstructions;
