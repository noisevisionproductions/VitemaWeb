package com.noisevisionsoftware.nutrilog.model.newsletter;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber {
    private String id;
    private String email;
    private SubscriberRole role;
    private Timestamp createdAt;
    private boolean verified;
    private String verificationToken;
    private Timestamp verifiedAt;
    private boolean active;
    private Timestamp lastEmailSent;
    private Map<String, String> metadata;

    public static NewsletterSubscriber create(String email, SubscriberRole role) {
        return NewsletterSubscriber.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .role(role)
                .createdAt(Timestamp.now())
                .verified(false)
                .verificationToken(UUID.randomUUID().toString())
                .active(true)
                .metadata(new HashMap<>())
                .build();
    }
}
