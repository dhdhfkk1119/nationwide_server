package com.nationwide.nationwide_server.location;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.location.kakao")
public class LocationProperties {
    private String restApiKey;

    public boolean isConfigured() {
        return restApiKey != null && !restApiKey.isBlank();
    }
}
