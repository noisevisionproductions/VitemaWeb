package com.noisevisionsoftware.nutrilog.service.auth;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthenticationService {

    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    public Authentication getAuthentication(String token) {
        try {
            FirebaseUser user = verifyToken(token);
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(
                        user,
                        token,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                );
            }
        } catch (Exception e) {
            log.error("Failed to verify Firebase token", e);
        }
        return null;
    }

    public FirebaseUser verifyToken(String token) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String uid = decodedToken.getUid();

            DocumentSnapshot userDoc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .get();

            if (!userDoc.exists()) {
                log.error("User document does not exist in Firestore for UID: {}", uid);
                return null;
            }

            UserRole role = UserRole.valueOf(userDoc.getString("role"));

            return FirebaseUser.builder()
                    .uid(uid)
                    .email(decodedToken.getEmail())
                    .role(role.name())
                    .build();
        } catch (Exception e) {
            log.error("Failed to verify Firebase token", e);
            return null;
        }
    }
}