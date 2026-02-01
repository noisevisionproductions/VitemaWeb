package com.noisevisionsoftware.vitema.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.invitation.FirestoreInvitationMapper;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InvitationRepository {
    private final Firestore firestore;
    private final FirestoreInvitationMapper firestoreInvitationMapper;
    private static final String COLLECTION_NAME = "invitations";

    public Invitation save(Invitation invitation) {
        try {
            DocumentReference docRef;
            if (invitation.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(invitation.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                invitation.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreInvitationMapper.toFirestoreMap(invitation);
            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save invitation", e);
            throw new RuntimeException("Failed to save invitation", e);
        }
        return invitation;
    }

    public Optional<Invitation> findByCode(String code) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("code", code)
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(firestoreInvitationMapper.toInvitation(snapshot.getDocuments().getFirst()));
        } catch (Exception e) {
            log.error("Failed to find invitation by code: {}", code, e);
            throw new RuntimeException("Failed to find invitation by code", e);
        }
    }

    public List<Invitation> findByTrainerId(String trainerId) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("trainerId", trainerId)
                    .get()
                    .get();

            return snapshot.getDocuments().stream()
                    .map(firestoreInvitationMapper::toInvitation)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch invitations for trainer: {}", trainerId, e);
            throw new RuntimeException("Failed to fetch invitations", e);
        }
    }

    public boolean existsByCode(String code) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("code", code)
                    .limit(1)
                    .get()
                    .get();

            return !snapshot.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check if invitation exists by code: {}", code, e);
            throw new RuntimeException("Failed to check invitation existence", e);
        }
    }

    public void update(String id, Invitation invitation) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            Map<String, Object> data = firestoreInvitationMapper.toFirestoreMap(invitation);
            docRef.update(data).get();
        } catch (Exception e) {
            log.error("Failed to update invitation with id: {}", id, e);
            throw new RuntimeException("Failed to update invitation", e);
        }
    }

    public Optional<Invitation> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreInvitationMapper.toInvitation(document));
        } catch (Exception e) {
            log.error("Failed to fetch invitation by id: {}", id, e);
            throw new RuntimeException("Failed to fetch invitation", e);
        }
    }

    public Optional<Invitation> findPendingByClientEmail(String clientEmail) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("clientEmail", clientEmail)
                    .whereEqualTo("status", "PENDING")
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(firestoreInvitationMapper.toInvitation(snapshot.getDocuments().get(0)));
        } catch (Exception e) {
            log.error("Failed to find pending invitation by email: {}", clientEmail, e);
            throw new RuntimeException("Failed to find pending invitation by email", e);
        }
    }

    public void delete(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            docRef.delete().get();
            log.info("Successfully deleted invitation with id: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete invitation with id: {}", id, e);
            throw new RuntimeException("Failed to delete invitation", e);
        }
    }

    /**
     * Finds all expired pending invitations.
     * Returns invitations where status is PENDING and expiresAt is less than the given timestamp.
     *
     * @param currentTime current timestamp in milliseconds
     * @return list of expired invitations
     */
    public List<Invitation> findExpiredPendingInvitations(long currentTime) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", "PENDING")
                    .whereLessThan("expiresAt", currentTime)
                    .get()
                    .get();

            List<Invitation> expiredInvitations = snapshot.getDocuments().stream()
                    .map(firestoreInvitationMapper::toInvitation)
                    .collect(Collectors.toList());

            log.debug("Found {} expired pending invitations", expiredInvitations.size());
            return expiredInvitations;
        } catch (Exception e) {
            log.error("Failed to fetch expired pending invitations", e);
            throw new RuntimeException("Failed to fetch expired pending invitations", e);
        }
    }

    /**
     * Znajduje zaakceptowane zaproszenie dla konkretnego klienta i trenera.
     * Wykorzystywane przy procesie rozłączania współpracy.
     *
     * UWAGA: Ta metoda wymaga Firestore Composite Index:
     * - Collection: invitations
     * - Fields: clientEmail (Ascending), trainerId (Ascending), status (Ascending)
     * 
     * @param clientEmail email podopiecznego
     * @param trainerId   ID trenera
     * @return Optional z zaproszeniem
     */
    public Optional<Invitation> findAcceptedByEmailAndTrainer(String clientEmail, String trainerId) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("clientEmail", clientEmail)
                    .whereEqualTo("trainerId", trainerId)
                    .whereEqualTo("status", "ACCEPTED")
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                log.warn("⚠️ Nie znaleziono zaakceptowanego zaproszenia dla: {} i trenera: {}", clientEmail, trainerId);
                log.warn("⚠️ Jeśli widzisz błąd 'FAILED_PRECONDITION', musisz utworzyć Firestore Composite Index:");
                log.warn("⚠️ Collection: invitations, Fields: clientEmail (Asc), trainerId (Asc), status (Asc)");
                return Optional.empty();
            }

            return Optional.ofNullable(firestoreInvitationMapper.toInvitation(snapshot.getDocuments().get(0)));
        } catch (com.google.cloud.firestore.FirestoreException e) {
            if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                log.error("❌ FIRESTORE INDEX MISSING! Utwórz Composite Index dla 'invitations':");
                log.error("❌ Fields: clientEmail (Asc), trainerId (Asc), status (Asc)");
                log.error("❌ Link do utworzenia indexu prawdopodobnie w komunikacie błędu powyżej ↑");
            }
            log.error("Błąd podczas szukania zaakceptowanego zaproszenia dla email: {}, trainer: {}", 
                    clientEmail, trainerId, e);
            throw new RuntimeException("Failed to find accepted invitation - check Firestore indexes", e);
        } catch (Exception e) {
            log.error("Błąd podczas szukania zaakceptowanego zaproszenia dla email: {}", clientEmail, e);
            throw new RuntimeException("Failed to find accepted invitation", e);
        }
    }
}
