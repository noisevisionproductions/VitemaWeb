package com.noisevisionsoftware.nutrilog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@Profile("prod")
public class ProductionSecurityConfig {

    @Bean
    public HttpFirewall strictFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();

        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        firewall.setAllowSemicolon(false);
        return firewall;
    }
}
