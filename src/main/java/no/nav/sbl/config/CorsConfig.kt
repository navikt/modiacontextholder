package no.nav.sbl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

public class CorsConfig {
    public static CorsConfigurationSource allowAllCorsConfig() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Accept", "Accept-language", "Content-Language", "Content-Type"));
        configuration.addAllowedOriginPattern(CorsConfiguration.ALL);
        configuration.setMaxAge(3600L);
        configuration.setAllowedMethods(List.of("GET", "HEAD", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
