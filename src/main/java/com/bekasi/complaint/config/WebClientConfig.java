package com.bekasi.complaint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.geocoding.base-url}")
    private String geocodingBaseUrl;

    @Value("${app.geocoding.user-agent}")
    private String userAgent;

    @Bean
    public WebClient geocodingWebClient() {
        return WebClient.builder()
                .baseUrl(geocodingBaseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }
}
