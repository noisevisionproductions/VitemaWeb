import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import {useMemo} from 'react';
import {InvitationService} from '../services/InvitationService';
import {InvitationRequest} from '../types';
import {toast} from '../utils/toast';

const INVITATIONS_QUERY_KEY = ['invitations'];

export const useInvitations = () => {
    const queryClient = useQueryClient();

    // Query do pobierania listy zaproszeń
    const {
        data: invitations = [],
        isLoading,
        error,
        refetch
    } = useQuery({
        queryKey: INVITATIONS_QUERY_KEY,
        queryFn: InvitationService.getMyInvitations,
        staleTime: 30000,
    });

    const stats = useMemo(() => {
        return {
            total: invitations.length,
            pending: invitations.filter(inv => inv.status === 'PENDING').length,
            accepted: invitations.filter(inv => inv.status === 'ACCEPTED').length
        };
    }, [invitations]);

    const sendInvitationMutation = useMutation({
        mutationFn: (request: InvitationRequest) =>
            InvitationService.sendInvitation(request),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: INVITATIONS_QUERY_KEY}).catch(console.error);
            toast.success('Zaproszenie zostało wysłane pomyślnie!');
        },
        onError: (error: any) => {
            const errorMessage =
                error.response?.data?.detail ||
                error.response?.data?.message ||
                'Nie udało się wysłać zaproszenia';
            toast.error(errorMessage);
        }
    });

    const deleteInvitationMutation = useMutation({
        mutationFn: (invitationId: string) =>
            InvitationService.deleteInvitation(invitationId),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: INVITATIONS_QUERY_KEY}).catch(console.error);
            toast.success('Zaproszenie zostało usunięte');
        },
        onError: (error: any) => {
            const errorMessage =
                error.response?.data?.detail ||
                error.response?.data?.message ||
                'Nie udało się usunąć zaproszenia';
            toast.error(errorMessage);
        }
    });

    const removeClientMutation = useMutation({
        mutationFn: (clientId: string) =>
            InvitationService.removeClient(clientId),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: INVITATIONS_QUERY_KEY}).catch(console.error);
            toast.success('Klient został usunięty. Współpraca zakończona.');
        },
        onError: (error: any) => {
            const errorMessage =
                error.response?.data?.detail ||
                error.response?.data?.message ||
                'Nie udało się usunąć klienta';
            toast.error(errorMessage);
        }
    });

    return {
        invitations,
        pendingCount: stats.pending,
        totalCount: stats.total,
        acceptedCount: stats.accepted,

        isLoading,
        error,
        refetch,
        sendInvitation: sendInvitationMutation.mutate,
        isSending: sendInvitationMutation.isPending,
        sendError: sendInvitationMutation.error,
        
        deleteInvitation: deleteInvitationMutation.mutate,
        isDeleting: deleteInvitationMutation.isPending,
        deleteError: deleteInvitationMutation.error,

        removeClient: removeClientMutation.mutate,
        isRemovingClient: removeClientMutation.isPending,
        removeClientError: removeClientMutation.error,
    };
};