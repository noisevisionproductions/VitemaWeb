package com.noisevisionsoftware.vitema.service.contact;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.noisevisionsoftware.vitema.dto.request.ContactFormRequest;
import com.noisevisionsoftware.vitema.model.ContactMessage;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final Firestore firestore;
    private final EmailService emailService;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String CONTACT_COLLECTION = "contact_messages";

    public void processContactForm(ContactFormRequest request) throws ExecutionException, InterruptedException {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .message(request.getMessage())
                .createdAt(Timestamp.now())
                .status("NEW")
                .build();

        CollectionReference docRef = firestore.collection(CONTACT_COLLECTION);

        Map<String, Object> data = new HashMap<>();
        data.put("name", message.getName());
        data.put("email", message.getEmail());
        data.put("phone", message.getPhone());
        data.put("message", message.getMessage());
        data.put("createdAt", message.getCreatedAt());
        data.put("status", message.getStatus());

        ApiFuture<WriteResult> result = docRef.document().set(data);

        result.get();

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", request.getName());
        variables.put("email", request.getEmail());
        variables.put("phone", request.getPhone());
        variables.put("message", request.getMessage());
        variables.put("showUnsubscribe", false);

        emailService.sendTemplatedEmail(
                adminEmail,
                "Nowa wiadomość kontaktowa: " + request.getName(),
                "contact-notification",
                variables
        );

        Map<String, Object> userVariables = new HashMap<>();
        userVariables.put("name", request.getName());
        userVariables.put("showUnsubscribe", false);
        userVariables.put("newsletterUrl", frontendUrl + "/newsletter");

        emailService.sendTemplatedEmail(
                request.getEmail(),
                "Dziękujemy za kontakt - Vitema",
                "contact-confirmation",
                userVariables
        );
    }
}
