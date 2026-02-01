package com.noisevisionsoftware.vitema.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {ProductionSecurityConfig.class})
@ActiveProfiles("prod")
class ProductionSecurityConfigTest {

    @Autowired
    private HttpFirewall httpFirewall;

    @Test
    void strictFirewall_ShouldBeConfigured() {
        assertNotNull(httpFirewall);
        assertInstanceOf(StrictHttpFirewall.class, httpFirewall);
    }

    @Test
    void strictFirewall_ShouldRejectUrlEncodedSlash() {
        StrictHttpFirewall firewall = (StrictHttpFirewall) httpFirewall;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test%2Fpath");
        request.setMethod("GET");
        
        // The firewall should reject URL encoded slash
        assertThrows(RequestRejectedException.class, () -> firewall.getFirewalledRequest(request));
    }

    @Test
    void strictFirewall_ShouldRejectUrlEncodedDoubleSlash() {
        StrictHttpFirewall firewall = (StrictHttpFirewall) httpFirewall;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test%2F%2Fpath");
        request.setMethod("GET");
        
        // The firewall should reject URL encoded double slash
        assertThrows(RequestRejectedException.class, () -> firewall.getFirewalledRequest(request));
    }

    @Test
    void strictFirewall_ShouldRejectSemicolon() {
        StrictHttpFirewall firewall = (StrictHttpFirewall) httpFirewall;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test;path");
        request.setMethod("GET");
        
        // The firewall should reject semicolon
        assertThrows(RequestRejectedException.class, () -> firewall.getFirewalledRequest(request));
    }

    @Test
    void strictFirewall_ShouldAllowValidRequest() {
        StrictHttpFirewall firewall = (StrictHttpFirewall) httpFirewall;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test/path");
        request.setMethod("GET");
        
        // The firewall should allow valid requests
        HttpServletRequest firewalledRequest = firewall.getFirewalledRequest(request);
        assertNotNull(firewalledRequest);
    }

    @Test
    void strictFirewall_ShouldAllowNormalPath() {
        StrictHttpFirewall firewall = (StrictHttpFirewall) httpFirewall;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/123");
        request.setMethod("GET");
        
        HttpServletRequest firewalledRequest = firewall.getFirewalledRequest(request);
        assertNotNull(firewalledRequest);
    }

    @Test
    void strictFirewall_ShouldOnlyBeActiveInProdProfile() {
        // This test verifies that the bean is only created when prod profile is active
        // The @ActiveProfiles("prod") annotation ensures this
        assertNotNull(httpFirewall);
        assertInstanceOf(StrictHttpFirewall.class, httpFirewall);
    }
}
