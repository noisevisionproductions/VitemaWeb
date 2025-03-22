package com.noisevisionsoftware.nutrilog.service.contact;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.model.ContactMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AdminContactService {

    private final Firestore firestore;
    private static final String CONTACT_COLLECTION = "contact_messages";

    public List<ContactMessage> getAllContactMessages() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("contact_messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<ContactMessage> messages = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            ContactMessage message = document.toObject(ContactMessage.class);
            message.setId(document.getId());
            messages.add(message);
        }

        return messages;
    }

    public ContactMessage getContactMessage(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(CONTACT_COLLECTION).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            ContactMessage message = document.toObject(ContactMessage.class);
            if (message != null) {
                message.setId(document.getId());
                return message;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void updateMessageStatus(String id, String status) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(CONTACT_COLLECTION).document(id);
        ApiFuture<WriteResult> result = docRef.update("status", status);
        result.get();
    }
}
