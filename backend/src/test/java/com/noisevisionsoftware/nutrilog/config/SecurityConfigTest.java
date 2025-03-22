package com.noisevisionsoftware.nutrilog.config;

import com.noisevisionsoftware.nutrilog.service.auth.FirebaseAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsConfigurationSource_ShouldReturnCorrectConfiguration() {
        FirebaseAuthenticationService mockAuthService = mock(FirebaseAuthenticationService.class);
        Environment environment = mock(Environment.class);
        SecurityConfig securityConfig = new SecurityConfig(mockAuthService, environment);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(corsConfiguration);
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedOrigins())
                .contains("http://localhost:5173"));
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedMethods())
                .containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")));
        assertTrue(Objects.requireNonNull(corsConfiguration.getAllowedHeaders())
                .containsAll(Arrays.asList(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"
                )));
        assertTrue(Objects.requireNonNull(corsConfiguration.getExposedHeaders())
                .contains("Authorization"));
        assertEquals(Boolean.TRUE, corsConfiguration.getAllowCredentials());
    }
}