package com.noisevisionsoftware.nutrilog.service.auth;

import com.noisevisionsoftware.nutrilog.exception.AuthenticationException;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FirebaseAuthenticationService firebaseAuthService;

    public FirebaseUser authenticateAdmin(String token) {
        if (token == null || token.isEmpty()) {
            log.error("Token is null or empty");
            throw new AuthenticationException("Invalid token");
        }

        FirebaseUser user = firebaseAuthService.verifyToken(token);

        if (user == null) {
            log.error("User verification failed");
            throw new AuthenticationException("User verification failed");
        }

        if (!"ADMIN".equals(user.getRole())) {
            throw new AuthenticationException("Insufficient privileges");
        }

        return user;
    }

    public FirebaseUser validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Invalid token");
        }

        FirebaseUser user = firebaseAuthService.verifyToken(token);

        if (user == null) {
            throw new AuthenticationException("Invalid token");
        }

        if (!"ADMIN".equals(user.getRole())) {
            throw new AuthenticationException("Insufficient privileges");
        }

        return user;
    }
}