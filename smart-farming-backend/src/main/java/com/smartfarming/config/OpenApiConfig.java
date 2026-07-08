package com.smartfarming.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/Knife4j 配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Token";

        return new OpenAPI()
                .info(new Info()
                        .title("智慧养殖环境异常溯源与因果分析系统")
                        .description("系统 API 文档 - 包含用户管理、传感器数据、异常检测、因果分析、溯源报告等模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Farming Team")
                                .email("admin@smartfarming.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("在请求头中添加 Authorization: Bearer <token>")));
    }
}
