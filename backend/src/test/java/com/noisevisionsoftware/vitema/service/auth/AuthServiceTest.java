package com.noisevisionsoftware.vitema.service.auth;

import com.noisevisionsoftware.vitema.exception.AuthenticationException;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FirebaseAuthenticationService firebaseAuthService;

    @InjectMocks
    private AuthService authService;

    private static final String VALID_TOKEN = "valid_token";
    private static final String INVALID_TOKEN = "";
    private static final String TEST_UID = "test-uid";
    private static final String TEST_EMAIL = "admin@example.com";

    private FirebaseUser adminUser;
    private FirebaseUser regularUser;

    @BeforeEach
    void setUp() {
        adminUser = FirebaseUser.builder()
                .uid(TEST_UID)
                .email(TEST_EMAIL)
                .role("ADMIN")
                .build();

        regularUser = FirebaseUser.builder()
                .uid(TEST_UID)
                .email("user@example.com")
                .role("USER")
                .build();
    }

    @Test
    void authenticateAdmin_WithValidAdminToken_ShouldReturnUser() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(adminUser);

        // Act
        FirebaseUser result = authService.authenticateAdmin(VALID_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_UID, result.getUid());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals("ADMIN", result.getRole());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    void authenticateAdmin_WithNullToken_ShouldThrowException() {
        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticateAdmin(null)
        );

        assertEquals("Invalid token", exception.getMessage());

        // Verify
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void authenticateAdmin_WithEmptyToken_ShouldThrowException() {
        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticateAdmin(INVALID_TOKEN)
        );

        assertEquals("Invalid token", exception.getMessage());

        // Verify
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void authenticateAdmin_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(regularUser);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticateAdmin(VALID_TOKEN)
        );

        assertEquals("Insufficient privileges", exception.getMessage());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    void authenticateAdmin_WithVerificationFailure_ShouldThrowException() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticateAdmin(VALID_TOKEN)
        );

        assertEquals("User verification failed", exception.getMessage());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should successfully validate token for admin user")
    void validateToken_WithValidAdminToken_ShouldReturnUser() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(adminUser);

        // Act
        FirebaseUser result = authService.validateToken(VALID_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_UID, result.getUid());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals("ADMIN", result.getRole());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should throw exception when token is null")
    void validateToken_WithNullToken_ShouldThrowException() {
        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.validateToken(null)
        );

        assertEquals("Invalid token", exception.getMessage());

        // Verify
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    @DisplayName("Should throw exception when token is empty")
    void validateToken_WithEmptyToken_ShouldThrowException() {
        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.validateToken(INVALID_TOKEN)
        );

        assertEquals("Invalid token", exception.getMessage());

        // Verify
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    @DisplayName("Should throw exception for non-admin user")
    void validateToken_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(regularUser);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.validateToken(VALID_TOKEN)
        );

        assertEquals("Insufficient privileges", exception.getMessage());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should throw exception when token verification fails")
    void validateToken_WithVerificationFailure_ShouldThrowException() {
        // Arrange
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.validateToken(VALID_TOKEN)
        );

        assertEquals("Invalid token", exception.getMessage());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should successfully validate token for owner user")
    void validateToken_WithOwnerToken_ShouldReturnUser() {
        // Arrange
        FirebaseUser ownerUser = FirebaseUser.builder()
                .uid(TEST_UID)
                .email(TEST_EMAIL)
                .role("OWNER")
                .build();
        when(firebaseAuthService.verifyToken(VALID_TOKEN)).thenReturn(ownerUser);

        // Act
        FirebaseUser result = authService.validateToken(VALID_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_UID, result.getUid());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals("OWNER", result.getRole());

        // Verify
        verify(firebaseAuthService).verifyToken(VALID_TOKEN);
    }
}