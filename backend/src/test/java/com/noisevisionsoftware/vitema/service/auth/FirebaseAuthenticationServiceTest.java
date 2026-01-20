package com.noisevisionsoftware.vitema.service.auth;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthenticationServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private Firestore firestore;

    @Mock
    private FirebaseToken firebaseToken;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private DocumentReference documentReference;

    private FirebaseAuthenticationService authService;

    @BeforeEach
    void setUp() {
        authService = new FirebaseAuthenticationService(firebaseAuth, firestore);
    }

    @Test
    void verifyToken_WhenValidTokenAndUserExists_ShouldReturnFirebaseUser() throws Exception {
        // given
        String token = "valid-token";
        String uid = "user123";
        String email = "user@example.com";

        when(firebaseAuth.verifyIdToken(token)).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn(uid);
        when(firebaseToken.getEmail()).thenReturn(email);

        CollectionReference collectionReference = mock(CollectionReference.class);
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(uid)).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("role")).thenReturn(UserRole.USER.name());

        // when
        FirebaseUser result = authService.verifyToken(token);

        // then
        assertNotNull(result);
        assertEquals(uid, result.getUid());
        assertEquals(email, result.getEmail());
        assertEquals(UserRole.USER.name(), result.getRole());

        verify(firebaseAuth).verifyIdToken(token);
        verify(firestore).collection("users");
        verify(documentReference).get();
        verify(documentSnapshot).exists();
        verify(documentSnapshot).getString("role");
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifyToken_WhenUserDocumentDoesNotExist_ShouldReturnNull() throws Exception {
        // given
        String token = "valid-token";
        String uid = "user123";

        when(firebaseAuth.verifyIdToken(token)).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn(uid);

        when(firestore.collection("users")).thenReturn(mock(com.google.cloud.firestore.CollectionReference.class));
        when(firestore.collection("users").document(uid)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(mock(com.google.api.core.ApiFuture.class));
        when(documentReference.get().get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        FirebaseUser result = authService.verifyToken(token);

        // then
        assertNull(result);

        verify(firebaseAuth).verifyIdToken(token);
        verify(documentSnapshot).exists();
        verify(documentSnapshot, never()).getString(anyString());
    }

    @Test
    void verifyToken_WhenTokenVerificationFails_ShouldReturnNull() throws Exception {
        // given
        String token = "invalid-token";

        when(firebaseAuth.verifyIdToken(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        // when
        FirebaseUser result = authService.verifyToken(token);

        // then
        assertNull(result);

        verify(firebaseAuth).verifyIdToken(token);
        verify(firestore, never()).collection(anyString());
    }

    @Test
    void getAuthentication_WhenValidToken_ShouldReturnAuthentication() {
        // given
        String token = "valid-token";
        String uid = "user123";
        String email = "user@example.com";

        FirebaseUser firebaseUser = FirebaseUser.builder()
                .uid(uid)
                .email(email)
                .role(UserRole.USER.name())
                .build();

        FirebaseAuthenticationService serviceSpy = spy(authService);
        doReturn(firebaseUser).when(serviceSpy).verifyToken(token);

        // when
        Authentication authentication = serviceSpy.getAuthentication(token);

        // then
        assertNotNull(authentication);
        assertEquals(firebaseUser, authentication.getPrincipal());
        assertEquals(token, authentication.getCredentials());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name())
        ));

        verify(serviceSpy).verifyToken(token);
    }

    @Test
    void getAuthentication_WhenTokenVerificationFails_ShouldReturnNull() {
        // given
        String token = "invalid-token";

        FirebaseAuthenticationService serviceSpy = spy(authService);
        doReturn(null).when(serviceSpy).verifyToken(token);

        // when
        Authentication authentication = serviceSpy.getAuthentication(token);

        // then
        assertNull(authentication);

        verify(serviceSpy).verifyToken(token);
    }

    @Test
    void getAuthentication_WhenExceptionOccurs_ShouldReturnNull() {
        // given
        String token = "valid-token";

        FirebaseAuthenticationService serviceSpy = spy(authService);
        doThrow(new RuntimeException("Test exception")).when(serviceSpy).verifyToken(token);

        // when
        Authentication authentication = serviceSpy.getAuthentication(token);

        // then
        assertNull(authentication);

        verify(serviceSpy).verifyToken(token);
    }
}