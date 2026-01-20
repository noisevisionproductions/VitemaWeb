package com.noisevisionsoftware.vitema.security.filter;

import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.auth.FirebaseAuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthenticationFilterTest {

    @Mock
    private FirebaseAuthenticationService firebaseAuthService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private FirebaseAuthenticationFilter filter;

    private static final String VALID_TOKEN = "valid_token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = BEARER_PREFIX + VALID_TOKEN;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithPublicEndpoint_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/public/test");
        when(request.getMethod()).thenReturn("GET");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void doFilterInternal_WithOptionsMethod_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("OPTIONS");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void doFilterInternal_WithLoginEndpoint_ShouldSkipAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void doFilterInternal_WithNoAuthHeader_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void doFilterInternal_WithInvalidAuthHeaderFormat_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(firebaseAuthService);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);

        FirebaseUser mockUser = FirebaseUser.builder()
                .uid("test-uid")
                .email("test@example.com")
                .role("ADMIN")
                .build();

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                mockUser,
                VALID_TOKEN,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(firebaseAuthService.getAuthentication(VALID_TOKEN)).thenReturn(mockAuth);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication resultAuth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(resultAuth);
        assertEquals(mockUser, resultAuth.getPrincipal());
        assertEquals(VALID_TOKEN, resultAuth.getCredentials());
        assertTrue(resultAuth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(firebaseAuthService).getAuthentication(VALID_TOKEN);
    }

    @Test
    void doFilterInternal_WithAuthenticationError_ShouldClearContextAndContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(firebaseAuthService.getAuthentication(VALID_TOKEN)).thenThrow(new RuntimeException("Authentication failed"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(firebaseAuthService).getAuthentication(VALID_TOKEN);
    }

    @Test
    void doFilterInternal_WithNullAuthentication_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(firebaseAuthService.getAuthentication(VALID_TOKEN)).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(firebaseAuthService).getAuthentication(VALID_TOKEN);
    }
}