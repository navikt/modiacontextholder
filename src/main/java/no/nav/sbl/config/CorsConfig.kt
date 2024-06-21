package no.nav.sbl.config

import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

object CorsConfig {
    fun allowAllCorsConfig(): CorsConfigurationSource {
        val corsConfiguration =
            CorsConfiguration()
                .apply {
                    allowCredentials = true
                    allowedHeaders = listOf("Accept", "Accept-language", "Content-Language", "Content-Type")
                    addAllowedOriginPattern(CorsConfiguration.ALL)
                    maxAge = 3600L
                    allowedMethods = listOf("GET", "HEAD", "PUT", "POST", "PATCH", "DELETE", "OPTIONS")
                }

        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", corsConfiguration) }
    }
}
