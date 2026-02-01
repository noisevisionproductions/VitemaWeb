export enum InvitationStatus {
    PENDING = 'PENDING',      // Oczekuje na akceptację
    ACCEPTED = 'ACCEPTED',    // Zaakceptowane - aktywna współpraca
    EXPIRED = 'EXPIRED',      // Wygasłe przez timeout (7 dni)
    ENDED = 'ENDED'           // Współpraca zakończona celowo (przez usera lub trenera)
}

export interface Invitation {
    id: string;
    trainerId: string;
    clientEmail: string;
    clientId?: string;      // ID klienta (null dla PENDING, wypełniane przy ACCEPTED)
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
