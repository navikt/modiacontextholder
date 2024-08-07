package no.nav.sbl.config

import io.micrometer.observation.ObservationRegistry
import no.nav.common.auth.context.UserRole
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter
import no.nav.common.auth.oidc.filter.OidcAuthenticator
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig
import no.nav.common.rest.filter.LogRequestFilter
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.util.AccesstokenServletFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.filter.CorsFilter
import org.springframework.web.filter.ServerHttpObservationFilter

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@EnableCaching
@Import(CorsConfig::class, ServiceContext::class)
open class ApplicationConfig {
    @Bean
    open fun corsFilterRegistration(): FilterRegistrationBean<CorsFilter> {
        val corsFilter = CorsFilter(CorsConfig.allowAllCorsConfig())

        return FilterRegistrationBean<CorsFilter>().apply {
            filter = corsFilter
            order = 0
            addUrlPatterns("/api/*", "/redirect/*", "/modiacontextholder/*")
        }
    }

    /**
     * Azure verdiene er automatisk injected til poden siden vi har lagt til azure-konfig i nais-yaml
     */
    @Bean
    open fun authenticationFilterRegistration(
        @Value("\${AZURE_APP_CLIENT_ID}") azureOBOClientId: String,
        @Value("\${AZURE_APP_WELL_KNOWN_URL}") azureOBODiscoveryUrl: String,
    ): FilterRegistrationBean<OidcAuthenticationFilter> {
        val azureAdOBO =
            OidcAuthenticatorConfig()
                .withClientId(azureOBOClientId)
                .withDiscoveryUrl(azureOBODiscoveryUrl)
                .withUserRole(UserRole.INTERN)

        val authenticators = OidcAuthenticator.fromConfigs(azureAdOBO)

        return FilterRegistrationBean<OidcAuthenticationFilter>().apply {
            filter = OidcAuthenticationFilter(authenticators)
            order = 1
            addUrlPatterns("/api/*", "/redirect/*", "/modiacontextholder/*")
        }
    }

    @Bean
    open fun accesstokenFilterRegistrationBean(): FilterRegistrationBean<AccesstokenServletFilter> =
        FilterRegistrationBean<AccesstokenServletFilter>().apply {
            filter = AccesstokenServletFilter()
            order = 2
            addUrlPatterns("/api/*", "/redirect/*", "/modiacontextholder/*")
        }

    @Bean
    open fun logFilterRegistrationBean(): FilterRegistrationBean<LogRequestFilter> =
        FilterRegistrationBean<LogRequestFilter>().apply {
            filter = LogRequestFilter("modiacontextholder", EnvironmentUtils.isDevelopment().orElse(false))
            order = 3
            addUrlPatterns("/*")
        }

    @Bean
    open fun setStandardHeadersFilterRegistrationBean(): FilterRegistrationBean<SetStandardHttpHeadersFilter> =
        FilterRegistrationBean<SetStandardHttpHeadersFilter>().apply {
            filter = SetStandardHttpHeadersFilter()
            order = 4
            addUrlPatterns("/*")
        }

    @Bean
    open fun serverHttpObservationFilterRegistrationBean(
        observationRegistry: ObservationRegistry,
        serverRequestObservationConvention: ServerRequestObservationConvention,
    ): FilterRegistrationBean<ServerHttpObservationFilter> =
        FilterRegistrationBean<ServerHttpObservationFilter>().apply {
            filter = ServerHttpObservationFilter(observationRegistry, serverRequestObservationConvention)
            order = 5
            addUrlPatterns("/api/*", "/redirect/*", "/modiacontextholder/*")
        }
}
