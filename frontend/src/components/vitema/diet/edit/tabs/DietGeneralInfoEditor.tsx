import React, {useEffect, useState} from "react";
import {Diet} from "../../../../../types";
import UserSelector from "../../upload/sections/user/UserSelector";
import {formatTimestamp} from "../../../../../utils/dateFormatters";
import useUsers from "../../../../../hooks/useUsers";
import {User} from "src/types/user";
import {useConfirmation} from "../../../../../hooks/useConfirmation";
import ConfirmationDialog from "../../../../shared/common/ConfirmationDialog";

interface DietGeneralInfoProps {
    diet: Diet;
    onUpdate: (updatedDiet: Diet) => Promise<void>;
}

interface UserChangeData {
    newUser: User;
    oldUser: User | null;
}

const DietGeneralInfoEditor: React.FC<DietGeneralInfoProps> = ({diet, onUpdate}) => {
    const [isEditing, setIsEditing] = useState(false);
    const [currentUser, setCurrentUser] = useState<any>(null);
    const {getUserById} = useUsers();

    const {
        isConfirmationOpen,
        confirmationData,
        openConfirmation,
        closeConfirmation
    } = useConfirmation<UserChangeData>();

    useEffect(() => {
        const loadUser = async () => {
            const user = await getUserById(diet.userId);
            setCurrentUser(user);
        };
        loadUser().catch(console.error);
    }, [diet.userId, getUserById]);

    const handleUserChange = async (newUser: User) => {
        if (newUser.id !== currentUser?.id) {
            openConfirmation({
                newUser,
                oldUser: currentUser
            });
        }
    };

    const handleConfirmUserChange = async () => {
        if (confirmationData) {
            await onUpdate({...diet, userId: confirmationData.newUser.id});
            setCurrentUser(confirmationData.newUser);
            closeConfirmation();
        }
    };

    return (
        <div className="space-y-6 p-4">
            <div className="flex justify-between items-center">
                <h3 className="text-lg font-medium">
                    Informacje ogólne
                </h3>
                <button
                    onClick={() => setIsEditing(!isEditing)}
                    className="px-4 py-2 text-sm bg-blue-50 text-blue-600 rounded-md hover:bg-blue-100"
                >
                    {isEditing ? 'Zakończ edycję' : 'Edytuj'}
                </button>
            </div>

            <div className="space-y-6">
                <div className="space-y-2 w-full">
                    <label className="block text-sm font-medium text-gray-700">
                        Użytkownik, do którego dieta jest przypisana
                    </label>
                    {isEditing ? (
                        <div className="w-full ">
                            <UserSelector
                                selectedUser={currentUser}
                                onUserSelect={handleUserChange}
                            />
                        </div>
                    ) : (
                        <div className="text-gray-900">
                            {currentUser?.email || 'Ładowanie...'}
                        </div>
                    )}
                </div>

                <ConfirmationDialog
                    isOpen={isConfirmationOpen}
                    onClose={closeConfirmation}
                    onConfirm={handleConfirmUserChange}
                    title="Potwierdź zmianę użytkownika"
                    description={`Czy na pewno chcesz zmienić przypisanego użytkownika z ${confirmationData?.oldUser?.email || ''} na ${confirmationData?.newUser?.email || ''}?`}
                    confirmLabel="Zmień użytkownika"
                    variant="warning"
                />

                <div className="grid grid-cols-2 gap-6">
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Data utworzenia diety
                        </label>
                        <div className="text-gray-900">
                            {formatTimestamp(diet.createdAt)}
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Ostatnia aktualizacja diety
                        </label>
                        <div className="text-gray-900">
                            {formatTimestamp(diet.updatedAt)}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DietGeneralInfoEditor;