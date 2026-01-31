package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.auth.LoginRequest;
import com.noisevisionsoftware.vitema.dto.request.auth.RegisterRequest;
import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.dto.response.MessageResponse;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private FirebaseUser firebaseUser;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private static final String VALID_TOKEN = "valid_token";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = BEARER_PREFIX + INVALID_TOKEN;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "testPassword");
    }

    @Test
    void login_WithValidToken_ShouldReturnOkWithUser() {
        String validAuthHeader = BEARER_PREFIX + VALID_TOKEN;
        when(firebaseUser.getUid()).thenReturn("test-uid");
        when(firebaseUser.getEmail()).thenReturn("test@example.com");
        when(authService.authenticateAdmin(VALID_TOKEN)).thenReturn(firebaseUser);

        ResponseEntity<?> response = authController.login(validAuthHeader, loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(FirebaseUser.class, response.getBody());

        FirebaseUser responseUser = (FirebaseUser) response.getBody();
        assertEquals("test-uid", responseUser.getUid());
        assertEquals("test@example.com", responseUser.getEmail());

        verify(authService).authenticateAdmin(VALID_TOKEN);
    }

    @Test
    void login_WithInvalidToken_ShouldReturnUnauthorized() {
        String errorMessage = "Invalid token";
        when(authService.authenticateAdmin(INVALID_TOKEN))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = authController.login(AUTH_HEADER, loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(authService).authenticateAdmin(INVALID_TOKEN);
    }

    @Test
    void login_WithNullErrorMessage_ShouldReturnDefaultMessage() {
        when(authService.authenticateAdmin(INVALID_TOKEN))
                .thenThrow(new RuntimeException());

        ResponseEntity<?> response = authController.login(AUTH_HEADER, loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Authentication failed", errorResponse.getMessage());

        verify(authService).authenticateAdmin(INVALID_TOKEN);
    }

    @Test
    void login_WithMissingAuthorizationHeader_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
    }

    @Test
    void login_WithEmptyToken_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(BEARER_PREFIX, loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header format", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithInvalidHeaderFormat_ShouldReturnUnauthorized() {
        String authHeader = "Invalid-Format";

        ResponseEntity<?> response = authController.login(authHeader, loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header format", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithNullHeader_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Authorization header is missing or empty", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithEmailMismatch_ShouldReturnUnauthorized() {
        // Arrange
        String validAuthHeader = BEARER_PREFIX + VALID_TOKEN;
        when(firebaseUser.getEmail()).thenReturn("different@example.com");
        when(authService.authenticateAdmin(VALID_TOKEN)).thenReturn(firebaseUser);

        // Act
        ResponseEntity<?> response = authController.login(validAuthHeader, loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Nieprawidłowe dane uwierzytelniające", errorResponse.getMessage());

        verify(authService).authenticateAdmin(VALID_TOKEN);
    }

    @Test
    void login_WithEmptyAuthHeader_ShouldReturnUnauthorized() {
        // Arrange
        String emptyAuthHeader = "";

        // Act
        ResponseEntity<?> response = authController.login(emptyAuthHeader, loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Authorization header is missing or empty", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithWhitespaceToken_ShouldReturnUnauthorized() {
        // Arrange
        String authHeaderWithWhitespaceToken = BEARER_PREFIX + "  ";

        // Act
        ResponseEntity<?> response = authController.login(authHeaderWithWhitespaceToken, loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Token is empty", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnOkWithUser() {
        // Arrange
        String validAuthHeader = BEARER_PREFIX + VALID_TOKEN;
        when(firebaseUser.getUid()).thenReturn("test-uid");
        when(firebaseUser.getEmail()).thenReturn("test@example.com");
        when(authService.validateToken(VALID_TOKEN)).thenReturn(firebaseUser);

        // Act
        ResponseEntity<?> response = authController.validateToken(validAuthHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(FirebaseUser.class, response.getBody());

        FirebaseUser responseUser = (FirebaseUser) response.getBody();
        assertEquals("test-uid", responseUser.getUid());
        assertEquals("test@example.com", responseUser.getEmail());

        verify(authService).validateToken(VALID_TOKEN);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnUnauthorized() {
        // Arrange
        String invalidAuthHeader = BEARER_PREFIX + INVALID_TOKEN;
        String errorMessage = "Invalid token";
        when(authService.validateToken(INVALID_TOKEN))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<?> response = authController.validateToken(invalidAuthHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(authService).validateToken(INVALID_TOKEN);
    }

    @Test
    void validateToken_WithMissingAuthorizationHeader_ShouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = authController.validateToken(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void validateToken_WithInvalidHeaderFormat_ShouldReturnUnauthorized() {
        // Arrange
        String invalidFormatHeader = "Invalid-Format";

        // Act
        ResponseEntity<?> response = authController.validateToken(invalidFormatHeader);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = authController.validateToken(BEARER_PREFIX);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void registerTrainer_WithValidRequest_ShouldReturnOk() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("trainer@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("TrainerNick");

        doNothing().when(authService).registerTrainer(registerRequest);

        // Act
        ResponseEntity<?> response = authController.registerTrainer(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(MessageResponse.class, response.getBody());

        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Trener zarejestrowany pomyślnie", messageResponse.getMessage());

        verify(authService).registerTrainer(registerRequest);
    }

    @Test
    void registerTrainer_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("trainer@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("TrainerNick");

        String errorMessage = "Rejestracja nie powiodła się: Email already exists";
        doThrow(new RuntimeException(errorMessage))
                .when(authService).registerTrainer(registerRequest);

        // Act
        ResponseEntity<?> response = authController.registerTrainer(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(authService).registerTrainer(registerRequest);
    }

    @Test
    void registerTrainer_WhenServiceThrowsExceptionWithNullMessage_ShouldReturnBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("trainer@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("TrainerNick");

        doThrow(new RuntimeException())
                .when(authService).registerTrainer(registerRequest);

        // Act
        ResponseEntity<?> response = authController.registerTrainer(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertNull(errorResponse.getMessage());

        verify(authService).registerTrainer(registerRequest);
    }

    @Test
    void registerTrainer_WhenServiceThrowsIllegalArgumentException_ShouldReturnBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("short");
        registerRequest.setNickname("");

        String errorMessage = "Invalid registration data";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(authService).registerTrainer(registerRequest);

        // Act
        ResponseEntity<?> response = authController.registerTrainer(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(authService).registerTrainer(registerRequest);
    }
}