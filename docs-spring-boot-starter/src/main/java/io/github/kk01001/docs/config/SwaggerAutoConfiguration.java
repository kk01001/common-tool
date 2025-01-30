package io.github.kk01001.docs.config;

import io.github.kk01001.docs.properties.SwaggerProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerAutoConfiguration {

    @Bean
    public OpenAPI openAPI(SwaggerProperties properties) {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(properties.getContactEmail())
                                .url(properties.getContactUrl())));

        // 如果开启了Basic认证
        if (Boolean.TRUE.equals(properties.getBasicAuth())) {
            openAPI.components(new Components()
                    .addSecuritySchemes("basicAuth",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("basic")));
        }
        
        return openAPI;
    }
} 