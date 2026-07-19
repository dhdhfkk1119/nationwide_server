package com.nationwide.nationwide_server.location;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({LocationProperties.class, NaverMapProperties.class})
public class LocationConfig {

    @Bean
    public RestClient kakaoLocationRestClient() {
        return RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    @Bean
    public RestClient naverMapRestClient() {
        return RestClient.builder()
                .baseUrl("https://maps.apigw.ntruss.com")
                .build();
    }
}
