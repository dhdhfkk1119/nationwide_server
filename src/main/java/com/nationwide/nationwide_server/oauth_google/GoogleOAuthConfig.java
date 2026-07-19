package com.nationwide.nationwide_server.oauth_google;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthConfig {

    @Bean
    public RestClient googleOAuthRestClient() {
        return RestClient.builder().build();
    }
}
