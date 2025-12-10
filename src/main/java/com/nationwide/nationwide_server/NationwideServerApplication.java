package com.nationwide.nationwide_server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Nation Wide - Server",
                description = "백엔드 API 문서",
                version = "1.0"
        )
)

@SpringBootApplication
public class NationwideServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NationwideServerApplication.class, args);
	}

}
