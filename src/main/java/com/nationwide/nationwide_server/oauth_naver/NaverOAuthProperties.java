package com.nationwide.nationwide_server.oauth_naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth.naver")
public class NaverOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
