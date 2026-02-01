import React from 'react';
import TrainerInvitationsList from '../../components/vitema/invitations/TrainerInvitationsList';

/**
 * Strona z zaproszeniami dla trenera/admina
 * 
 * Umożliwia:
 * - Przeglądanie listy wysłanych zaproszeń
 * - Wysyłanie nowych zaproszeń
 * - Kopiowanie kodów zaproszeń
 * - Monitorowanie statusu zaproszeń
 */
const InvitationsPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <TrainerInvitationsList />
            </div>
        </div>
    );
};

export default InvitationsPage;
