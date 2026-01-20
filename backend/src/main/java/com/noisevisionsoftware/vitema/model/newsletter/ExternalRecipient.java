package com.noisevisionsoftware.vitema.model.newsletter;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Klasa reprezentująca zewnętrznego odbiorcę
 */
@Entity
@Table(name = "external_recipients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"tags"})
public class ExternalRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_contact_date")
    private LocalDateTime lastContactDate;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<ExternalRecipientTag> tags = new HashSet<>();

    /**
     * Pobiera listę tagów jako proste stringi
     *
     * @return lista tagów
     */
    public List<String> getTagList() {
        return tags.stream()
                .map(ExternalRecipientTag::getTag)
                .collect(Collectors.toList());
    }

    /**
     * Ustawia listę tagów na podstawie listy stringów
     *
     * @param tagList lista tagów
     */
    public void setTagList(List<String> tagList) {
        tags.clear();

        if (tagList != null) {
            for (String tag : tagList) {
                ExternalRecipientTag tagEntity = new ExternalRecipientTag();
                tagEntity.setRecipient(this);
                tagEntity.setTag(tag);
                tags.add(tagEntity);
            }
        }
    }
}