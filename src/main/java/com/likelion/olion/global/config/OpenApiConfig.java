package com.likelion.olion.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI olionOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("O-lion Backend API")
                .version("v1")
                .description("O-lion 서비스의 도서·감정 진단·책장·메이트·독서 세션 API 문서입니다. "
                        + "모든 응답은 isSuccess, code, httpStatus, message, data 형식으로 반환됩니다."));
    }
}
