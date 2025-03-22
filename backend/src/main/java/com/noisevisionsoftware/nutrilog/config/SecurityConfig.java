package com.noisevisionsoftware.nutrilog.config;

import com.noisevisionsoftware.nutrilog.security.RateLimitingFilter;
import com.noisevisionsoftware.nutrilog.security.filter.FirebaseAuthenticationFilter;
import com.noisevisionsoftware.nutrilog.service.auth.FirebaseAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthenticationService firebaseAuthenticationService;
    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .xssProtection(HeadersConfigurer.XXssConfig::disable)
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; frame-ancestors 'self'; img-src 'self' data:;"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .httpStrictTransportSecurity(hsts ->
                                hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/diets/**").permitAll()
                        .requestMatchers("/api/shopping-lists/**").hasRole("ADMIN")
                        .requestMatchers("/api/recipes/**").hasRole("ADMIN")
                        .requestMatchers("/api/measurements/**").hasRole("ADMIN")
                        .requestMatchers("/api/changelog/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/diets/upload/**").hasRole("ADMIN")
                        .requestMatchers("/api/diets/categorization/**").hasRole("ADMIN")
                        .requestMatchers("/api/diets/manager/**").hasRole("ADMIN")
                        .requestMatchers("/api/newsletter/**").permitAll()
                        .requestMatchers("/api/contact/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new RateLimitingFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        new FirebaseAuthenticationFilter(firebaseAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://nutrilog.pl",
                "https://www.nutrilog.pl",
                "http://213.136.76.56",
                "https://213.136.76.56"
        ));

        if (isDevProfile()) {
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:5173",
                    "http://localhost:5174",
                    "https://nutrilog.pl",
                    "https://www.nutrilog.pl"
            ));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-XSRF-TOKEN"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN" ));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private boolean isDevProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equals("dev") || profile.equals("local"));
    }
}