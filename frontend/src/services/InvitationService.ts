import api from '../config/axios';
import { Invitation, InvitationRequest, MessageResponse } from '../types';


export class InvitationService {

    private static readonly BASE_URL = '/invitations';
    /**
     * Wysyła zaproszenie do podopiecznego
     */
    static async sendInvitation(request: InvitationRequest): Promise<Invitation> {
        const response = await api.post(`${InvitationService.BASE_URL}/send`, request);
        return response.data;
    }

    /**
     * Pobiera listę zaproszeń trenera
     */
    static async getMyInvitations(): Promise<Invitation[]> {
        const response = await api.get(`${InvitationService.BASE_URL}/my`);
        return response.data;
    }

    /**
     * Akceptuje zaproszenie (dla aplikacji mobilnej)
     */
    static async acceptInvitation(code: string): Promise<MessageResponse> {
        const response = await api.post(`${InvitationService.BASE_URL}/accept`, { code });
        return response.data;
    }

    /**
     * Usuwa zaproszenie
     */
    static async deleteInvitation(invitationId: string): Promise<MessageResponse> {
        const response = await api.delete(`${InvitationService.BASE_URL}/${invitationId}`);
        return response.data;
    }
}
