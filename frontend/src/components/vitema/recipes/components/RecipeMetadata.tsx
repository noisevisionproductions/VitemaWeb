import React from 'react';
import {formatTimestamp} from "../../../../utils/dateFormatters";
import {Timestamp} from "firebase/firestore";

interface RecipeMetadataProps {
    id: string;
    createdAt: Timestamp;
}

const RecipeMetadata: React.FC<RecipeMetadataProps> = ({id, createdAt}) => {
    return (
        <div className="bg-white p-6 rounded-lg shadow-sm">
            <h3 className="text-sm font-medium text-gray-500 mb-2">
                Informacje dodatkowe
            </h3>
            <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                    <span className="text-gray-500">ID:</span>
                    <span className="font-mono">{id}</span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-500">Data utworzenia:</span>
                    <span>{formatTimestamp(createdAt)}</span>
                </div>
            </div>
        </div>
    );
};

export default RecipeMetadata;
