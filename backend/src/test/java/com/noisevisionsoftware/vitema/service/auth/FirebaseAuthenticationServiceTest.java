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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.core.Authentication;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebaseAuthenticationServiceTest {

    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String TEST_UID = "user123";
    private static final String TEST_EMAIL = "user@example.com";

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

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentFuture;

    @InjectMocks
    private FirebaseAuthenticationService authService;

    @BeforeEach
    void setUp() {
        when(firestore.collection("users")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentFuture);
    }

    @Nested
    @DisplayName("verifyToken")
    class VerifyTokenTests {

        @Test
        @DisplayName("Should return FirebaseUser when token is valid and user exists with USER role")
        void givenValidTokenAndUserExists_When_VerifyToken_Then_ReturnFirebaseUserWithUserRole() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(firebaseToken.getEmail()).thenReturn(TEST_EMAIL);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("role")).thenReturn(UserRole.USER.name());

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo(TEST_UID);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getRole()).isEqualTo(UserRole.USER.name());
            verify(firebaseAuth).verifyIdToken(VALID_TOKEN);
            verify(documentSnapshot).getString("role");
        }

        @Test
        @DisplayName("Should return FirebaseUser when user has ADMIN role")
        void givenValidTokenAndUserWithAdminRole_When_VerifyToken_Then_ReturnFirebaseUser() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(firebaseToken.getEmail()).thenReturn(TEST_EMAIL);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("role")).thenReturn(UserRole.ADMIN.name());

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN.name());
        }

        @Test
        @DisplayName("Should return FirebaseUser when user has OWNER role")
        void givenValidTokenAndUserWithOwnerRole_When_VerifyToken_Then_ReturnFirebaseUser() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(firebaseToken.getEmail()).thenReturn(TEST_EMAIL);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("role")).thenReturn(UserRole.OWNER.name());

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo(UserRole.OWNER.name());
        }

        @Test
        @DisplayName("Should return FirebaseUser when user has TRAINER role")
        void givenValidTokenAndUserWithTrainerRole_When_VerifyToken_Then_ReturnFirebaseUser() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(firebaseToken.getEmail()).thenReturn(TEST_EMAIL);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("role")).thenReturn(UserRole.TRAINER.name());

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo(UserRole.TRAINER.name());
        }

        @Test
        @DisplayName("Should return null when user document does not exist in Firestore")
        void givenValidTokenAndUserDocNotExists_When_VerifyToken_Then_ReturnNull() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(false);

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNull();
            verify(documentSnapshot).exists();
            verify(documentSnapshot, never()).getString(anyString());
        }

        @Test
        @DisplayName("Should return null when Firebase token verification fails")
        void givenInvalidToken_When_VerifyToken_Then_ReturnNull() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(INVALID_TOKEN))
                    .thenThrow(new IllegalArgumentException("Invalid token"));

            // When
            FirebaseUser result = authService.verifyToken(INVALID_TOKEN);

            // Then
            assertThat(result).isNull();
            verify(firebaseAuth).verifyIdToken(INVALID_TOKEN);
            verify(firestore, never()).collection(anyString());
        }

        @Test
        @DisplayName("Should return null when Firestore get throws exception")
        void givenFirestoreGetThrows_When_VerifyToken_Then_ReturnNull() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(documentFuture.get()).thenThrow(new ExecutionException("Firestore error", null));

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when Firestore get throws InterruptedException")
        void givenFirestoreGetInterrupted_When_VerifyToken_Then_ReturnNull() throws Exception {
            // Given
            when(firebaseAuth.verifyIdToken(VALID_TOKEN)).thenReturn(firebaseToken);
            when(firebaseToken.getUid()).thenReturn(TEST_UID);
            when(documentFuture.get()).thenThrow(new InterruptedException("Interrupted"));

            // When
            FirebaseUser result = authService.verifyToken(VALID_TOKEN);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getAuthentication")
    class GetAuthenticationTests {

        @Test
        @DisplayName("Should return Authentication with ROLE_USER when user has USER role")
        void givenValidTokenWithUserRole_When_GetAuthentication_Then_ReturnAuthenticationWithRoleUser() {
            // Given
            FirebaseAuthenticationService serviceSpy = spy(authService);
            FirebaseUser firebaseUser = FirebaseUser.builder()
                    .uid(TEST_UID)
                    .email(TEST_EMAIL)
                    .role(UserRole.USER.name())
                    .build();
            doReturn(firebaseUser).when(serviceSpy).verifyToken(VALID_TOKEN);

            // When
            Authentication authentication = serviceSpy.getAuthentication(VALID_TOKEN);

            // Then
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(firebaseUser);
            assertThat(authentication.getCredentials()).isEqualTo(VALID_TOKEN);
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Should return Authentication with ROLE_OWNER and ROLE_ADMIN when user has OWNER role")
        void givenValidTokenWithOwnerRole_When_GetAuthentication_Then_ReturnAuthenticationWithOwnerAndAdminRoles() {
            // Given
            FirebaseAuthenticationService serviceSpy = spy(authService);
            FirebaseUser firebaseUser = FirebaseUser.builder()
                    .uid(TEST_UID)
                    .email(TEST_EMAIL)
                    .role(UserRole.OWNER.name())
                    .build();
            doReturn(firebaseUser).when(serviceSpy).verifyToken(VALID_TOKEN);

            // When
            Authentication authentication = serviceSpy.getAuthentication(VALID_TOKEN);

            // Then
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_OWNER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should return Authentication with ROLE_ADMIN when user has ADMIN role")
        void givenValidTokenWithAdminRole_When_GetAuthentication_Then_ReturnAuthenticationWithRoleAdmin() {
            // Given
            FirebaseAuthenticationService serviceSpy = spy(authService);
            FirebaseUser firebaseUser = FirebaseUser.builder()
                    .uid(TEST_UID)
                    .email(TEST_EMAIL)
                    .role(UserRole.ADMIN.name())
                    .build();
            doReturn(firebaseUser).when(serviceSpy).verifyToken(VALID_TOKEN);

            // When
            Authentication authentication = serviceSpy.getAuthentication(VALID_TOKEN);

            // Then
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should return null when token verification returns null")
        void givenTokenVerificationReturnsNull_When_GetAuthentication_Then_ReturnNull() {
            // Given
            FirebaseAuthenticationService serviceSpy = spy(authService);
            doReturn(null).when(serviceSpy).verifyToken(VALID_TOKEN);

            // When
            Authentication authentication = serviceSpy.getAuthentication(VALID_TOKEN);

            // Then
            assertThat(authentication).isNull();
        }

        @Test
        @DisplayName("Should return null when token verification throws exception")
        void givenTokenVerificationThrows_When_GetAuthentication_Then_ReturnNull() {
            // Given
            FirebaseAuthenticationService serviceSpy = spy(authService);
            doThrow(new RuntimeException("Verification failed")).when(serviceSpy).verifyToken(VALID_TOKEN);

            // When
            Authentication authentication = serviceSpy.getAuthentication(VALID_TOKEN);

            // Then
            assertThat(authentication).isNull();
        }
    }
}
