import React, {useEffect, useState} from "react";
import UsersList from "../../../../users/UsersList";
import UserDetailsModal from "../../../../users/UserDetailsModal";
import {User} from "../../../../../../types/user";
import useUsers from "../../../../../../hooks/useUsers";
import LoadingSpinner from "../../../../../shared/common/LoadingSpinner";
import {Mail, UserPlus, Users} from "lucide-react";
import {cn} from "../../../../../../utils/cs";
import TrainerInvitationsList from "../../../../invitations/TrainerInvitationsList";
import SendInvitationModal from "../../../../invitations/SendInvitationModal";
import {useInvitations} from "../../../../../../hooks/useInvitations";
import RefreshButton from "../../../../../shared/common/RefreshButton";

type TabType = 'active-users' | 'invitations';

const UsersManagement: React.FC = () => {
    const {users, loading: usersLoading, fetchUsers} = useUsers();

    const {
        pendingCount,
        refetch: refetchInvitations,
        isLoading: invitationsLoading
    } = useInvitations();

    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [activeTab, setActiveTab] = useState<TabType>('active-users');
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

    useEffect(() => {
        fetchUsers().catch(console.error);
    }, []);

    const handleRefresh = async () => {
        if (activeTab === 'active-users') {
            await fetchUsers();
        } else {
            await refetchInvitations();
        }
    };

    const isLoading = activeTab === 'active-users' ? usersLoading : invitationsLoading;

    return (
        <div className="space-y-6 pb-8">
            <div className="flex justify-between items-start">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        {activeTab === 'active-users' ? "Zarządzanie klientami" : "Zaproszenia"}
                    </h1>
                    <p className="mt-1 text-sm text-gray-600">
                        {activeTab === 'active-users'
                            ? "Przeglądaj profile swoich podopiecznych i zarządzaj ich dietami."
                            : "Monitoruj status zaproszeń i wysyłaj nowe kody dostępu."}
                    </p>
                </div>

                <RefreshButton
                    onRefresh={handleRefresh}
                    isLoading={isLoading}
                    className="bg-white border border-gray-200 shadow-sm hover:bg-gray-50"
                />
            </div>

            <div
                className="border-b border-gray-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4 mt-6">

                {/* Taby */}
                <nav className="flex space-x-8 -mb-px overflow-x-auto" aria-label="Tabs">
                    <button
                        onClick={() => setActiveTab('active-users')}
                        className={cn(
                            "whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition-colors",
                            activeTab === 'active-users'
                                ? "border-primary text-primary"
                                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                        )}
                    >
                        <Users className="w-4 h-4"/>
                        Aktywni Klienci
                        <span className="ml-2 bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs">
                            {users.length}
                        </span>
                    </button>

                    <button
                        onClick={() => setActiveTab('invitations')}
                        className={cn(
                            "whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition-colors",
                            activeTab === 'invitations'
                                ? "border-primary text-primary"
                                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                        )}
                    >
                        <Mail className="w-4 h-4"/>
                        Zaproszenia

                        {pendingCount > 0 && (
                            <span
                                className="ml-2 bg-blue-100 text-blue-600 py-0.5 px-2 rounded-full text-xs font-bold animate-pulse">
                                {pendingCount}
                            </span>
                        )}
                    </button>
                </nav>

                {/* Przycisk Akcji */}
                <div className="pb-2 sm:pb-3">
                    <button
                        onClick={() => setIsInviteModalOpen(true)}
                        className="w-full sm:w-auto flex items-center justify-center gap-2 px-4 py-2 bg-primary text-white text-sm font-medium rounded-lg hover:bg-primary-dark transition-colors shadow-sm"
                    >
                        <UserPlus className="w-4 h-4"/>
                        <span>Zaproś klienta</span>
                    </button>
                </div>
            </div>

            {/* ZAWARTOŚĆ */}
            <div className="mt-6">
                {activeTab === 'active-users' ? (
                    usersLoading && users.length === 0 ? (
                        <div className="flex justify-center items-center h-64">
                            <LoadingSpinner/>
                        </div>
                    ) : users.length > 0 ? (
                        <UsersList
                            users={users}
                            onUserSelect={setSelectedUser}
                            onUpdate={fetchUsers}
                        />
                    ) : (
                        // Empty State
                        <div
                            className="flex flex-col items-center justify-center py-16 px-4 border-2 border-dashed border-gray-200 rounded-xl bg-gray-50/50">
                            <div className="bg-white p-4 rounded-full shadow-sm mb-4">
                                <Users className="w-10 h-10 text-gray-400"/>
                            </div>
                            <h3 className="text-lg font-medium text-gray-900">Brak aktywnych podopiecznych</h3>
                            <p className="text-gray-500 text-center max-w-sm mt-1">
                                Lista jest pusta. Użyj przycisku powyżej, aby zaprosić pierwszą osobę.
                            </p>
                        </div>
                    )
                ) : (
                    <TrainerInvitationsList hideHeader={true}/>
                )}
            </div>

            {selectedUser && (
                <UserDetailsModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={fetchUsers}
                />
            )}

            <SendInvitationModal
                isOpen={isInviteModalOpen}
                onClose={() => setIsInviteModalOpen(false)}
            />
        </div>
    );
};

export default UsersManagement;