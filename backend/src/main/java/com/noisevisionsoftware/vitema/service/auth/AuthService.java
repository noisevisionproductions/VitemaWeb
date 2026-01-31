package com.noisevisionsoftware.vitema.service.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.noisevisionsoftware.vitema.dto.request.auth.RegisterRequest;
import com.noisevisionsoftware.vitema.exception.AuthenticationException;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.UserRepository;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FirebaseAuthenticationService firebaseAuthService;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_ROLES = Set.of(
            UserRole.ADMIN.name(),
            UserRole.OWNER.name(),
            UserRole.TRAINER.name()
    );

    public FirebaseUser authenticateAdmin(String token) {
        return verifyAndValidateUser(token, "Authentication failed");
    }

    public FirebaseUser validateToken(String token) {
        return verifyAndValidateUser(token, "Token validation failed");
    }

    /**
     * Metoda pomocnicza eliminująca duplikację kodu weryfikacji.
     */
    private FirebaseUser verifyAndValidateUser(String token, String errorMessage) {
        if (token == null || token.isEmpty()) {
            log.error("Token is null or empty");
            throw new AuthenticationException("Invalid token");
        }

        FirebaseUser user = firebaseAuthService.verifyToken(token);

        if (user == null) {
            log.error("User verification failed");
            throw new AuthenticationException(errorMessage);
        }

        if (!ALLOWED_ROLES.contains(user.getRole())) {
            log.error("User does not have sufficient privileges: {}", user.getEmail());
            throw new AuthenticationException("Insufficient privileges");
        }

        return user;
    }

    public void registerTrainer(RegisterRequest request) {
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getNickname())
                    .setEmailVerified(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

            User trainer = User.builder()
                    .id(userRecord.getUid())
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .role(UserRole.TRAINER)
                    .createdAt(System.currentTimeMillis())
                    .profileCompleted(true)
                    .build();

            userRepository.save(trainer);

        } catch (Exception e) {
            log.error("Error registering trainer", e);
            throw new RuntimeException("Rejestracja nie powiodła się: " + e.getMessage());
        }
    }
}