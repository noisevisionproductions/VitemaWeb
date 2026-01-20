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
        <div ref={sectionRef} className="bg-white p-6 rounded-lg shadow-md">
            <div
                className="flex items-center justify-between cursor-pointer"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-3">
                    <UserCircle className="h-5 w-5 text-primary"/>
                    <h3 className="text-lg font-medium">
                        Wybierz użytkownika
                    </h3>
                </div>
                <div className="flex items-center">
                    {selectedUser && (
                        <div className="mr-4 px-3 py-1.5 bg-blue-50 rounded-lg text-blue-700 font-medium">
                            {selectedUser.email}
                        </div>
                    )}
                    <button
                        type="button"
                        className="text-gray-500 hover:text-gray-700 focus:outline-none"
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