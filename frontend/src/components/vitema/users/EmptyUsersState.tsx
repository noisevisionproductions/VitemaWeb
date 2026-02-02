import {Users, SearchX} from "lucide-react";
import React from "react";

export const EmptyUsersState: React.FC<{ isSearching: boolean }> = ({isSearching}) => (
    <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
        <div className="bg-gray-100 p-3 rounded-full mb-4">
            {isSearching ? (
                <SearchX className="w-8 h-8 text-gray-400"/>
            ) : (
                <Users className="w-8 h-8 text-gray-400"/>
            )}
        </div>
        <h3 className="text-sm font-medium text-gray-900">
            {isSearching ? "Nie znaleziono użytkowników" : "Brak użytkowników na liście"}
        </h3>
        <p className="text-xs text-gray-500 mt-1 max-w-[200px]">
            {isSearching
                ? "Spróbuj zmienić frazę wyszukiwania lub sprawdź pisownię."
                : "Obecnie nie ma żadnych użytkowników do wyświetlenia."}
        </p>
    </div>
);