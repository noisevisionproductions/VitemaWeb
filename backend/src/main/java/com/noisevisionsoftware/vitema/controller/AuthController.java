package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.LoginRequest;
import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LoginRequest loginRequest) {
        try {
            if (authHeader == null || authHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Authorization header is missing or empty"));
            }

            if (!authHeader.startsWith("Bearer ") || authHeader.length() == 7) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid authorization header format"));
            }

            String token = authHeader.substring(7);

            if (token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token is empty"));
            }

            FirebaseUser user = authService.authenticateAdmin(token);

            if (!user.getEmail().equals(loginRequest.getEmail())) {
                log.warn("Email mismatch: {} vs {}", user.getEmail(), loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Nieprawidłowe dane uwierzytelniające"));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage() != null ? e.getMessage() : "Authentication failed"));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() == 7) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            FirebaseUser user = authService.validateToken(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}