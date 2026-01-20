package com.noisevisionsoftware.vitema.model.newsletter;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "newsletter_subscribers_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subscriber"})
public class NewsletterSubscribersMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    @JsonBackReference
    private NewsletterSubscriber subscriber;

    @Column(nullable = false)
    private String key;

    @Column
    private String value;
}
