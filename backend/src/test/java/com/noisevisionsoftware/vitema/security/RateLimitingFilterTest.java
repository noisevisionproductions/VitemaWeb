package com.noisevisionsoftware.vitema.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
    }

    @Test
    void doFilterInternal_WithPublicEndpoint_ShouldBypassRateLimit() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/public/test");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    void doFilterInternal_WithStaticEndpoint_ShouldBypassRateLimit() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/static/css/style.css");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    void doFilterInternal_WithProtectedEndpoint_WithinLimit_ShouldContinue() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_WithProtectedEndpoint_ExceedingLimit_ShouldReturn429() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - Make 500 requests (at the limit)
        for (int i = 0; i < 500; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Reset mocks to verify the 501st request
        reset(response, filterChain);
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).getWriter();
        assertThat(responseWriter.toString()).contains("Rate limit exceeded. Please try again later.");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithProtectedEndpoint_ExactlyAtLimit_ShouldContinue() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - Make exactly 500 requests (at the limit)
        for (int i = 0; i < 500; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Assert & Verify
        verify(filterChain, times(500)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_WithDifferentIPs_ShouldHaveSeparateCounters() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - Make 500 requests from first IP
        for (int i = 0; i < 500; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Change IP
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");

        // Act - Make request from second IP
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain, times(501)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_WithXForwardedForHeader_ShouldUseFirstIP() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(request).getHeader("X-Forwarded-For");
        // Verify that subsequent requests from the same X-Forwarded-For IP are counted together
        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(2)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithXForwardedForHeader_WithWhitespace_ShouldTrimIP() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getHeader("X-Forwarded-For")).thenReturn("  203.0.113.1  , 198.51.100.1  ");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(request).getHeader("X-Forwarded-For");
    }

    @Test
    void doFilterInternal_WithEmptyXForwardedForHeader_ShouldUseRemoteAddr() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(request).getRemoteAddr();
    }

    @Test
    void doFilterInternal_WithNullXForwardedForHeader_ShouldUseRemoteAddr() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain).doFilter(request, response);
        verify(request).getRemoteAddr();
    }

    @Test
    void doFilterInternal_WithRateLimitExceeded_ShouldNotCallFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act - Make 500 requests (at the limit)
        for (int i = 0; i < 500; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Reset mocks for the 501st request
        reset(response, filterChain);
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void doFilterInternal_WithPublicEndpoint_ShouldNotIncrementCounter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/public/test");

        // Act - Make many requests to public endpoint
        for (int i = 0; i < 1000; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then make a request to protected endpoint - should still work
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain, times(1001)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_WithStaticEndpoint_ShouldNotIncrementCounter() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/static/js/app.js");

        // Act - Make many requests to static endpoint
        for (int i = 0; i < 1000; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then make a request to protected endpoint - should still work
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Assert & Verify
        verify(filterChain, times(1001)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
