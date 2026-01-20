package com.noisevisionsoftware.vitema.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class OpenFoodFactsConfig {

    @Bean("openFoodFactsRestTemplate")
    public RestTemplate openFoodFactsRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri("https://pl.openfoodfacts.org")
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalInterceptors(((request, body, execution) -> {
                    request.getHeaders().add("User-Agent", "Vitema/1.0 (contact@vitema.pl)");
                    return execution.execute(request, body);
                }))
                .build();
    }
}
