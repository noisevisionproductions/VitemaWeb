package com.noisevisionsoftware.vitema.model.invitation;

public enum InvitationStatus {
    PENDING,    // Zaproszenie oczekuje na akceptację
    ACCEPTED,   // Zaproszenie zaakceptowane - aktywna współpraca
    EXPIRED,    // Zaproszenie wygasło przez timeout (7 dni)
    ENDED       // Współpraca zakończona celowo (przez usera lub trenera)
}
