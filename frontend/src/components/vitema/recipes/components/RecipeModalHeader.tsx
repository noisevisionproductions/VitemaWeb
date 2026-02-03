import React from 'react';
import {Edit2, Upload} from 'lucide-react';
import {Button} from "../../../shared/ui/Button";
import {DialogDescription, DialogTitle} from "../../../shared/ui/Dialog";

interface RecipeModalHeaderProps {
    title: string;
    description: string;
    loading: boolean;
    editMode: boolean;
    isCreateMode: boolean;
    onEdit: () => void;
    onUploadImage: () => void;
}

const RecipeModalHeader: React.FC<RecipeModalHeaderProps> = ({
                                                                  title,
                                                                  description,
                                                                  loading,
                                                                  editMode,
                                                                  isCreateMode,
                                                                  onEdit,
                                                                  onUploadImage
                                                              }) => {
    return (
        <div className="flex justify-between items-center">
            <DialogTitle className="text-xl">{title}</DialogTitle>
            <DialogDescription className="sr-only">{description}</DialogDescription>

            <div className="pr-5 flex gap-2">
                {!loading && !editMode && !isCreateMode && (
                    <>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={onUploadImage}
                            className="flex items-center gap-2"
                        >
                            <Upload size={16}/>
                            <span>Dodaj zdjęcie</span>
                        </Button>
                        <Button
                            variant="default"
                            size="sm"
                            onClick={onEdit}
                            className="flex items-center gap-2"
                        >
                            <Edit2 size={16}/>
                            <span>Edytuj</span>
                        </Button>
                    </>
                )}
            </div>
        </div>
    );
};

export default RecipeModalHeader;
