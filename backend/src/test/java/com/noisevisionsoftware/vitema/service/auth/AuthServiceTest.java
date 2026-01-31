package com.noisevisionsoftware.vitema.service.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.noisevisionsoftware.vitema.dto.request.auth.RegisterRequest;
import com.noisevisionsoftware.vitema.exception.AuthenticationException;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.UserRepository;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FirebaseAuthenticationService firebaseAuthService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private static final String VALID_TOKEN = "valid_token";
    private static final String INVALID_TOKEN = "";
    private static final String TEST_UID = "test-uid";
    private static final String TEST_EMAIL = "admin@example.com";

    private FirebaseUser adminUser;
    private FirebaseUser ownerUser;
    private FirebaseUser trainerUser;
    private FirebaseUser regularUser;

    @BeforeEach
    void setUp() {
        adminUser = FirebaseUser.builder()
                .uid(TEST_UID)
                .email(TEST_EMAIL)
                .role(UserRole.ADMIN.name())
                .build();

        ownerUser = FirebaseUser.builder()
                .uid("owner-uid")
                .email("owner@example.com")
                .role(UserRole.OWNER.name())
                .build();

        trainerUser = FirebaseUser.builder()
                .uid("trainer-uid")
                .email("trainer@example.com")
                .role(UserRole.TRAINER.name())
                .build();

        regularUser = FirebaseUser.builder()
                .uid("user-uid")
                .email("user@example.com")
                .role(UserRole.USER.name())
                .build();
    }

    @Nested
    @DisplayName("authenticateAdmin")
    class AuthenticateAdminTests {

        @Test
        @DisplayName("Should return user when token is valid and user has ADMIN role")
        void givenValidAdminToken_When_AuthenticateAdmin_Then_ReturnAdminUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(adminUser);

            // When
            FirebaseUser result = authService.authenticateAdmin(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo(TEST_UID);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should return user when token is valid and user has OWNER role")
        void givenValidOwnerToken_When_AuthenticateAdmin_Then_ReturnOwnerUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(ownerUser);

            // When
            FirebaseUser result = authService.authenticateAdmin(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo("owner-uid");
            assertThat(result.getEmail()).isEqualTo("owner@example.com");
            assertThat(result.getRole()).isEqualTo(UserRole.OWNER.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should return user when token is valid and user has TRAINER role")
        void givenValidTrainerToken_When_AuthenticateAdmin_Then_ReturnTrainerUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(trainerUser);

            // When
            FirebaseUser result = authService.authenticateAdmin(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo("trainer-uid");
            assertThat(result.getEmail()).isEqualTo("trainer@example.com");
            assertThat(result.getRole()).isEqualTo(UserRole.TRAINER.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when regular USER tries to authenticate")
        void givenValidUserToken_When_AuthenticateAdmin_Then_ThrowInsufficientPrivileges() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(regularUser);

            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.authenticateAdmin(VALID_TOKEN))
                    .withMessage("Insufficient privileges");
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token is null")
        void givenNullToken_When_AuthenticateAdmin_Then_ThrowInvalidToken() {
            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.authenticateAdmin(null))
                    .withMessage("Invalid token");
            verifyNoInteractions(firebaseAuthService);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token is empty")
        void givenEmptyToken_When_AuthenticateAdmin_Then_ThrowInvalidToken() {
            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.authenticateAdmin(INVALID_TOKEN))
                    .withMessage("Invalid token");
            verifyNoInteractions(firebaseAuthService);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token verification returns null")
        void givenTokenVerificationFails_When_AuthenticateAdmin_Then_ThrowAuthenticationFailed() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(null);

            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.authenticateAdmin(VALID_TOKEN))
                    .withMessage("Authentication failed");
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return user when token is valid and user has ADMIN role")
        void givenValidAdminToken_When_ValidateToken_Then_ReturnAdminUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(adminUser);

            // When
            FirebaseUser result = authService.validateToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo(TEST_UID);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should return user when token is valid and user has OWNER role")
        void givenValidOwnerToken_When_ValidateToken_Then_ReturnOwnerUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(ownerUser);

            // When
            FirebaseUser result = authService.validateToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo("owner-uid");
            assertThat(result.getEmail()).isEqualTo("owner@example.com");
            assertThat(result.getRole()).isEqualTo(UserRole.OWNER.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should return user when token is valid and user has TRAINER role")
        void givenValidTrainerToken_When_ValidateToken_Then_ReturnTrainerUser() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(trainerUser);

            // When
            FirebaseUser result = authService.validateToken(VALID_TOKEN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUid()).isEqualTo("trainer-uid");
            assertThat(result.getEmail()).isEqualTo("trainer@example.com");
            assertThat(result.getRole()).isEqualTo(UserRole.TRAINER.name());
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when regular USER tries to validate token")
        void givenValidUserToken_When_ValidateToken_Then_ThrowInsufficientPrivileges() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(regularUser);

            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.validateToken(VALID_TOKEN))
                    .withMessage("Insufficient privileges");
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token is null")
        void givenNullToken_When_ValidateToken_Then_ThrowInvalidToken() {
            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.validateToken(null))
                    .withMessage("Invalid token");
            verifyNoInteractions(firebaseAuthService);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token is empty")
        void givenEmptyToken_When_ValidateToken_Then_ThrowInvalidToken() {
            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.validateToken(INVALID_TOKEN))
                    .withMessage("Invalid token");
            verifyNoInteractions(firebaseAuthService);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token verification returns null")
        void givenTokenVerificationFails_When_ValidateToken_Then_ThrowTokenValidationFailed() {
            // Given
            when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(null);

            // When & Then
            assertThatExceptionOfType(AuthenticationException.class)
                    .isThrownBy(() -> authService.validateToken(VALID_TOKEN))
                    .withMessage("Token validation failed");
            verify(firebaseAuthService).verifyToken(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("registerTrainer")
    class RegisterTrainerTests {

        private static final String TEST_EMAIL = "trainer@example.com";
        private static final String TEST_PASSWORD = "password123";
        private static final String TEST_NICKNAME = "TestTrainer";
        private static final String TEST_UID = "firebase-uid-123";

        private RegisterRequest registerRequest;

        @BeforeEach
        void setUp() {
            registerRequest = new RegisterRequest();
            registerRequest.setEmail(TEST_EMAIL);
            registerRequest.setPassword(TEST_PASSWORD);
            registerRequest.setNickname(TEST_NICKNAME);
        }

        private UserRecord createMockUserRecord() {
            UserRecord userRecord = mock(UserRecord.class);
            when(userRecord.getUid()).thenReturn(TEST_UID);
            // getEmail() is not used in AuthService.registerTrainer, only getUid() is used
            return userRecord;
        }

        @Test
        @DisplayName("Should successfully register trainer when all data is valid")
        void givenValidRequest_When_RegisterTrainer_Then_SaveTrainerSuccessfully() throws FirebaseAuthException {
            // Given
            UserRecord userRecord = createMockUserRecord();
            try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                authService.registerTrainer(registerRequest);

                // Then
                verify(firebaseAuthInstance).createUser(any(UserRecord.CreateRequest.class));

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());

                User savedUser = userCaptor.getValue();
                assertThat(savedUser.getId()).isEqualTo(TEST_UID);
                assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
                assertThat(savedUser.getNickname()).isEqualTo(TEST_NICKNAME);
                assertThat(savedUser.getRole()).isEqualTo(UserRole.TRAINER);
                assertThat(savedUser.isProfileCompleted()).isTrue();
                assertThat(savedUser.getCreatedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when Firebase Auth throws FirebaseAuthException")
        void givenFirebaseAuthException_When_RegisterTrainer_Then_ThrowRuntimeException() throws FirebaseAuthException {
            // Given
            try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                // Use IllegalArgumentException which is commonly thrown by Firebase Auth
                IllegalArgumentException firebaseException = new IllegalArgumentException("Email already exists");
                when(firebaseAuthInstance.createUser(any(UserRecord.CreateRequest.class))).thenThrow(firebaseException);

                // When & Then
                assertThatExceptionOfType(RuntimeException.class)
                        .isThrownBy(() -> authService.registerTrainer(registerRequest))
                        .withMessageContaining("Rejestracja nie powiodła się")
                        .withMessageContaining("Email already exists");

                verify(firebaseAuthInstance).createUser(any(UserRecord.CreateRequest.class));
                verifyNoInteractions(userRepository);
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when Firebase Auth throws generic Exception")
        void givenGenericException_When_RegisterTrainer_Then_ThrowRuntimeException() throws FirebaseAuthException {
            // Given
            try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                RuntimeException genericException = new RuntimeException("Network error");
                when(firebaseAuthInstance.createUser(any(UserRecord.CreateRequest.class))).thenThrow(genericException);

                // When & Then
                assertThatExceptionOfType(RuntimeException.class)
                        .isThrownBy(() -> authService.registerTrainer(registerRequest))
                        .withMessageContaining("Rejestracja nie powiodła się")
                        .withMessageContaining("Network error");

                verify(firebaseAuthInstance).createUser(any(UserRecord.CreateRequest.class));
                verifyNoInteractions(userRepository);
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when UserRepository.save throws exception")
        void givenRepositoryException_When_RegisterTrainer_Then_ThrowRuntimeException() throws FirebaseAuthException {
            // Given
            UserRecord userRecord = createMockUserRecord();
            try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
                RuntimeException repositoryException = new RuntimeException("Database connection failed");
                when(userRepository.save(any(User.class))).thenThrow(repositoryException);

                // When & Then
                assertThatExceptionOfType(RuntimeException.class)
                        .isThrownBy(() -> authService.registerTrainer(registerRequest))
                        .withMessageContaining("Rejestracja nie powiodła się")
                        .withMessageContaining("Database connection failed");

                verify(firebaseAuthInstance).createUser(any(UserRecord.CreateRequest.class));
                verify(userRepository).save(any(User.class));
            }
        }

        @Test
        @DisplayName("Should set correct timestamp when saving trainer")
        void givenValidRequest_When_RegisterTrainer_Then_SetCorrectTimestamp() throws FirebaseAuthException {
            // Given
            UserRecord userRecord = createMockUserRecord();
            long beforeRegistration = System.currentTimeMillis();
            try (MockedStatic<FirebaseAuth> mockedFirebaseAuth = mockStatic(FirebaseAuth.class)) {
                FirebaseAuth firebaseAuthInstance = mock(FirebaseAuth.class);
                mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthInstance);
                when(firebaseAuthInstance.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                authService.registerTrainer(registerRequest);

                // Then
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());

                User savedUser = userCaptor.getValue();
                long afterRegistration = System.currentTimeMillis();
                assertThat(savedUser.getCreatedAt()).isNotNull();
                assertThat(savedUser.getCreatedAt()).isGreaterThanOrEqualTo(beforeRegistration);
                assertThat(savedUser.getCreatedAt()).isLessThanOrEqualTo(afterRegistration);
            }
        }
    }
}
