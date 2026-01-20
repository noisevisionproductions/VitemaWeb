package com.noisevisionsoftware.vitema.controller.contact;

import com.noisevisionsoftware.vitema.model.ContactMessage;
import com.noisevisionsoftware.vitema.service.contact.AdminContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/contact")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminContactController {

    private final AdminContactService adminContactService;

    @GetMapping("/messages")
    public ResponseEntity<List<ContactMessage>> getAllMessages() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(adminContactService.getAllContactMessages());
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<ContactMessage> getMessage(@PathVariable String id) throws ExecutionException, InterruptedException {
        ContactMessage message = adminContactService.getContactMessage(id);
        if (message != null) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/messages/{id}/status")
    public ResponseEntity<?> updateMessageStatus(@PathVariable String id, @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status jest wymagany"));
        }

        adminContactService.updateMessageStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
