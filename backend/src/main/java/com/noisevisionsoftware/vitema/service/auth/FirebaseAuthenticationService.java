package com.noisevisionsoftware.vitema.service.auth;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

                if ("OWNER".equals(user.getRole())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                return new UsernamePasswordAuthenticationToken(user, token, authorities);
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