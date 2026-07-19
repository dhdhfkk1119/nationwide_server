package com.nationwide.nationwide_server.oauth_kakao;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoOAuthProperties.class)
public class KakaoOAuthConfig {

    @Bean
    public RestClient kakaoOAuthRestClient() {
        return RestClient.builder().build();
    }
}
