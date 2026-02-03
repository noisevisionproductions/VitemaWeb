import React, {useState} from 'react';
import {User} from "../../../../../../types/user";
import {ChevronDown, ChevronUp, UserCircle} from "lucide-react";
import UserSelector from "./UserSelector";

interface UserSelectionSectionProps {
    selectedUser: User | null;
    onUserSelect: (user: User) => void;
    sectionRef?: React.RefObject<HTMLDivElement>;
}

const UserSelectionSection: React.FC<UserSelectionSectionProps> = ({
                                                                       selectedUser,
                                                                       onUserSelect,
                                                                       sectionRef
                                                                   }) => {
    const [isExpanded, setIsExpanded] = useState(true);

    const handleUserSelectionAndCollapse = (user: User) => {
        onUserSelect(user);
        setIsExpanded(false);
    };

    return (
        <div ref={sectionRef} className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-md transition-colors">
            <div
                className="flex items-center justify-between cursor-pointer"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-3">
                    <UserCircle className="h-5 w-5 text-primary dark:text-primary-light"/>
                    <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
                        Wybierz użytkownika
                    </h3>
                </div>
                <div className="flex items-center">
                    {selectedUser && (
                        <div className="mr-4 px-3 py-1.5 bg-blue-50 dark:bg-blue-900/30 rounded-lg text-blue-700 dark:text-blue-300 font-medium">
                            {selectedUser.email}
                        </div>
                    )}
                    <button
                        type="button"
                        className="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 focus:outline-none transition-colors"
                        aria-label={isExpanded ? "Zwiń listę użytkowników" : "Rozwiń listę użytkowników"}
                    >
                        {isExpanded ? (
                            <ChevronUp className="h-5 w-5"/>
                        ) : (
                            <ChevronDown className="h-5 w-5"/>
                        )}
                    </button>
                </div>
            </div>

            {isExpanded && (
                <UserSelector
                    selectedUser={selectedUser}
                    onUserSelect={handleUserSelectionAndCollapse}
                />
            )}
        </div>
    );
};

export default UserSelectionSection;