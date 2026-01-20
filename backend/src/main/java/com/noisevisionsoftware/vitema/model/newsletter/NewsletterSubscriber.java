package com.noisevisionsoftware.vitema.model.newsletter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "newsletter_subscribers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"metadataEntries"})
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriberRole role;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_email_sent")
    private LocalDateTime lastEmailSent;

    @Column(name = "verification_token")
    private String verificationToken;

    @Setter
    @Transient
    private Map<String, String> metadata;

    @Setter
    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<NewsletterSubscribersMetadata> metadataEntries;

    public static NewsletterSubscriber create(String email, SubscriberRole role) {
        return NewsletterSubscriber.builder()
                .email(email)
                .role(role)
                .createdAt(LocalDateTime.now())
                .verified(false)
                .active(true)
                .verificationToken(UUID.randomUUID().toString())
                .metadataEntries(new HashSet<>())
                .build();
    }

    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
            if (metadataEntries != null) {
                for (NewsletterSubscribersMetadata entry : metadataEntries) {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return metadata;
    }

    @PrePersist
    @PreUpdate
    public void updateMetadata() {
        if (metadata != null && metadataEntries != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                boolean found = false;
                for (NewsletterSubscribersMetadata metadataEntry : metadataEntries) {
                    if (metadataEntry.getKey().equals(entry.getKey())) {
                        metadataEntry.setValue(entry.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    NewsletterSubscribersMetadata newEntry = new NewsletterSubscribersMetadata();
                    newEntry.setSubscriber(this);
                    newEntry.setKey(entry.getKey());
                    newEntry.setValue(entry.getValue());
                    metadataEntries.add(newEntry);
                }
            }
        }
    }
}
