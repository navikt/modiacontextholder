package no.nav.sbl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class CorsConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowCredentials(true)
                        .allowedHeaders("Accept", "Accept-language", "Content-Language", "Content-Type")
                        .allowedOrigins(CorsConfiguration.ALL)
                        .maxAge(3600)
                        .allowedMethods("GET", "HEAD", "PUT", "POST", "PATCH", "DELETE", "OPTIONS");
            }
        };
    }
}
