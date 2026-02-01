export enum InvitationStatus {
    PENDING = 'PENDING',
    ACCEPTED = 'ACCEPTED',
    EXPIRED = 'EXPIRED'
}

export interface Invitation {
    id: string;
    trainerId: string;
    clientEmail: string;
    code: string;
    status: InvitationStatus;
    createdAt: number;
    expiresAt: number;
}

export interface InvitationRequest {
    email: string;
}

export interface InvitationResponse extends Invitation {}

export interface AcceptInvitationRequest {
    code: string;
}

export interface MessageResponse {
    message: string;
}
