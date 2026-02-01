package com.noisevisionsoftware.vitema.config;

import com.noisevisionsoftware.vitema.service.auth.FirebaseAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private FirebaseAuthenticationService mockAuthService;

    @Mock
    private Environment environment;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(mockAuthService, environment);
    }


    @Test
    void corsConfigurationSource_WhenDevProfile_ShouldIncludeLocalhost() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedOrigins())
                .contains("http://localhost:5173"));
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedOrigins())
                .contains("http://localhost:5174"));
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedOrigins())
                .contains("https://vitema.pl"));
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedOrigins())
                .contains("https://www.vitema.pl"));
    }

    @Test
    void corsConfigurationSource_WhenProdProfile_ShouldNotIncludeLocalhost() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        List<String> allowedOrigins = Objects.requireNonNull(corsConfiguration.getAllowedOrigins());
        assertTrue(allowedOrigins.contains("https://vitema.pl"));
        assertTrue(allowedOrigins.contains("https://www.vitema.pl"));
        assertTrue(allowedOrigins.contains("http://213.136.76.56"));
        assertTrue(allowedOrigins.contains("https://213.136.76.56"));
        assertFalse(allowedOrigins.contains("http://localhost:5173"));
        assertFalse(allowedOrigins.contains("http://localhost:5174"));
    }

    @Test
    void corsConfigurationSource_ShouldHaveCorrectAllowedMethods() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"}); // Zapewnienie konkretnego profilu

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        List<String> allowedMethods = corsConfiguration.getAllowedMethods();

        assertThat(allowedMethods)
                .containsExactlyInAnyOrderElementsOf(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    }

    @Test
    void corsConfigurationSource_ShouldHaveCorrectAllowedHeaders() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"}); // Zapewnienie konkretnego profilu

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        List<String> allowedHeaders = corsConfiguration.getAllowedHeaders();
        List<String> expectedHeaders = Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-XSRF-TOKEN"
        );

        assertThat(allowedHeaders).containsExactlyInAnyOrderElementsOf(expectedHeaders);
    }

    @Test
    void corsConfigurationSource_ShouldHaveCorrectExposedHeaders() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        List<String> exposedHeaders = corsConfiguration.getExposedHeaders();
        List<String> expectedExposedHeaders = List.of("Authorization", "X-XSRF-TOKEN");

        assertThat(exposedHeaders).containsExactlyInAnyOrderElementsOf(expectedExposedHeaders);
    }

    @Test
    void corsConfigurationSource_ShouldHaveCorrectAccessSettings() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // when
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        // then
        assertNotNull(corsConfiguration);
        assertEquals(Boolean.TRUE, corsConfiguration.getAllowCredentials());
        assertEquals(3600L, Objects.requireNonNull(corsConfiguration.getMaxAge()).longValue());
    }

    @Test
    void isDevProfile_ShouldReturnTrueForDevProfile() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        // when
        boolean result = securityConfig.isDevProfile();

        // then
        assertTrue(result);
    }

    @Test
    void isDevProfile_ShouldReturnTrueForLocalProfile() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});

        // when
        boolean result = securityConfig.isDevProfile();

        // then
        assertTrue(result);
    }

    @Test
    void isDevProfile_ShouldReturnFalseForProdProfile() {
        // given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        // when
        boolean result = securityConfig.isDevProfile();

        // then
        assertFalse(result);
    }
}