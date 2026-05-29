package com.campus.community.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园信息聚合与互动平台 API")
                        .version("1.0.0")
                        .description("校园信息聚合与互动平台 — 用户认证、帖子管理、评论互动")
                        .contact(new Contact()
                                .name("Campus Community")
                                .email("admin@campus.edu")));
    }
}
