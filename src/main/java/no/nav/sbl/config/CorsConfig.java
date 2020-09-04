package no.nav.sbl.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static no.nav.common.utils.EnvironmentUtils.isProduction;

@Configuration
@Slf4j
public class CorsConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String origins = isProduction()
                        .filter(Boolean::booleanValue)
                        .map((i) -> ".adeo.no")
                        .orElseGet(() -> {
                            log.info("Setting CORS-headers to .preprod.local, YOU SHOULD NOT SEE THIS IN PRODUCTION!!!");
                            return ".preprod.local";
                        });

                registry.addMapping("/api/**")
                        .allowCredentials(true)
                        .allowedHeaders("Accept", "Accept-language", "Content-Language", "Content-Type")
                        .allowedOrigins(origins)
                        .maxAge(3600)
                        .allowedMethods("GET", "HEAD", "PUT", "POST", "PATCH", "DELETE", "OPTIONS");
            }
        };
    }
}
