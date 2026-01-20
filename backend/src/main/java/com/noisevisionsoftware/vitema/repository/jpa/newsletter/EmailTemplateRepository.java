package com.noisevisionsoftware.vitema.repository.jpa.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    List<EmailTemplate> findAllByOrderByCreatedAtDesc();
}
