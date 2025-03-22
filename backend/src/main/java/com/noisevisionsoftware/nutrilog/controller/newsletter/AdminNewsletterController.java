package com.noisevisionsoftware.nutrilog.controller.newsletter;

import com.noisevisionsoftware.nutrilog.dto.request.newsletter.BulkEmailRequest;
import com.noisevisionsoftware.nutrilog.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.nutrilog.service.newsletter.AdminNewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/newsletter")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminNewsletterController {

    private final AdminNewsletterService newsletterService;

    @GetMapping("/subscribers")
    public ResponseEntity<List<NewsletterSubscriber>> getAllSubscribers() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(newsletterService.getAllSubscribers());
    }

    @PostMapping("/subscribers/{id}/activate")
    public ResponseEntity<?> activateSubscriber(@PathVariable String id) throws ExecutionException, InterruptedException {
        newsletterService.activateSubscriber(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subscribers/{id}/verify")
    public ResponseEntity<?> verifySubscriber(@PathVariable String id) throws ExecutionException, InterruptedException {
        newsletterService.verifySubscriberManually(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscribers/{id}")
    public ResponseEntity<?> deleteSubscriber(@PathVariable String id) throws ExecutionException, InterruptedException {
        newsletterService.deleteSubscriber(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-bulk-email")
    public ResponseEntity<?> sendBulkEmail(@Valid @RequestBody BulkEmailRequest request) {
        newsletterService.sendBulkEmail(request.getSubject(), request.getContent());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNewsletterStats() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(newsletterService.getNewsletterStats());
    }

    @PostMapping("/subscribers/{id}/deactivate")
    public ResponseEntity<?> deactivateSubscriber(@PathVariable String id) throws ExecutionException, InterruptedException {
        newsletterService.deactivateSubscriber(id);
        return ResponseEntity.ok().build();
    }
}
