package com.nationwide.nationwide_server._core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.upload")
@Data
public class UploadProperties {
    private String imageDir = "image/";
    private String memberDir = "member-images/";
    private String communityDir = "community-images/";
    private String rootDir = "./uploads/";
}
