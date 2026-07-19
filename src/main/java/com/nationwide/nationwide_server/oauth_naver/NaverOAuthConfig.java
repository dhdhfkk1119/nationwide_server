package com.nationwide.nationwide_server.oauth_naver;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NaverOAuthProperties.class)
public class NaverOAuthConfig {

    @Bean
    public RestClient naverOAuthRestClient() {
        return RestClient.builder().build();
    }
}
