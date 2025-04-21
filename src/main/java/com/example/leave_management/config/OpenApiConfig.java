package com.example.leave_management.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Value("${server.servlet.context-path:/api}")
        private String contextPath;

        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemaName = "bearerAuth";
                return new OpenAPI()
                                .addServersItem(new Server()
                                                .url("http://localhost:5500" + contextPath)
                                                .description("Local Development Server"))
                                .info(new Info()
                                                .title("Leave Management System API")
                                                .description("API documentation for Leave Management System")
                                                .version("1.0")
                                                .contact(new Contact()
                                                                .name("Your Name")
                                                                .email("your.email@example.com")
                                                                .url("https://your-website.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))

                                .components(new Components()
                                                .addSecuritySchemes(securitySchemaName, new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP).scheme("bearer")
                                                                .bearerFormat("JWT")));

        }
}