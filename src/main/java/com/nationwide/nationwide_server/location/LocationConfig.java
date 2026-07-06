package com.nationwide.nationwide_server.location;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(LocationProperties.class)
public class LocationConfig {

    @Bean
    public RestClient kakaoLocationRestClient() {
        return RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }
}
